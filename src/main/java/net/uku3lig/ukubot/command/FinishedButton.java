package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.uku3lig.ukubot.core.IButton;
import net.uku3lig.ukubot.core.IModal;
import net.uku3lig.ukubot.util.Util;

import java.util.Objects;

public class FinishedButton implements IButton, IModal {
    @Override
    public Button getButton() {
        return Button.primary("mod_finished", "Mark as finished");
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        MessageEmbed edited = Util.getEmbed(event)
                .setColor(0xff41b1b5)
                .setTitle("MOD REQUEST FINISHED")
                .build();

        Util.closeTicket(event, "finished", edited, event.replyModal(Util.addUserToModal(edited, getModal()))).queue();
    }

    @Override
    public Modal getModal() {
        TextInput link = TextInput.create("mod_link", "Link to the mod", TextInputStyle.SHORT)
                .setPlaceholder("https://modrinth.com/mod/potioncounter")
                .build();

        TextInput received = TextInput.create("mod_received", "Amount received", TextInputStyle.SHORT)
                .setPlaceholder("15€")
                .build();

        return Modal.create("mod_finished", "Finshed Mod")
                .addActionRow(link)
                .addActionRow(received)
                .build();
    }

    @Override
    public void onModal(ModalInteractionEvent event) {
        String link = Objects.requireNonNull(event.getValue("mod_link")).getAsString();

        // TODO send embed in channel
        Util.sendRejectionToUser(event, "finished", link).queue();
    }
}
