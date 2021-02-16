package net.uku3lig.ukubot.commands.xp;

import net.dv8tion.jda.api.EmbedBuilder;
import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.hibernate.entities.MemberXp;
import net.uku3lig.ukubot.utils.progress.ProgressRenderer;
import net.uku3lig.ukubot.utils.progress.ProgressStyle;
import net.uku3lig.ukubot.subsystems.xp.ExperienceListener;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RankCommand extends Command {
    @Override
    public String command() {
        return "rank";
    }

    @Override
    public String description() {
        return "Show the user's xp stats";
    }

    @Override
    public void onCommandReceived(CommandReceivedEvent event) {
        MemberXp member = ExperienceListener.findMember((event.args.length == 0 ?
                event.getAuthor().getIdLong() : Long.parseLong(event.args[0].replaceAll("\\D", ""))),
                event.getGuild());

        double progressToNextLvl = member.getCurrentLevelXp() / ExperienceListener.xpToLevelUp(member.getLevel());
        String levelField = formatNum(member.getCurrentLevelXp()) + " / " +
                formatNum(ExperienceListener.xpToLevelUp(member.getLevel())) + "\n`" +
                ProgressRenderer.render(ProgressStyle.UNICODE_BLOCK, progressToNextLvl, 30) + "`";

        MemberXp[] membersAround = getMembersAround(member);
        String around = "You";
        if (membersAround[0] != null) {
            String uname = Main.getJda().retrieveUserById(membersAround[0].getMemberId()).complete().getName();
            around = uname + " > " + formatNum(getXpDiff(member, membersAround[0])) + " > " + around;
        }
        if (membersAround[1] != null) {
            String uname = Main.getJda().retrieveUserById(membersAround[1].getMemberId()).complete().getName();
            around += " > " + formatNum(getXpDiff(member, membersAround[1])) + " > " + uname;
        }

        member.getParent().getMembers().sort(Comparator.comparing(MemberXp::getTotalXp).reversed());

        EmbedBuilder builder = Main.getDefaultEmbed()
                .setTitle("Stats of " + Main.getJda().retrieveUserById(member.getMemberId()).complete().getName() +
                        " (#" + (member.getParent().getMembers().indexOf(member) + 1) + ")")
                .addField("Level " + member.getLevel(), levelField, false)
                .addField("Total XP points", formatNum(member.getTotalXp()), false)
                .addField("Average XP per message", formatNum(member.getAvgXp()), false)
                .addField("Total messages", formatNum(member.getTotalMsgCount()), true)
                .addField("Total messages that gave XP", formatNum(member.getTotalXpMsgCount()), true)
                .addField("Other users", around, false);

        event.getChannel().sendMessage(builder.build()).queue();
    }

    private String formatNum(double number) {
        return new DecimalFormat("#.##").format(number <= 1000 ? number : number / 1000)
                + (number <= 1000 ? "" : "k");
    }

    private MemberXp[] getMembersAround(MemberXp member) {
        final MemberXp[] around = new MemberXp[] {null, null};
        member.getParent().getMembers().sort(Comparator.comparing(MemberXp::getTotalXp));
        List<MemberXp> membersAround = member.getParent().getMembers().stream()
                .skip(Math.max(member.getParent().getMembers().indexOf(member) - 1, 0)).limit(3)
                .collect(Collectors.toList());

        if (membersAround.indexOf(member) == 0) around[1] = membersAround.get(1);
        else {
            around[0] = membersAround.get(0);
            if (membersAround.size() == 3) around[1] = membersAround.get(2);
        }

        return around;
    }

    private double getXpDiff(MemberXp one, MemberXp two) {
        return Math.abs(one.getTotalXp() - two.getTotalXp());
    }

    @Override
    public String help() {
        return "rank [@mention|id]";
    }
}
