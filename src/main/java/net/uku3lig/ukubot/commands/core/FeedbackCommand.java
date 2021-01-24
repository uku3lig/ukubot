package net.uku3lig.ukubot.commands.core;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.core.Main;

import java.time.Duration;

public class FeedbackCommand extends Command {
    @Override
    public String command() {
        return "feedback";
    }

    @Override
    public String description() {
        return "Sends a feedback to Uku";
    }

    @Override
    public void onCommandReceived(CommandReceivedEvent event) {
        if (event.args.length == 0) {
            sendHelp(event.getChannel());
            return;
        }
        String content = String.join(" ", event.args);
        event.getChannel().sendMessage("Thank you for your feedback!").queue();
        sendFeedback(content, event.getAuthor());
    }

    private void sendFeedback(String content, User sender) {
        Main.getJda().openPrivateChannelById("319463560356823050").flatMap(uku -> {
            EmbedBuilder builder = Main.getDefaultEmbed()
                    .setAuthor(sender.getName(),null, sender.getEffectiveAvatarUrl())
                    .setTitle("New feedback from " + sender.getName())
                    .setDescription(content)
                    .setFooter(sender.getName() + "#" + sender.getDiscriminator() + " (" + sender.getId() + ")");
            return uku.sendMessage(builder.build());
        }).queue();
    }

    @Override
    public String help() {
        return "feedback <message>";
    }

    @Override
    public Duration cooldown() {
        return Duration.ofMinutes(1);
    }
}
