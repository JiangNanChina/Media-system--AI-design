package com.example.photography;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class PhotographyApplication {

	public static final ZoneId APPLICATION_TIME_ZONE = ZoneId.of("Asia/Shanghai");

	public static void main(String[] args) {
		configureApplicationTimeZone();
		SpringApplication.run(PhotographyApplication.class, args);
	}

	/**
	 * 固定应用业务时区，避免 LocalDateTime.now()、定时任务和日志随服务器时区变化。
	 * 必须在 Spring 容器和数据库连接池启动前执行。
	 */
	static void configureApplicationTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone(APPLICATION_TIME_ZONE));
	}

}
