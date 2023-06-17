package net.uku3lig.ukubot.core;

import com.electronwill.nightconfig.core.Config;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.uku3lig.ukubot.Main;
import org.jetbrains.annotations.NotNull;

public class LeagueListener extends ListenerAdapter {
    @Override
    public void onUserActivityStart(@NotNull UserActivityStartEvent event) {
        if (event.getNewActivity().getName().toLowerCase().contains("league of legends")) {
            Config config = Main.getGuildConfig(event.getGuild());
            TextChannel channel = event.getGuild().getTextChannelById(config.getLongOrElse("league_channel", 0));

            if (channel != null) {
                channel.sendMessageFormat("%s stop playing league loser", event.getUser().getAsMention()).queue();
            }
        }
    }
}
