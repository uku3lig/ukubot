package net.uku3lig.ukubot.core;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandAdapter;
import net.uku3lig.ukubot.commands.IsSenderAllowed;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Config {
    public static final Map<Guild, Config> configs = new HashMap<>();
    private static final Config defaultConfig = new Config(null);

    @Setter
    private String prefix = "?";
    private final Map<Class<? extends Command>, Duration> cooldowns = new HashMap<>();
    private final Map<Class<? extends Command>, IsSenderAllowed> permissions = new HashMap<>();

    protected Config(Guild g) {
        CommandAdapter.getCommands().forEach(c -> {
            cooldowns.put(c.getClass(), c.cooldown());
            permissions.put(c.getClass(), c.allowed());
        });

        if (g != null) configs.put(g, this);
    }

    public Config editCooldown(Class<? extends Command> command, Duration cooldown) {
        if (cooldowns.get(command).isNegative() || permissions.get(command).equals(IsSenderAllowed.Uku))
            throw new SecurityException("Cannot change this command's cooldown");
        cooldowns.put(command, cooldown);
        return this;
    }

    public Config editAllowed(Class<? extends Command> command, IsSenderAllowed allowed) {
        if (permissions.get(command).equals(IsSenderAllowed.Uku))
            throw new SecurityException("Cannot change this command's allowed");
        permissions.put(command, allowed);
        return this;
    }

    public static Config newDefaultConfig(Guild g) {
        return new Config(g);
    }
}
