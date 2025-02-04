package peer.backend.service;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import peer.backend.dto.security.EmailMessage;
import peer.backend.dto.security.Message;
import peer.backend.exception.BadRequestException;
import peer.backend.exception.ForbiddenException;
import peer.backend.exception.UnauthorizedException;

@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private final JavaMailSender sender;
    private final RedisTemplate<String, String> redisTemplate;

    private String getAuthCode(String email) {
        Random random = new Random();
        String code = random.ints('0', 'Z' + 1)
            .filter(i -> (i <= '9' || i >= 'A'))
            .limit(7)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
        this.redisTemplate.opsForValue().set(email, code, 5, TimeUnit.MINUTES);
        return code;
    }

    private void send(EmailMessage emailMessage) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        try {
            mailMessage.setTo(emailMessage.getTo());
            mailMessage.setSubject(emailMessage.getSubject());
            mailMessage.setText(emailMessage.getText());
            sender.send(mailMessage);
        } catch (Exception e) {
            throw new ForbiddenException("잘못된 접근 입니다.");
        }
    }

    public Message sendEmail(String email) {
        Message message = new Message();
        EmailMessage emailMessage = new EmailMessage();
        try {
            emailMessage.setTo(email);
            emailMessage.setSubject("Peer 인증 코드");
            emailMessage.setText(
                String.format("회원가입을 위해 아래의 코드를 입력창에 입력해 주세요.\n\n%s\n", getAuthCode(email)));
            this.send(emailMessage);
            message.setStatus(HttpStatus.OK);
        } catch (Exception e) {
            throw new ForbiddenException("Failed to send E-mail");
        }
        return message;
    }

    public void emailCodeVerification(String email, String code) {
        String redisCode = this.redisTemplate.opsForValue().get(email);
        if (redisCode == null) {
            throw new BadRequestException("잘못된 이메일입니다!");
        }
        if (!redisCode.equals(code)) {
            throw new UnauthorizedException("잘못된 인증 코드입니다!");
        }
    }
}
