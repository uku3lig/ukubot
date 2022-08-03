package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.uku3lig.ukubot.core.ButtonData;
import net.uku3lig.ukubot.core.IButton;

import java.awt.*;

public class RejectButton implements IButton {
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

        event.editMessageEmbeds(edited).setActionRows().queue();
        // TODO dm the user
    }
}
