package dio.budgeting;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OpenAiSpeechModelIT {

    @Autowired
    OpenAiAudioSpeechModel openAiSpeechModel;

    @Test
    public void should_produceAudio_when_textIsProvided() throws IOException {
        var response = openAiSpeechModel.call("Eu só acordo mais cedo para poder me atrasar com calma.Sou apaixonado pela minha cama, mas o despertador não aprova.Não sou feio, você está me vendo em baixa resolução.Guardo seus segredos comigo, afinal, nem estava escutando.");

        assertThat(response).hasSizeGreaterThan(1024);

        var tempFile = Files.createTempFile("AUDIO", ".mp3");
        Files.write(tempFile, response);
        System.out.println(tempFile.toAbsolutePath());
    }
}
