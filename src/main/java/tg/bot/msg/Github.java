package tg.bot.msg;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrFormatter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import tg.bot.util.TgUtil;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public class Github implements Consumer<JsonObject> {

    private static final Cache<String, Integer> COUNT = CacheUtil.newFIFOCache(2);

    @Override
    public synchronized void accept(JsonObject jsonObject) {
        JsonElement action = jsonObject.get("action");
        if (Objects.isNull(action)) {
            return;
        }
        if (!"released".equals(action.getAsString())) {
            return;
        }
        JsonObject release = jsonObject.getAsJsonObject("release");
        if (Objects.isNull(release)) {
            return;
        }

        String key = DateUtil.format(DateUtil.date(), DatePattern.NORM_DATE_PATTERN);
        Integer i = COUNT.get(key);
        if (Objects.isNull(i)) {
            i = 0;
        }
        i++;
        COUNT.put(key, i, TimeUnit.DAYS.toMillis(1));

        String version = release.get("tag_name").getAsString();
        String htmlUrl = release.get("html_url").getAsString();
        String body = release.get("body").getAsString();
        log.info("release: {} {}", version, body);
        String text = StrFormatter.format("大家好，我是你们的小珍珠\n" +
                "ANI-RSS 又又又又又又又又又又更新了\n" +
                "这次的版本号是: {}\n" +
                "更新内容: {}\n" +
                "链接: {}\n" +
                "今天已经更新了{}个版本了!!!!!!", version, body, htmlUrl, i);
        TgUtil.send(text);
    }
}
