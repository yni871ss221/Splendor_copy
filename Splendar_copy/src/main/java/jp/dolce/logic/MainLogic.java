package jp.dolce.logic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.dolce.constant.GameSystemConst;
import jp.dolce.constant.MessageType;
import jp.dolce.entity.GameData;
import jp.dolce.entity.GameDataForJson;
import jp.dolce.form.GamePlayerForm;
import jp.dolce.form.SendMessageForm;

public class MainLogic implements GameSystemConst{
	
	/**
	 * 発展カード、貴族カードのデータをJsonファイルから読み込みredisへ登録
	 * @param resourceLoader
	 * @param redisTemplate
	 */
	public void gameDataInit(ResourceLoader resourceLoader, StringRedisTemplate redisTemplate) {
		
		ObjectMapper objectMapper = new ObjectMapper();
		GameDataForJson gameDataMap = new GameDataForJson();
		GameData gameData = new GameData();
		
		Resource development_resource = resourceLoader.getResource("classpath:data/develop_card_data.json");
		Resource noble_resource = resourceLoader.getResource("classpath:data/noble_card_data.json");
		try {
	        JsonNode development_datas = objectMapper.readTree(development_resource.getFile());
	        for (JsonNode card1 : development_datas.get("development_card1")) {
	        	gameData = new GameData();
	        	gameData.setId(card1.get("id").asInt());
	        	gameData.setColor(card1.get("color").asText());
	        	gameData.setScore(card1.get("score").asInt());	        	
	        	gameData.getCost().setEmerald(card1.get("cost").get("emerald").asInt());
        		gameData.getCost().setSapphire(card1.get("cost").get("sapphire").asInt());
        		gameData.getCost().setRuby(card1.get("cost").get("ruby").asInt());
        		gameData.getCost().setDiamond(card1.get("cost").get("diamond").asInt());
        		gameData.getCost().setOnyx(card1.get("cost").get("onyx").asInt());
	        	gameDataMap.getDevlopment_card_deck1_data().put(card1.get("id").asInt(), gameData);
	        }
	        
	        for (JsonNode card2 : development_datas.get("development_card2")) {
	        	gameData = new GameData();
	        	gameData.setId(card2.get("id").asInt());
	        	gameData.setColor(card2.get("color").asText());
	        	gameData.setScore(card2.get("score").asInt());	        	
	        	gameData.getCost().setEmerald(card2.get("cost").get("emerald").asInt());
        		gameData.getCost().setSapphire(card2.get("cost").get("sapphire").asInt());
        		gameData.getCost().setRuby(card2.get("cost").get("ruby").asInt());
        		gameData.getCost().setDiamond(card2.get("cost").get("diamond").asInt());
        		gameData.getCost().setOnyx(card2.get("cost").get("onyx").asInt());
	        	gameDataMap.getDevlopment_card_deck2_data().put(card2.get("id").asInt(), gameData);
	        }
	        
	        for (JsonNode card3 : development_datas.get("development_card3")) {
	        	gameData = new GameData();
	        	gameData.setId(card3.get("id").asInt());
	        	gameData.setColor(card3.get("color").asText());
	        	gameData.setScore(card3.get("score").asInt());	        	
	        	gameData.getCost().setEmerald(card3.get("cost").get("emerald").asInt());
        		gameData.getCost().setSapphire(card3.get("cost").get("sapphire").asInt());
        		gameData.getCost().setRuby(card3.get("cost").get("ruby").asInt());
        		gameData.getCost().setDiamond(card3.get("cost").get("diamond").asInt());
        		gameData.getCost().setOnyx(card3.get("cost").get("onyx").asInt());
	        	gameDataMap.getDevlopment_card_deck3_data().put(card3.get("id").asInt(), gameData);
	        }
	        
	        JsonNode noble_datas = objectMapper.readTree(noble_resource.getFile());
	        for (JsonNode noble : noble_datas.get("noble_card")) {
	        	gameData = new GameData();
	        	gameData.setId(noble.get("id").asInt());
	        	gameData.setScore(noble.get("score").asInt());	        	
	        	gameData.getCost().setEmerald(noble.get("cost").get("emerald").asInt());
        		gameData.getCost().setSapphire(noble.get("cost").get("sapphire").asInt());
        		gameData.getCost().setRuby(noble.get("cost").get("ruby").asInt());
        		gameData.getCost().setDiamond(noble.get("cost").get("diamond").asInt());
        		gameData.getCost().setOnyx(noble.get("cost").get("onyx").asInt());
	        	gameDataMap.getNoble_card_data().put(noble.get("id").asInt(), gameData);
	        }
	        
	        // データをJSONへ
        	String gameDataJson = objectMapper.writeValueAsString(gameDataMap);
        	redisTemplate.opsForValue().set("gameData", gameDataJson);
        	
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	
	/**
	 * ログインしている利用者をMapで返却するメソッド
	 * (新たにログインする利用者の情報がある場合は追加してから返却)
	 * @param form
	 * @return
	 */
	public Map<String, String> addUserLogic(SendMessageForm form, StringRedisTemplate redisTemplate) {
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, String> user_json_list = new HashMap<String, String>();
		
		try {
        	// Redisからログインしている全て利用者を取得(リストはJsonで管理)
        	user_json_list = redisTemplate.<String, String>opsForHash().entries("loginUser");
        	
        	// ログインユーザー情報がある場合は情報を追加
        	if(form != null) {

        		// 今回ログインした利用者情報をJSONへ
	        	String user_json = objectMapper.writeValueAsString(form.getLoginUserMap());
	        	// ログアウト利用者じゃない場合は、Redisにも登録
        		if(form.getType() != MessageType.LEAVE) {
		        	redisTemplate.opsForHash().put("loginUser", form.getSender() ,user_json);
        		}
        		
	        	// これまでのリストに追加(view側に返却用)
	        	user_json_list.put(form.getSender() ,user_json);
        	}
        	
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return user_json_list;
	}
	
	/**
	 * ゲームテーブルについている利用者をMapで返却するメソッド
	 * (新たにゲームテーブルにつく利用者の情報がある場合は追加してから返却)
	 * @param form
	 * @return
	 */
	public Map<String, String> playerReadyLogic(GamePlayerForm form, StringRedisTemplate redisTemplate) {
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, String> player_json_list = new HashMap<String, String>();
		int player_count = 0;
		try {
			
			if(form.getMode().equals("GAME_LEAVE")) {
				redisTemplate.opsForHash().delete("playUser", form.getName());
			}
			
        	// Redisから席についている全ての利用者を取得(リストはJsonで管理)
			player_json_list = redisTemplate.<String, String>opsForHash().entries("playUser");
			
			if(form.getMode().equals("GAME_PLAY")) {
				// 席についているプレイヤー数を取得
				player_count = player_json_list.size();
				// 席順によってプレイヤーID(カラー)を設定
				form.setUserid(USER_ID_COLOR.get(player_count));
				
	        	// ログインユーザー情報がある場合は情報を追加
	        	if(form != null) {
	
	        		// 今回ログインした利用者情報をJSONへ
		        	String user_json = objectMapper.writeValueAsString(form);
		        	// ログアウト利用者じゃない場合は、Redisにも登録
	        		redisTemplate.opsForHash().put("playUser", form.getName() ,user_json);
	        		
		        	// これまでのリストに追加(view側に返却用)
	        		player_json_list.put(form.getName() ,user_json);
	        	}
			}
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return player_json_list;
	}
}
