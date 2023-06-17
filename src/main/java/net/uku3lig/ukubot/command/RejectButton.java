package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.uku3lig.ukubot.core.IButton;
import net.uku3lig.ukubot.core.IModal;
import net.uku3lig.ukubot.util.Util;

import java.awt.*;

public class RejectButton implements IButton, IModal {
    @Override
    public Button getButton() {
        return Button.danger("mod_reject", "Reject Mod");
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        MessageEmbed edited = Util.getEmbed(event)
                .setTitle("MOD REQUEST REJECTED")
                .setColor(Color.RED)
                .build();

        event.replyModal(Util.addUserToModal(edited, getModal()))
                .flatMap(v -> event.getHook().editOriginalEmbeds(edited).setActionRow())
                .queue();
    }

    @Override
    public Modal getModal() {
        TextInput reason = TextInput.create("reject_reason", "Rejection reason", TextInputStyle.PARAGRAPH)
                .setRequired(false)
                .build();

        return Modal.create("mod_reject_modal", "Mod Rejection")
                .addActionRow(reason)
                .build();
    }

    @Override
    public void onModal(ModalInteractionEvent event) {
        String reasonText;
        ModalMapping modalReason = event.getValue("reject_reason");

        if (modalReason != null && !modalReason.getAsString().isEmpty() && !modalReason.getAsString().isBlank()) {
            reasonText = "Reason: " + modalReason.getAsString();
        } else {
            reasonText = "No reason was provided.";
        }

        event.deferReply(true).flatMap(v -> Util.sendRejectionToUser(event, "rejected", reasonText)).queue();
    }
}
