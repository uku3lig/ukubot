package net.uku3lig.ukubot.commands.core;

import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.spring.OAuth2Controller;
import net.uku3lig.ukubot.utils.Util;
import net.uku3lig.ukubot.utils.translation.T;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.model.ExternalDependency;
import org.gradle.tooling.model.GradleModuleVersion;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InfoCommand extends Command {
    @Override
    public String command() {
        return "info";
    }

    @Override
    public String description() {
        return "Shows info about the bot";
    }

    public InfoCommand() {
        jdaVersion = GradleConnector.newConnector().forProjectDirectory(new File(".")).connect()
                .model(EclipseProject.class).get().getClasspath().getAll()
                .stream().map(ExternalDependency::getGradleModuleVersion)
                .filter(Objects::nonNull)
                .filter(d -> d.getName().equalsIgnoreCase("JDA"))
                .map(GradleModuleVersion::getVersion).findFirst().orElse("unknown");
    }

    private final String jdaVersion;
    private static final String java = "<:java:813387615478743060>";

    @Override
    public void onCommandReceived(CommandReceivedEvent event) {
        event.getChannel().sendMessage("Computing, please wait").queue(m -> info(m, event));
    }

    @SneakyThrows
    private void info(Message m, CommandReceivedEvent event) {
        String javaVersion = System.getProperty("java.version");

        long totMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        String ramUsage = Util.humanReadableByteCount(totMem - freeMem);
        long pid = ProcessHandle.current().pid();

        long guilds = Main.getJda().getGuilds().size();
        long channels = Main.getJda().getTextChannels().size() + Main.getJda().getVoiceChannels().size();
        long users = Main.getJda().getGuilds().stream()
                .flatMap(g -> g.loadMembers().get().stream())
                .map(Member::getUser)
                .collect(Collectors.toSet()).size();

        Set<File> totalFiles = Files.walk(Paths.get(".")).filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toSet());
        long totalJavaFiles = Files.walk(Paths.get("."))
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .count();
        long totalLines = totalFiles.stream().mapToLong(Util::countLinesNew).sum();
        long totalChars = totalFiles.stream().mapToLong(File::length).sum();

        GHRepository repo = GitHub.connectAnonymously().getRepository("uku3lig/ukubot");
        String sha1 = repo.getBranch("dev").getSHA1();
        GHCommit lastCommit = repo.getCommit(sha1);

        String uku = "<@319463560356823050> | [git](https://github.com/uku3lig)";

        EmbedBuilder builder = Main.getDefaultEmbed(event.getAuthor())
                .setTitle(T.get("cmd.info.title", event))
                .addField(T.get("cmd.info.authors", event), uku, true)
                .addField("Java", "**java** `%s`\n**JDA** `%s`".formatted(javaVersion, jdaVersion), true)
                .addField(T.get("cmd.info.usage", event),
                        "**%s** used ram\nPID: **%d**".formatted(ramUsage, pid), true)
                .addField(T.get("cmd.info.guilds", event), String.valueOf(guilds), true)
                .addField(T.get("cmd.info.channels", event), String.valueOf(channels), true)
                .addField(T.get("cmd.info.users", event), String.valueOf(users), true)
                .addField(T.get("cmd.info.files", event),
                        "%d (%d %s)".formatted(totalFiles.size(), totalJavaFiles, java), true)
                .addField(T.get("cmd.info.lines", event), String.valueOf(totalLines), true)
                .addField(T.get("cmd.info.chars", event), String.valueOf(totalChars), true)
                .addField(T.get("cmd.info.commit", event), "%s | %s | [`%s`](%s)"
                        .formatted(lastCommit.getCommitShortInfo().getMessage(), lastCommit.getAuthor().getName(),
                                sha1.substring(0, 7), lastCommit.getHtmlUrl().toString()), false)
                .addField(T.get("cmd.info.links", event), "[source](https://github.com/uku3lig/ukubot) | " +
                        "[invite](%s)".formatted(OAuth2Controller.getInviteUrl()), false);

        m.editMessage(" ").embed(builder.build()).queue();
    }

    @Override
    public String help() {
        return "info";
    }
}
