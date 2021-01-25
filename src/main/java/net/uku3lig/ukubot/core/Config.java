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
@Getter(AccessLevel.PUBLIC)
public class Config {
    @Id
    @Setter(AccessLevel.PRIVATE)
    private long guildId;

    @Transient
    private Guild guild;

    @Setter(AccessLevel.PRIVATE)
    private String prefix;

    protected Config(Guild g) {
        this.prefix = "?";

        if (g != null) guildId = g.getIdLong();
        if (g != null) Database.saveOrUpdate(this);
    }

    //JPA constructor
    protected Config() {
        Main.runWhenReady(jda -> guild = jda.getGuildById(guildId));
    }

    public Config editPrefix(String newPrefix) {
        prefix = newPrefix;
        Database.saveOrUpdate(this);
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

    public static Config getEffectiveConfig(Guild g) {
        return getConfigByGuild(g).orElseGet(() -> newDefaultConfig(g));
    }
}
