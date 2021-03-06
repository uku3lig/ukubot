package net.uku3lig.ukubot.core;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.uku3lig.ukubot.commands.CommandAdapter;
import net.uku3lig.ukubot.console.ConsoleAdapter;
import net.uku3lig.ukubot.hibernate.Database;
import net.uku3lig.ukubot.subsystems.SubsystemAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SpringBootApplication(scanBasePackages = "net.uku3lig.ukubot.spring")
public class Main {
    @Getter
    private static JDA jda = null;
    private static final Set<Consumer<JDA>> runWhenReady = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static final String botName = "UkuBot";
    public static final Color embedColor = Color.getHSBColor(1.37f, 1, 0.58f);

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        ConsoleAdapter.getInstance().start();
        Database.init();
        try {
            jda = JDABuilder.createDefault(getToken())
                    //add our adapters
                    .addEventListeners(CommandAdapter.getInstance(), SubsystemAdapter.getInstance())
                    //Cache and intents
                    .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                    .enableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_VOICE_STATES)
                    .enableCache(CacheFlag.ACTIVITY)
                    //fun
                    .setActivity(Activity.playing("vous rendre heureux"))
                    //login to discord
                    .build();
        } catch (LoginException e) {
            logger.error("The token seems to be wrong, are you sure it's correct ?");
            Runtime.getRuntime().exit(2);
        }
        try {
            jda.awaitReady();
            runWhenReady.forEach(c -> new Thread(() -> c.accept(jda)).start());
            logger.info("Bot ready!");
        } catch (InterruptedException ignored) {
        }
    }

    private static String getToken() {
        if (DockerSecrets.getSecret("token").isPresent()) return DockerSecrets.getSecret("token").get();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("./TOKEN"));
            String token = reader.readLine();
            if (token == null || token.isEmpty() || token.isBlank()) {
                logger.error("Cannot find token in file './TOKEN', are you sure it is there ?");
                Runtime.getRuntime().exit(1);
            }
            return token.strip();
        } catch (FileNotFoundException e) {
            logger.error("Cannot find token, check if 'TOKEN' exists");
            Runtime.getRuntime().exit(1);
            return null;
        } catch (IOException e) {
            logger.error("An error happened while starting the bot", e);
            Runtime.getRuntime().exit(1);
            return null;
        }
    }

    public static void runWhenReady(Consumer<JDA> c) {
        runWhenReady.add(c);
    }

    public static boolean isJar() {
        return Main.class.getResource("Main.class").toExternalForm().startsWith("jar:");
    }

    public static boolean isDocker() {
        try (Stream<String> stream = Files.lines(Paths.get("/proc/1/cgroup"))) {
            return stream.anyMatch(line -> line.contains("/docker") || line.contains("/ecs"));
        } catch (IOException e) {
            return false;
        }
    }

    public static EmbedBuilder getDefaultEmbed() {
        return new EmbedBuilder()
                .setAuthor(botName)
                .setColor(embedColor)
                .setTimestamp(Instant.now());
    }
}
