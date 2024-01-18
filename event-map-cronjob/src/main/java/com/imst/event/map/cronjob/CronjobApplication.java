package com.imst.event.map.cronjob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.imst.event.map.cronjob.utils.ApplicationContextUtils;

@EnableScheduling
@SpringBootApplication
public class CronjobApplication {

	public static void main(String[] args) {
		SpringApplication.run(CronjobApplication.class, args);
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		
		return new BCryptPasswordEncoder();
	}
	
	
	@Autowired
	public void context(ApplicationContext context) {
		
		ApplicationContextUtils.setApplicationContext(context);
	}
	
	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		ApplicationContextUtils.setMessageSource(messageSource);
	}
	
	
	@Value("${datatable.page.length}")
	private void setTableLength(Integer length) {
		
		ApplicationContextUtils.setTableLength(length);
	}
}
