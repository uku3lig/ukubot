package net.uku3lig.ukubot.subsystems.core;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.subsystems.Subsystem;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;
import java.util.function.Predicate;

public class SubCommandListener extends Subsystem {
    @Override
    public @NotNull String getName() {
        return "Sub-command listener";
    }

    private final User sender;
    private final MessageChannel channel;
    private final Predicate<Message> action;
    private final Duration timeout;
    private final boolean retry;
    private Timer timer = new Timer();

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (!(channel instanceof TextChannel)) return;
        if (!sender.getId().equalsIgnoreCase(event.getAuthor().getId())) return;
        if (!action.test(event.getMessage()) && retry) timer = scheduleFromNow(timer, timeout);
        else {
            timer.cancel();
            Main.getJda().removeEventListener(this);
        }
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        if (!(channel instanceof PrivateChannel)) return;
        if (!sender.getId().equals(event.getAuthor().getId())) return;
        if (!action.test(event.getMessage()) && retry) timer = scheduleFromNow(timer, timeout);
        else {
            timer.cancel();
            Main.getJda().removeEventListener(this);
        }
    }

    private SubCommandListener() {
        sender = null;
        channel = null;
        action = m -> false;
        timeout = Duration.ZERO;
        retry = false;
    }

    private SubCommandListener(User sender, MessageChannel channel, Predicate<Message> action, Duration timeout, boolean retry) {
        this.sender = sender;
        this.channel = channel;
        this.action = action;
        this.timeout = timeout;
        this.retry = retry;

        timer = scheduleFromNow(timer, timeout);

        Main.getJda().addEventListener(this);
    }

    private Timer scheduleFromNow(Timer t, Duration d) {
        t.cancel();
        t = new Timer();
        SubCommandListener instance = this;
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                channel.sendMessage("Cancelled due to timeout").queue();
                Main.getJda().removeEventListener(instance);
            }
        }, Date.from(Instant.now().plus(d)));
        return t;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private User sender = null;
        private MessageChannel channel = null;
        private Predicate<Message> action = null;
        private Duration timeout = Duration.ofMinutes(1);
        private boolean retry = false;

        public Builder sender(User sender) {
            this.sender = sender;
            return this;
        }

        public Builder channel(MessageChannel channel) {
            this.channel = channel;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder retry(boolean retry) {
            this.retry = retry;
            return this;
        }

        public Builder action(Predicate<Message> action) {
            this.action = action;
            return this;
        }

        public void build() {
            if (channel == null || sender == null || action == null)
                throw new IllegalArgumentException("action, channel and sender must NOT be null");
            new SubCommandListener(sender, channel, action, timeout, retry);
        }
    }
}
