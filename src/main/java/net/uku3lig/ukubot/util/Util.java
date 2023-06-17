package net.uku3lig.ukubot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.RestAction;
import net.uku3lig.ukubot.Main;
import net.uku3lig.ukubot.command.DeleteTicketButton;
import net.uku3lig.ukubot.command.ExportButton;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.io.StringWriter;
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
                .flatMap(v -> event.getHook().editOriginalEmbeds(embed).setActionRow(new ExportButton().getButton(), new DeleteTicketButton().getButton()))
                .flatMap(v -> event.getHook().sendMessage("Closed ticket.").setEphemeral(true));
    }

    public static String exportMessage(Message m) {
        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);

        // header
        printer.print("[" + m.getTimeCreated() + "] ");
        printer.print(m.getAuthor().getName());
        if (m.isPinned()) printer.print(" (pinned)");
        printer.println();

        // content
        printer.println(m.getContentDisplay());
        printer.println();

        // attachments
        if (!m.getAttachments().isEmpty()) {
            printer.println("{Attachments}");
            m.getAttachments().forEach(a -> printer.println(a.getUrl()));
            printer.println();
        }

        // embeds
        if (!m.getEmbeds().isEmpty()) {
            for (MessageEmbed embed : m.getEmbeds()) {
                printer.println("{Embed}");

                if (embed.getAuthor() != null && !isEmpty(embed.getAuthor().getName()))
                    printer.println("author: " + embed.getAuthor().getName());
                if (!isEmpty(embed.getUrl())) printer.println("url: " + embed.getUrl());
                if (!isEmpty(embed.getTitle())) printer.println("title: " + embed.getTitle());
                if (!isEmpty(embed.getDescription())) printer.println("description: " + embed.getDescription());

                if (!embed.getFields().isEmpty()) {
                    printer.println();
                    for (MessageEmbed.Field field : embed.getFields()) {
                        if (!isEmpty(field.getName())) printer.println(field.getName() + ": ");
                        if (!isEmpty(field.getValue())) printer.println(field.getValue());
                        printer.println();
                    }
                }

                if (embed.getThumbnail() != null && !isEmpty(embed.getThumbnail().getUrl()))
                    printer.println("thumbnail: " + embed.getThumbnail().getUrl());
                if (embed.getImage() != null && !isEmpty(embed.getImage().getUrl()))
                    printer.println("image: " + embed.getImage().getUrl());
                if (embed.getFooter() != null && !isEmpty(embed.getFooter().getText()))
                    printer.println("footer: " + embed.getFooter().getText());

                printer.println();
            }
        }

        if (!m.getStickers().isEmpty()) {
            printer.println("{Stickers}");
            m.getStickers().forEach(s -> printer.println(s.getIconUrl()));
            printer.println();
        }

        if (!m.getReactions().isEmpty()) {
            printer.println("{Reactions}");
            for (MessageReaction reaction : m.getReactions()) {
                printer.print(reaction.getEmoji().getName());
                if (reaction.getCount() > 1) printer.print(" (" + reaction.getCount() + ")");
                printer.print(' ');
            }
            printer.println();
        }

        printer.println();
        return writer.toString();
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty() || s.isBlank();
    }

    private Util() {}
}
