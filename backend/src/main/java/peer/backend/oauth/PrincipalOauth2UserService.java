package peer.backend.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import peer.backend.entity.user.SocialLogin;
import peer.backend.entity.user.User;
import peer.backend.exception.ForbiddenException;
import peer.backend.oauth.enums.LoginStatus;
import peer.backend.oauth.enums.SocialLoginProvider;
import peer.backend.oauth.provider.FortyTwoUserInfo;
import peer.backend.oauth.provider.GitHubUserInfo;
import peer.backend.oauth.provider.GoogleUserInfo;
import peer.backend.oauth.provider.OAuth2UserInfo;
import peer.backend.repository.user.SocialLoginRepository;
import peer.backend.repository.user.UserRepository;
import peer.backend.service.profile.ProfileService;

@Slf4j
@RequiredArgsConstructor
@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

  private static final String GOOGLE = "google";
  private static final String FT = "ft";
  private static final String GITHUB = "github";


  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final UserRepository userRepository;
  private final SocialLoginRepository socialLoginRepository;
  private final ProfileService profileService;
  private final OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//    System.out.println("getClientRegistration: " + userRequest.getClientRegistration());
//    System.out.println("getAccessToken: " + userRequest.getAccessToken().getTokenValue());

    OAuth2User oAuth2User = super.loadUser(userRequest);
//    System.out.println("attributes: " + oAuth2User.getAttributes());
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    OAuth2UserInfo oAuth2UserInfo = this.getOAuth2UserInfo(oAuth2User, registrationId);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    LoginStatus loginStatus;

    UserDetails userDetails;
    if (authentication != null) {
      userDetails = (UserDetails) authentication.getPrincipal();
      log.info(userDetails.getUsername());
    }

    SocialLoginProvider provider = oAuth2UserInfo.getProvider();
    String providerId = oAuth2UserInfo.getProviderId();
    String email = oAuth2UserInfo.getEmail();

//    log.info("provider : " + provider);
//    log.info("providerId : " + providerId);
//    log.info("email : " + email);
//    log.info("accessToken : " + userRequest.getAccessToken().getTokenValue());

    User user;
    SocialLogin socialInfo = this.socialLoginRepository.findByEmail(email).orElse(null);

    if (socialInfo == null) {
      if (authentication == null) {
        user = null;
        // 회원가입
        loginStatus = LoginStatus.REGISTER;
      } else {
        // 연동
        loginStatus = LoginStatus.LINK;
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        user = principalDetails.getUser();
        socialInfo = SocialLogin.builder()
            .user(user)
            .provider(oAuth2UserInfo.getProvider())
            .providerId(providerId)
            .accessToken(userRequest.getAccessToken().getTokenValue())
            .email(oAuth2UserInfo.getEmail())
            .build();
        this.socialLoginRepository.save(socialInfo);
      }
    } else {
      // 소셜 로그인 시켜주기!!
      loginStatus = LoginStatus.LOGIN;
      user = this.userRepository.findById(socialInfo.getUser().getId()).orElse(null);
    }

    if (user == null) {
      user = User.builder().name("tmp").build();
    }
    return new PrincipalDetails(user, oAuth2User.getAttributes(), loginStatus);
  }

  private OAuth2UserInfo getOAuth2UserInfo(OAuth2User oAuth2User, String registrationId) {
    if (SocialLoginProvider.GOOGLE.getValue().equals(registrationId)) {
      return new GoogleUserInfo(oAuth2User.getAttributes());
    } else if (SocialLoginProvider.GITHUB.getValue().equals(registrationId)) {
      return new GitHubUserInfo(oAuth2User.getAttributes());
    } else if (SocialLoginProvider.FT.getValue().equals(registrationId)) {
      return new FortyTwoUserInfo(oAuth2User.getAttributes());
    } else {
      throw new ForbiddenException("지원하지 않는 OAuth 입니다!");
    }
  }
}
