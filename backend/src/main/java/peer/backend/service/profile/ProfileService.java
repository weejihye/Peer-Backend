package peer.backend.service.profile;

import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import peer.backend.dto.profile.request.EditProfileRequest;
import peer.backend.dto.profile.response.MyProfileResponse;
import peer.backend.dto.profile.request.UserLinkDTO;
import peer.backend.dto.profile.response.OtherProfileDto;
import peer.backend.entity.user.User;
import peer.backend.entity.user.UserLink;
import peer.backend.exception.BadRequestException;
import peer.backend.exception.NotFoundException;
import peer.backend.repository.user.UserLinkRepository;
import peer.backend.repository.user.UserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final UserLinkRepository userLinkRepository;
    private final Tika tika;

    private void deleteUserImage(User user) throws IOException {
        String imagePath = user.getImageUrl();
        if (imagePath == null) {
            return;
        }
        imagePath = imagePath.substring(7);
        File file = new File(imagePath);
        if (!file.exists()) {
            user.setImageUrl(null);
            return;
        }
        else if (!file.delete()) {
            throw new IOException("파일 삭제에 실패했습니다.");
        }
        user.setImageUrl(null);
    }

    private Path saveImageFilePath(User user, MultipartFile file) throws IOException {
        String fileType = tika.detect(file.getInputStream());
        if (!fileType.startsWith("image")) {
            throw new IllegalArgumentException("image 타입이 아닙니다.");
        }
        StringBuilder builder = new StringBuilder();
        String folderPath = builder
                .append("upload")
                .append(File.separator)
                .append("profiles")
                .append(File.separator)
                .append(user.getId().toString())
                .toString();
        File folder = new File(folderPath);
        if (!folder.mkdirs()) {
            if (!folder.exists()) {
                throw new IOException("폴더 생성에 실패했습니다.");
            }
        }
        String originalName = file.getOriginalFilename();
        assert originalName != null;
        String filePath = builder
                .append(File.separator)
                .append("profile")
                .append(originalName.substring(originalName.lastIndexOf(".")))
                .toString();
        Path path = Paths.get(filePath);
        file.transferTo(path.toFile());
        return path;
    }

    @Transactional(readOnly = true)
    public MyProfileResponse getProfile(String name) {
        User user = userRepository.findByName(name).orElseThrow(
                () -> new NotFoundException("사용자를 찾을 수 없습니다.")
        );
        List<UserLinkDTO> links = new ArrayList<>();
        for (UserLink link : user.getUserLinks()) {
            UserLinkDTO userLink = UserLinkDTO.builder()
                    .linkName(link.getLinkName())
                    .linkUrl(link.getLinkUrl())
                    .build();
            links.add(userLink);
        }
        return MyProfileResponse.builder()
                .profileImageUrl(user.getImageUrl())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .company(user.getCompany())
                .introduction(user.getIntroduce())
                .linkList(links)
                .build();
    }

    @Transactional(readOnly = true)
    public boolean isExistNickname(String nickname) {
        return userRepository.findByNickname(nickname).isPresent();
    }

    @Transactional
    public void editLinks(String name, List<UserLinkDTO> links) {
        User user = userRepository.findByName(name).orElseThrow(
                () -> new NotFoundException("사용자를 찾을 수 없습니다.")
        );
        userLinkRepository.deleteAll(user.getUserLinks());
        List<UserLink> newLink = user.getUserLinks();
        newLink.clear();
        for (UserLinkDTO link : links) {
            UserLink userLink = UserLink.builder()
                    .user(user)
                    .linkName(link.getLinkName())
                    .linkUrl(link.getLinkUrl())
                    .build();
            newLink.add(userLink);
        }
        for (int index = newLink.size() - 1; index >= 0; index--) {
            userLinkRepository.save(newLink.get(index));
        }
        user.setUserLinks(newLink);
        userRepository.save(user);
    }

    @Transactional
    public OtherProfileDto getOtherProfile(Long userId, List<String> infoList) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("사용자를 찾을 수 없습니다.")
        );
        OtherProfileDto profile = new OtherProfileDto();
        for (String info : infoList) {
            switch (info) {
                case "nickname":
                    profile.setNickname(user.getNickname());
                    break;
                case "profileImageUrl":
                    profile.setProfileImageUrl(user.getImageUrl());
                    break;
                default:
                    throw new BadRequestException("잘못된 요청입니다.");
            }
        }
        return (profile);
    }

    @Transactional
    public void editProfile(String name, EditProfileRequest profile) throws IOException {
        User user = userRepository.findByName(name).orElseThrow(
                () -> new NotFoundException("사용자가 존재하지 않습니다.")
        );
        if (profile.getProfileImage() == null && profile.isImageChange()) {
            deleteUserImage(user);
        }
        else if (profile.getProfileImage() != null) {
            deleteUserImage(user);
            user.setImageUrl(
                    saveImageFilePath(user, profile.getProfileImage()).toUri().toString()
            );
        }
        user.setNickname(profile.getNickname());
        user.setIntroduce(profile.getIntroduction());
        userRepository.save(user);
    }
}
