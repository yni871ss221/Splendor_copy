package jp.dolce.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ReceiveGameStatus {

	private String userid = "";
	private Map<String, Integer> devCard1 = new HashMap<String, Integer>();
	private Map<String, Integer> devCard2 = new HashMap<String, Integer>();
	private Map<String, Integer> devCard3 = new HashMap<String, Integer>();
    private List<String> token = new ArrayList<String>();
    private List<Integer> nobleTiles = new ArrayList<Integer>();
    private String event;
}
