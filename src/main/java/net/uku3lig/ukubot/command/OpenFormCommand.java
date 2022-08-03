package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.uku3lig.ukubot.core.ButtonData;
import net.uku3lig.ukubot.core.IButton;
import net.uku3lig.ukubot.core.ICommand;
import net.uku3lig.ukubot.core.IModal;

public class OpenFormCommand implements ICommand, IModal, IButton {
    @Override
    public CommandData getCommandData() {
        return Commands.slash("openform", "opens the form, duh");
    }

    @Override
    public void onCommand(GenericCommandInteractionEvent event) {
        if (event.getGuild() == null) event.reply("no guild? :nobitches:").queue();
        else event.reply("omg a button").addActionRow(getButtonData().getButton(event.getGuild())).queue();
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

    @Override
    public ButtonData getButtonData() {
        return new ButtonData(Button.success("open_form_button", "open le form")
                .withEmoji(Emoji.fromUnicode("\uD83D\uDCDC")));
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        event.replyModal(getModal()).queue();
    }
}
