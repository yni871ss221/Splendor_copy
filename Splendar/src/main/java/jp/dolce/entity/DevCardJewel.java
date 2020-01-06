package jp.dolce.entity;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class DevCardJewel {

	private List<PlayerHasDevCard> emerald = new ArrayList<PlayerHasDevCard>();
	private List<PlayerHasDevCard> sapphire = new ArrayList<PlayerHasDevCard>();
	private List<PlayerHasDevCard> ruby = new ArrayList<PlayerHasDevCard>();
	private List<PlayerHasDevCard> diamond = new ArrayList<PlayerHasDevCard>();
	private List<PlayerHasDevCard> onyx = new ArrayList<PlayerHasDevCard>();
	private List<PlayerHasDevCard> goldJoker = new ArrayList<PlayerHasDevCard>();
}
