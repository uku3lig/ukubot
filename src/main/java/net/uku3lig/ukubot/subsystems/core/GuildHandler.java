package net.uku3lig.ukubot.subsystems.core;

import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.uku3lig.ukubot.config.Config;
import net.uku3lig.ukubot.hibernate.Database;
import net.uku3lig.ukubot.subsystems.Subsystem;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GuildHandler extends Subsystem {
    @Override
    public @NotNull String getName() {
        return "GuildJoinEvent handler";
    }

    @Override
    public @NotNull String getDescription() {
        return "Removes guilds from database when left";
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        Optional<Config> cfg = Config.getConfigByGuild(event.getGuild());
        if (cfg.isEmpty()) return;
        Database.delete(cfg.get());
    }

    @Override
    public void onGenericGuild(@NotNull GenericGuildEvent event) {
        if (Database.getById(Config.class, event.getGuild().getIdLong()).isPresent()) return;
        Config.newDefaultConfig(event.getGuild());
    }
}
