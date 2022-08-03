package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.uku3lig.ukubot.Main;
import net.uku3lig.ukubot.core.ButtonData;
import net.uku3lig.ukubot.core.IButton;
import net.uku3lig.ukubot.core.ICommand;
import net.uku3lig.ukubot.core.IModal;

import java.time.Instant;
import java.util.Objects;

public class OpenRequestsCommand implements ICommand, IButton, IModal {
    @Override
    public CommandData getCommandData() {
        return Commands.slash("openrequests", "opens the server for requests in the current channel")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(GenericCommandInteractionEvent event) {
        if (!event.getChannelType().isMessage()) return;
        if (event.getGuild() == null) return;

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Request a mod/plugin")
                .setDescription("Click the button below to request a mod or plugin.");

        event.getMessageChannel().sendMessageEmbeds(builder.build())
                .setActionRow(getButton(event.getGuild()))
                .flatMap(m -> event.reply("Created.").setEphemeral(true))
                .queue();
    }

    @Override
    public ButtonData getButtonData() {
        return new ButtonData(Button.success("open_mod_request", "Open Mod Request")
                .withEmoji(Emoji.fromUnicode("\uD83D\uDCD1")));
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        event.replyModal(getModal()).queue();
    }

    @Override
    public Modal getModal() {
        TextInput desc = TextInput.create("mod_desc", "Give a brief description of your mod", TextInputStyle.PARAGRAPH)
                .setRequiredRange(20, 1000)
                .setPlaceholder("A mod that counts how many potions you have in your inventory")
                .build();

        TextInput amount = TextInput.create("mod_amount", "How much would you pay?", TextInputStyle.SHORT)
                .setRequiredRange(2, 50)
                .setPlaceholder("15€")
                .build();

        TextInput deadline = TextInput.create("mod_deadline", "(OPTIONAL) Do you have a deadline?", TextInputStyle.SHORT)
                .setRequired(false)
                .setRequiredRange(5, 100)
                .setPlaceholder("Don't say 'as soon as possible'.")
                .build();

        return Modal.create("mod_form", "Mod Request Form")
                .addActionRow(desc)
                .addActionRow(amount)
                .addActionRow(deadline)
                .build();
    }

    @Override
    public void onModal(ModalInteractionEvent event) {
        if (event.getGuild() == null) {
            event.reply("ok").queue();
            return;
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(event.getUser().getAsTag(), null, event.getUser().getEffectiveAvatarUrl())
                .setFooter(event.getUser().getId())
                .setTimestamp(Instant.now());

        event.getValues().stream()
                .filter(m -> !m.getAsString().isEmpty() && !m.getAsString().isBlank())
                .forEach(m -> builder.addField(m.getId(), m.getAsString(), false));

        Objects.requireNonNull(Main.getJda().getTextChannelById(1002390384082702426L))
                .sendMessageEmbeds(builder.build())
                .setActionRow(new AcceptButton().getButton(event.getGuild()), new RejectButton().getButton(event.getGuild()))
                .flatMap(m -> event.reply("Thanks for your submission. You will receive a reply shortly.").setEphemeral(true))
                .queue();
    }
}
