package tg.bot.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Config {
    private String botToken;
    private String chatId;
}
