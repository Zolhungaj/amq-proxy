package tech.zolhungaj.amqproxy;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RequestController {
    private final FfmpegRunner ffmpegRunner;

    @PostMapping("/proxy-video")
    public String proxyVideo(@Valid @RequestBody FfmpegRequest request){
        return ffmpegRunner.copyVideo(request);
    }

    @PostMapping("/proxy-audio")
    public String proxyAudio(@Valid @RequestBody FfmpegRequest request){
        return ffmpegRunner.extractAudioAndEncodeToMp3(request);
    }
}
