package net.uku3lig.ukubot.commands.reddit;

import com.vdurmont.emoji.EmojiParser;
import lombok.SneakyThrows;
import net.dean.jraw.models.Submission;
import net.dean.jraw.tree.RootCommentNode;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.uku3lig.ukubot.commands.SingleCommand;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.core.Main;

import java.util.concurrent.atomic.AtomicReference;

public class MemeCommand extends SingleCommand {
    @Override
    public String command() {
        return "meme";
    }

    @Override
    public String description() {
        return "Sends a meme from r/dankmemes";
    }

    @SneakyThrows
    @Override
    public void onSingleCommandReceived(CommandReceivedEvent event) {
        AtomicReference<Message> msg = new AtomicReference<>();
        event.getChannel().sendMessage("Computing, please wait...").queue(msg::set);
        RootCommentNode r = RedditCommand.randomPost(RedditCommand.getReddit().subreddit("dankmemes"), 1500);
        if (r == null) {
            msg.get().editMessage("Sorry, cannot fetch meme right now. Please retry later").queue();
            return;
        }
        Submission s = r.getSubject();
        EmbedBuilder builder = Main.getDefaultEmbed(event.getAuthor())
                .setTitle(s.getTitle(), s.getUrl())
                .setAuthor("r/" + s.getSubreddit() + " • " + s.getAuthor(), "https://reddit.com/u/" + s.getAuthor())
                .setImage(s.getUrl())
                .setTimestamp(s.getCreated().toInstant())
                .setFooter(s.getScore() + " " + EmojiParser.parseToUnicode(":arrow_up:"));
        msg.get().editMessage(" ").embed(builder.build()).queue();
    }

    @Override
    public String help() {
        return "meme";
    }
}
