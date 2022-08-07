package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;
import net.uku3lig.ukubot.Main;
import net.uku3lig.ukubot.core.ICommand;

import java.util.ArrayList;
import java.util.List;

public class HallOfShameCommand implements ICommand {
    @Override
    public CommandData getCommandData() {
        return Commands.slash("hallofshame", "hacking subhuman list");
    }

    @Override
    public void onCommand(GenericCommandInteractionEvent event) {
        if (event.getGuild() == null) return;
        if (event.getMember() == null) return;

        boolean ephemeral = !event.getMember().hasPermission(Permission.MESSAGE_MANAGE);

        List<Long> ids = Main.getGuildConfig(event.getGuild()).getOrElse("blacklisted", new ArrayList<>());
        if (ids.isEmpty()) {
            event.reply("no one is blacklisted D:").setEphemeral(ephemeral).queue();
            return;
        }

        RestAction.allOf(ids.stream().map(Main.getJda()::retrieveUserById).toList())
                .map(l -> String.join(" ", l.stream().map(User::getAsTag).toList()))
                .flatMap(s -> event.replyFormat("blacklisted mfs:%n%s", s).setEphemeral(ephemeral))
                .queue();
    }
}
