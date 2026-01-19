package ai.ripple.Notifications.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public Mono<Object> sendOtp(String toEmail, String otp, String purpose) {
        return Mono.fromRunnable(() -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your OTP for " + purpose);
            message.setText(buildOtpMessage(otp));
            message.setFrom("shakyashivam4510@gmail.com");

            mailSender.send(message);
            System.out.println("OTP email sent to: " + toEmail);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private String buildOtpMessage(String otp) {
        return "Hello,\n\nYour OTP is: " + otp +
               "\nIt is valid for 5 minutes.\n\n" +
               "If you did not request this, please ignore this email.\n\nThanks!";
    }
}
