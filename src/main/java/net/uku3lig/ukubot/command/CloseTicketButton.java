package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.uku3lig.ukubot.core.ButtonData;
import net.uku3lig.ukubot.core.IButton;

public class CloseTicketButton implements IButton {
    @Override
    public ButtonData getButtonData() {
        return new ButtonData(Button.danger("close_ticket", "Close ticket"));
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        if (event.getGuild() == null || event.getMember() == null) return;

        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.reply("You do not have permission to do this.").setEphemeral(true).queue();
            return;
        }

        event.reply("closing ticket...").setEphemeral(true)
                .flatMap(InteractionHook::editOriginalComponents)
                .flatMap(h -> event.getChannel().asTextChannel().getManager().sync())
                .queue();

        // TODO closed tickets category
        // TODO delete on close
        // TODO modal to choose what to do after ticket is closed (finished -> send embed in finished channel, not finished -> delete/archive)
    }
}
