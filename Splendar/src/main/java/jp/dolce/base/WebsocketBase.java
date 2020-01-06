package jp.dolce.base;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class WebsocketBase implements WebSocketMessageBrokerConfigurer  {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// queueまたはtopicを有効にする(両方可)。queueは1対1(P2P)、topicは1対多(Pub-Sub)
		registry.enableSimpleBroker("/topic", "/queue");
		// Controllerに処理させる宛先のPrefix
		registry.setApplicationDestinationPrefixes("/app");
	}
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// WebSocketのエンドポイント
		registry.addEndpoint("/spring-websocket-app").withSockJS();
	}
}
