package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.uku3lig.ukubot.core.ICommand;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RatioCommand implements ICommand {
    private static final String RATIO = "what is this + L + ratio + wrong + get a job + unfunny + you fell off + never liked you anyway + cope + you can't catch this ratio + why about you talk with real peoples + I don't care about your opinion + genshin player + put some thought into what you're going to do with that + au dodo + go to bed + yes, i'm taller than you + I win + conversation over + :) + you don’t know 2 + 2 with yo head + you are going to my cringe compilation + try again loser + rickrolled + no lifer + go ahead whine about it + eat paper + you lose + your problem + no one cares + log off + not okay + glhf + problematic + dog water + you look like a wall + you're a confused block of cheese + slight_smile + aired + cringe again + mad cuz bad + irrelevant + deal with it + screencapped your bio + jealous + i'll be right back + ask deez + ez clap + straight cash + idgaf + ratio again + stay mad + stay pressed + cancelled + done for + don't give a damn + get a job + get blocked + glace à la pistache + rip bozo + no + ok + ok boomer + France-Dijon + Oignon rouge + Méthode Roux + coefficient de raideur + le rap français + la myopie + acide hyaluronique + eau pétillante + chicken louisiane steackhouse + france tv + Rust + plaque tectonique + la troisième loi de Newton+ Scwheps agrume + legrand electronique + BDW TP5 + elodie + valise + méthode d'orthonormalisation de gram-schmidt + hannah montana + les simsons le film + actifry + pic pic alcool et drame + bourre bourre jfais un dram + OM-PSG + 30 mg de sucre en poudre + nescafé + chignon+ Rubik's cube + leo in the baignoire + Le pivot de Gauss + L'aventurier + matrice échelonnée + mocap + l'abonnement velov à 16€ + gdb + module image + la belote + Level'Up + mister mv + stabilo boss + sauce algérienne + ligma + pikachu + Chargeur usb C + métro c + inflation + Pablo + pyjama dinausore + Jonnhy Halliday à Bercy + + QWERTY + télésiège + fraude fiscale + escalope milanaise + les lacs du côneMara + damnation + Chocolat Viennois + raclette + Intégrale de Riemann + Macbook Air + de a u 4 + hubble telescope + sigma grindset + distributivité simple + LIFRW + Chaise pliante + inégalité de Cauchy Schwartz + no polypoints + eduroam + grapic + le duke + macdo charpennes+ Take the L + rip bozo + auttgames + développement limité + ucbl portail + pdt + metro C + Eddy Malou + Mimir moment + feur +";
    private static final Random random = new Random();

    @Override
    public CommandData getCommandData() {
        return Commands.slash("ratio", "FEUR");
    }

    @Override
    public void onCommand(GenericCommandInteractionEvent event) {
        String[] strings = RATIO.split("\\+");
        String ratio = IntStream.range(0, 20)
                .map(i -> random.nextInt(strings.length))
                .mapToObj(i -> strings[i])
                .collect(Collectors.joining("+"));

        event.reply(ratio).queue();
    }
}
