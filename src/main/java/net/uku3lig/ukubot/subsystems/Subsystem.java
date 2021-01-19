package net.uku3lig.ukubot.subsystems;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Subsystem extends ListenerAdapter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public boolean enabled = isEnabledByDefault();

    @NotNull
    public abstract String getName();

    @NotNull
    public String getDescription() {
        return "No description provided.";
    }

    public boolean isEnabledByDefault() {
        return false;
    }
}
