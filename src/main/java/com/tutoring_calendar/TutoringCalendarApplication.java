package com.tutoring_calendar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.tutoring_calendar")
@EnableScheduling
public class TutoringCalendarApplication {

    public static void main(String[] args) {
        SpringApplication.run(TutoringCalendarApplication.class, args);
    }

//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//		return new WebMvcConfigurer() {
//			@Override
//			public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/events/{date}").allowedOrigins("http://localhost:63342");
//                registry.addMapping("/create-event").allowedOrigins("http://localhost:63342");
//			}
//		};
//    }

}
