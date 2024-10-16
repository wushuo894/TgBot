package tg.bot.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tg.bot.entity.Config;

import java.io.File;

public class ConfigUtil {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public static final Config CONFIG = new Config();

    static {
        CONFIG.setBotToken("")
                .setChatId("");
    }

    public static void load() {
        File configDir = new File("config");
        FileUtil.mkdir(configDir);
        File configFile = new File(configDir + File.separator + "config.json");
        if (!configFile.exists()) {
            FileUtil.writeUtf8String(GSON.toJson(CONFIG), configFile);
        }
        String s = FileUtil.readUtf8String(configFile);
        BeanUtil.copyProperties(GSON.fromJson(s, Config.class), CONFIG, CopyOptions
                .create()
                .setIgnoreNullValue(true));
        String botToken = CONFIG.getBotToken();
        String chatId = CONFIG.getChatId();

        Assert.notBlank(botToken, "botToken 为空");
        Assert.notBlank(chatId, "chatId 为空");
    }

}