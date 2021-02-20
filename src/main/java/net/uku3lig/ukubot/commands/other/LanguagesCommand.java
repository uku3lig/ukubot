package net.uku3lig.ukubot.commands.other;

import net.dv8tion.jda.api.EmbedBuilder;
import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.utils.translation.Language;
import net.uku3lig.ukubot.utils.translation.T;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class LanguagesCommand extends Command {
    @Override
    public String command() {
        return "languages";
    }

    @Override
    public String description() {
        return "Shows all the possible languages";
    }

    @Override
    public void onCommandReceived(CommandReceivedEvent event) {
        String languages = Arrays.stream(Language.values())
                .map(l -> l.name() + ": " + l.locale)
                .collect(Collectors.joining("\n"));
        EmbedBuilder builder = Main.getDefaultEmbed(event.getAuthor())
                .setTitle(T.get("commands.languages.name", event).orElse("Languages"))
                .setDescription(languages);
        event.getChannel().sendMessage(builder.build()).queue();
    }

    @Override
    public Collection<String> aliases() {
        return Collections.singleton("lang");
    }

    @Override
    public String help() {
        return "languages";
    }
}
