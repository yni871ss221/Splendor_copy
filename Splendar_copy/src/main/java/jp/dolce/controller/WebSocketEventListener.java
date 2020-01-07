
package jp.dolce.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.dolce.form.ReciveMessageForm;
import jp.dolce.form.SendMessageForm;
import jp.dolce.logic.MainLogic;
import jp.dolce.constant.MessageType;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    @Autowired
	private StringRedisTemplate redisTemplate; // Redisを使用するためのimport
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
    	logger.info("Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    	
    	StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());    	
    	// 利用者を取得
    	String username = (String) headerAccessor.getSessionAttributes().get("username");
    	
    	if(username != null) {
    		
    	    MainLogic mainLogic = new MainLogic();
    	    
    		logger.info("User Disconnected : " + username);
    		
    		// ログアウト用のメッセージを返却するためLEAVEを設定
    		SendMessageForm leave_user = new SendMessageForm(MessageType.LEAVE, null, username, null);
    		
    		// Redisからログアウトした利用者の情報を取得
    		redisTemplate.opsForHash().delete("loginUser", username);
    		redisTemplate.opsForHash().delete("playUser", username);
    		
    		/**
    		ObjectMapper objectMapper = new ObjectMapper();
    		Map<String,String> map = new LinkedHashMap<>();
    		List<Map<String,String>> returnMap = new ArrayList<Map<String,String>>();
    		Map<String, String> user_json_list = mainLogic.addUserLogic(leave_user, redisTemplate);
    		
            try {
            	for(String json_str : user_json_list.keySet()){
                	map = objectMapper.readValue(user_json_list.get(json_str), new TypeReference<LinkedHashMap<String,String>>(){});
                	returnMap.add(map);
                }
            	
    		} catch (Exception e) {
    			// TODO 自動生成された catch ブロック
    			e.printStackTrace();
    		}
            **/
    		
    		messagingTemplate.convertAndSend("/topic/leaveUserInfo", leave_user);
    	}
    }
}
