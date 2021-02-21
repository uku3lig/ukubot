package net.uku3lig.ukubot.commands.xp;

import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.hibernate.Database;
import net.uku3lig.ukubot.hibernate.entities.GuildXp;
import net.uku3lig.ukubot.hibernate.entities.MemberXp;
import net.uku3lig.ukubot.subsystems.core.PagedEmbed;
import net.uku3lig.ukubot.subsystems.xp.ExperienceListener;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TopXpCommand extends Command {
    @Override
    public String command() {
        return "top";
    }

    @Override
    public Collection<String> aliases() {
        return Collections.singleton("levels");
    }

    @Override
    public String description() {
        return "Shows the xp leaderboard";
    }

    @Override
    public void onCommandReceived(CommandReceivedEvent event) {
        GuildXp guild = ExperienceListener.findGuild(event.getGuild().getIdLong());
        Function<PagedEmbed.Offset, MemberXp[]> objects = o -> Database.findSorted(
                MemberXp.class, o.offset, o.pageSize,
                "parent.guildId=" + guild.getGuildId(),
                Database.Order.desc("totalXp")).toArray(MemberXp[]::new);

        PagedEmbed.builder(Database.count(MemberXp.class, (b, r) -> b.equal(r.get("parent"), guild)), MemberXp.class)
                .objects(objects)
                .sorter(Comparator.comparing(MemberXp::getTotalXp).reversed())
                .allowed(m -> m.getIdLong() == event.getAuthor().getIdLong())
                .channel(event.getChannel())
                .embed(Main.getDefaultEmbed(event.getAuthor()).setTitle("Xp leaderboard").build())
                .build();
    }

    @Override
    public String help() {
        return "top";
    }
}
