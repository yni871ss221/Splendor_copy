package jp.dolce.entity;

import java.util.HashMap;
import java.util.Map;

import jp.dolce.entity.DevelopmentCard;
import jp.dolce.entity.NobleTiles;
import jp.dolce.entity.Token;
import lombok.Data;

@Data
public class Global {

	public Global() {
		this.devCard.put("level1", new DevelopmentCard());
		this.devCard.put("level2", new DevelopmentCard());
		this.devCard.put("level3", new DevelopmentCard());
	}
	private Map<String,DevelopmentCard> devCard = new HashMap<String,DevelopmentCard>();
	private Token token = new Token();
	private NobleTiles nobleTiles = new NobleTiles();
}
