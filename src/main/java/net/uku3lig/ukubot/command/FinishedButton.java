package net.uku3lig.ukubot.command;

import com.electronwill.nightconfig.core.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.uku3lig.ukubot.Main;
import net.uku3lig.ukubot.core.IButton;
import net.uku3lig.ukubot.core.IModal;
import net.uku3lig.ukubot.util.Util;

import java.util.Objects;
import java.util.Optional;

public class FinishedButton implements IButton, IModal {
    @Override
    public Button getButton() {
        return Button.primary("mod_finished", "Mark as finished");
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        if (event.getGuild() == null) return;

        MessageEmbed edited = Util.getEmbed(event)
                .setColor(0xff41b1b5)
                .setTitle("MOD REQUEST FINISHED")
                .build();

        Config config = Main.getGuildConfig(event.getGuild());
        TextChannel channel = Main.getJda().getTextChannelById(config.getLongOrElse("finished_channel", 0));
        if (channel == null) {
            event.reply("Please set a finished mod channel with /config.").setEphemeral(true).queue();
            return;
        }

        channel.sendMessageEmbeds(edited)
                .map(m -> Util.addInfoToModal(event, edited, m, getModal()))
                .flatMap(m -> Util.closeTicket(event, "finished", edited, event.replyModal(m)))
                .queue();
    }

    @Override
    public Modal getModal() {
        TextInput link = TextInput.create("mod_link", "Link to the mod", TextInputStyle.SHORT)
                .setPlaceholder("https://modrinth.com/mod/potioncounter")
                .build();

        TextInput received = TextInput.create("mod_received", "Amount received", TextInputStyle.SHORT)
                .setPlaceholder("15€")
                .build();

        return Modal.create("mod_finished", "Finshed Mod")
                .addActionRow(link)
                .addActionRow(received)
                .build();
    }

    @Override
    public void onModal(ModalInteractionEvent event) {
        if (event.getGuild() == null) return;
        String link = Objects.requireNonNull(event.getValue("mod_link")).getAsString();
        String amount = Objects.requireNonNull(event.getValue("mod_received")).getAsString();
        String msgID = Optional.ofNullable(event.getValue("message_id")).map(ModalMapping::getAsString).orElse("0");
        String msgUrl = Objects.requireNonNull(event.getValue("message_url")).getAsString();

        Config config = Main.getGuildConfig(event.getGuild());

        event.deferReply(true)
                .flatMap(v -> Util.sendRejectionToUser(event, "finished", link))
                .map(v -> event.getGuild().getTextChannelById(config.getLongOrElse("finished_channel", 0)))
                .flatMap(c -> c.retrieveMessageById(msgID))
                .flatMap(m -> {
                    EmbedBuilder builder = new EmbedBuilder(m.getEmbeds().stream().findFirst().orElse(null))
                            .addField("link", link, false)
                            .addField("received", amount, false)
                            .addField("original request", "[link]("+msgUrl+")", false);
                    return m.editMessageEmbeds(builder.build());
                })
                .queue();
    }
}
