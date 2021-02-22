package net.uku3lig.ukubot.config;

import io.mokulu.discord.oauth.model.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.hibernate.Database;
import net.uku3lig.ukubot.utils.translation.Language;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;
import java.util.Optional;

@Entity
@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PACKAGE)
public class Config {
    @Id
    private long guildId;

    private String prefix;

    private double xpFactor;
    private String lvlMsg;

    private String language;

    protected Config(Guild g) {
        this.prefix = "?";
        this.xpFactor = 0.35;
        this.lvlMsg = "GG @mention, you leveled up to (level)!";
        this.language = Language.English.locale;

        if (g != null) guildId = g.getIdLong();
        if (g != null) Database.saveOrUpdate(this);
    }

    //JPA constructor
    protected Config() {
    }

    public static Config newDefaultConfig(Guild g, User user) {
        Objects.requireNonNull(Main.getJda().getUserById(user.getId())).openPrivateChannel().queue(pch ->
                pch.sendMessage("Thank you for inviting me to your guild **" + g.getName() + "**!\n" +
                        "My default prefix is `?`, and use `?help` to see the available commands.\n" +
                        "If you need help, come to my discord server: https://discord.gg/CN8vCMyq6H\n" +
                        "Do `?invite` to get an invite link for another guild!\n" +
                        "Do `?settings` to change the configuration of the bot.\n" +
                        "Do `?settings language` to change the language.").queue());
        return new Config(g);
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
