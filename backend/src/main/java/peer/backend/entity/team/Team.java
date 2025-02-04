package peer.backend.entity.team;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import peer.backend.dto.team.TeamSettingInfoDto;
import peer.backend.dto.team.UpdateTeamRequest;
import peer.backend.entity.BaseEntity;
import peer.backend.entity.team.enums.*;
import peer.backend.entity.user.InterestedProject;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@Table(name = "team")
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TeamType type;

    @Column(length = 30, nullable = false)
    String dueTo;

    @Column()
    String teamPicturePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TeamOperationFormat operationFormat;

    @Column()
    String teamLogoPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TeamStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TeamMemberStatus teamMemberStatus;

    @Column(nullable = false)
    Boolean isLock;

    @Column()
    Integer maxMember;

    @Column(length = 10, nullable = false)
    String region1;

    @Column(length = 10, nullable = false)
    String region2;

    @Column(length = 10, nullable = false)
    String region3;

    @OneToMany(mappedBy = "team", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<TeamUser> teamUsers = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<InterestedProject> interestedProjects = new ArrayList<>();

    public void update(UpdateTeamRequest request) {
        this.name = request.getName();
        this.type = request.getType();
        this.dueTo = request.getDueTo();
        this.teamPicturePath = request.getTeamPicturePath();
        this.operationFormat = request.getOperationFormat();
        this.teamLogoPath = request.getTeamLogoPath();
        this.status = request.getStatus();
        this.teamMemberStatus = request.getTeamMemberStatus();
        this.isLock = request.getIsLock();
        this.maxMember = request.getMaxMember();
        this.region1 = request.getRegion1();
        this.region2 = request.getRegion2();
        this.region3 = request.getRegion3();
    }

    public void update(TeamSettingInfoDto teamSettingInfoDto) {
        this.name = teamSettingInfoDto.getName();
        this.dueTo = teamSettingInfoDto.getDueTo();
        this.status = TeamStatus.from(teamSettingInfoDto.getStatus());
        String[] regions = teamSettingInfoDto.getRegion();
        this.region1 = regions.length > 0 ? regions[0] : "";
        this.region2 = regions.length > 1 ? regions[1] : "";
        this.region3 = regions.length > 2 ? regions[2] : "";
        this.operationFormat = TeamOperationFormat.from(teamSettingInfoDto.getOperationForm());
        this.maxMember = Integer.valueOf(teamSettingInfoDto.getMaxMember());
    }

    public boolean deleteTeamUser(Long deletingToUserId) {
        return this.teamUsers.removeIf(teamUser -> teamUser.getUserId().equals(deletingToUserId));
    }

    public void grantLeaderPermission(Long grantingUserId, TeamUserRoleType teamUserRoleType) {
        for (TeamUser teamUser: this.teamUsers) {
            if (teamUser.getUserId().equals(grantingUserId)) {
                teamUser.grantLeader(teamUserRoleType);
            }
        }
    }
}
