package tech.zolhungaj.amqproxy;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class Cleaner {
    private final ProxyConfiguration configuration;


    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void deleteOldFiles() throws IOException {
        log.info("Cleaning up old files");
        try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(configuration.getBaseDirectory().toPath())){
            Iterator<Path> iterator = directoryStream.iterator();
            Stream<Path> stream = Stream.iterate(iterator.next(), path -> iterator.hasNext(), path -> iterator.next());
            stream
                    .filter(this::fileShouldBeDeleted)
                    .forEach(this::delete);
        }
    }

    private boolean fileShouldBeDeleted(@NonNull Path path){
        String fileName = path.getFileName().toString();
        if(!fileName.endsWith(".mp3") && !fileName.endsWith(".webm")){
            return false;
        }
        try {
            Instant timeToDelete = Instant.now().minus(configuration.getDeleteAfterMinutes(), ChronoUnit.MINUTES);
            return Files.getLastModifiedTime(path).toInstant().isBefore(timeToDelete);
        }catch (IOException e){
            log.error("Could not get last modified time of file {}", path, e);
            return false;
        }
    }

    private void delete(Path path){
        try {
            log.info("Deleting file {}", path);
            Files.delete(path);
        }catch (IOException e){
            log.error("Could not delete file {}", path, e);
        }
    }
}
