package tech.zolhungaj.amqproxy;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.NotDirectoryException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
public class FfmpegRunner {
    private static final String QUOTED_STRING = "\"%s\"";
    private static final DecimalFormat FORMATTER = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.US));

    @Value("${amq-proxy.base-directory}")
    private String directoryString;
    private File directory;

    @PostConstruct
    private void init(){
        log.info("Using directory: {}", directoryString);
        if(directoryString == null || directoryString.isBlank()){
            throw new UncheckedIOException(new FileNotFoundException("amq-proxy.base-directory is not set"));
        }
        directory = new File(directoryString);
        if(!directory.isDirectory()){
            throw new UncheckedIOException(new NotDirectoryException(directoryString));
        }
    }
    public String copyVideo(@Valid FfmpegRequest request){
        //ffmpeg -ss 20 -t 30 -i "https://files.catbox.moe/xxx.webm" -c copy test.webm
        String filename = UUID.randomUUID() + request.url().substring(request.url().lastIndexOf('.'));
        runCommand(
                "ffmpeg",
                "-ss", FORMATTER.format(request.start()),
                "-t", FORMATTER.format(request.length()),
                "-i", QUOTED_STRING.formatted(request.url()),
                "-map_chapters", "-1",
                "-map_metadata", "-1",
                "-map", "0:v:0",
                "-map", "0:a:0",
                "-c", "copy",
                QUOTED_STRING.formatted(filename)
        );
        return filename;
    }

    public String extractAudioAndEncodeToMp3(@Valid FfmpegRequest request){
        //ffmpeg -ss xx -t xx -i "https://files.catbox.moe/xxx.webm" -map_chapters -1 -map_metadata -1 -map 0:a:0 -c libmp3lame -ar 44100 -b:a 320k xx-audio.mp3
        String filename = UUID.randomUUID() + "-audio.mp3";
        runCommand(
                "ffmpeg",
                "-ss", FORMATTER.format(request.start()),
                "-t", FORMATTER.format(request.length()),
                "-i", QUOTED_STRING.formatted(request.url()),
                "-map_chapters", "-1",
                "-map_metadata", "-1",
                "-map", "0:a:0",
                "-c", "libmp3lame",
                "-ar", "44100",
                "-b:a", "320k",
                QUOTED_STRING.formatted(filename)
        );
        return filename;
    }

    private void runCommand(String... command){
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .directory(directory);
        log.info("Running command {}", String.join(" ", builder.command()));
        try{
            Process process = builder.start();
            int exitCode = process.waitFor();
            if(exitCode != 0){
                throw new RuntimeException("Command failed with exit code " + exitCode);
            }
        }catch (IOException e){
            throw new UncheckedIOException(e);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
