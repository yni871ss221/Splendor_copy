package jp.dolce.entity;

import java.util.Collections;
import java.util.LinkedList;

import jp.dolce.constant.GameSystemConst;
import lombok.Data;

@Data
public class GameData{
	
	private int id;
	private Token cost = new Token();
	private int score;
	private String color;
}
