package jp.dolce.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.dolce.form.ReciveMessageForm;
import jp.dolce.form.SendMessageForm;
import jp.dolce.entity.GameData;
import jp.dolce.entity.GameDataForJson;
import jp.dolce.entity.ReceiveGameStatus;
import jp.dolce.entity.Token;
import jp.dolce.form.GamePlayerForm;
import jp.dolce.logic.MainLogic;

@Controller
public class MainController {

	 @Autowired
	 private StringRedisTemplate redisTemplate; // Redisを使用するためのimport
	 @Autowired
     protected ResourceLoader resourceLoader;
	 
	@RequestMapping(value = "/index", method = RequestMethod.GET, headers="Accept=application/*")
	public ModelAndView index() {
		
		// 初期化
		MainLogic mainLogic = new MainLogic();
		
		// ゲームデータを読み込みredis上に登録
		mainLogic.gameDataInit(resourceLoader, redisTemplate);
		
		// 生成
		ModelAndView mv = new ModelAndView();
		
		// テンプレートを指定
		mv.setViewName("index");
		
		// 返却
		return mv;
	}
	
	/**
	 * メッセージを送る押下時に受け取るコントローラ
	 * @param topic
	 * @param message
	 * @return
	 * @throws Exception
	 */
    @MessageMapping("/send_chat/{topic}")
	@SendTo("/topic/messages")
    public List<SendMessageForm> greeting(@DestinationVariable("topic") String topic, @Payload ReciveMessageForm message) throws Exception {
        // メッセージ内容を"/topic/greetings"のSubscriberに渡す.
    	List<SendMessageForm> sendMessageForms = new ArrayList<SendMessageForm>();
    	sendMessageForms.add(new SendMessageForm(message.getType(), message.getContent(), message.getSender(), topic));
        return sendMessageForms;
    }
	
    /**
     * チャット開始押下時に受け取るコントローラ
     * @param message
     * @param headerAccessor
     * @return
     */
	@MessageMapping("/adduser")
	@SendTo("/topic/messages")
	public List<Map<String,String>> addUser(@Payload ReciveMessageForm message, SimpMessageHeaderAccessor headerAccessor) {
		headerAccessor.getSessionAttributes().put("username", message.getSender());
		
		SendMessageForm out = new SendMessageForm(message.getType(), message.getContent(), message.getSender(), null);
		
		MainLogic mainLogic = new MainLogic();
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String,String> map = new LinkedHashMap<>();
		List<Map<String,String>> returnMap = new ArrayList<Map<String,String>>();
		Map<String, String> user_json_list = mainLogic.addUserLogic(out, redisTemplate);
		
        try {
        	for(String json_str : user_json_list.keySet()){
            	map = objectMapper.readValue(user_json_list.get(json_str), new TypeReference<LinkedHashMap<String,String>>(){});
            	returnMap.add(map);
            }
        	
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return returnMap;
	}
	
	/**
	 * 利用者がゲームテーブルについた際に受け取るコントローラ
	 * @param player
	 * @return
	 * @throws Exception
	 */
	@MessageMapping("/play_control")
	@SendTo("/topic/gamePlayerInfo")
	public GamePlayerForm sitDown(@Payload GamePlayerForm player) throws Exception {

		MainLogic mainLogic = new MainLogic();
		mainLogic.playerReadyLogic(player, redisTemplate);
		
		return player;
    }
}