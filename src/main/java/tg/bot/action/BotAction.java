package tg.bot.action;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import cn.hutool.http.server.action.Action;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
public class BotAction implements Action {

    static final List<Consumer<JsonObject>> consumers = new ArrayList<>();

    static {
        Set<Class<?>> classes = ClassUtil.scanPackage("tg.bot.msg");
        for (Class<?> aClass : classes) {
            consumers.add((Consumer<JsonObject>) ReflectUtil.newInstance(aClass));
        }
    }

    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    @Override
    public void doAction(HttpServerRequest req, HttpServerResponse res) {
        String body = req.getBody();
        if (!JSONUtil.isTypeJSON(body)) {
            return;
        }
        res.sendOk();
        log.info("body: {}", body);
        JsonObject jsonObject = GSON.fromJson(body, JsonObject.class);
        for (Consumer<JsonObject> consumer : consumers) {
            try {
                consumer.accept(jsonObject);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
