package jp.dolce.constant;

import java.util.Arrays;
import java.util.List;

public interface GameSystemConst {
	public final List<String> USER_ID_COLOR = Arrays.asList("RED", "BLUE", "YELLOW", "GREEN");
	
	public final int DEVELOPMENT_CARD_LEVEL1_DECK_TOTAL = 40;
	public final int DEVELOPMENT_CARD_LEVEL2_DECK_TOTAL = 30;
	public final int DEVELOPMENT_CARD_LEVEL3_DECK_TOTAL = 20;
	public final int NOBILE_TILES_DECK_TOTAL = 10;
	public final int NOBILE_TILES_FIELD_TOTAL = 5;
	public final int TOKEN_TOTAL = 7;
	public final int GOLD_JOKER_TOKEN_TOTAL = 5;
	
	public final int NOTHING_DEVELOPMENT_CARD_ID = -1;
	public final int NOTHING_NOBLE_CARD_ID = -1;
	
	public final String EVENT_CODE_GAME_INIT = "gameInit";
	public final String EVENT_CODE_TOKEN = "tokenTilesClick";
	public final String EVENT_CODE_NOBLE = "nobleTiles";
	public final String EVENT_CODE_DEP_CARD_DECK = "depCardDeckClick";
	public final String EVENT_CODE_KEEP = "keep";
	public final String EVENT_CODE_EXCHANGE_KEEP_DEV_CARD = "exchangeKeepDevCard";
	
}
