package jp.dolce.form;

import java.util.Date;

import jp.dolce.constant.MessageType;
import lombok.Data;

@Data
public class ReciveMessageForm {
	private MessageType type;
	private String content;
	private String sender;
	private Date timestamp;
}
