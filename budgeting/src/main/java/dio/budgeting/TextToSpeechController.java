package dio.budgeting;

import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api")
public class TextToSpeechController {

    private final TextToSpeechModel textToSpeechModel;

    public TextToSpeechController(TextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }

    @PostMapping(value = "/synthesize", produces = "audio/mpeg")
    public ResponseEntity<Resource> synthesize(@RequestBody SynthesizeRequest request) {
        byte[] audio = textToSpeechModel.call(request.text());
        ByteArrayResource resource = new ByteArrayResource(audio);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("audio.mp3")
                                .build()
                                .toString()
                )
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .contentLength(audio.length)
                .body(resource);
    }


    record SynthesizeRequest(String text) {}

}
