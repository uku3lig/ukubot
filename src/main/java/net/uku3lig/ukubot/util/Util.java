package net.uku3lig.ukubot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.RestAction;
import net.uku3lig.ukubot.Main;
import net.uku3lig.ukubot.command.DeleteTicketButton;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.Optional;

public class Util {
    public static EmbedBuilder getEmbed(ComponentInteraction event) {
        return new EmbedBuilder(event.getMessage().getEmbeds().stream().findFirst().orElse(null));
    }

    public static Modal addUserToModal(MessageEmbed embed, Modal modal) {
        TextInput.Builder text = TextInput.create("user", "User", TextInputStyle.SHORT);

        Modal.Builder builder = modal.createCopy();
        MessageEmbed.Footer info = embed.getFooter();
        if (info != null) builder.addActionRow(text.setValue(info.getText()).build());

        return builder.build();
    }

    public static Modal addInfoToModal(ButtonInteractionEvent event, MessageEmbed embed, Message message, Modal modal) {
        TextInput text = TextInput.create("message_id", "Message ID", TextInputStyle.SHORT)
                .setValue(message.getId())
                .build();
        TextInput url = TextInput.create("message_url", "Message url", TextInputStyle.SHORT)
                .setValue(event.getMessage().getJumpUrl())
                .build();

        return addUserToModal(embed, modal).createCopy().addActionRow(text).addActionRow(url).build();
    }

    public static TextChannel getTicket(ButtonInteractionEvent event) {
        if (event.getGuild() == null) return null;

        MessageEmbed embed = getEmbed(event).build();
        String channelId = Optional.ofNullable(embed.getDescription()).orElse("0").replaceAll("\\D+", "");
        return event.getGuild().getTextChannelById(channelId);
    }

    @CheckReturnValue
    public static RestAction<?> sendRejectionToUser(ModalInteractionEvent event, String action, @Nullable final String reason) {
        ModalMapping user = event.getValue("user");
        if (user == null || user.getAsString().isEmpty()) {
            return event.replyFormat("Mod request %s. No DM sent.", action).setEphemeral(true);
        }

        return Main.getJda().retrieveUserById(user.getAsString())
                .flatMap(User::openPrivateChannel)
                .flatMap(c -> c.sendMessageFormat("Your mod request was %s. %s", action, reason))
                .flatMap(m -> event.getHook().sendMessageFormat("Mod request %s.", action).setEphemeral(true));
    }

    @CheckReturnValue
    public static RestAction<?> closeTicket(ButtonInteractionEvent event, String action, MessageEmbed embed, RestAction<?> after) {
        if (event.getGuild() == null) return event.reply("ok!");

        String channelId = Optional.ofNullable(embed.getDescription()).orElse("0").replaceAll("\\D+", "");
        TextChannel channel = event.getGuild().getTextChannelById(channelId);
        if (channel == null) return event.getHook().sendMessage("Unknown channel.").setEphemeral(true);

        Category category = event.getGuild().getCategoryById(Main.getGuildConfig(event.getGuild()).getLongOrElse("closed_category", 0));
        if (category == null) return event.reply("Please set a closed ticket category with /config.").setEphemeral(true);

        return after.flatMap(v -> channel.getManager().setParent(category).setName(channel.getName() + "-" + action))
                .flatMap(v -> channel.getManager().sync())
                .flatMap(v -> event.getHook().editOriginalEmbeds(embed).setActionRow(new DeleteTicketButton().getButton()))
                .flatMap(v -> event.getHook().sendMessage("Closed ticket.").setEphemeral(true));
    }

    private Util() {}
}
