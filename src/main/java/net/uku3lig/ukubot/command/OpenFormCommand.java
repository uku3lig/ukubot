package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.uku3lig.ukubot.core.ICommand;
import net.uku3lig.ukubot.core.IModal;

public class OpenFormCommand implements ICommand, IModal {
    @Override
    public CommandData getData() {
        return Commands.slash("openform", "opens the form, duh");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        event.replyModal(getModal()).queue();
    }

    @Override
    public Modal getModal() {
        TextInput name = TextInput.create("name", "your name", TextInputStyle.SHORT)
                .setPlaceholder("shigma tuwubine")
                .build();

        TextInput gender = TextInput.create("gender", "your gender", TextInputStyle.SHORT)
                .setPlaceholder("woman")
                .build();

        return Modal.create("owo", "very serious form")
                .addActionRow(name)
                .addActionRow(gender)
                .build();
    }

    @Override
    public void onModal(ModalInteractionEvent event) {
        event.replyFormat("%s is a %s", event.getValue("name").getAsString(), event.getValue("gender").getAsString()).queue();
    }
}
