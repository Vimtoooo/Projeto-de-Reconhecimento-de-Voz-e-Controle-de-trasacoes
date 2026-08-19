package dio.budgeting;

import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.MediaType;


@RestController
@RequestMapping("/api")
public class TranscriptionController {
    private final TranscriptionModel transcriptionModel;

    public TranscriptionController(TranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }

    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String transcribe(@RequestParam("file") MultipartFile file) {
        System.out.println("=== TRANSCRIBE REQUEST ===");
        System.out.println("File received: " + file.getOriginalFilename());
        System.out.println("File size: " + file.getSize() + " bytes");
        System.out.println("File content type: " + file.getContentType());
        System.out.println("File empty: " + file.isEmpty());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        var resource = file.getResource();
        return transcriptionModel.transcribe(resource);
    }
}
