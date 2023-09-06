package peer.backend.entity.composite;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievementPK implements Serializable {

    private Long userId;
    private Long achievementId;
}
