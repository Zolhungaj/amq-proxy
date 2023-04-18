package tech.zolhungaj.amqproxy;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

public record FfmpegRequest(
        @URL(host = "files.catbox.moe", protocol = "https")
        @Pattern(regexp = "^[^\"]+\\.(webm|mp3)$")
        String url,
        @Min(0)
        BigDecimal start,
        @Min(1)
        @Max(90)
        BigDecimal length
) {
}
