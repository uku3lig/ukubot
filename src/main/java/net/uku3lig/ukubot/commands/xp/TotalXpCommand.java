package net.uku3lig.ukubot.commands.xp;

import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.subsystems.xp.ExperienceListener;
import net.uku3lig.ukubot.utils.Util;

import java.text.DecimalFormat;

public class TotalXpCommand extends Command {
    @Override
    public String command() {
        return "totalxp";
    }

    @Override
    public String description() {
        return "Returns the total xp amount needed to reach a certain level";
    }

    @Override
    public void onCommandReceived(CommandReceivedEvent event) {
        if (event.args.length < 1) {
            sendHelp(event.getMessage());
            return;
        }

        long level = Long.parseLong(event.args[0].replaceAll("\\D", ""));
        String message = "Total xp needed for level **`%d`**: `%s`"
                .formatted(level, Util.formatNum(ExperienceListener.totalXpRequired(level)));
        event.getChannel().sendMessage(message).queue();
    }

    @Override
    public String help() {
        return "totalxp <level>";
    }
}
