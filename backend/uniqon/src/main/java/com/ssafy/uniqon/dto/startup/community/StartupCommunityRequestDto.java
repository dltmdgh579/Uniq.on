package com.ssafy.uniqon.dto.startup.community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartupCommunityRequestDto {
    private String title;
    private String content;
}
