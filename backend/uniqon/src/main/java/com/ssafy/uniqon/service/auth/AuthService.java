package com.ssafy.uniqon.service.auth;

import com.ssafy.uniqon.config.jwt.TokenProvider;
import com.ssafy.uniqon.domain.member.Member;
import com.ssafy.uniqon.dto.member.MemberJoinDto;
import com.ssafy.uniqon.dto.member.MemberLoginDto;
import com.ssafy.uniqon.dto.member.MetaMaskLoginDto;
import com.ssafy.uniqon.dto.token.TokenDto;
import com.ssafy.uniqon.dto.token.TokenRequestDto;
import com.ssafy.uniqon.exception.ex.CustomException;
import com.ssafy.uniqon.repository.member.MemberRepository;
import com.ssafy.uniqon.service.s3.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ssafy.uniqon.exception.ex.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    private final AwsS3Service awsS3Service;

    @Transactional
    public void signup(MemberJoinDto memberJoinDto){
        if(memberRepository.existsByEmail(memberJoinDto.getEmail())){
            throw new CustomException(ALREADY_SAVED_MEMBER);
        }

        Member member = memberJoinDto.toMember();
        member.changeProfileImage(getRandomImage());
        memberRepository.save(member);
    }

//    @Transactional
//    public TokenDto login(MemberLoginDto memberLoginDto) throws RuntimeException{
//
//        // 1. Login ID/PW ??? ???????????? AuthenticationToken ??????
//        UsernamePasswordAuthenticationToken authenticationToken = memberLoginDto.toAuthentication();
//
//        // 2. ????????? ?????? (????????? ???????????? ??????) ??? ??????????????? ??????
//        //    authenticate ???????????? ????????? ??? ??? CustomUserDetailsService ?????? ???????????? loadUserByUsername ???????????? ?????????
//        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
//
//        // 3. ?????? ????????? ???????????? JWT ?????? ??????
//        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);
//        // 5. ?????? ??????
//        return tokenDto;
//    }

    @Transactional
    public TokenDto metaMasklogin(String walletAddress) throws RuntimeException{

        MetaMaskLoginDto metaMaskLoginDto = new MetaMaskLoginDto(walletAddress, "");

        // 1. Login ID/PW ??? ???????????? AuthenticationToken ??????
        UsernamePasswordAuthenticationToken authenticationToken = metaMaskLoginDto.toAuthentication();

        // AuthenticationManager ??? token ??? ????????? UserDetailsService ??? ?????? ??????????????? ??????.
//        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. ?????? ????????? ???????????? JWT ?????? ??????
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);
        // 5. ?????? ??????
        return tokenDto;
    }

    @Transactional
    public TokenDto reissue(TokenRequestDto tokenRequestDto) {
        // 1. Refresh Token ??????
        if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new CustomException(INVALID_REFRESH_TOKEN);
        }
        // 2. Access Token ?????? Member ID ????????????
        Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken());

        //redis??? ?????? refreshtoken ??????
        tokenProvider.checkRefreshToken(authentication.getName(), tokenRequestDto.getRefreshToken());

        // 5. ????????? ?????? ??????
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        // ?????? ??????
        return tokenDto;
    }

    @Transactional
    public void logout(String accessToken) {
        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        tokenProvider.logout(authentication.getName(), accessToken);
    }

    public String getRandomImage() {
        int random = (int) (Math.random() * 6) + 1;
        String path = "member/default/" + random + ".jpg";
        String thumbnailPath = awsS3Service.getThumbnailPath(path);
        return thumbnailPath;
    }

}
