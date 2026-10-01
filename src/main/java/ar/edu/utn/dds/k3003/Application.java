package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.tools.LogisticaTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public ToolCallbackProvider logisticaToolCallbacks(LogisticaTools tools) {

    return MethodToolCallbackProvider.builder().toolObjects(tools).build();
  }
}
