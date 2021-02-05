package net.uku3lig.ukubot.subsystems.core;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.commands.IsSenderAllowed;
import net.uku3lig.ukubot.subsystems.Subsystem;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

public class ReactionListener extends Subsystem {
    @Override
    public @NotNull String getName() {
        return "Reaction Listener";
    }

    @Override
    public @NotNull String getDescription() {
        return "Handles reactions on messages, permitting actions on reaction";
    }

    private final Message source;
    private final Map<String, Runnable> actions;
    private final IsSenderAllowed allowed;
    private final int maxUses;
    private int currentUses = 0;
    private final Duration timeout;
    private Timer timer = new Timer();

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (!event.getMessageId().equals(source.getId())) return;
        if (event.getReactionEmote().isEmote()) return;
        if (event.getReactionEmote().isEmote())
            if (!actions.containsKey(event.getReactionEmote().getEmote().getAsMention())) return;
        else if (!actions.containsKey(event.getReactionEmote().getEmoji())) return;
        if (actions.keySet().stream()
                .noneMatch(emoji -> event.getReactionEmote().getEmoji().equals(emoji))) return;
        if (!allowed.isAllowed(event.getMember())) return;

        actions.get(event.getReactionEmote().isEmote() ?
                event.getReactionEmote().getEmote().getAsMention() :
                event.getReactionEmote().getEmoji()).run();
        currentUses++;
        timer = scheduleFromNow(timer, timeout);
        if (currentUses >= maxUses) Main.getJda().removeEventListener(this);
    }

    private Timer scheduleFromNow(Timer t, Duration d) {
        t.cancel();
        t = new Timer();
        ReactionListener instance = this;
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                source.getChannel().sendMessage("Cancelled due to timeout").queue();
                Main.getJda().removeEventListener(instance);
            }
        }, Date.from(Instant.now().plus(d)));
        return t;
    }

    //Protected to prevent user instantiation, but can still be used by reflections
    private ReactionListener() {
        source = null;
        actions = Collections.emptyMap();
        allowed = IsSenderAllowed.Default;
        maxUses = 0;
        timeout = Duration.ZERO;
    }

    private ReactionListener(Message source, Map<String, Runnable> actions, IsSenderAllowed allowed,
                             int maxUses, Duration timeout) {
        this.source = source;
        this.actions = actions;
        this.allowed = allowed;
        this.maxUses = maxUses;
        this.timeout = timeout;
        this.timer = scheduleFromNow(timer, timeout);

        this.actions.keySet().forEach(r -> this.source.addReaction(getReactionEmote(r)).queue());

        Main.getJda().addEventListener(this);
    }

    private String getReactionEmote(String source) {
        return String.join(":",
                source.replace("<", "")
                .replace(">", "")
                .split(":"));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder yesNo(Runnable yes, Runnable no) {
        return new Builder(Map.of(
                EmojiParser.parseToUnicode(":white_check_mark:"), yes,
                EmojiParser.parseToUnicode(":negative_squared_cross_mark:"), no));
    }

    public static Builder yesNo(Runnable yes, TextChannel c) {
        return yesNo(yes, () -> c.sendMessage("ok canceled").queue());
    }

    public static class Builder {
        private Message source = null;
        private Map<String, Runnable> actions = null;
        private IsSenderAllowed allowed = IsSenderAllowed.Default;
        private int maxUses = 1;
        private Duration timeout = Duration.ofMinutes(1);

        public Builder(Map<String, Runnable> actions) {
            this.actions = actions;
        }

        public Builder() {}

        public Builder source(Message source) {
            this.source = source;
            return this;
        }

        /**
         * Adds the reaction emojis, along with the action for each emoji. <br>
         * <b>WARNING:</b> The emoji (discord native) MUST be in unicode, and the
         * {@link net.dv8tion.jda.api.entities.Emote emotes} (user added) must be in a mention format (<code>&lt;:name:id></code>).
         * @param actions The {@link Map map} containing all the emojis and actions.
         * @return itself.
         */
        public Builder actions(Map<String, Runnable> actions) {
            this.actions = actions;
            return this;
        }

        public Builder allowed(IsSenderAllowed allowed) {
            this.allowed = allowed;
            return this;
        }

        public Builder maxUses(int maxUses) {
            this.maxUses = maxUses;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public void build() {
            if (source == null || actions == null) throw new IllegalArgumentException("Source message or actions are not set!");
            new ReactionListener(source, actions, allowed, maxUses, timeout);
        }
    }
}
