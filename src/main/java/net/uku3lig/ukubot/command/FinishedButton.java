package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.uku3lig.ukubot.core.ButtonData;
import net.uku3lig.ukubot.core.IButton;
import net.uku3lig.ukubot.util.Util;

import java.util.Optional;

public class FinishedButton implements IButton {
    @Override
    public ButtonData getButtonData() {
        return new ButtonData(Button.primary("mod_finished", "Mark as finished"));
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        if (event.getGuild() == null) return;

        MessageEmbed edited = Util.getEmbed(event)
                .setColor(0xff41b1b5)
                .setTitle("MOD REQUEST FINISHED")
                .build();

        String channelId = Optional.ofNullable(edited.getDescription()).orElse("0").replaceAll("\\D+", "");
        TextChannel channel = event.getGuild().getTextChannelById(channelId);
        if (channel == null) {
            event.reply("Unknown channel.").setEphemeral(true).queue();
            return;
        }

        channel.getManager().sync()
                .flatMap(v -> event.editMessageEmbeds(edited).setActionRows())
                .flatMap(v -> event.getHook().sendMessage("Closed ticket for being finished.").setEphemeral(true))
                .queue();

        // TODO modal to provide info about the finished mod
    }
}
