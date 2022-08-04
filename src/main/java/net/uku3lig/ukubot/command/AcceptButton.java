package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.uku3lig.ukubot.Main;
import net.uku3lig.ukubot.core.ButtonData;
import net.uku3lig.ukubot.core.IButton;
import net.uku3lig.ukubot.util.Util;

import java.awt.*;
import java.util.EnumSet;
import java.util.Optional;

public class AcceptButton implements IButton {
    @Override
    public ButtonData getButtonData() {
        return new ButtonData(Button.success("mod_accept", "Accept Mod"));
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        if (event.getGuild() == null) return;

        EmbedBuilder builder = Util.getEmbed(event)
                .setTitle("MOD REQUEST ACCEPTED")
                .setColor(Color.GREEN);

        Category category = Main.getJda().getCategoryById(Main.getGuildConfig(event.getGuild()).getLongOrElse("ticket_category", 0));
        if (category == null) {
            event.reply("Please set a ticket category with /config.").setEphemeral(true).queue();
            return;
        }

        Optional<String> opt = Optional.ofNullable(builder.build().getFooter()).map(MessageEmbed.Footer::getText);
        event.getGuild().retrieveMemberById(opt.orElse("0"))
                .flatMap(member -> category.createTextChannel(member.getUser().getAsTag())
                        .addMemberPermissionOverride(member.getIdLong(), EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.noneOf(Permission.class)))
                .flatMap(c -> c.sendMessageEmbeds(builder.build()).setActionRow(new CloseTicketButton().getButton(event.getGuild())))
                .flatMap(m -> event.editMessageEmbeds(builder.setDescription(m.getChannel().getAsMention()).build()).setActionRows())
                .queue();
    }
}
