package net.uku3lig.ukubot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.RestAction;
import net.uku3lig.ukubot.Main;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

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

    @CheckReturnValue
    public static RestAction<?> sendRejectionToUser(ModalInteractionEvent event, String action, @Nullable final String reason) {
        ModalMapping user = event.getValue("user");
        if (user == null || user.getAsString().isEmpty()) {
            return event.replyFormat("Mod request %s. No DM sent.", action).setEphemeral(true);
        }

        return Main.getJda().retrieveUserById(user.getAsString())
                .flatMap(User::openPrivateChannel)
                .flatMap(c -> c.sendMessageFormat("Your mod request was %s. %s", action, reason))
                .flatMap(m -> event.replyFormat("Mod request %s.", action).setEphemeral(true));
    }

    private Util() {}
}
