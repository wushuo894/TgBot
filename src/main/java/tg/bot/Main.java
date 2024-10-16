package tg.bot;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import tg.bot.action.BotAction;
import tg.bot.util.ConfigUtil;

@Slf4j
public class Main {

    public static void main(String[] args) {
        try {
            ConfigUtil.load();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        }

        BotAction botAction = new BotAction();
        HttpUtil.createServer(13891)
                .addAction("/webhook", (req, res) -> {
                    try {
                        botAction.doAction(req, res);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    res.write("200");
                    res.sendOk();
                })
                .start();
    }
}