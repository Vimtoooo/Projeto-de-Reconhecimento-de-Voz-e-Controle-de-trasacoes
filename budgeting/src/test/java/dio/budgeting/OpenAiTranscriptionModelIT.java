package dio.budgeting;

import static org.assertj.core.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;

@SpringBootTest
public class OpenAiTranscriptionModelIT {

    @Autowired
    OpenAiAudioTranscriptionModel openAiTranscriptionModel;

    @ParameterizedTest
    @CsvSource({
        "Recording-1.m4a, pão",
        "Recording-2.m4a, dia",
        "Recording-3.m4a, dormir",
        "Recording-4.m4a, rato",
    })
    public void should_containExpectedKeywords_when_audioFilesAreProcessed(String filename, String expectedKeyword) {
        var recording = new FileSystemResource(new File("src/test/resources/audio/" + filename));

        var response = openAiTranscriptionModel.call(recording);

        assertThat(response).contains(expectedKeyword);
        System.out.println(response);
    }
}
