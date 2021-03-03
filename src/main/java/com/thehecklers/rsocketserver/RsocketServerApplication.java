package com.thehecklers.rsocketserver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@SpringBootApplication
public class RsocketServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RsocketServerApplication.class, args);
    }

}

@Configuration
class ServerConfig {
    @Bean
    RSocketRequester requester(RSocketRequester.Builder builder) {
        return builder.tcp("localhost", 7635);
    }
}

@Controller
@AllArgsConstructor
class ServerController {
    private final RSocketRequester requester;

    @MessageMapping("reqstream")
    Flux<Aircraft> reqStream(Mono<Instant> instantMono) {
        return instantMono.doOnNext(i -> System.out.println("⏰ " + i))
                .thenMany(requester.route("acstream")
                        .data(instantMono)
                        .retrieveFlux(Aircraft.class)
                        .share());
    }
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Aircraft {
    private String callsign, flightno, reg, type;
    private int altitude, heading, speed;
    private double lat, lon;
}