package tg.bot;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import tg.bot.action.BotAction;
import tg.bot.util.ConfigUtil;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class Main {

    public static void main(String[] args) {
        try {
            ConfigUtil.load();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        }

        int port = 13891;
        List<String> list = Arrays.asList(ObjectUtil.defaultIfNull(args, new String[]{}));
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            if (s.equals("--port")) {
                port = Integer.parseInt(list.get(i + 1));
            }
        }

        log.info("port: {}", port);

        BotAction botAction = new BotAction();
        HttpUtil.createServer(port)
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