package tg.bot.util;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import tg.bot.entity.Config;

import java.io.File;
import java.util.Map;

@Slf4j
public class TgUtil {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();


    public static void send(String text) {
        String chatId = ConfigUtil.CONFIG.getChatId();
        send(chatId, text);
    }

    public static void send(String chatId, String text) {
        send(chatId, "", text);
    }

    public static synchronized void send(String chatId, String replyToMessageId, String text) {
        log.info(text);
        Config config = ConfigUtil.CONFIG;
        String botToken = config.getBotToken();

        String url = StrFormatter.format("https://api.telegram.org/bot{}/sendMessage", botToken);
        HttpRequest.post(url)
                .body(GSON.toJson(Map.of(
                        "chat_id", chatId,
                        "text", text,
                        "parse_mode", "HTML",
                        "reply_to_message_id", replyToMessageId,
                        "has_spoiler", true
                )))
                .thenFunction(HttpResponse::isOk);
    }

    public static synchronized void sendPhoto(String chatId, File photo) {
        Config config = ConfigUtil.CONFIG;
        String botToken = config.getBotToken();
        String url = StrFormatter.format("https://api.telegram.org/bot{}/sendPhoto", botToken);
        HttpRequest.post(url)
                .contentType(ContentType.MULTIPART.getValue())
                .form("chat_id", chatId)
                .form("caption", "")
                .form("photo", photo)
                .thenFunction(HttpResponse::isOk);
    }

    public static synchronized void sendFile(String chatId, File file) {
        sendFile(chatId, "", file);
    }

    public static synchronized void sendFile(String chatId, String text, File file) {
        Config config = ConfigUtil.CONFIG;
        String botToken = config.getBotToken();
        String url = StrFormatter.format("https://api.telegram.org/bot{}/sendDocument", botToken);
        HttpRequest
                .post(url)
                .form("chat_id", chatId)
                .form("parse_mode", "HTML")
                .form("caption", text)
                .form("document", file)
                .then(HttpResponse::isOk);
    }

}
