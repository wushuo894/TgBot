package tg.bot.msg;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tg.bot.util.TgUtil;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Tg implements Consumer<JsonObject> {

    private static final Cache<String, Integer> CACHE = CacheUtil.newFIFOCache(32);

    @Override
    public synchronized void accept(JsonObject jsonObject) {
        JsonElement updateId = jsonObject.get("update_id");
        if (Objects.isNull(updateId)) {
            return;
        }
        JsonObject message = jsonObject.getAsJsonObject("message");
        if (Objects.isNull(message)) {
            return;
        }
        JsonElement messageId = message.get("message_id");
        if (Objects.isNull(messageId)) {
            return;
        }
        JsonObject chat = message.getAsJsonObject("chat");
        if (Objects.isNull(chat)) {
            return;
        }
        String chatId = chat.get("id").getAsString();

        if (CACHE.containsKey(messageId.getAsString())) {
            return;
        }
        CACHE.put(messageId.getAsString(), 1, TimeUnit.HOURS.toMillis(1));

        TgUtil.send(chatId, messageId.getAsString(), "雪豹闭嘴");
    }
}
