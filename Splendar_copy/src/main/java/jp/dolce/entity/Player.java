package jp.dolce.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class Player {

	private String name = "";
	private String id;
	private String avatar_url;
	private Boolean game_start_flg;
	private int score = 0;
    private DevCardJewel devCard = new DevCardJewel();
    private Token token = new Token();
    private List<Integer> nobleTiles = new ArrayList<Integer>();
}
