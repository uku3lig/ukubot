package net.uku3lig.ukubot.core;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class ButtonData {
    private final Button button;

    public ButtonData(Button button) {
        this.button = button;
    }

    public Button getButton(Guild guild) {
        return button.withId(guild.getId() + "_" + button.getId());
    }
}
