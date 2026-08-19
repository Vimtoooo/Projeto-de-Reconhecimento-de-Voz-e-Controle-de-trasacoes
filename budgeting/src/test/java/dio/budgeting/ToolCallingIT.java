package dio.budgeting;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ToolCallingIT {

    @Autowired
    OpenAiChatModel openAiChatModel;

    static class MathTools {

        @Tool(description = "soma dois números inteiros, a e b.")
        public int sum(int a, int b) {
            return a + b;
        }

        @Tool(description = "subtrai dois números inteiros, a e b.")
        public int diff(int a, int b) {
            return a - b;
        }
    }

    @Test
    void should_execute_when_prompted() {
        var chatClient = ChatClient.builder(openAiChatModel)
            .defaultSystem("Você é um matemático.")
            .defaultTools(new MathTools())
            .build();
    
        var response = chatClient.prompt("Soma 10 mais 20, depois subitraia o resultado anterior. Exiba apenas o resultado final sem explicações.")
            .call().content();
    
        assertThat(response).isNotNull().asString().contains("0");
        System.out.println(response);
    }
}
