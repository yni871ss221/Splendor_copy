package jp.dolce.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class PlayerForJson {

	private String userid = "";
	private List<Integer> devCard = new ArrayList<Integer>();
    private List<String> token = new ArrayList<String>();
    private List<Integer> nobleTiles = new ArrayList<Integer>();
    private String event;
}
