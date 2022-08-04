package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.uku3lig.ukubot.core.ButtonData;
import net.uku3lig.ukubot.core.IButton;
import net.uku3lig.ukubot.util.Util;

import java.util.Optional;

public class DiscontinuedButton implements IButton {
    @Override
    public ButtonData getButtonData() {
        return new ButtonData(Button.secondary("mod_discontinued", "Mark as discontinued"));
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        if (event.getGuild() == null) return;

        MessageEmbed edited = Util.getEmbed(event)
                .setColor(0xff7a1e0d)
                .setTitle("MOD REQUEST DISCONTINUED")
                .build();

        String channelId = Optional.ofNullable(edited.getDescription()).orElse("0").replaceAll("\\D+", "");
        TextChannel channel = event.getGuild().getTextChannelById(channelId);
        if (channel == null) {
            event.reply("Unknown channel.").setEphemeral(true).queue();
            return;
        }

        channel.getManager().sync()
                .flatMap(v -> event.editMessageEmbeds(edited).setActionRows())
                .flatMap(v -> event.getHook().sendMessage("Closed ticket for being discontinued.").setEphemeral(true))
                .queue();

        // TODO closed tickets category
        // TODO delete on close
        // TODO dm the user
    }
}
