package peer.backend.service.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import peer.backend.dto.profile.KeywordResponse;
import peer.backend.entity.user.User;
import peer.backend.exception.BadRequestException;
import peer.backend.exception.NotFoundException;
import peer.backend.oauth.PrincipalDetails;
import peer.backend.repository.user.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeywordAlarmService {
    private final UserRepository userRepository;

    @Transactional
    public void addKeyword(PrincipalDetails principalDetails, String newKeyword) {
        User user = principalDetails.getUser();
        if (user.getKeywordAlarm() == null) {
            user.setKeywordAlarm(newKeyword);
        }
        else if (user.getKeywordAlarm().contains(newKeyword)) {
            throw new BadRequestException("이미 존재하는 키워드 입니다.");
        }
        else {
            String keyword = String.format("%s^&%%%s", user.getKeywordAlarm(), newKeyword);
            user.setKeywordAlarm(keyword);
        }
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public KeywordResponse getKeyword(PrincipalDetails principalDetails) {
        User user = principalDetails.getUser();
        return KeywordResponse.builder()
                .keyword(user.getKeywordAlarm())
                .build();
    }

    @Transactional
    public void deleteKeyword(PrincipalDetails principalDetails, String keyword) {
        User user = principalDetails.getUser();
        String userKeyword = user.getKeywordAlarm();
        if (userKeyword != null) {
            if (!userKeyword.contains(keyword)) {
                throw new BadRequestException("없는 키워드 입니다.");
            }
            List<String> keywordList = new ArrayList<>(
                    Arrays.asList(user.getKeywordAlarm().split("\\^&%"))
            );
            keywordList.remove(keyword);
            userKeyword = keywordList.get(0);
            for (int index = 1; index < keywordList.size(); index++) {
                userKeyword = String.format("%s^&%%%s", userKeyword, keywordList.get(index));
            }
            user.setKeywordAlarm(userKeyword);
            userRepository.save(user);
        }
    }

    @Transactional
    public void deleteAll(PrincipalDetails principalDetails) {
        User user = principalDetails.getUser();
        user.setKeywordAlarm(null);
        userRepository.save(user);
    }
}