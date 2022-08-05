package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.uku3lig.ukubot.core.IButton;
import net.uku3lig.ukubot.core.IModal;
import net.uku3lig.ukubot.util.Util;

public class DiscontinuedButton implements IButton, IModal {
    @Override
    public Button getButton() {
        return Button.secondary("mod_discontinued", "Mark as discontinued");
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        MessageEmbed edited = Util.getEmbed(event)
                .setColor(0xff7a1e0d)
                .setTitle("MOD REQUEST DISCONTINUED")
                .build();

        Util.closeTicket(event, "discontinued", edited, event.replyModal(Util.addUserToModal(edited, getModal()))).queue();
    }

    @Override
    public Modal getModal() {
        TextInput reason = TextInput.create("reason", "Discontinuation reason", TextInputStyle.SHORT)
                .setRequired(false)
                .build();

        return Modal.create("mod_discont_modal", "Mod Discontinuation")
                .addActionRow(reason)
                .build();
    }

    @Override
    public void onModal(ModalInteractionEvent event) {
        String reasonText;
        ModalMapping modalReason = event.getValue("reason");

        if (modalReason != null && !modalReason.getAsString().isEmpty() && !modalReason.getAsString().isBlank()) {
            reasonText = "Reason: " + modalReason.getAsString();
        } else {
            reasonText = "";
        }

        event.deferReply(true).flatMap(v -> Util.sendRejectionToUser(event, "discontinued", reasonText)).queue();
    }
}
