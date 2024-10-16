package tg.bot.util;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import tg.bot.entity.Config;

import java.util.Map;

@Slf4j
public class TgUtil {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public static synchronized void send(String text) {
        log.info(text);
        Config config = ConfigUtil.CONFIG;
        String botToken = config.getBotToken();
        String chatId = config.getChatId();

        String url = StrFormatter.format("https://api.telegram.org/bot{}/sendMessage", botToken);
        HttpRequest.post(url)
                .body(GSON.toJson(Map.of(
                        "chat_id", chatId,
                        "text", text
                )))
                .thenFunction(HttpResponse::isOk);
    }
}
