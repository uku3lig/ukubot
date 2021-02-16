package net.uku3lig.ukubot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.TextChannel;
import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class SingleCommand extends Command {
    protected final Set<TextChannel> instances = new HashSet<>();

    public abstract void onSingleCommandReceived(CommandReceivedEvent event);

    @Override
    public final void onCommandReceived(CommandReceivedEvent event) {
        if (instances.stream().map(TextChannel::getGuild)
                .map(Guild::getId).anyMatch(id -> event.getGuild().getId().equalsIgnoreCase(id))) {
            Optional<TextChannel> c = instances.stream()
                    .filter(t -> t.getId().equalsIgnoreCase(event.getChannel().getId()))
                    .findFirst();
            event.getChannel().sendMessage("Error: someone is already using this command in " +
                    c.map(IMentionable::getAsMention).orElse("a channel of this guild")).queue();
        } else {
            instances.add(event.getChannel());
            onSingleCommandReceived(event);
            instances.remove(event.getChannel());
        }
    }
}
