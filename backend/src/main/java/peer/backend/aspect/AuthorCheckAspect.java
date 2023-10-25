package peer.backend.aspect;

//@Aspect
//@Component
//public class AuthorCheckAspect {
//
//    @Before("@annotation(com.example.AuthorCheck)")
//    public void checkAuthor(JoinPoint joinPoint) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        // 현재 사용자와 글의 게시자를 비교하고 권한 검사 로직을 여기에 추가하세요.
//        // 예를 들어, 현재 사용자와 글의 게시자가 다르면 예외를 던질 수 있습니다.
//        // 게시자 확인 로직을 작성해야 합니다.
//    }
//}


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import peer.backend.entity.board.recruit.Recruit;
import peer.backend.entity.board.recruit.RecruitRole;
import peer.backend.entity.board.team.Post;
import peer.backend.entity.user.User;
import peer.backend.exception.ForbiddenException;
import peer.backend.exception.NotFoundException;
import peer.backend.repository.board.recruit.RecruitRepository;
import peer.backend.repository.user.UserRepository;

@Aspect
@Component
public class AuthorCheckAspect {
    private final RecruitRepository recruitRepository;
    private final UserRepository userRepository;

    @Autowired
    public AuthorCheckAspect(RecruitRepository recruitRepository, UserRepository userRepository) {
        this.recruitRepository = recruitRepository;
        this.userRepository = userRepository;
    }


    @Before("@annotation(peer.backend.annotation.AuthorCheck)")
    public void checkAuthor(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        if (args.length < 2 || !(args[0] instanceof Authentication) || !(args[1] instanceof Long)) {
            throw new IllegalArgumentException("메서드 파라미터가 부적절합니다.");
        }

        Authentication authentication = (Authentication) args[0];
        Long recruit_id = (Long) args[1];
        Recruit recruit = recruitRepository.findById(recruit_id).orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

        if (!User.authenticationToUser(authentication).getNickname().equals(recruit.getWriter().getNickname())){
            throw new ForbiddenException("작성자가 아닙니다.");
        }
    }

}