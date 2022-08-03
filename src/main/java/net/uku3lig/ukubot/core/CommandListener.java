package net.uku3lig.ukubot.core;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.uku3lig.ukubot.Main;
import net.uku3lig.ukubot.util.ClassScanner;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public class CommandListener extends ListenerAdapter {
    private final Set<ICommand> commands;
    private final Set<IModal> modals;

    public CommandListener() {
        this.commands = ClassScanner.findSubtypes(ICommand.class);
        this.modals = ClassScanner.findSubtypes(IModal.class);
        Main.runWhenReady(jda -> {
            Collection<CommandData> data = commands.stream().map(ICommand::getData).toList();
            jda.getGuilds().forEach(g -> g.updateCommands().addCommands(data).queue());
        });
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        commands.stream()
                .filter(c -> c.getData().getName().equals(event.getName()))
                .forEach(c -> c.onCommand(event));
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        modals.stream()
                .filter(m -> m.getModal().getId().equals(event.getModalId()))
                .forEach(m -> m.onModal(event));
    }
}
