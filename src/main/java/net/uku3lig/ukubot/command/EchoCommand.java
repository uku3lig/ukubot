package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.uku3lig.ukubot.core.ICommand;

import java.util.EnumSet;

public class EchoCommand implements ICommand {
    private static final EnumSet<Message.MentionType> allowed = EnumSet.complementOf(EnumSet.of(Message.MentionType.EVERYONE, Message.MentionType.ROLE, Message.MentionType.HERE));

    @Override
    public CommandData getCommandData() {
        return Commands.slash("echo", "say things")
                .addOption(OptionType.STRING, "text", "the thing to say", true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(GenericCommandInteractionEvent event) {
        OptionMapping mapping = event.getOption("text");
        if (mapping == null) return;

        event.getMessageChannel().sendMessage(mapping.getAsString())
                .setAllowedMentions(allowed)
                .flatMap(m -> event.reply("sent!").setEphemeral(true))
                .queue();
    }
}
