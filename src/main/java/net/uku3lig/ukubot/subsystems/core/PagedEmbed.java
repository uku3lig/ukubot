package net.uku3lig.ukubot.subsystems.core;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.uku3lig.ukubot.commands.IsSenderAllowed;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.subsystems.Subsystem;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PagedEmbed<T> extends Subsystem {
    private static final List<String> reactions =
            Arrays.asList(EmojiParser.parseToUnicode(":arrow_left:"),
                    EmojiParser.parseToUnicode(":arrow_right:"));

    @Override
    public @NotNull String getName() {
        return "Paged Embed";
    }

    @Override
    public @NotNull String getDescription() {
        return "Creates and handles embeds that act like pages";
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser().getId().equalsIgnoreCase(Main.getJda().getSelfUser().getId())) return;
        if (!event.getMessageId().equalsIgnoreCase(message.getId())) return;
        if (!allowed.test(event.getMember())) return;
        if (!reactions.contains(event.getReactionEmote().getName())) return;
        //everything ok
        event.getReaction().removeReaction(event.getUser()).queue();
        t = getTimer(t, this);
        if (event.getReactionEmote().getName().equalsIgnoreCase(reactions.get(0)) && currentPage > 0) currentPage--;
        else if (event.getReactionEmote().getName().equalsIgnoreCase(reactions.get(1))
                && currentPage+1 < (int) Math.ceil((double) totalSize / pageSize)) currentPage++;
        else return;
        message.editMessage(renderPage(currentPage * pageSize)).queue();
    }

    private final Function<Offset, T[]> objects;
    private Message message;
    private final MessageEmbed embed;
    private final Duration timeout;
    private final Predicate<Member> allowed;
    private final byte pageSize;
    private final long totalSize;
    private int currentPage = 0;
    private Timer t = new Timer();

    private PagedEmbed() {
        objects = null;
        embed = null;
        timeout = null;
        allowed = null;
        pageSize = 0;
        totalSize = 0;
    }

    public PagedEmbed(Function<Offset, T[]> objects,
                      TextChannel channel, MessageEmbed embed, Duration timeout,
                      Predicate<Member> allowed, byte pageSize, long totalSize) {
        this.objects = objects;
        this.embed = embed;
        this.timeout = timeout;
        this.allowed = allowed;
        this.pageSize = pageSize;
        this.totalSize = totalSize;

        channel.sendMessage(renderPage(0)).queue(msg -> {
            this.message = msg;
            reactions.forEach(r -> msg.addReaction(r).queue());
        });
        this.t = getTimer(t, this);

        Main.getJda().addEventListener(this);
    }

    private Timer getTimer(Timer t, PagedEmbed<T> instance) {
        t.cancel();
        t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                message.getChannel().sendMessage("Vote ended").queue();
                message.delete().queue();
                Main.getJda().removeEventListener(instance);
            }
        }, Date.from(Instant.now().plus(timeout)));
        return t;
    }

    private MessageEmbed renderPage(int offset) {
        String content = Arrays.stream(objects.apply(new Offset(offset, pageSize)))
                .limit(pageSize)
                .map(Objects::toString)
                .collect(Collectors.joining("\n"));
        return new EmbedBuilder(embed)
                .setDescription(content)
                .setFooter("Page %d/%d".formatted(currentPage+1,
                        (int) Math.ceil((double) totalSize / pageSize)))
                .build();
    }

    public static <T> Builder<T> builder(long totalSize, Class<T> klass) {
        return new Builder<>(totalSize, klass);
    }

    public static class Builder<T> {
        private Function<Offset, T[]> objects;
        private Comparator<T> sorter;
        private TextChannel channel;
        private MessageEmbed embed;
        private Duration timeout = Duration.ofMinutes(1);
        private Predicate<Member> allowed = IsSenderAllowed.Default.get();
        private byte pageSize = 10;
        private final long totalSize;

        public Builder(long totalSize, Class<T> klass) {
            this.totalSize = totalSize;
        }

        public Builder<T> objects(Function<Offset, T[]> objects) {
            this.objects = objects;
            return this;
        }

        public Builder<T> sorter(Comparator<T> sorter) {
            this.sorter = sorter;
            return this;
        }

        public Builder<T> channel(TextChannel channel) {
            this.channel = channel;
            return this;
        }

        public Builder<T> timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder<T> allowed(Predicate<Member> allowed) {
            this.allowed = allowed;
            return this;
        }

        public Builder<T> pageSize(byte pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder<T> embed(MessageEmbed embed) {
            this.embed = embed;
            return this;
        }

        public void build() {
            if (objects == null || sorter == null || channel == null || embed == null)
                throw new IllegalArgumentException("Error: objects, sorter, channel or embed cannot be null");
            new PagedEmbed<>(objects, channel, embed, timeout, allowed, pageSize, totalSize);
        }
    }

    public static class Offset {
        public final int offset;
        public final byte pageSize;

        public Offset(int offset, byte pageSize) {
            this.offset = offset;
            this.pageSize = pageSize;
        }
    }
}
