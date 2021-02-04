package net.uku3lig.ukubot.subsystems.core;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.EmbedBuilder;
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
import java.util.concurrent.atomic.AtomicReference;
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
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser().getId().equalsIgnoreCase(Main.getJda().getSelfUser().getId())) return;
        if (!event.getMessageId().equalsIgnoreCase(message.getId())) return;
        if (!allowed.isAllowed(event.getMember())) return;
        if (!reactions.contains(event.getReactionEmote().getName())) return;
        //everything ok
        event.getReaction().removeReaction(event.getUser()).queue();
        t = getTimer(t, this);
        if (event.getReactionEmote().getName().equalsIgnoreCase(reactions.get(0))) currentPage--;
        else currentPage++;
        message.editMessage(renderPage(currentPage*pageSize)).queue();
    }

    private final List<T> objects;
    private Message message;
    private final MessageEmbed embed;
    private final Duration timeout;
    private final IsSenderAllowed allowed;
    private final int pageSize;
    private int currentPage = 0;
    private Timer t = new Timer();

    private PagedEmbed() {
        objects = null;
        embed = null;
        timeout = null;
        allowed = null;
        pageSize = 0;
    }

    public PagedEmbed(List<T> objects, Comparator<T> sorter,
                      TextChannel channel, MessageEmbed embed, Duration timeout,
                      IsSenderAllowed allowed, int pageSize) {
        objects.sort(sorter);
        this.objects = objects;
        this.embed = embed;
        this.timeout = timeout;
        this.allowed = allowed;
        this.pageSize = pageSize;

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
        String content = objects.stream()
                .skip(offset).limit(pageSize)
                .map(Objects::toString)
                .collect(Collectors.joining("\n"));
        return new EmbedBuilder(embed)
                .setDescription(content)
                .setFooter("Page %d/%d".formatted(currentPage+1,
                        (int) Math.ceil((double) objects.size() / pageSize)))
                .build();
    }

    public static <T> Builder<T> builder(List<T> objects) {
        return new Builder<>(objects);
    }

    public static class Builder<T> {
        private final List<T> objects;
        private Comparator<T> sorter;
        private TextChannel channel;
        private MessageEmbed embed;
        private Duration timeout = Duration.ofMinutes(1);
        private IsSenderAllowed allowed = IsSenderAllowed.Default;
        private int pageSize = 10;

        public Builder(List<T> objects) {
            this.objects = objects;
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

        public Builder<T> allowed(IsSenderAllowed allowed) {
            this.allowed = allowed;
            return this;
        }

        public Builder<T> pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder<T> embed(MessageEmbed embed) {
            this.embed = embed;
            return this;
        }

        public void build() {
            if (objects == null || objects.isEmpty() || sorter == null || channel == null || embed == null)
                throw new IllegalArgumentException("Error: objects, sorter, channel or embed cannot be null");
            new PagedEmbed<>(objects, sorter, channel, embed, timeout, allowed, pageSize);
        }
    }
}
