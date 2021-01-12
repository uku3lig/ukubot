package net.uku3lig.ukubot.commands;

import net.dv8tion.jda.api.entities.Member;

import java.util.function.Predicate;

public enum IsSenderAllowed {
    Default(member -> true),
    Uku(member -> member.getId().equals("319463560356823050"));

    private final Predicate<Member> p;

    IsSenderAllowed(Predicate<Member> p) {
        this.p = p;
    }

    public boolean isAllowed(Member m) {
        return p.test(m);
    }
}
