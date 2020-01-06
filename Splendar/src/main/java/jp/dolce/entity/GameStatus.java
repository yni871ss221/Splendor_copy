package jp.dolce.entity;

import java.util.Collections;
import java.util.LinkedList;

import jp.dolce.constant.GameSystemConst;
import lombok.Data;

@Data
public class GameStatus implements GameSystemConst{

	public GameStatus(int total_token) {
		// 各デッキの初期化
		for(int i=1; i <= DEVELOPMENT_CARD_LEVEL1_DECK_TOTAL; i++) {
			this.developmentCard1Deck.add(i);
		}
		
		for(int i=1; i <= DEVELOPMENT_CARD_LEVEL2_DECK_TOTAL; i++) {
			this.developmentCard2Deck.add(i);
		}
		
		for(int i=1; i <= DEVELOPMENT_CARD_LEVEL3_DECK_TOTAL; i++) {
			this.developmentCard3Deck.add(i);
		}
		
		for(int i=1; i <= NOBILE_TILES_DECK_TOTAL; i++) {
			this.nobleTilesDeck.add(i);
		}
		
		// デッキをシャッフル
		Collections.shuffle(this.developmentCard1Deck);
		Collections.shuffle(this.developmentCard2Deck);
		Collections.shuffle(this.developmentCard3Deck);
		Collections.shuffle(this.nobleTilesDeck);
	
		this.globalStatus.getDevCard().get("level1").setA(this.developmentCard1Deck.pop());
		this.globalStatus.getDevCard().get("level2").setA(this.developmentCard2Deck.pop());
		this.globalStatus.getDevCard().get("level3").setA(this.developmentCard3Deck.pop());
		
		this.globalStatus.getDevCard().get("level1").setB(this.developmentCard1Deck.pop());
		this.globalStatus.getDevCard().get("level2").setB(this.developmentCard2Deck.pop());
		this.globalStatus.getDevCard().get("level3").setB(this.developmentCard3Deck.pop());
		
		this.globalStatus.getDevCard().get("level1").setC(this.developmentCard1Deck.pop());
		this.globalStatus.getDevCard().get("level2").setC(this.developmentCard2Deck.pop());
		this.globalStatus.getDevCard().get("level3").setC(this.developmentCard3Deck.pop());
		
		this.globalStatus.getDevCard().get("level1").setD(this.developmentCard1Deck.pop());
		this.globalStatus.getDevCard().get("level2").setD(this.developmentCard2Deck.pop());
		this.globalStatus.getDevCard().get("level3").setD(this.developmentCard3Deck.pop());
		
		// 貴族カードの初期化
		this.globalStatus.getNobleTiles().setA(this.nobleTilesDeck.pop());
		this.globalStatus.getNobleTiles().setB(this.nobleTilesDeck.pop());
		this.globalStatus.getNobleTiles().setC(this.nobleTilesDeck.pop());
		this.globalStatus.getNobleTiles().setD(this.nobleTilesDeck.pop());
		this.globalStatus.getNobleTiles().setE(this.nobleTilesDeck.pop());
		
		// 各トークンを初期化
		this.globalStatus.getToken().setEmerald(total_token);
		this.globalStatus.getToken().setSapphire(total_token);
		this.globalStatus.getToken().setRuby(total_token);
		this.globalStatus.getToken().setDiamond(total_token);
		this.globalStatus.getToken().setOnyx(total_token);
		this.globalStatus.getToken().setGoldJoker(GOLD_JOKER_TOKEN_TOTAL);
	}
	
	private LinkedList<Integer> developmentCard1Deck = new LinkedList<Integer>();
	private LinkedList<Integer> developmentCard2Deck = new LinkedList<Integer>();
	private LinkedList<Integer> developmentCard3Deck = new LinkedList<Integer>();
	private LinkedList<Integer> nobleTilesDeck = new LinkedList<Integer>();
	
	private Global globalStatus = new Global();
}
