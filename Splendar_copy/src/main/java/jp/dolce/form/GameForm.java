package jp.dolce.form;

import jp.dolce.entity.GameDataForJson;
import jp.dolce.entity.Global;
import jp.dolce.entity.Player;
import lombok.Data;

@Data
public class GameForm {
	private Global global = new Global();
	private GamePlayerForm player = new GamePlayerForm();
	private GameDataForJson gameData = new GameDataForJson();
	private String event = "";
	private String userid = "";
	private String receiveJsonStr = "";
	private String turnPlayerName = "";
}
