package net.uku3lig.ukubot.commands.xp;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.commands.IsSenderAllowed;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.hibernate.Database;
import net.uku3lig.ukubot.hibernate.entities.GuildXp;
import net.uku3lig.ukubot.hibernate.entities.MemberXp;
import net.uku3lig.ukubot.subsystems.core.ReactionListener;
import net.uku3lig.ukubot.subsystems.xp.ExperienceListener;

import java.util.Objects;

public class ResetXpCommand extends Command {
    @Override
    public String command() {
        return "resetxp";
    }

    @Override
    public String description() {
        return "Resets the xp of a member";
    }

    @Override
    public void onCommandReceived(CommandReceivedEvent event) {
        if (event.args.length < 1) {
            sendHelp(event.getMessage());
            return;
        }

        MemberXp member = ExperienceListener.findMember(
                Long.parseLong(event.args[0].replaceAll("\\D", "")), event.getGuild());

        event.getChannel().sendMessage("Are you sure you want to reset this member's xp?").queue(msg ->
                ReactionListener.yesNo(() -> {
                    member.reset();
                    event.getChannel().sendMessage("Sucessfully reset").queue();
                }, event.getChannel())
                .allowed(m -> m.getIdLong() == member.getMemberId())
                .source(msg)
                .build());
    }

    @Override
    public String help() {
        return "resetxp <user|id>";
    }

    @Override
    public IsSenderAllowed allowed() {
        return IsSenderAllowed.Administrator;
    }
}
