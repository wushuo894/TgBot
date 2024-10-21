package tg.bot.msg;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

    static {
        for (String value : MAP.values()) {
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
                MAP_FILE.put(value, files);
            });
        }
    }

    @Override
    public synchronized void accept(JsonObject jsonObject) {
        if (CACHE2.containsKey("sync")) {
            return;
        }
        CACHE2.put("sync", 1, 3000);

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
        List<String> split = StrUtil.split(textAsString, " ", true, false);

        if (split.get(0).equals("/start")) {
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

        if (split.get(0).equals("/来一份色图")) {
            List<File> files = MAP_FILE.get("/色图");
            int i = RandomUtil.randomInt(0, files.size());
            TgUtil.sendFile(chatId, files.get(i));
            return;
        }

        if (split.size() < 2) {
            TgUtil.send(chatId, messageId.getAsString(), "使用 /start 查看使用方法");
            return;
        }

        if (split.get(1).equals("random")) {
            String s = MAP_FILE.get(MAP.get(split.get(0)))
                    .stream()
                    .sorted(Comparator.comparingLong(o -> RandomUtil.randomLong()))
                    .map(file -> {
                        String parentName = file.getParentFile().getName();
                        String name = file.getName();
                        return URLUtil.encodeBlank(parentName + "/" + name);
                    })
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

        if (split.size() != 3) {
            return;
        }
        if (!MAP.containsKey(split.get(0))) {
            return;
        }
        if (!List.of("search", "download").contains(split.get(1))) {
            return;
        }
        String fileName = URLUtil.decode(split.get(2));
        if (split.get(1).equals("search")) {
            String s = MAP_FILE.get(MAP.get(split.get(0)))
                    .stream()
                    .map(file -> {
                        String parentName = file.getParentFile().getName();
                        String name = file.getName();
                        return URLUtil.encodeBlank(parentName + "/" + name);
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

        Optional<File> first = MAP_FILE.get(MAP.get(split.get(0)))
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
