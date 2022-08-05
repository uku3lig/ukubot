package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
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

import java.util.Optional;

public class DiscontinuedButton implements IButton, IModal {
    @Override
    public Button getButton() {
        return Button.secondary("mod_discontinued", "Mark as discontinued");
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        if (event.getGuild() == null) return;

        MessageEmbed edited = Util.getEmbed(event)
                .setColor(0xff7a1e0d)
                .setTitle("MOD REQUEST DISCONTINUED")
                .build();

        String channelId = Optional.ofNullable(edited.getDescription()).orElse("0").replaceAll("\\D+", "");
        TextChannel channel = event.getGuild().getTextChannelById(channelId);
        if (channel == null) {
            event.reply("Unknown channel.").setEphemeral(true).queue();
            return;
        }

        event.replyModal(Util.addUserToModal(edited, getModal()))
                .flatMap(v -> channel.getManager().sync())
                .flatMap(v -> event.getHook().editOriginalEmbeds(edited).setActionRows())
                .flatMap(v -> event.getHook().sendMessage("Closed ticket for being discontinued.").setEphemeral(true))
                .queue();

        // TODO closed tickets category
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
        String reasonText = "";
        ModalMapping modalReason = event.getValue("reason");

        if (modalReason != null && !modalReason.getAsString().isEmpty() && !modalReason.getAsString().isBlank()) {
            reasonText = "Reason: " + modalReason.getAsString();
        }

        Util.sendRejectionToUser(event, "discontinued", reasonText).queue();
    }
}
