package net.uku3lig.ukubot.subsystems.core;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.uku3lig.ukubot.core.Config;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.subsystems.Subsystem;
import org.jetbrains.annotations.NotNull;

public class PrefixOnMention extends Subsystem {
    @Override
    public @NotNull String getName() {
        return "Prefix sender on mention";
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (!event.getMessage().getMentionedMembers()
                .contains(event.getGuild().getMember(Main.getJda().getSelfUser()))) return;
        Config.getConfigByGuild(event.getGuild()).ifPresent(cfg ->
                event.getChannel().sendMessage("My prefix here is `%s`".formatted(cfg.getPrefix())).queue());
    }
}
