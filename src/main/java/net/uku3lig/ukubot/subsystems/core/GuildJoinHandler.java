package net.uku3lig.ukubot.subsystems.core;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.uku3lig.ukubot.core.Config;
import net.uku3lig.ukubot.subsystems.Subsystem;
import org.jetbrains.annotations.NotNull;

public class GuildJoinHandler extends Subsystem {
    @Override
    public @NotNull String getName() {
        return "GuildJoinEvent handler";
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Config.newDefaultConfig(event.getGuild());
    }
}
