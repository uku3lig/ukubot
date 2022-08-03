package net.uku3lig.ukubot.core;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public interface IButton {
    ButtonData getButtonData();

    void onButtonClick(ButtonInteractionEvent event);

    default Button getButton(Guild guild) {
        return getButtonData().getButton(guild);
    }
}
