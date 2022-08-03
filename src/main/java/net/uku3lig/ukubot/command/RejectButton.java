package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.uku3lig.ukubot.Main;
import net.uku3lig.ukubot.core.ButtonData;
import net.uku3lig.ukubot.core.IButton;
import net.uku3lig.ukubot.core.IModal;

import java.awt.*;
import java.util.Objects;

public class RejectButton implements IButton, IModal {
    @Override
    public ButtonData getButtonData() {
        return new ButtonData(Button.danger("mod_reject", "Reject Mod"));
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        MessageEmbed edited = new EmbedBuilder(event.getMessage().getEmbeds().stream().findFirst().orElse(null))
                .setTitle("MOD REQUEST REJECTED")
                .setColor(Color.RED)
                .build();

        Modal.Builder builder = getModal().createCopy();
        MessageEmbed.AuthorInfo info = edited.getAuthor();
        if (info != null) builder.addActionRow(TextInput.create("user", "User", TextInputStyle.SHORT).setValue(info.getName()).build());

        event.replyModal(builder.build())
                .flatMap(v -> event.getHook().editOriginalEmbeds(edited).setActionRows())
                .queue();
    }

    @Override
    public Modal getModal() {
        TextInput reason = TextInput.create("reject_reason", "Rejection reason", TextInputStyle.SHORT)
                .setRequired(false)
                .build();

        return Modal.create("mod_reject_modal", "Mod Rejection")
                .addActionRow(reason)
                .build();
    }

    @Override
    public void onModal(ModalInteractionEvent event) {
        ModalMapping user = event.getValue("user");
        if (user == null) {
            event.reply("Request rejected. No DM sent.").setEphemeral(true).queue();
            return;
        }

        String reasonText = "No reason was provided.";
        ModalMapping modalReason = event.getValue("reject_reason");

        if (modalReason != null && !modalReason.getAsString().isEmpty() && !modalReason.getAsString().isBlank()) {
            reasonText = "Reason: " + modalReason.getAsString();
        }

        String finalReasonText = reasonText;
        event.reply("Request rejected.").setEphemeral(true)
                .flatMap(h -> Objects.requireNonNull(Main.getJda().getUserByTag(user.getAsString())).openPrivateChannel())
                .flatMap(c -> c.sendMessage("Your mod request was rejected. " + finalReasonText))
                .queue();
    }
}
