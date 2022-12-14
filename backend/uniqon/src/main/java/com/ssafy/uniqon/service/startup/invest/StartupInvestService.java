package com.ssafy.uniqon.service.startup.invest;

import com.ssafy.uniqon.domain.alarm.Alarm;
import com.ssafy.uniqon.domain.invest.InvestStatus;
import com.ssafy.uniqon.domain.invest.Invest_history;
import com.ssafy.uniqon.domain.member.Member;
import com.ssafy.uniqon.domain.startup.Startup;
import com.ssafy.uniqon.exception.ex.CustomException;
import com.ssafy.uniqon.exception.ex.ErrorCode;
import com.ssafy.uniqon.repository.invest.InvestHistoryRepository;
import com.ssafy.uniqon.repository.alarm.AlarmRepository;
import com.ssafy.uniqon.repository.startup.StartupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class StartupInvestService {
    private final StartupRepository startupRepository;
    private final InvestHistoryRepository investHistoryRepository;
    private final AlarmRepository alarmRepository;

    @Transactional
    public void investReserve(Long memberId, Long startupId) {
        Startup startup = startupRepository.findById(startupId).orElseThrow(
                () -> new CustomException(ErrorCode.STARTUP_NOT_FOUND)
        );
        startup.nftReserveCountIncrement();

        // 현재 투자 예약 수가 목표 예약 수 보다 클 경우 목표 달성
        if (!startup.getIsGoal() && startup.getNftReserveCount() >= startup.getNftTargetCount()) {
            startup.changeIsGoal();
        }

        Member member = new Member();
        member.changeId(memberId);

        Invest_history investHistory = Invest_history.builder()
                .member(member)
                .startup(startup)
                .investStatus(InvestStatus.INVESTING)
                .build();
        investHistoryRepository.save(investHistory);
    }

    @Transactional
//    @Scheduled(cron = "*/20 * * * * *")
    @Scheduled(cron = "1 * * * * *")
    public void test() {
        List<Alarm> alarmList = new ArrayList<>();
        List<Startup> startupList = startupRepository.findByInvestingStartupList();
        //List<Startup> startupList = startupRepository.findAll();

        startupList.forEach(
                startup -> {
                    log.info("현재시간 {} startupId {} 마감 시간 {}", LocalDateTime.now(), startup.getId(), startup.getDueDate());
                    if (startup.getDueDate().isBefore(LocalDateTime.now())) { // 마감일 지났을 때
                      //  log.info("현재시간 {} startupId {} 마감 시간 {}", LocalDateTime.now(), startup.getId(), startup.getDueDate());
                        List<Invest_history> investHistoryList = investHistoryRepository.findByInvestingInvestHistoryList(startup.getId());
                        if (startup.getIsGoal() && !startup.getIsFinished()) { // 목표금액 달성 했을 경우
                            startup.changeIsFinish();   // 투자 마감 표시
                            // 알람 생성 (투자자에게 보내는 알람)
                            investHistoryList.forEach(invest_history -> {
                                Member member = new Member();
                                member.changeId(invest_history.getMember().getId());
                                Alarm alarm = Alarm.builder()
                                        .content(startup.getStartupName() + "이 NFT 발행을 진행 중에 있습니다. 잠시만 기다려 주세요!!")
                                        .isRead(Boolean.FALSE)
                                        .startupId(startup.getId())
                                        .member(member)
                                        .build();
                                alarmList.add(alarm);
                            });

                            // 알람 생성 (스타트업에게만 보내는 알람)
                            Member memberStartup = new Member();
                            memberStartup.changeId(startup.getMember().getId());

                            Alarm alarmToStartup = Alarm.builder()
                                    .member(memberStartup)
                                    .isRead(Boolean.FALSE)
                                    .startupId(startup.getId())
                                    .investCount(startup.getNftReserveCount())
                                    .content(startup.getStartupName() + " 투자 유치에 성공했습니다. NFT 토큰을 발급해주세요")
                                    .nftPrice(startup.getNftPrice())
                                    .tokenURI(startup.getTokenUri())
                                    .build();
                            alarmList.add(alarmToStartup);

                        } else if(!startup.getIsGoal() && !startup.getIsFinished()){ // 목표금액 달성하지 못했을 경우
                            startup.changeIsFinish();   // 투자 마감 표시
                            investHistoryList.forEach(invest_history -> {
                                invest_history.changeInvestStatus(InvestStatus.CANCELED);

                                // 알람 생성 (투자자에게 보내는 알람)
                                Member member = new Member();
                                member.changeId(invest_history.getMember().getId());
                                Alarm alarm = Alarm.builder()
                                        .content(startup.getStartupName() + " 투자 예약에 실패했습니다.")
                                        .isRead(Boolean.FALSE)
                                        .startupId(startup.getId())
                                        .member(member)
                                        .build();
                                alarmList.add(alarm);

                            });
                            // 알람 생성(스타트업에게 보내는 알람)
                            Member memberStartup = new Member();
                            memberStartup.changeId(startup.getMember().getId());

                            Alarm alarmToStartup = Alarm.builder()
                                    .member(memberStartup)
                                    .isRead(Boolean.FALSE)
                                    .startupId(startup.getId())
                                    .content(startup.getStartupName() + " 투자 유치에 실패했습니다.")
                                    .build();
                            alarmList.add(alarmToStartup);

                        } // end of else
                    }
                }
        );
        alarmRepository.saveAll(alarmList);
    }

//    @Scheduled(cron = "0 1 * * * * ")   // 매시각 1분에 실행
//    public void startupInvestCheck() {
//        log.info("test");
//    }
}
