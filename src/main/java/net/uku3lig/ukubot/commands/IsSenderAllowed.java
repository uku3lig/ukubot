package net.uku3lig.ukubot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.function.Predicate;

public enum IsSenderAllowed {
    Default(member -> true),
    Moderator(member -> member.hasPermission(Permission.MESSAGE_MANAGE)),
    Administrator(member -> member.hasPermission(Permission.ADMINISTRATOR)),
    Friend(member -> member.getUser().getMutualGuilds().stream().map(Guild::getIdLong)
            .anyMatch(id -> id == 796380718481408000L)),
    Uku(member -> member.getId().equals("319463560356823050"));

    private final Predicate<Member> p;

    IsSenderAllowed(Predicate<Member> p) {
        this.p = p;
    }

    public boolean isAllowed(Member m) {
        return p.test(m);
    }
}
