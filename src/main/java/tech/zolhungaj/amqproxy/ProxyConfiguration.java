package tech.zolhungaj.amqproxy;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.io.File;

@Configuration
@ConfigurationProperties(prefix = "amq-proxy")
@Validated
@Data
public class ProxyConfiguration {

    @NotNull(message = "amq-proxy.base-directory cannot be null")
    private File baseDirectory;

    @NotEmpty(message = "amq-proxy.external-path cannot be empty")
    private String externalPath;

    @Min(5)
    private int deleteAfterMinutes;

    @AssertFalse(message = "amq-proxy.base-directory cannot be blank")
    public boolean isBaseDirectoryBlank(){
        return baseDirectory.getAbsolutePath().isBlank();
    }

    @AssertTrue(message = "amq-proxy.base-directory must be a directory")
    public boolean isBaseDirectoryADirectory(){
        return baseDirectory.isDirectory();
    }
}
