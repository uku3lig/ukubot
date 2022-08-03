package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.uku3lig.ukubot.core.ButtonData;
import net.uku3lig.ukubot.core.IButton;

import java.awt.*;

public class AcceptButton implements IButton {
    @Override
    public ButtonData getButtonData() {
        return new ButtonData(Button.success("mod_accept", "Accept Mod"));
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder(event.getMessage().getEmbeds().stream().findFirst().orElse(null))
                .setTitle("MOD REQUEST ACCEPTED")
                .setColor(Color.GREEN);

        event.editMessageEmbeds(builder.build()).setActionRows().queue();
        // TODO create ticket
    }
}
