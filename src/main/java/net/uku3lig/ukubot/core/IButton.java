package net.uku3lig.ukubot.core;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface IButton {
    ButtonData getButtonData();

    void onButtonClick(ButtonInteractionEvent event);
}
