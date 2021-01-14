package net.uku3lig.ukubot;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.uku3lig.ukubot.commands.CommandAdapter;
import net.uku3lig.ukubot.subsystems.SubsystemAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class Main {
    @Getter
    private static JDA jda = null;
    private static final Set<Consumer<JDA>> runWhenReady = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            jda = JDABuilder.createDefault(getToken())
                    //add our adapters
                    .addEventListeners(CommandAdapter.getInstance(), SubsystemAdapter.getInstance())
                    //Cache and intents
                    .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                    .setDisabledIntents(GatewayIntent.DIRECT_MESSAGES,GatewayIntent.GUILD_VOICE_STATES)
                    .enableIntents(GatewayIntent.GUILD_PRESENCES)
                    .enableCache(CacheFlag.ACTIVITY)
                    //login to discord
                    .build();
        } catch (LoginException e) {
            logger.error("The token seems to be wrong, are you sure it's correct ?");
            Runtime.getRuntime().exit(2);
        }
        try {
            jda.awaitReady();
            runWhenReady.forEach(c -> new Thread(() -> c.accept(jda)).start());
        } catch (InterruptedException ignored) {
        }
    }

    private static String getToken() {
        String path = Files.exists(Path.of("/run/secrets/token")) ? "/run/secrets/token" : "./TOKEN";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String token = reader.readLine();
            if (token == null || token.isEmpty() || token.isBlank()) {
                logger.error("Cannot find token in file '" + path + "', are you sure it is there ?");
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
}
