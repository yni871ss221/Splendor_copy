package jp.dolce.base;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileManagement {

	@Autowired
    ResourceLoader resourceLoader;
	public void initData() throws Throwable {
		ObjectMapper objectMapper = new ObjectMapper();
        String filepath = "data/develop_card_deck1.json"; // src/main/resources 配下の相対パス
        /**
        Resource resource = resourceLoader.getResource("classpath:" + filepath);
        
        
        String name = resource.getFilename();
        File file = resource.getFile();
        **/
        File file = new File(filepath);
        JsonNode root = objectMapper.readTree(file);
        
    }
}
