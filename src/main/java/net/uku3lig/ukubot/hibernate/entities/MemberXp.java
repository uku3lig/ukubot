package net.uku3lig.ukubot.hibernate.entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.hibernate.Database;
import net.uku3lig.ukubot.subsystems.xp.ExperienceListener;
import net.uku3lig.ukubot.utils.Util;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter @Setter(AccessLevel.PRIVATE)
public class MemberXp {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "guildId")
    private GuildXp parent;

    @Id
    private long memberId;

    private long level, totalXpMsgCount, totalMsgCount;
    private double currentLevelXp, totalXp, avgXp;

    /**
     * JPA constructor.
     */
    protected MemberXp() {}

    public MemberXp(long memberId, GuildXp parent) {
        this.memberId = memberId;
        this.level = 0;
        this.totalMsgCount = 0;
        this.totalXpMsgCount = 0;
        this.currentLevelXp = 0;
        this.totalXp = 0;
        this.avgXp = 0;
        this.parent = parent;

        Database.saveOrUpdate(this);
    }

    /**
     * Adds an amount of experience to the member.
     * @param amount The amount of experience.
     * @return <code>true</code> if the player levels up, otherwise <code>false</code>.
     */
    public boolean addXp(double amount) {
        totalXp += amount;
        totalXpMsgCount++;
        avgXp = totalXp / totalXpMsgCount;
        boolean levelsUp = false;
        if (totalXp > ExperienceListener.totalXpRequired(level+1)) {
            level++;
            currentLevelXp = totalXp - ExperienceListener.totalXpRequired(level);
            levelsUp = true;
        } else currentLevelXp += amount;
        Database.saveOrUpdate(this);
        return levelsUp;
    }

    public void increaseMsgCount() {
        totalMsgCount++;
        Database.saveOrUpdate(this);
    }

    public void reset() {
        this.level = 0;
        this.totalMsgCount = 0;
        this.totalXpMsgCount = 0;
        this.currentLevelXp = 0;
        this.totalXp = 0;
        this.avgXp = 0;

        Database.saveOrUpdate(this);
    }

    public static MemberXp from(Member m, GuildXp guild) {
        return new MemberXp(m.getIdLong(), guild);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberXp memberXp = (MemberXp) o;
        return memberId == memberXp.memberId && parent.equals(memberXp.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, memberId);
    }

    @Override
    public String toString() {
        return "**" + Main.getJda().retrieveUserById(memberId).complete().getName() + "**" +
                " - __Total XP:__ `" + Util.formatNum(totalXp) +
                "` - __Total messages:__ `" + Util.formatNum(totalXpMsgCount) + "`";
    }
}
