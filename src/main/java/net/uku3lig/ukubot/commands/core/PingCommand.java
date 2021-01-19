package net.uku3lig.ukubot.commands.core;

import net.dv8tion.jda.api.EmbedBuilder;
import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.core.Main;

import java.time.Instant;

public class PingCommand extends Command {
    @Override
    public String command() {
        return "ping";
    }

    @Override
    public String description() {
        return "Returns the ping of the bot";
    }

    @Override
    public void onCommandReceived(CommandReceivedEvent event) {
        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(Main.botName)
                .setTitle("Ping")
                .setColor(Main.embedColor)
                .addField("Gateway", Main.getJda().getGatewayPing() + " ms", true)
                .setTimestamp(Instant.now());
        Main.getJda().getRestPing().queue(ping -> {
            builder.addField("REST", ping + " ms", true);
            event.getChannel().sendMessage(builder.build()).queue();
        });
    }

    @Override
    public String help() {
        return "ping";
    }
}
