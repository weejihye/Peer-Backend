package peer.backend.entity.board.recruit;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import peer.backend.entity.BaseEntity;
import peer.backend.entity.composite.RecruitApplicantPK;
import peer.backend.entity.composite.RecruitFavoritePK;
import peer.backend.entity.user.User;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "recruit_favorite")
@IdClass(RecruitFavoritePK.class)
public class RecruitFavorite extends BaseEntity {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "recruit_id")
    private Long recruitId;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("recruitId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_id")
    private Recruit recruit;
}
