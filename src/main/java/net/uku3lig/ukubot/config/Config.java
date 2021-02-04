package net.uku3lig.ukubot.config;

import io.mokulu.discord.oauth.model.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.hibernate.Database;

import javax.persistence.*;
import java.util.*;

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

    public static Config newDefaultConfig(Guild g, User user) {
        Objects.requireNonNull(Main.getJda().getUserById(user.getId())).openPrivateChannel().queue(pch -> {
            pch.sendMessage("Thank you for inviting me to your guild **" + g.getName() + "**!\n" +
                    "My default prefix is `?`, and use `?help` to see the available commands.\n" +
                    "If you need help, come to my discord server: https://discord.gg/CN8vCMyq6H\n" +
                    "Do `?invite` to get an invite link for another guild!").queue();
        });
        return new Config(g);
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
