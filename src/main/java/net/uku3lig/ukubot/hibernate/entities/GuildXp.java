package net.uku3lig.ukubot.hibernate.entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.uku3lig.ukubot.hibernate.Database;

import javax.persistence.*;
import java.util.*;

@Entity
@Getter @Setter(AccessLevel.PRIVATE)
public class GuildXp {
    @Id
    private long guildId;

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    @OrderBy("totalXp DESC")
    private List<MemberXp> members;

    /**
     * JPA constructor.
     */
    protected GuildXp() {}

    public GuildXp(long guildId, MemberXp... members) {
        this.guildId = guildId;
        this.members = new ArrayList<>(Arrays.asList(members));

        Database.saveOrUpdate(this);
    }

    public void addMember(MemberXp member) {
        members.add(member);
        Database.saveOrUpdate(this);
    }

    public static GuildXp from(Guild g) {
        return new GuildXp(g.getIdLong());
    }

    public static GuildXp from(Guild g, Collection<MemberXp> members) {
        return new GuildXp(g.getIdLong(), members.toArray(MemberXp[]::new));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuildXp guildXp = (GuildXp) o;
        return guildId == guildXp.guildId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(guildId);
    }
}
