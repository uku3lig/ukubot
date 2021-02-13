package net.uku3lig.ukubot.hibernate.entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.uku3lig.ukubot.hibernate.Database;

import javax.persistence.*;

@Entity
@Table(name = "wordFrequency")
@Getter @Setter(AccessLevel.PRIVATE)
public class Word {
    @Id
    @Column(length = 50)
    private String word;
    private long frequency;

    protected Word() {}

    public Word(String word, long frequency) {
        this.word = word;
        this.frequency = frequency;

        Database.saveOrUpdate(this);
    }

    public Word(String word) {
        this.word = word;
        this.frequency = 1;

        Database.saveOrUpdate(this);
    }

    public void increaseFrequency() {
        increaseFrequency(1);
    }

    public void increaseFrequency(long amount) {
        frequency += amount;
        Database.saveOrUpdate(this);
    }
}
