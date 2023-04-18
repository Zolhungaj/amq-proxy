package tech.zolhungaj.amqproxy;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
public class FfmpegRunner {
    private static final DecimalFormat FORMATTER = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.US));
    public String runFfmpeg(@Valid FfmpegRequest request){
        //ffmpeg -ss 20 -t 30 -i "https://files.catbox.moe/xxx.webm" -c copy test.webm
        String filename = UUID.randomUUID() + request.url().substring(request.url().lastIndexOf('.'));
        String command = "ffmpeg -ss %s -t %s -i \"%s\" -c copy \"%s\"".formatted(
                FORMATTER.format(request.start()),
                FORMATTER.format(request.length()),
                request.url(),
                filename);
        log.info("Running command {}", command);
        return filename;
    }
}
