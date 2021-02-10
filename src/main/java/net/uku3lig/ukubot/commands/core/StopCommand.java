package net.uku3lig.ukubot.commands.core;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.commands.IsSenderAllowed;
import net.uku3lig.ukubot.subsystems.core.ReactionListener;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;

public class StopCommand extends Command {
    @Override
    public String command() {
        return "stop";
    }

    @Override
    public String description() {
        return "Stops the bot";
    }

    @Override
    public void onCommandReceived(CommandReceivedEvent event) {
        boolean now = event.args.length > 0 && event.args[0].equalsIgnoreCase("now");
        event.getChannel().sendMessage("Are you sure you want to stop the bot ?")
                .queue(m -> ReactionListener.yesNo(() -> stop(event.getChannel(), now), event.getChannel())
                        .source(m)
                        .allowed(IsSenderAllowed.Uku.get())
                        .maxUses(1)
                        .timeout(Duration.ofMinutes(1))
                        .build());
    }

    public static void stop(@Nullable TextChannel c, boolean now) {
        if (c != null) c.sendMessage("Shutting down...").queue();
        if (now) Main.getJda().shutdownNow();
        else Main.getJda().shutdown();
        Runtime.getRuntime().exit(0);
    }

    @Override
    public IsSenderAllowed allowed() {
        return IsSenderAllowed.Uku;
    }

    @Override
    public String help() {
        return "stop";
    }
}
