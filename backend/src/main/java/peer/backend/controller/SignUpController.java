package peer.backend.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import peer.backend.dto.security.Message;
import peer.backend.dto.security.UserInfo;
import peer.backend.dto.security.request.EmailAddress;
import peer.backend.dto.security.request.EmailCode;
import peer.backend.entity.user.User;
import peer.backend.exception.UnauthorizedException;
import peer.backend.oauth.PrincipalDetails;
import peer.backend.exception.ConflictException;
import peer.backend.service.EmailAuthService;
import peer.backend.service.MemberService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/signup")
public class SignUpController {

    private final MemberService memberService;
    private final EmailAuthService emailService;

    @PostMapping("/email") // 메일을 전송하기 전, DB에서 메일이 있는지 확인
    public ResponseEntity<Object> sendEmail(@Valid @RequestBody EmailAddress address) {
        String email = address.getEmail();

        if (this.memberService.emailDuplicationCheck(email)) {
            throw new ConflictException("이미 존재하는 이메일입니다!");
        }

        Message message = emailService.sendEmail(address.getEmail());
        return new ResponseEntity<Object>(message.getStatus());
    }

    @PostMapping("/code")
    public ResponseEntity<Object> emailCodeVerification(@RequestBody EmailCode code) {
        this.emailService.emailCodeVerification(code.getEmail(), code.getCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/form")
    public ResponseEntity<Object> signUp(@Valid @RequestBody UserInfo info) {
        // SQL 인젝션 체크
        User createdUser = memberService.signUp(info);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/withdrawal")
    public ResponseEntity<Object> withdrawal(@RequestBody String password,
        Authentication authentication) {
        User user = User.authenticationToUser(authentication);
        if (this.memberService.verificationPassword(password, user.getPassword())) {
            throw new UnauthorizedException("비밀번호가 잘못되었습니다!");
        }
        this.memberService.deleteUser(user);
        return ResponseEntity.ok().build();
    }
}
