package tg.bot.msg;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tg.bot.util.TgUtil;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Tg implements Consumer<JsonObject> {

    private static final Cache<String, Integer> CACHE = CacheUtil.newFIFOCache(128);

    private static final Cache<String, Integer> CACHE2 = CacheUtil.newFIFOCache(1);


    private static final Map<String, String> MAP = Map.of(
            "/comic", "/Comic",
            "/novel", "/Novel",
            "/music", "/Music",
            "/色图", "/色图"
    );

    private static final Map<String, List<File>> MAP_FILE = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(Tg.class);

    static {
        for (Map.Entry<String, String> entry : MAP.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            ThreadUtil.execute(() -> {
                List<File> files = FileUtil.loopFiles(value)
                        .stream()
                        .filter(file -> {
                            String extName = FileUtil.extName(file);
                            if (StrUtil.isBlank(extName)) {
                                return false;
                            }

                            long fileSize = file.length();
                            double fileSizeInMB = (double) fileSize / (1024 * 1024);
                            if (fileSizeInMB >= 50) {
                                return false;
                            }

                            return List.of(
                                    "epub", "rar", "zip", "txt", "mobi",
                                    "flac", "mp3", "jpg", "png"
                            ).contains(extName);
                        })
                        .collect(Collectors.toList());
                log.info("{} 加载完成", key);
                MAP_FILE.put(key, files);
            });
        }
    }

    @Override
    public synchronized void accept(JsonObject jsonObject) {
        if (CACHE2.containsKey("sync")) {
            return;
        }
        CACHE2.put("sync", 1, 500);

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
        JsonElement text = message.get("text");
        if (Objects.isNull(text)) {
            return;
        }
        String chatId = chat.get("id").getAsString();

        if (CACHE.containsKey(messageId.getAsString())) {
            return;
        }
        CACHE.put(messageId.getAsString(), 1, TimeUnit.HOURS.toMillis(1));
        String textAsString = text.getAsString();
        if (!textAsString.startsWith("/")) {
            TgUtil.send(chatId, messageId.getAsString(), "雪豹闭嘴");
            return;
        }

        String regStr = "^(\\/\\w+) (random|search|download)(.*)$";

        if (textAsString.equals("/start") || textAsString.startsWith("/start@")) {
            String s =
                    "/来一份色图\n\n" +
                            "/comic random \n" +
                            "/comic search [漫画名] \n" +
                            "/comic download [漫画名] \n\n" +
                            "/novel random \n" +
                            "/novel search [小说名] \n" +
                            "/novel download [小说名]\n\n" +
                            "/music random \n" +
                            "/music search [音乐名] \n" +
                            "/music download [音乐名]";
            TgUtil.send(chatId, messageId.getAsString(), s);
            return;
        }

        if (textAsString.equals("/来一份色图")) {
            List<File> files = MAP_FILE.get("/色图");
            int i = RandomUtil.randomInt(0, files.size());
            ThreadUtil.execute(() -> TgUtil.sendPhoto(chatId, files.get(i)));
            return;
        }

        if (!ReUtil.contains(regStr, textAsString)) {
            TgUtil.send(chatId, messageId.getAsString(), "使用 /start 查看使用方法");
            return;
        }

        String k = ReUtil.get(regStr, textAsString, 1);
        String v = ReUtil.get(regStr, textAsString, 2);

        if (v.equals("random")) {
            List<File> files = MAP_FILE.get(k);

            Set<File> tempFiles = new HashSet<>();

            while (tempFiles.size() < 10) {
                int i = RandomUtil.randomInt(0, files.size());
                File file = files.get(i);
                tempFiles.add(file);
            }

            String s = tempFiles.stream()
                    .map(file -> {
                        String parentName = file.getParentFile().getName();
                        String name = file.getName();
                        return parentName + "/" + name;
                    })
                    .collect(Collectors.joining("\n"));
            if (StrUtil.isBlank(s)) {
                s = "搜索不到捏";
            } else {
                s = "<code>" + s + "</code>";
            }
            TgUtil.send(chatId, messageId.getAsString(), s);
            return;
        }

        String txt = ReUtil.get(regStr, textAsString, 3);

        if (StrUtil.isBlank(txt)) {
            return;
        }
        txt = txt.trim();

        if (!MAP.containsKey(k)) {
            return;
        }
        String fileName = txt;
        if (v.equals("search")) {
            String s = MAP_FILE.get(k)
                    .stream()
                    .map(file -> {
                        String parentName = file.getParentFile().getName();
                        String name = file.getName();
                        return parentName + "/" + name;
                    })
                    .filter(name -> name.contains(fileName))
                    .distinct()
                    .limit(10)
                    .collect(Collectors.joining("\n"));
            if (StrUtil.isBlank(s)) {
                s = "搜索不到捏";
            } else {
                s = "<code>" + s + "</code>";
            }
            TgUtil.send(chatId, messageId.getAsString(), s);
            return;
        }

        Optional<File> first = MAP_FILE.get(k)
                .stream()
                .filter(file -> {
                    String parentName = file.getParentFile().getName();
                    String name = file.getName();
                    return fileName.equals(parentName + "/" + name);
                })
                .findFirst();
        if (first.isEmpty()) {
            return;
        }
        TgUtil.send(chatId, messageId.getAsString(), "正在发送。。。");
        TgUtil.sendFile(chatId, fileName, first.get());
    }
}
