package example.cucumber.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@ComponentScan({"example.cucumber.steps", "example.cucumber.pages", "example.utils",
        "example.helper"})
@PropertySources({@PropertySource("classpath:application.properties"),})
public class SpringConfiguration {
}
