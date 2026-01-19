package ai.ripple.Notifications.controller;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ai.ripple.Notifications.service.OtpService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import java.time.Duration;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class OtpController {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final OtpService otpService;

    @PostMapping("/generate-otp")
    public Mono<ResponseEntity<String>> generateOtp(
            @RequestParam String email,
            @RequestParam String purpose) {

        return otpService.generateOtp(email, purpose)
                .thenReturn(ResponseEntity.ok("OTP sent to email: " + email))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.badRequest().body(e.getMessage()))
                );
    }

    @PostMapping("/verify-otp")
    public Mono<ResponseEntity<String>> verifyOtp(
            @RequestParam String email,
            @RequestParam String purpose,
            @RequestParam String otp) {

        return otpService.verifyOtp(email, purpose, otp)
                .map(valid -> ResponseEntity.ok("OTP verified successfully"))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.badRequest().body(e.getMessage()))
                );
    }

    @GetMapping("/set")
    public Mono<String> setKey(
            @RequestParam String key,
            @RequestParam String value) {

        return redisTemplate.opsForValue()
                .set(key, value, Duration.ofMinutes(5))
                .map(result -> Boolean.TRUE.equals(result)
                        ? "Key stored successfully: " + key
                        : "Failed to store key: " + key
                )
                .onErrorResume(e ->
                        Mono.just("Error storing key: " + e.getMessage())
                );
    }

    @GetMapping("/get")
    public Mono<String> getKey(@RequestParam String key) {
        return redisTemplate.opsForValue()
                .get(key)
                .map(val -> "Value: " + val)
                .switchIfEmpty(Mono.just("Key not found"));
    }
}
