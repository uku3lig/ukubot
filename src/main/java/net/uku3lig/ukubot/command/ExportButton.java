package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.uku3lig.ukubot.core.IButton;
import net.uku3lig.ukubot.util.Util;

import java.nio.charset.StandardCharsets;

public class ExportButton implements IButton {
    @Override
    public Button getButton() {
        return Button.primary("export_ticket", "Export & delete ticket");
    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        if (event.getGuild() == null) return;

        event.deferEdit().queue();

        TextChannel channel = Util.getTicket(event);
        if (channel == null) {
            event.reply("unknown channel").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embed = Util.getEmbed(event);

        StringBuilder builder = new StringBuilder()
                .append("=".repeat(62)).append("\n")
                .append("Guild: ").append(channel.getGuild().getName()).append("\n")
                .append("Channel: ").append(channel.getName()).append("\n");

        if (!Util.isEmpty(channel.getTopic())) builder.append("Topic: ").append(channel.getTopic()).append("\n");
        builder.append("=".repeat(62)).append("\n\n");

        channel.getIterableHistory().cache(false).reverse().forEachAsync(m -> {
            builder.append(Util.exportMessage(m));
            return true;
        }).thenRun(() -> {
            Member owner = channel.getGuild().getOwner();
            if (owner != null) {
                String title = String.join("-", channel.getName(), channel.getId()) + ".txt";
                byte[] content = builder.toString().getBytes(StandardCharsets.UTF_8);

                owner.getUser().openPrivateChannel()
                        .flatMap(c -> c.sendMessageFormat("Log exports for " + channel.getName()).addFile(content, title))
                        .flatMap(m -> {
                            String url = m.getAttachments().get(0).getUrl();
                            return event.getHook().editOriginalEmbeds(embed.addField("export", url, false).build());
                        })
                        .flatMap(v -> event.getHook().editOriginalComponents())
                        .flatMap(v -> channel.delete())
                        .flatMap(v -> event.getHook().sendMessage("Exported & deleted channel.").setEphemeral(true))
                        .queue();
            }
        });
    }
}
