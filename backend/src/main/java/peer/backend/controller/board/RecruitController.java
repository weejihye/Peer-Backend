package peer.backend.controller.board;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import peer.backend.annotation.AuthorCheck;
import peer.backend.dto.Board.Recruit.RecruitUpdateRequestDTO;
import peer.backend.dto.board.recruit.*;
import peer.backend.entity.user.User;
import peer.backend.exception.NotFoundException;
import peer.backend.repository.user.UserRepository;
import peer.backend.service.board.recruit.RecruitService;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recruit")
public class RecruitController {
    private final RecruitService recruitService;
    private final UserRepository userRepository;

    @ApiOperation(value = "", notes = "특정 모집게시글을 불러온다.")
    @GetMapping("/{recruit_id}")
    public RecruitResponce getRecruit(@PathVariable Long recruit_id){
        return  recruitService.getRecruit(recruit_id);
    }

    @ApiOperation(value = "", notes = "수정을 위한 모집글 정보를 불러온다.")
    @GetMapping("/modify/{recruit_id}")
    @AuthorCheck(id = "{recruit_id}")
    public RecruitResponce getRecruitForModify(@PathVariable Long recruit_id, Authentication auth){
        return  recruitService.getRecruit(recruit_id);
    }

    @ApiOperation(value = "", notes = "모집글 작성을 위한 태그리스트를 작성한다.")
    @GetMapping("/write")
    public List<TagResponce> getTagList(){
        List<TagResponce> result = new ArrayList<>();
        result.add(new TagResponce("Java", "#9AFE2E"));
        result.add(new TagResponce("JavaScript", "#045FB4"));
        result.add(new TagResponce("React", "#FF8000"));
        result.add(new TagResponce("SpringBoot", "#FE2EC8"));
        return result;
    }

    @ApiOperation(value = "", notes = "조건에 따라 list를 반환한다.")
    @GetMapping("")
    public Page<RecruitListResponse> getRecruitListByConditions(@RequestParam int page, @RequestParam int pageSize, @ModelAttribute("request") RecruitRequest request, Principal principal) {
        User user = userRepository.findByName(principal.getName()).orElseThrow(() -> new NotFoundException("존재하지 않는 유저입니다."));
        Pageable pageable = PageRequest.of(page, pageSize);
        return recruitService.getRecruitSearchList(pageable, request, user.getId());
    }

    @ApiOperation(value = "", notes = "모집글과 팀을 함께 생성한다.")
    @PostMapping("")
    public void createRecruit(@RequestBody RecruitListRequestDTO recruitListRequestDTO) throws IOException{
        recruitService.createRecruit(recruitListRequestDTO);
    }

    @ApiOperation(value = "", notes = "모집글을 업데이트 한다. 팀도 함께 업데이트 한다.")
    @PutMapping("/{recruit_id}")
    public void updateRecruit(@PathVariable Long recruit_id, @RequestBody RecruitUpdateRequestDTO recruitUpdateRequestDTO, Principal principal) throws IOException {
        //TODO:principal로 권한검사
        recruitService.updateRecruit(recruit_id, recruitUpdateRequestDTO);
    }

    @ApiOperation(value = "", notes = "모집글을 삭제한다.")
    @DeleteMapping("/{recruit_id}")
    public void deleteRecruit(@PathVariable Long recruit_id){
        recruitService.deleteRecruit(recruit_id);
    }

    @ApiOperation(value = "", notes = "모집에 지원한다.")
    @PostMapping("/interview/{recruit_id}")
    public void applyRecruit(@PathVariable Long recruit_id, @RequestBody ApplyRecruitRequest request){
        recruitService.applyRecruit(recruit_id, request);
    }
    @PostMapping("/favorite/{recruit_id}")
    public void goFavorite(@PathVariable Long recruit_id, Principal principal){
        User user = userRepository.findByName(principal.getName()).orElseThrow( () -> new NotFoundException("존재하지 않는 유저입니다."));
        recruitService.changeRecruitFavorite(user.getId(), recruit_id );
    }
}
