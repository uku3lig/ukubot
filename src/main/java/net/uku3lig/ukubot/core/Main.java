package net.uku3lig.ukubot.core;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.WebhookCluster;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.uku3lig.ukubot.commands.CommandAdapter;
import net.uku3lig.ukubot.console.ConsoleAdapter;
import net.uku3lig.ukubot.hibernate.Database;
import net.uku3lig.ukubot.subsystems.SubsystemAdapter;
import net.uku3lig.ukubot.utils.DockerSecrets;
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
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SpringBootApplication(scanBasePackages = "net.uku3lig.ukubot.spring")
public class Main {
    @Getter
    private static JDA jda = null;
    private static final Set<Consumer<JDA>> runWhenReady = new HashSet<>();
    private static final WebhookCluster webhooks = new WebhookCluster();

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static final int embedColor = 0x009420;

    public static void main(String[] args) {
        Instant start = Instant.now();
        SpringApplication.run(Main.class, args);
        Database.init();
        ConsoleAdapter.getInstance().start();
        try {
            jda = JDABuilder.createDefault(getToken())
                    //add our adapters
                    .addEventListeners(CommandAdapter.getInstance(), SubsystemAdapter.getInstance())
                    //Cache and intents
                    .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                    .enableIntents(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS)
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
            Duration timeToStart = Duration.between(start, Instant.now());
            logger.info("Bot ready! (%s)".formatted(timeToStart));
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
                .setColor(embedColor)
                .setTimestamp(Instant.now())
                .setFooter("UkuBot", jda.getSelfUser().getEffectiveAvatarUrl());
    }

    public static EmbedBuilder getDefaultEmbed(User u) {
        return getDefaultEmbed().setFooter("Requested by " + u.getName(), u.getEffectiveAvatarUrl());
    }

    private static final AtomicLong webhookIdSupplier = new AtomicLong(0);

    public static WebhookClient createWebhook(String url) {
        WebhookClient webhook = new WebhookClientBuilder(url)
                .setThreadFactory(job -> {
                    Thread t = new Thread(job);
                    t.setName("Webhook-" + webhookIdSupplier.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }).setWait(true).build();
        webhooks.addWebhooks(webhook);
        return webhook;
    }

    public static WebhookEmbedBuilder getDefaultWebhookEmbed() {
        return new WebhookEmbedBuilder()
                .setTimestamp(Instant.now())
                .setColor(embedColor)
                .setFooter(new WebhookEmbed.EmbedFooter("UkuBot Webhook", jda.getSelfUser().getEffectiveAvatarUrl()));
    }
}
