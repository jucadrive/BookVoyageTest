package com.finalproject.Security;

import com.finalproject.Common.CookieConfig;
import com.finalproject.Common.TokenNotValidateException;
import com.finalproject.Member.MemberDTO;
import com.finalproject.Member.MemberEntity;
import com.finalproject.Member.MemberRepository;
import com.finalproject.Member.MemberRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final TokenConfig tokenConfig;
    private final CookieConfig cookieConfig;
    private final ModelMapper modelMapper;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final Cookie[] cookies = request.getCookies();

        String accessToken = "";
        String refreshToken = "";

        try{
            for (Cookie cookie : cookies) {
                String cookieName = cookie.getName();

                switch (cookieName) {
                    case "accessToken" -> accessToken = cookie.getValue();
                    case "refreshToken" -> refreshToken = cookie.getValue();
                }
            }
        }catch (Exception ignore){}


        boolean validateAccessToken = tokenConfig.validateAccessToken(accessToken);
        boolean validateRefreshToken = tokenConfig.validateRefreshToken(refreshToken);


        if (validateAccessToken && validateRefreshToken) { //두 토큰이 인증될 때

            log.info("엑세스 토큰과 리프레쉬 토큰 둘 다 인증되었습니다.");

            String map = modelMapper.map(MemberRole.USER, String.class); // USER

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("", "", List.of(new SimpleGrantedAuthority(map)));

            token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(token);


        }
        else if(!validateAccessToken && validateRefreshToken){ //엑세스는 인증되지 않고 리프레쉬만 인증될 때

            log.info("엑세스 토큰은 인증되지 않고 리프레쉬 토큰만 인증되었습니다.");

            TokenDecoder decoder = new TokenDecoder();
            String userNumber = decoder.refreshTokenDecoder(refreshToken, "userNumber");

            Optional<MemberEntity> allByUserNumber = memberRepository.findAllByUserNumber(userNumber);

            if(allByUserNumber.isPresent()){

                MemberDTO map = modelMapper.map(allByUserNumber, MemberDTO.class);
                TokenDTO tokenDTO = tokenConfig.generateAccessToken(map);
                Cookie regeneratedAccessToken = cookieConfig.setCookie(tokenDTO.getAccessToken(), "accessToken", false, "/", 3600);

                response.addCookie(regeneratedAccessToken);

            }


        }
        else if(validateAccessToken && !validateRefreshToken){ // 리프레쉬는 인증되지 않고 엑세스만 인증될 때
            log.info("리프레쉬 토큰은 인증되지 않고 엑세스 토큰만 인증되었습니다.");

            TokenDecoder decoder = new TokenDecoder();
            String userNumber = decoder.accessTokenDecoder(accessToken, "userNumber");

            Optional<MemberEntity> allByUserNumber = memberRepository.findAllByUserNumber(userNumber);

            if(allByUserNumber.isPresent()){

                MemberDTO map = modelMapper.map(allByUserNumber, MemberDTO.class);
                TokenDTO tokenDTO = tokenConfig.generateRefreshToken(map);
                Cookie regeneratedRefreshToken = cookieConfig.setCookie(tokenDTO.getRefreshToken(), "refreshToken", true, "/", 7 * 24 * 3600);

                response.addCookie(regeneratedRefreshToken);

            }

        }
        else{
            log.error("두 토큰이 인증되지 않았습니다.");
        }

        filterChain.doFilter(request, response);

    }

}
