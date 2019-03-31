package game;

import bean.HelloCounter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

public class MyHandler extends TextWebSocketHandler {
    private static final Logger log = Logger.getLogger(MyHandler.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            HelloCounter helloCounter = objectMapper.readValue(message.asBytes(), HelloCounter.class);
            log.debug("decode success: " + helloCounter.getMsg() + " " + helloCounter.getCount());
            helloCounter.setCount(helloCounter.getCount() + 1);
            String payload = objectMapper.writeValueAsString(helloCounter);
            TextMessage returnMessage = new TextMessage(payload);
            session.sendMessage(returnMessage);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.debug("unable to decode: " + message.getPayload());

    }
}
