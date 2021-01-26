package net.uku3lig.ukubot.spring;

import io.mokulu.discord.oauth.DiscordAPI;
import io.mokulu.discord.oauth.DiscordOAuth;
import io.mokulu.discord.oauth.model.TokensResponse;
import io.mokulu.discord.oauth.model.User;
import net.uku3lig.ukubot.core.Config;
import net.uku3lig.ukubot.utils.DockerSecrets;
import net.uku3lig.ukubot.core.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
public class OAuth2Controller {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2Controller.class);
    private static DiscordOAuth oAuth = null;
    private static final String clientID = "775431908666245121",
            redirectUri = Main.isDocker() ? "vps ip" : "http://localhost:8080/";

    @GetMapping
    public ModelAndView login(@RequestParam(name = "code") String code, @RequestParam(name = "guild_id") String guildId) throws IOException {
        if (oAuth == null) oAuth = getOAuth();
        TokensResponse tokens = oAuth.getTokens(code);
        DiscordAPI api = new DiscordAPI(tokens.getAccessToken());
        User user = api.fetchUser();
        Config.newDefaultConfig(Main.getJda().getGuildById(guildId), user);
        return new ModelAndView("redirect:https://discord.com/oauth2/authorized");
    }

    private DiscordOAuth getOAuth() {
        String clientSecret = DockerSecrets.getSecretOrFile("client_secret", Path.of("./CLIENT_SECRET"));
        return new DiscordOAuth(clientID, clientSecret, redirectUri, new String[] {"identify"});
    }
}
