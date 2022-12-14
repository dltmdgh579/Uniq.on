package com.ssafy.uniqon.service.startup.community;

import com.ssafy.uniqon.domain.member.Member;
import com.ssafy.uniqon.domain.startup.Startup;
import com.ssafy.uniqon.domain.startup.community.StartupCommunity;
import com.ssafy.uniqon.dto.startup.community.StartupCommunityRequestDto;
import com.ssafy.uniqon.dto.startup.community.StartupCommunityRequestModifyDto;
import com.ssafy.uniqon.dto.startup.community.StartupCommunityResponseDetailDto;
import com.ssafy.uniqon.dto.startup.community.StartupCommunityResponseListDto;
import com.ssafy.uniqon.exception.ex.CustomException;
import com.ssafy.uniqon.exception.ex.ErrorCode;
import com.ssafy.uniqon.repository.member.MemberRepository;
import com.ssafy.uniqon.repository.startup.StartupRepository;
import com.ssafy.uniqon.repository.startup.community.StartupCommunityRepository;
import com.ssafy.uniqon.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.ssafy.uniqon.exception.ex.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class StartupCommunityService {

    private final StartupCommunityRepository startupCommunityRepository;
    private final MemberRepository memberRepository;
    private final StartupRepository startupRepository;

    public List<StartupCommunityResponseListDto> communityList(Long startupId){
        List<StartupCommunity> communityList = startupCommunityRepository.findAllByStartupId(startupId);
        Startup startup = startupRepository.findById(startupId).orElseThrow(() -> new CustomException(STARTUP_NOT_FOUND));
        List<StartupCommunityResponseListDto> responseDtoList = new ArrayList<>();

        responseDtoList.add(new StartupCommunityResponseListDto(null, startup.getStartupName(), null, null, startup.getDiscordUrl(), null, null, null));

        for(StartupCommunity sc : communityList){
            responseDtoList.add(new StartupCommunityResponseListDto(sc.getId(), sc.getStartup().getStartupName(), sc.getTitle(), sc.getMember().getNickname(), null, sc.getHit(), sc.getCommunityCommentList().size(), sc.getCreatedDate()));
        }

        return responseDtoList;
    }

    public void communityWrite(Long startupId, StartupCommunityRequestDto requestDto){

        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        Startup startup = startupRepository.findById(startupId).orElseThrow(() -> new CustomException(STARTUP_NOT_FOUND));
        StartupCommunity startupCommunity =
                StartupCommunity.createStartupCommunity(requestDto.getTitle(), requestDto.getContent(), member, startup);
        startupCommunityRepository.save(startupCommunity);
    }

    public void communityModify(Long startupId, Long communityId, StartupCommunityRequestModifyDto requestDto){
        StartupCommunity startupCommunity = startupCommunityRepository.findById(communityId).orElseThrow(() -> new CustomException(COMMUNITY_NOT_FOUND));
        if(startupCommunity.getStartup().getId() == startupId){
            startupCommunity.changePost(requestDto.getTitle(), requestDto.getContent());
        } else {
            throw new CustomException(COMMUNITY_NOT_FOUND);
        }
    }

    public void communityDelete(Long startupId, Long communityId){
        StartupCommunity startupCommunity = startupCommunityRepository.findById(communityId).orElseThrow(() -> new CustomException(COMMUNITY_NOT_FOUND));
        if(startupCommunity.getStartup().getId() == startupId){
            startupCommunityRepository.deleteById(communityId);
        } else {
            throw new CustomException(COMMUNITY_NOT_FOUND);
        }
    }

    public StartupCommunityResponseDetailDto communityDetail(Long communityId){
        Long memberId = SecurityUtil.getCurrentMemberId();
        StartupCommunity startupCommunity = startupCommunityRepository.findById(communityId).orElseThrow(() -> new CustomException(COMMUNITY_NOT_FOUND));
        startupCommunity.updateHit();

        StartupCommunityResponseDetailDto detailDto = startupCommunityRepository.findDetail(communityId, memberId).orElseThrow(() -> new CustomException(COMMUNITY_NOT_FOUND));
        detailDto.setCommentsCount(startupCommunity.getCommunityCommentList().size());
        return detailDto;
    }
}
