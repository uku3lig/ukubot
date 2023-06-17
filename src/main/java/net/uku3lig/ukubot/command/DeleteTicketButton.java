package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.uku3lig.ukubot.core.IButton;
import net.uku3lig.ukubot.util.Util;

public class DeleteTicketButton implements IButton {
    @Override
    public Button getButton() {
        return Button.secondary("ticket_delete", "Delete ticket");
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        if (event.getGuild() == null) return;

        MessageEmbed embed = Util.getEmbed(event)
                .setDescription("ticket has been deleted")
                .build();

        TextChannel channel = Util.getTicket(event);
        if (channel == null) {
            event.reply("could not find channel.").setEphemeral(true).queue();
            return;
        }

        channel.delete()
                .flatMap(v -> event.editMessageEmbeds(embed).setActionRow())
                .flatMap(v -> event.getHook().sendMessage("Deleted ticket.").setEphemeral(true)).queue();
    }
}
