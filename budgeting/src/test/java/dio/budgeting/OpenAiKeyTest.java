package dio.budgeting;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OpenAiKeyTest {

    @Autowired
    private OpenAiChatModel openAiChatModel;

    @Test
    void shouldCallOpenAi() {
        ChatClient chatClient = ChatClient.builder(openAiChatModel).build();

        String response = chatClient.prompt()
            .user("Responda somente: OK")
            .call()
            .content();

        assertThat(response).isNotBlank();
    }
}