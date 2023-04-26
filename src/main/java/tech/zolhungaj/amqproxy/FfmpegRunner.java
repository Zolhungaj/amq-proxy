package tech.zolhungaj.amqproxy;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FfmpegRunner {
    private static final String QUOTED_STRING = "\"%s\"";
    private static final DecimalFormat FORMATTER = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.US));

    private final ProxyConfiguration configuration;
    public String copyVideo(@Valid FfmpegRequest request){
        //ffmpeg -ss 20 -t 30 -i "https://files.catbox.moe/xxx.webm" -c copy test.webm
        String filename = UUID.randomUUID() + request.url().substring(request.url().lastIndexOf('.'));
        List<String> command = getStartOfCommand(request);
        command.addAll(List.of(
                "-map", "0:v:0",
                "-map", "0:a:0",
                "-c", "copy",
                QUOTED_STRING.formatted(filename)
        ));
        runCommandWithOsSpecificStarter(command);
        return filename;
    }

    public String extractAudioAndEncodeToMp3(@Valid FfmpegRequest request){
        //ffmpeg -ss xx -t xx -i "https://files.catbox.moe/xxx.webm" -map_chapters -1 -map_metadata -1 -map 0:a:0 -c libmp3lame -ar 44100 -b:a 320k xx-audio.mp3
        String filename = UUID.randomUUID() + "-audio.mp3";
        ArrayList<String> command = getStartOfCommand(request);
        command.addAll(List.of(
                "-map", "0:a:0",
                "-c", "libmp3lame",
                "-ar", "44100",
                "-b:a", "320k",
                QUOTED_STRING.formatted(filename)
        ));
        runCommandWithOsSpecificStarter(command);
        return filename;
    }


    private ArrayList<String> getStartOfCommand(FfmpegRequest request){
        return new ArrayList<>(List.of(
                "ffmpeg",
                "-ss", FORMATTER.format(request.start()),
                "-t", FORMATTER.format(request.length()),
                "-i", QUOTED_STRING.formatted(request.url()),
                "-map_chapters", "-1",
                "-map_metadata", "-1"
        ));
    }

    private void runCommandWithOsSpecificStarter(List<String> command){
        List<String> list = getOsSpecificCommandStarter();
        list.add(String.join(" ", command));
        runCommand(list.toArray(new String[0]));
    }

    private ArrayList<String> getOsSpecificCommandStarter(){
        ArrayList<String> command = new ArrayList<>();
        if(System.getProperty("os.name").toLowerCase().startsWith("windows")){
            command.add("cmd");
            command.add("/c");
        }else{
            command.add("/bin/sh");
            command.add("-c");
        }
        return command;
    }

    private void runCommand(String... command){
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .directory(configuration.getBaseDirectory());
        log.info("Running command {}", builder.command());
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
