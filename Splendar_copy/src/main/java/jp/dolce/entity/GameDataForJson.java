package jp.dolce.entity;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class GameDataForJson{
	
	private Map<Integer, GameData> devlopment_card_deck1_data = new HashMap<Integer, GameData>();
	private Map<Integer, GameData> devlopment_card_deck2_data = new HashMap<Integer, GameData>();
	private Map<Integer, GameData> devlopment_card_deck3_data = new HashMap<Integer, GameData>();
	private Map<Integer, GameData> noble_card_data = new HashMap<Integer, GameData>();
}
