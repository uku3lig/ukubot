package net.uku3lig.ukubot.commands;

import net.dv8tion.jda.api.entities.Guild;

import java.util.Collection;
import java.util.Collections;

public abstract class Command {
    public boolean enabled = true;

    public abstract String command();

    public abstract void onCommandReceived(CommandReceivedEvent event);

    public Collection<String> aliases() {
        return Collections.emptyList();
    }

    public IsSenderAllowed allowed() {
        return IsSenderAllowed.Default;
    }

    public String help() {
        return CommandAdapter.defaultPrefix + command();
    }

    public String help(Guild g) {
        return CommandAdapter.prefixes.get(g) + command();
    }
}
