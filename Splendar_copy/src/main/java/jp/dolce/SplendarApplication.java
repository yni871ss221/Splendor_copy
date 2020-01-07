package jp.dolce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootApplication
public class SplendarApplication extends SpringBootServletInitializer{

	@Autowired
	private StringRedisTemplate redisTemplate; // Redisを使用するためのimport
	
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(SplendarApplication.class, args);
		SplendarApplication app = ctx.getBean(SplendarApplication.class);
		app.execStartup(args);
	}
	
	public void execStartup(String[] args) {
        // 起動時にredisに登録された情報を削除する
		redisTemplate.delete("loginUser");
		redisTemplate.delete("playUser");
		redisTemplate.delete("gameStatus");
	}
}

