package tg.bot.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class LogUtil {

    public static void loadLogback() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            String s = ResourceUtil.readUtf8Str("logback-template.xml");
            s = s.replace("${config}", ConfigUtil.getConfigDir() + "/");
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();

            byteArrayInputStream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
            configurator.doConfigure(byteArrayInputStream);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            IoUtil.close(byteArrayInputStream);
        }
    }
}
