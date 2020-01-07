package jp.dolce.entity;

import java.util.Collections;
import java.util.LinkedList;

import jp.dolce.constant.GameSystemConst;
import lombok.Data;

@Data
public class GameStatusForJson{
	
	private LinkedList<Integer> developmentCard1Deck = new LinkedList<Integer>();
	private LinkedList<Integer> developmentCard2Deck = new LinkedList<Integer>();
	private LinkedList<Integer> developmentCard3Deck = new LinkedList<Integer>();
	private LinkedList<Integer> nobleTilesDeck = new LinkedList<Integer>();
	
	private Global globalStatus = new Global();
}
