package jp.dolce.logic;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.dolce.constant.GameSystemConst;
import jp.dolce.entity.DevelopmentCard;
import jp.dolce.entity.GameData;
import jp.dolce.entity.GameDataForJson;
import jp.dolce.entity.GameStatus;
import jp.dolce.entity.GameStatusForJson;
import jp.dolce.entity.PlayerHasDevCard;
import jp.dolce.entity.ReceiveGameStatus;
import jp.dolce.form.GameForm;
import jp.dolce.form.GamePlayerForm;

public class GameLogic implements GameSystemConst{
	
	/**
	 * ゲームテーブルについている利用者をMapで返却するメソッド
	 * (新たにゲームテーブルにつく利用者の情報がある場合は追加してから返却)
	 * @param form
	 * @return
	 * @throws IOException 
	 */
	public GameForm initGameGloabal(GameForm form, StringRedisTemplate redisTemplate){
	
		Map<String, String> player_json_list = new HashMap<String, String>();
		GameDataForJson gameData = new GameDataForJson();
		
		// 返却するイベントを設定する
		form.setEvent(EVENT_CODE_GAME_INIT);
		int total_token = TOKEN_TOTAL;
		
		// 席についているゲームプレイヤーを取得
		player_json_list = redisTemplate.<String, String>opsForHash().entries("playUser");
		
		// プレイヤーの数によって、トークンの数を変更
		if(player_json_list.size() == 2) {
			total_token = total_token - 3;
		} else if(player_json_list.size() == 3) {
			total_token = total_token - 2;
		}
		
		// 内部でデッキを初期化し、シャッフルを行う
		GameStatus gameStatus = new GameStatus(total_token);		
		
		ObjectMapper mapper = new ObjectMapper();
        try {
        	// 初期化した内容をRedisに登録
			String json = mapper.writeValueAsString(gameStatus);
			redisTemplate.opsForValue().set("gameStatus", json);
			
			// ゲームデータをクライアント側へ渡すため取得
			gameData = mapper.readValue(redisTemplate.opsForValue().get("gameData"), GameDataForJson.class);

		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
        
		form.setGlobal(gameStatus.getGlobalStatus());
		form.setGameData(gameData);
		return form;
	}
	
	/**
	 * ゲームテーブルについている利用者をMapで返却するメソッド
	 * (新たにゲームテーブルにつく利用者の情報がある場合は追加してから返却)
	 * @param form
	 * @return
	 * @throws IOException 
	 */
	public GameForm eventGameGloabal(ReceiveGameStatus playerInfo, StringRedisTemplate redisTemplate){
	
		String player_json = "";
		ObjectMapper mapper = new ObjectMapper();
		GameForm form = new GameForm();
		GamePlayerForm gamePlayer = new GamePlayerForm();
		GameStatusForJson gameStatus = new GameStatusForJson();
		GameDataForJson gameData = new GameDataForJson();
        try {

        	gameStatus = mapper.readValue(redisTemplate.opsForValue().get("gameStatus"), GameStatusForJson.class);
        	gameData = mapper.readValue(redisTemplate.opsForValue().get("gameData"), GameDataForJson.class);
        	
			// 席についているゲームプレイヤーを取得
			player_json = redisTemplate.<String, String>opsForHash().entries("playUser").get(playerInfo.getUserid());
			gamePlayer = mapper.readValue(player_json, GamePlayerForm.class);
			
			gamePlayer.setName(playerInfo.getUserid());
			
			// イベントを設定
			form.setEvent(playerInfo.getEvent());
			switch(playerInfo.getEvent()) {
				// イベントが、発展カードがクリックされた場合
				case EVENT_CODE_DEP_CARD_DECK:
					
					GameData playerGetDevCardData = new GameData();
					PlayerHasDevCard playerHasDevCard = new PlayerHasDevCard();
					int id = 0;
					// プレイヤーが取得した発展カードのレベルと場所を判別し、フィールドに発展カードを補充する
					// 発展カードレベル1に関して
					if(playerInfo.getDevCard1().size() != 0) {
						id = setGlobalDevCard(playerInfo.getDevCard1(), gameStatus.getDevelopmentCard1Deck(), gameStatus.getGlobalStatus().getDevCard().get("level1"));
						// プレイヤーが取得したカードのIDからカードの情報を取得する
						playerGetDevCardData = gameData.getDevlopment_card_deck1_data().get(id);
						// プレイヤーが保持する発展カードのレベルを設定
						playerHasDevCard.setLevel(1);
					}
					// 発展カードレベル2に関して
					else if(playerInfo.getDevCard2().size() != 0) {
						id = setGlobalDevCard(playerInfo.getDevCard2(), gameStatus.getDevelopmentCard2Deck(), gameStatus.getGlobalStatus().getDevCard().get("level2"));
						// プレイヤーが取得したカードのIDからカードの情報を取得する
						playerGetDevCardData = gameData.getDevlopment_card_deck2_data().get(id);
						// プレイヤーが保持する発展カードのレベルを設定
						playerHasDevCard.setLevel(2);
					}
					// 発展カードレベル3に関して
					else if(playerInfo.getDevCard3().size() != 0) {
						id = setGlobalDevCard(playerInfo.getDevCard3(), gameStatus.getDevelopmentCard3Deck(), gameStatus.getGlobalStatus().getDevCard().get("level3"));
						// プレイヤーが取得したカードのIDからカードの情報を取得する
						playerGetDevCardData = gameData.getDevlopment_card_deck3_data().get(id);
						// プレイヤーが保持する発展カードのレベルを設定
						playerHasDevCard.setLevel(3);
					}
					
					// プレイヤーの発展カード情報に取得した発展カードID情報を追加
					playerHasDevCard.setId(playerGetDevCardData.getId());
					switch(playerGetDevCardData.getColor()) {
    				case "emerald":
    					gamePlayer.getDevCard().getEmerald().add(playerHasDevCard);
    	    			break;
    				case "sapphire" :
    					gamePlayer.getDevCard().getSapphire().add(playerHasDevCard);
    					break;
    				case "ruby" :
    					gamePlayer.getDevCard().getRuby().add(playerHasDevCard);
    					break;
    				case "diamond":
    					gamePlayer.getDevCard().getDiamond().add(playerHasDevCard);
    					break;
    				case "onyx":
    					gamePlayer.getDevCard().getOnyx().add(playerHasDevCard);
    					break;
    				case "goldJoker":
    					gamePlayer.getDevCard().getGoldJoker().add(playerHasDevCard);
    					break;
    				}
					
					// プレイヤーが取得した発展カードの点数を加算
					gamePlayer.setScore(gamePlayer.getScore() + playerGetDevCardData.getScore()); 
					
					// プレイヤーが支払ったトークンによって、フィールドのトークンを増やす
	    			for(int i = 0; i < playerInfo.getToken().size(); i++) {
	    				switch(playerInfo.getToken().get(i)) {
	    				case "emerald":
	    					gameStatus.getGlobalStatus().getToken().setEmerald(gameStatus.getGlobalStatus().getToken().getEmerald() + 1);
	    	    			gamePlayer.getToken().setEmerald(gamePlayer.getToken().getEmerald() - 1);
	    	    			break;
	    				case "sapphire" :
	    					gameStatus.getGlobalStatus().getToken().setSapphire(gameStatus.getGlobalStatus().getToken().getSapphire() + 1);
	    					gamePlayer.getToken().setSapphire(gamePlayer.getToken().getSapphire() - 1);
	    					break;
	    				case "ruby" :
	    					gameStatus.getGlobalStatus().getToken().setRuby(gameStatus.getGlobalStatus().getToken().getRuby() + 1);
	    					gamePlayer.getToken().setRuby(gamePlayer.getToken().getRuby() - 1);
	    					break;
	    				case "diamond":
	    					gameStatus.getGlobalStatus().getToken().setDiamond(gameStatus.getGlobalStatus().getToken().getDiamond() + 1);
	    					gamePlayer.getToken().setDiamond(gamePlayer.getToken().getDiamond() - 1);
	    					break;
	    				case "onyx":
	    					gameStatus.getGlobalStatus().getToken().setOnyx(gameStatus.getGlobalStatus().getToken().getOnyx() + 1);
	    					gamePlayer.getToken().setOnyx(gamePlayer.getToken().getOnyx() - 1);
	    					break;
	    				case "goldJoker":
	    					gameStatus.getGlobalStatus().getToken().setGoldJoker(gameStatus.getGlobalStatus().getToken().getGoldJoker() + 1);
	    					gamePlayer.getToken().setGoldJoker(gamePlayer.getToken().getGoldJoker() - 1);
	    					break;
	    				}
	    			}
					break;
				// イベントが、フィールド上のトークンをクリックされた場合
				case EVENT_CODE_TOKEN:
					// 取得されたフィールドのトークンによって、フィールドのトークンを減らす
	    			for(int i= 0; i < playerInfo.getToken().size(); i++) {
	    				switch(playerInfo.getToken().get(i)) {
	    				case "emerald":
	    					gameStatus.getGlobalStatus().getToken().setEmerald(gameStatus.getGlobalStatus().getToken().getEmerald() - 1);
	    	    			gamePlayer.getToken().setEmerald(gamePlayer.getToken().getEmerald() + 1);
	    	    			break;
	    				case "sapphire" :
	    					gameStatus.getGlobalStatus().getToken().setSapphire(gameStatus.getGlobalStatus().getToken().getSapphire() - 1);
	    					gamePlayer.getToken().setSapphire(gamePlayer.getToken().getSapphire() + 1);
	    					break;
	    				case "ruby" :
	    					gameStatus.getGlobalStatus().getToken().setRuby(gameStatus.getGlobalStatus().getToken().getRuby() - 1);
	    					gamePlayer.getToken().setRuby(gamePlayer.getToken().getRuby() + 1);
	    					break;
	    				case "diamond":
	    					gameStatus.getGlobalStatus().getToken().setDiamond(gameStatus.getGlobalStatus().getToken().getDiamond() - 1);
	    					gamePlayer.getToken().setDiamond(gamePlayer.getToken().getDiamond() + 1);
	    					break;
	    				case "onyx":
	    					gameStatus.getGlobalStatus().getToken().setOnyx(gameStatus.getGlobalStatus().getToken().getOnyx() - 1);
	    					gamePlayer.getToken().setOnyx(gamePlayer.getToken().getOnyx() + 1);
	    					break;
	    				case "goldJoker":
	    					gameStatus.getGlobalStatus().getToken().setGoldJoker(gameStatus.getGlobalStatus().getToken().getGoldJoker() - 1);
	    					gamePlayer.getToken().setGoldJoker(gamePlayer.getToken().getGoldJoker() + 1);
	    					break;
	    				}
	    			}
	    			break;
				case EVENT_CODE_NOBLE:
					// 取得されたフィールドのトークンによって、フィールドのトークンを減らす
	    			for(int i= 0; i < playerInfo.getNobleTiles().size(); i++) {
	    				// フィールド上のどの貴族カードが取得されたのか判別し、NOTHINGを設定
	    				if(gameStatus.getGlobalStatus().getNobleTiles().getA() == playerInfo.getNobleTiles().get(i)){
	    					gameStatus.getGlobalStatus().getNobleTiles().setA(NOTHING_NOBLE_CARD_ID);
	    				} else if(gameStatus.getGlobalStatus().getNobleTiles().getB() == playerInfo.getNobleTiles().get(i)){
	    					gameStatus.getGlobalStatus().getNobleTiles().setB(NOTHING_NOBLE_CARD_ID);
	    				} else if(gameStatus.getGlobalStatus().getNobleTiles().getC() == playerInfo.getNobleTiles().get(i)){
	    					gameStatus.getGlobalStatus().getNobleTiles().setC(NOTHING_NOBLE_CARD_ID);
	    				} else if(gameStatus.getGlobalStatus().getNobleTiles().getD() == playerInfo.getNobleTiles().get(i)){
	    					gameStatus.getGlobalStatus().getNobleTiles().setD(NOTHING_NOBLE_CARD_ID);
	    				} else if(gameStatus.getGlobalStatus().getNobleTiles().getE() == playerInfo.getNobleTiles().get(i)){
	    					gameStatus.getGlobalStatus().getNobleTiles().setE(NOTHING_NOBLE_CARD_ID);
	    				}
    	    			gamePlayer.getNobleTiles().add(playerInfo.getNobleTiles().get(i));
    	    			
    	    			// プレイヤーが取得した貴族カードの点数を加算
    					gamePlayer.setScore(gamePlayer.getScore() + gameData.getNoble_card_data().get(playerInfo.getNobleTiles().get(i)).getScore()); 
	    			}
	    			break;
				case EVENT_CODE_KEEP:
					
					playerGetDevCardData = new GameData();
					playerHasDevCard = new PlayerHasDevCard();
					id = 0;
					// プレイヤーが取得した発展カードのレベルと場所を判別し、フィールドに発展カードを補充する
					// 発展カードレベル1に関して
					if(playerInfo.getDevCard1().size() != 0) {
						id = setGlobalDevCard(playerInfo.getDevCard1(), gameStatus.getDevelopmentCard1Deck(), gameStatus.getGlobalStatus().getDevCard().get("level1"));
						// プレイヤーが取得したカードのIDからカードの情報を取得する
						playerGetDevCardData = gameData.getDevlopment_card_deck1_data().get(id);
						// プレイヤーが保持する発展カードのレベルを設定
						playerHasDevCard.setLevel(1);
					}
					// 発展カードレベル2に関して
					else if(playerInfo.getDevCard2().size() != 0) {
						id = setGlobalDevCard(playerInfo.getDevCard2(), gameStatus.getDevelopmentCard2Deck(), gameStatus.getGlobalStatus().getDevCard().get("level2"));
						// プレイヤーが取得したカードのIDからカードの情報を取得する
						playerGetDevCardData = gameData.getDevlopment_card_deck2_data().get(id);
						// プレイヤーが保持する発展カードのレベルを設定
						playerHasDevCard.setLevel(2);
					}
					// 発展カードレベル3に関して
					else if(playerInfo.getDevCard3().size() != 0) {
						id = setGlobalDevCard(playerInfo.getDevCard3(), gameStatus.getDevelopmentCard3Deck(), gameStatus.getGlobalStatus().getDevCard().get("level3"));
						// プレイヤーが取得したカードのIDからカードの情報を取得する
						playerGetDevCardData = gameData.getDevlopment_card_deck3_data().get(id);
						// プレイヤーが保持する発展カードのレベルを設定
						playerHasDevCard.setLevel(3);
					}
					// 取得したIDを設定
					playerHasDevCard.setId(id);
					// プレイヤーの発展カード情報に取得した発展カードID情報を追加
					gamePlayer.getDevCard().getGoldJoker().add(playerHasDevCard);
					
					// 場のトークンから-1
					gameStatus.getGlobalStatus().getToken().setGoldJoker(gameStatus.getGlobalStatus().getToken().getGoldJoker() - 1);
					
					// プレイヤーのトークンに+1
					gamePlayer.getToken().setGoldJoker(gamePlayer.getToken().getGoldJoker() + 1);
					break;
				case EVENT_CODE_EXCHANGE_KEEP_DEV_CARD:
					
					playerGetDevCardData = new GameData();
					playerHasDevCard = new PlayerHasDevCard();
					id=0;
					
					// 交換対象の発展カードのidを取得
					// 発展カードレベル1に関して
					if(playerInfo.getDevCard1().size() != 0) {
						id = setGlobalDevCard(playerInfo.getDevCard1(), gameStatus.getDevelopmentCard1Deck(), gameStatus.getGlobalStatus().getDevCard().get("level1"));
						// プレイヤーが取得したカードのIDからカードの情報を取得する
						playerGetDevCardData = gameData.getDevlopment_card_deck1_data().get(id);
						// プレイヤーが保持する発展カードのレベルを設定
						playerHasDevCard.setLevel(1);
					}
					// 発展カードレベル2に関して
					else if(playerInfo.getDevCard2().size() != 0) {
						id = setGlobalDevCard(playerInfo.getDevCard2(), gameStatus.getDevelopmentCard2Deck(), gameStatus.getGlobalStatus().getDevCard().get("level2"));
						// プレイヤーが取得したカードのIDからカードの情報を取得する
						playerGetDevCardData = gameData.getDevlopment_card_deck2_data().get(id);
						// プレイヤーが保持する発展カードのレベルを設定
						playerHasDevCard.setLevel(2);
					}
					// 発展カードレベル3に関して
					else if(playerInfo.getDevCard3().size() != 0) {
						id = setGlobalDevCard(playerInfo.getDevCard3(), gameStatus.getDevelopmentCard3Deck(), gameStatus.getGlobalStatus().getDevCard().get("level3"));
						// プレイヤーが取得したカードのIDからカードの情報を取得する
						playerGetDevCardData = gameData.getDevlopment_card_deck3_data().get(id);
						// プレイヤーが保持する発展カードのレベルを設定
						playerHasDevCard.setLevel(3);
					}
					
					// プレイヤーの発展カード情報に取得した発展カードID情報を追加
					playerHasDevCard.setId(playerGetDevCardData.getId());
					// キープされていた発展カードをプレイヤーの発展カードに追加
					switch(playerGetDevCardData.getColor()) {
					case "emerald":
    					gamePlayer.getDevCard().getEmerald().add(playerHasDevCard);
    	    			break;
    				case "sapphire" :
    					gamePlayer.getDevCard().getSapphire().add(playerHasDevCard);
    					break;
    				case "ruby" :
    					gamePlayer.getDevCard().getRuby().add(playerHasDevCard);
    					break;
    				case "diamond":
    					gamePlayer.getDevCard().getDiamond().add(playerHasDevCard);
    					break;
    				case "onyx":
    					gamePlayer.getDevCard().getOnyx().add(playerHasDevCard);
    					break;
    				case "goldJoker":
    					gamePlayer.getDevCard().getGoldJoker().add(playerHasDevCard);
    					break;
    				}
					
					// プレイヤーが取得した発展カードの点数を加算
					gamePlayer.setScore(gamePlayer.getScore() + playerGetDevCardData.getScore()); 
					
					// プレイヤーが支払ったトークンによって、フィールドのトークンを増やす
	    			for(int i = 0; i < playerInfo.getToken().size(); i++) {
	    				switch(playerInfo.getToken().get(i)) {
	    				case "emerald":
	    					gameStatus.getGlobalStatus().getToken().setEmerald(gameStatus.getGlobalStatus().getToken().getEmerald() + 1);
	    	    			gamePlayer.getToken().setEmerald(gamePlayer.getToken().getEmerald() - 1);
	    	    			break;
	    				case "sapphire" :
	    					gameStatus.getGlobalStatus().getToken().setSapphire(gameStatus.getGlobalStatus().getToken().getSapphire() + 1);
	    					gamePlayer.getToken().setSapphire(gamePlayer.getToken().getSapphire() - 1);
	    					break;
	    				case "ruby" :
	    					gameStatus.getGlobalStatus().getToken().setRuby(gameStatus.getGlobalStatus().getToken().getRuby() + 1);
	    					gamePlayer.getToken().setRuby(gamePlayer.getToken().getRuby() - 1);
	    					break;
	    				case "diamond":
	    					gameStatus.getGlobalStatus().getToken().setDiamond(gameStatus.getGlobalStatus().getToken().getDiamond() + 1);
	    					gamePlayer.getToken().setDiamond(gamePlayer.getToken().getDiamond() - 1);
	    					break;
	    				case "onyx":
	    					gameStatus.getGlobalStatus().getToken().setOnyx(gameStatus.getGlobalStatus().getToken().getOnyx() + 1);
	    					gamePlayer.getToken().setOnyx(gamePlayer.getToken().getOnyx() - 1);
	    					break;
	    				case "goldJoker":
	    					gameStatus.getGlobalStatus().getToken().setGoldJoker(gameStatus.getGlobalStatus().getToken().getGoldJoker() + 1);
	    					gamePlayer.getToken().setGoldJoker(gamePlayer.getToken().getGoldJoker() - 1);
	    					break;
	    				}
	    			}
					
					// キープカードから対象のカードを探し削除
					List<PlayerHasDevCard> devCards = gamePlayer.getDevCard().getGoldJoker();
					for(int i=0; i<devCards.size(); i++) {
						if(devCards.get(i).getId() == id) {
							devCards.remove(i);
						}
					}
					break;
	    		default:
	    			break;
			}
			
        	// 更新した内容をRedisに登録
			String gameStatusJson = mapper.writeValueAsString(gameStatus);
			redisTemplate.opsForValue().set("gameStatus", gameStatusJson);
			
			String gamePlayerJson = mapper.writeValueAsString(gamePlayer);
			// ログアウト利用者じゃない場合は、Redisにも登録
    		redisTemplate.opsForHash().put("playUser", playerInfo.getUserid() ,gamePlayerJson);
			
			form.setGlobal(gameStatus.getGlobalStatus());
			form.setPlayer(gamePlayer);
			
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return form;
	}
	
	public int setGlobalDevCard(Map<String, Integer> devCard, LinkedList<Integer> developmentCardDeck, DevelopmentCard gameStatusDevCard) {

		int returnId = 0;
		for(Map.Entry<String, Integer> entry : devCard.entrySet()) {
			switch(entry.getKey()) {
			case "A" : 
				if(developmentCardDeck.size() != 0) {
					gameStatusDevCard.setA(developmentCardDeck.pop());
				} else {
					gameStatusDevCard.setA(NOTHING_DEVELOPMENT_CARD_ID);
				}
				break;
			case "B" : 
				if(developmentCardDeck.size() != 0) {
					gameStatusDevCard.setB(developmentCardDeck.pop());
				} else {
					gameStatusDevCard.setB(NOTHING_DEVELOPMENT_CARD_ID);
				}
				break;
			case "C" : 
				if(developmentCardDeck.size() != 0) {
					gameStatusDevCard.setC(developmentCardDeck.pop());
				} else {
					gameStatusDevCard.setC(NOTHING_DEVELOPMENT_CARD_ID);
				}
				break;
			case "D" : 
				if(developmentCardDeck.size() != 0) {
					gameStatusDevCard.setD(developmentCardDeck.pop());
				} else {
					gameStatusDevCard.setD(NOTHING_DEVELOPMENT_CARD_ID);
				}					
				break;
			default:
				break;
			}

			returnId = entry.getValue();
		}
		return returnId;
	}
}
