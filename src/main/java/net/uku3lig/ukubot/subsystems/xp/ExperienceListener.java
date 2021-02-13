package net.uku3lig.ukubot.subsystems.xp;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.uku3lig.ukubot.config.Config;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.hibernate.Database;
import net.uku3lig.ukubot.hibernate.entities.GuildXp;
import net.uku3lig.ukubot.hibernate.entities.MemberXp;
import net.uku3lig.ukubot.hibernate.entities.Word;
import net.uku3lig.ukubot.subsystems.Subsystem;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExperienceListener extends Subsystem {
    @Override
    public @NotNull String getName() {
        return "Experience listener";
    }

    @Override
    public @NotNull String getDescription() {
        return "Listens for messages and gives xp points based on the messages content";
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    private static final Set<MemberXp> onCooldown = new HashSet<>();
    private static final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();
    private static final Logger logger = LoggerFactory.getLogger(ExperienceListener.class);

    private ExperienceListener() {
        executor.scheduleAtFixedRate(onCooldown::clear, 1, 1, TimeUnit.MINUTES);
        if (Database.getAll(Word.class).isEmpty()) {
            try {
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("freq.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        Objects.requireNonNull(is), StandardCharsets.UTF_8));
                reader.lines().forEach(word -> {
                    String name = word.split("[\\s\\t]+")[1];
                    long freq = Long.parseLong(word.split("[\\s\\t]+")[0]);
                    if (Database.getById(Word.class, name).isPresent())
                        Database.getById(Word.class, name).get().increaseFrequency(freq);
                    else new Word(name, freq);
                });
            } catch (Exception e) {
                logger.error("Error: an exception occurred while reading freq.txt");
                e.printStackTrace();
                Runtime.getRuntime().exit(6);
            }
        }
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.getAuthor().getIdLong() == Main.getJda().getSelfUser().getIdLong()) return;
        if (event.getMessage().getContentRaw().startsWith(Config.getEffectiveConfig(event.getGuild()).getPrefix()))
            return; //if it is a command, ignore it

        MemberXp member = findMember(event.getAuthor().getIdLong(), event.getGuild());

        member.increaseMsgCount();
        //checks if the member already got his xp
        if (onCooldown.contains(member)) return;

        onCooldown.add(member);
        double xpAmount = computeXpAmount(event.getMessage());

        if (member.addXp(xpAmount))
            event.getChannel().sendMessage(Config.getEffectiveConfig(event.getGuild()).getLevelUpMessage()
                    .replace("@mention", event.getAuthor().getAsMention())
                    .replace("(level)", String.valueOf(member.getLevel()))).queue();
    }

    private double computeXpAmount(Message message) {
        String[] words = message.getContentDisplay()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\w\\s\\n\\r]+", "")
                .split("[\\s\\n\\r]+");

        return Arrays.stream(words)
                .filter(w -> w.length() <= 50)
                .peek(w -> {
                    if (Database.getById(Word.class, w).isEmpty()) new Word(w);
                    else Database.getById(Word.class, w).get().increaseFrequency();
                })
                .map(w -> Database.getById(Word.class, w))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparingLong(Word::getFrequency).reversed())
                .limit(10)
                .mapToDouble(w -> computeWordXp(w, message.getGuild()))
                .sum();
    }

    private double computeWordXp(Word word, Guild guild) {
        long sum = Database.getAll(Word.class).stream().mapToLong(Word::getFrequency).sum();
        double freq = (double) word.getFrequency() / sum;
        double factor = Config.getEffectiveConfig(guild).getXpFactor();
        double freqLog = Math.log((freq * factor * 100) + 1);
        double wordFactor = 0.05 * Math.log(word.getWord().length());
        return freqLog + wordFactor;
    }

    public static long xpToLevelUp(long currentLevel) {
        if (0 <= currentLevel && currentLevel <= 15) return 2 * currentLevel + 7;
        else if (15 < currentLevel && currentLevel <= 30) return 5 * currentLevel - 38;
        else return 9 * currentLevel - 158;
    }

    public static double totalXpRequired(long targetLevel) {
        if (0 <= targetLevel && targetLevel <= 16) return Math.pow(targetLevel, 2) + 6 * targetLevel;
        else if (16 < targetLevel && targetLevel <= 31) return 2.5 * Math.pow(targetLevel, 2) - 40.5 * targetLevel + 360;
        else return 4.5 * Math.pow(targetLevel, 2) - 162.5 * targetLevel + 2220;
    }

    public static GuildXp findGuild(long id) {
        return Database.getById(GuildXp.class, id).orElseGet(() -> new GuildXp(id));
    }

    public static MemberXp findMember(long id, Guild guild) {
        GuildXp guildXp = findGuild(guild.getIdLong());
        return guildXp.getMembers().stream()
                .filter(m -> m.getMemberId() == id)
                .findFirst().orElseGet(() -> new MemberXp(id, guildXp));
    }
}
