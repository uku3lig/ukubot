package net.uku3lig.ukubot.command;

import com.electronwill.nightconfig.core.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
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

        Config config = Main.getGuildConfig(event.getGuild());
        TextChannel channel = Main.getJda().getTextChannelById(config.getLongOrElse("form_channel", 0));
        if (channel == null) {
            event.reply("Please set a form channel using the /config command.").setEphemeral(true).queue();
            return;
        }

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
        if (event.getGuild() == null) return;
        if (Boolean.TRUE.equals(Main.getGuildConfig(event.getGuild()).getOrElse("requests_open", true)))
            event.replyModal(getModal()).queue();
        else event.reply("Requests are closed, please retry later.").setEphemeral(true).queue();
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

        Config config = Main.getGuildConfig(event.getGuild());
        TextChannel channel = Main.getJda().getTextChannelById(config.getLongOrElse("form_channel", 0));
        if (channel == null) {
            event.reply("The owner didn't set a form result channel.").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(event.getUser().getAsTag(), null, event.getUser().getEffectiveAvatarUrl())
                .setTimestamp(Instant.now());

        event.getValues().stream()
                .filter(m -> !m.getAsString().isEmpty() && !m.getAsString().isBlank())
                .forEach(m -> builder.addField(m.getId(), m.getAsString(), false));

        channel.sendMessageEmbeds(builder.build())
                .setActionRow(new AcceptButton().getButton(event.getGuild()), new RejectButton().getButton(event.getGuild()))
                .flatMap(m -> event.reply("Thanks for your submission. You will receive a reply shortly.").setEphemeral(true))
                .queue();
    }
}