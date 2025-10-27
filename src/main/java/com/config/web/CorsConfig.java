package com.config.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig {

@Bean
WebMvcConfigurer corsConfigurer() {
 return new WebMvcConfigurer() {
   @Override
   public void addCorsMappings(CorsRegistry registry) {
     registry.addMapping("/**")
         .allowedOrigins("http://localhost:3000")
         .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
         .allowedHeaders("*")
         .allowCredentials(true)  
         .maxAge(3600);
   }
 };
}
}
