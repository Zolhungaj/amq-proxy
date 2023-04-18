package tech.zolhungaj.amqproxy;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.hibernate.validator.constraints.URL;
import org.springframework.validation.annotation.Validated;


@Validated
public record FfmpegRequest(
        @URL(host = "files.catbox.moe", protocol = "https")
        String url,
        @Min(0)
        double start,
        @Min(1)
        @Max(90)
        double length
) {
}
