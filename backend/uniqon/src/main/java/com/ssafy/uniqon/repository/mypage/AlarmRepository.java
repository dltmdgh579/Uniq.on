package com.ssafy.uniqon.repository.mypage;

import com.ssafy.uniqon.domain.alarm.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
}
