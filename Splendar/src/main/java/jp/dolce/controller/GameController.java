package jp.dolce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.dolce.entity.ReceiveGameStatus;
import jp.dolce.form.GameForm;
import jp.dolce.logic.GameLogic;

@Controller
public class GameController {
	
	@Autowired
	private StringRedisTemplate redisTemplate; // Redisを使用するためのimport
	
	/**
	 * 利用者がゲームテーブルについた際に受け取るコントローラ
	 * @param player
	 * @return
	 * @throws Exception
	 */
	@MessageMapping("/game_start")
	@SendTo("/topic/setGameField")
	public GameForm geameStart(@Payload GameForm form) throws Exception {
		
		GameLogic gameLogic = new GameLogic();
		form = gameLogic.initGameGloabal(form, redisTemplate);
		return form;
    }
	
	/**
	 * ゲーム中のイベントを受け取るコントローラ
	 * @param player
	 * @return
	 * @throws Exception
	 */
	@MessageMapping("/game_event")
	@SendTo("/topic/setGameField")
	public GameForm geameEvent(@Payload GameForm form) throws Exception {
		
		GameLogic gameLogic = new GameLogic();
		ObjectMapper objectMapper = new ObjectMapper();
		ReceiveGameStatus playerInfo = new ReceiveGameStatus();
		try {			
        	playerInfo = objectMapper.readValue(form.getReceiveJsonStr(), ReceiveGameStatus.class);
        	form = gameLogic.eventGameGloabal(playerInfo, redisTemplate);
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return form;
    }
}