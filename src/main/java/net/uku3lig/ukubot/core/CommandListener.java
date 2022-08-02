package net.uku3lig.ukubot.core;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.uku3lig.ukubot.Main;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Main.getCommands().stream()
                .filter(c -> c.getData().getName().equals(event.getName()))
                .forEach(c -> c.onCommand(event));
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        Main.getModals().stream()
                .filter(m -> m.getModal().getId().equals(event.getModalId()))
                .forEach(m -> m.onModal(event));
    }
}
