package com.itinordic.sadombo;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 *
 * @author Charles Chigoriwa
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.itinordic.sadombo")
public class SadomboWebMvcConfigurer implements WebMvcConfigurer{
    
    
    
}
