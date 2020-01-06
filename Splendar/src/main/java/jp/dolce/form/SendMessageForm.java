package jp.dolce.form;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jp.dolce.constant.MessageType;
import lombok.Data;

@Data
public class SendMessageForm {

	private MessageType type;
	private String content;
	private String sender;
	private Date timestamp;
	private String topic;
	private Map<String, String> loginUserMap;
	
	public SendMessageForm(MessageType type, String content, String sender, String topic) {
		this.type = type;
		this.content = content;
		this.sender = sender;
		this.topic = topic;
		this.timestamp = new Date();
		
		loginUserMap = new HashMap<String, String>();
		loginUserMap.put("type", type.toString());
		loginUserMap.put("content", content);
		loginUserMap.put("sender", sender);
		loginUserMap.put("topic", topic);
		loginUserMap.put("timestamp", new Date().toString());
	}
}
