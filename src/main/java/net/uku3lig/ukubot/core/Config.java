package net.uku3lig.ukubot.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandAdapter;
import net.uku3lig.ukubot.commands.IsSenderAllowed;
import net.uku3lig.ukubot.hibernate.Database;

import javax.persistence.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter @Setter(AccessLevel.PRIVATE)
public class Config {
    private static Config defaultInstance;

    @Id
    private long guildId;

    @Transient
    private Guild guild;

    @Setter(AccessLevel.PUBLIC)
    private String prefix = "?";

    @MapKeyColumn
    @ElementCollection
    private final Map<String, Duration> cooldowns = new HashMap<>();

    @Transient
    private final Map<String, IsSenderAllowed> permissions = new HashMap<>();

    @MapKeyColumn
    @ElementCollection
    private final Map<String, String> persistentPermissions = new HashMap<>();

    protected Config(Guild g) {
        CommandAdapter.getCommands().forEach(c -> {
            cooldowns.put(c.getClass().getSimpleName(), c.cooldown());
            permissions.put(c.getClass().getSimpleName(), c.allowed());
            persistentPermissions.put(c.getClass().getSimpleName(), c.allowed().name());
        });

        if (g != null) guildId = g.getIdLong();
        Database.saveOrUpdate(this);
    }

    //JPA constructor
    protected Config() {
        Main.runWhenReady(jda -> guild = jda.getGuildById(guildId));
        persistentPermissions.forEach((s, p) -> permissions.put(s, IsSenderAllowed.valueOf(p)));
    }

    public Config editCooldown(Class<? extends Command> command, Duration cooldown) {
        if (cooldowns.get(command.getSimpleName()).isNegative() ||
                permissions.get(command.getSimpleName()).equals(IsSenderAllowed.Uku))
            throw new SecurityException("Cannot change this command's cooldown");
        cooldowns.put(command.getSimpleName(), cooldown);
        return this;
    }

    public Config editAllowed(Class<? extends Command> command, IsSenderAllowed allowed) {
        if (permissions.get(command.getSimpleName()).equals(IsSenderAllowed.Uku))
            throw new SecurityException("Cannot change this command's allowed");
        permissions.put(command.getSimpleName(), allowed);
        return this;
    }

    public static Config newDefaultConfig(Guild g) {
        return new Config(g);
    }

    public static Optional<Config> getConfigByGuild(Guild g) {
        return Database.getAll(Config.class).stream()
                .filter(cfg -> cfg.guildId == g.getIdLong())
                .findFirst();
    }

    static {
        Main.runWhenReady(jda -> {
            Set<Guild> cfgs = Database.getAll(Config.class).stream()
                    .map(cfg -> cfg.guild).collect(Collectors.toSet());
            Set<Guild> guilds = new HashSet<>(Main.getJda().getGuilds());
            if (cfgs.size() != guilds.size()) {
                guilds.removeAll(cfgs); //all guilds that aren't in config
                cfgs.removeAll(Main.getJda().getGuilds()); //all configs that are linked to left guilds

                guilds.stream().map(Config::new).forEach(Database::saveOrUpdate);
                cfgs.stream().map(Config::new).forEach(Database::delete);
            }
        });
    }
}
