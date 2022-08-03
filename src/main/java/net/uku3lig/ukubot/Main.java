package net.uku3lig.ukubot;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.uku3lig.ukubot.core.CommandListener;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
public class Main {
    private static final Path TOKEN_PATH = Path.of("./UKUBOT_TOKEN");
    private static final Path CONFIG_PATH = Path.of("./ukubot_config.toml");
    private static final Set<Consumer<JDA>> consumers = new HashSet<>();

    @Getter
    private static JDA jda;
    @Getter
    private static FileConfig config;

    public static void main(String[] args) throws LoginException, InterruptedException {
        jda = JDABuilder.createDefault(readToken())
                .setActivity(Activity.watching("your mod requests"))
                .addEventListeners(new CommandListener())
                .build()
                .awaitReady();

        config = FileConfig.builder(CONFIG_PATH)
                .autoreload()
                .autosave()
                .build();
        config.load();

        consumers.forEach(c -> c.accept(jda));
    }

    public static void runWhenReady(Consumer<JDA> consumer) {
        consumers.add(consumer);
    }

    public static Config getGuildConfig(Guild guild) {
        return config.getOrElse(guild.getId(), Config::inMemory);
    }

    public static void editGuildConfig(Guild guild, Consumer<Config> operator) {
        Config cfg = getGuildConfig(guild);
        operator.accept(cfg);
        config.set(guild.getId(), cfg);
    }

    private static String readToken() {
        if (!Files.exists(TOKEN_PATH) || !Files.isRegularFile(TOKEN_PATH)) return "";
        try {
            return Files.readString(TOKEN_PATH, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Could not read token file", e);
            return "";
        }
    }
}
