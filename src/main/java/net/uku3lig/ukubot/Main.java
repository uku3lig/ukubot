package net.uku3lig.ukubot;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.uku3lig.ukubot.command.OpenFormCommand;
import net.uku3lig.ukubot.command.StopCommand;
import net.uku3lig.ukubot.core.CommandListener;
import net.uku3lig.ukubot.core.ICommand;
import net.uku3lig.ukubot.core.IModal;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@Slf4j
public class Main {
    private static final Path TOKEN_PATH = Path.of("./UKUBOT_TOKEN");
    @Getter
    private static final Set<ICommand> commands = Set.of(new OpenFormCommand(), new StopCommand());
    @Getter
    private static final Set<IModal> modals = Set.of(new OpenFormCommand());

    @Getter
    private static JDA jda;

    public static void main(String[] args) throws LoginException, InterruptedException {
        jda = JDABuilder.createDefault(readToken())
                .setActivity(Activity.watching("your mod requests"))
                .addEventListeners(new CommandListener())
                .build()
                .awaitReady();

        jda.getGuilds().forEach(g -> g.updateCommands().addCommands(commands.stream().map(ICommand::getData).toArray(CommandData[]::new)).queue());
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
