package peer.backend.dto.profile.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordRequest {
    @NotBlank(message = "현재 사용 중인 비밀번호를 입력하세요.")
    private String presentPassword;
    @NotBlank(message = "사용할 비밀번호를 입력하세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 반드시 8자 이상이어야 합니다.")
    private String newPassword;
    @NotBlank(message = "사용할 비밀번호를 한번 더 입력하세요.")
    private String confirmPassword;
}
