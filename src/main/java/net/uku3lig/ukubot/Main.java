package net.uku3lig.ukubot;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class Main {
    private static final Path TOKEN_PATH = Path.of("./UKUBOT_TOKEN");

    @Getter
    private static JDA jda;

    public static void main(String[] args) throws LoginException {
        jda = JDABuilder.createDefault(readToken())
                .setActivity(Activity.watching("your mod requests"))
                .build();
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
