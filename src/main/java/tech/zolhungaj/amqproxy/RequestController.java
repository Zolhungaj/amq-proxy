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

    @PostMapping("/getVideo")
    public String getVideo(@Valid @RequestBody FfmpegRequest request){
        return ffmpegRunner.runFfmpeg(request);
    }
}
