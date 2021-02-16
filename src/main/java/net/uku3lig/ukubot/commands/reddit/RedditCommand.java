package net.uku3lig.ukubot.commands.reddit;

import com.vdurmont.emoji.EmojiParser;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Submission;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.references.SubredditReference;
import net.dean.jraw.tree.RootCommentNode;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.uku3lig.ukubot.commands.SingleCommand;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.utils.DockerSecrets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class RedditCommand extends SingleCommand {
    @Getter
    private static RedditClient reddit;
    private static final Logger logger = LoggerFactory.getLogger(RedditCommand.class);
    private static final int maxCalls = 15, maxUpvotes = 1500;

    @Override
    public String command() {
        return "reddit";
    }

    @Override
    public String description() {
        return "Gets random posts from reddit";
    }

    public RedditCommand() {
        String password = DockerSecrets.getSecretOrFile("reddit_password", Path.of("./REDDIT_PASSWORD"));
        String clientSecret = DockerSecrets.getSecretOrFile("reddit_client_secret", Path.of("./REDDIT_CS"));
        Credentials credentials = Credentials.script("uku3lig", password, "CnkSx8zfOvJzbg", clientSecret);
        UserAgent userAgent = new UserAgent("bot", "net.uku3lig.ukubot", "1.0.1", "UkuBot");
        reddit = OAuthHelper.automatic(new OkHttpNetworkAdapter(userAgent), credentials);
    }

    @SneakyThrows
    @Override
    public void onSingleCommandReceived(CommandReceivedEvent event) {
        AtomicReference<Message> msg = new AtomicReference<>();
        event.getChannel().sendMessage("Computing, please wait...").queue(msg::set);

        Submission s;
        switch (event.args.length) {
            case 0 -> s = reddit.randomSubreddit().randomSubmission().getSubject();
            case 1 -> {
                if (Pattern.compile("\\d+").matcher(event.args[0]).matches()) {
                    RootCommentNode r = randomPost(reddit.randomSubreddit(), Integer.parseInt(event.args[0]));
                    if (r == null) {
                        msg.get().editMessage("Sorry, cannot find post. Please retry later.").queue();
                        return;
                    } else s = r.getSubject();
                } else s = reddit.subreddit(event.args[0]).randomSubmission().getSubject();
            }
            default -> {
                if (!Pattern.compile("\\d+").matcher(event.args[1]).matches()) {
                    sendHelp(event.getChannel());
                    return;
                }
                int minUpvotes = Integer.parseInt(event.args[1]);
                RootCommentNode r = randomPost(reddit.subreddit(event.args[0]), minUpvotes);
                if (r == null) {
                    msg.get().editMessage("Sorry, cannot find post. Please retry later.").queue();
                    return;
                } else s = r.getSubject();
            }
        }
        SubmissionType type = SubmissionType.of(new URL(s.getUrl()));
        EmbedBuilder builder = Main.getDefaultEmbed()
                .setTitle(s.getTitle(), s.getUrl())
                .setAuthor("r/" + s.getSubreddit() + " • " + s.getAuthor() + "(" + s.getAuthorFlairText() + ")",
                        "https://reddit.com/u/" + s.getAuthor())
                .setTimestamp(s.getCreated().toInstant())
                .setFooter(s.getScore() + " " + EmojiParser.parseToUnicode(":arrow_up:"));
        switch (type) {
            case Text -> builder.setDescription(s.getSelfText()
                    .substring(0, Math.min(s.getSelfText().length(), MessageEmbed.TEXT_MAX_LENGTH)));
            case Image -> builder.setImage(s.getUrl());
            case Default -> builder.setDescription(s.getUrl());
        }
        msg.get().editMessage(" ").embed(builder.build()).queue();
    }

    @Override
    public String help() {
        return "reddit [sub] [minimum upvotes (0 < n < 1500)]";
    }

    public static RootCommentNode randomPost(SubredditReference sub, int minUpvotes) {
        minUpvotes = Math.min(minUpvotes, maxUpvotes);
        RootCommentNode r;
        for (int i = 0; i < maxCalls; i++) {
            r = sub.randomSubmission();
            if (r.getSubject().getScore() >= minUpvotes) return r;
        }
        return null;
    }

    public enum SubmissionType {
        Text(0),
        Image(1),
        Default(2);

        SubmissionType(int type) {

        }

        public static SubmissionType of(URL url) {
            return switch (url.getHost()) {
                case "www.reddit.com" -> Text;
                case "i.redd.it" -> Image;
                default -> Default;
            };
        }
    }
}
