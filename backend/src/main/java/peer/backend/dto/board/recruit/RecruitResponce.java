package peer.backend.dto.board.recruit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import peer.backend.entity.board.recruit.RecruitInterview;
import peer.backend.entity.board.recruit.RecruitRole;
import peer.backend.entity.board.recruit.enums.RecruitStatus;
import peer.backend.entity.user.enums.Role;

import javax.swing.*;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitResponce {
    private String title;
    private String name;
    private String type;
    private int totalNumber;
    private RecruitStatus status;
    private String due;
    private String content;
    private Long leader_id;
    private String region;
    private String link;
    private String leader_nickname;
    private String leader_image;
    private List<String> tagList;
    private List<RecruitRoleDTO> roleList;
    private List<RecruitInterviewDto> interviewsList;
}
