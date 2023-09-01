package com.kdt.BookVoyage.Member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kdt.BookVoyage.Cart.CartEntity;
import com.kdt.BookVoyage.Common.TimeBaseEntity;
import com.kdt.BookVoyage.Order.OrderEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.List;

@Entity
@Table(name = "MEMBER_INFO")
@Getter
@Setter
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // pk
    @Column(nullable = false)
    private String username; //사용자 이름
    @Column(nullable = false)
    private String password; //사용자 암호
    @Column(nullable = false, unique = true)
    private String userId; //사용자 아이디
    @Column(nullable = false, unique = true)
    private String nickname; //사용자 닉네임
    @Column(nullable = false)
    private String userAddress; //사용자 주소
    @Column(nullable = false)
    private String userDetailAddress; //사용자 상세 주소
    @Column(nullable = false, unique = true)
    private String userEmail; //사용자 이메일
    @Column(nullable = false)
    private String userAge; // 사용자 나이
    @Column(nullable = false)
    private String gender; // 사용자 성별
    @Column(nullable = false)
    private String userTel; // 사용자 전화번호
    @Column
    private String role; // 사용자 권한
    @Column(nullable = false)
    private String deleteFlag;// DB에서 완전 삭제 대신 값이 0일때 비활성화 처리. 추후 계정 복구를 위함
    @Column(nullable = false, unique = true)
    private String userNumber; //회원 고유번호 (난수)
    @OneToOne(mappedBy = "member")
    @ToString.Exclude
    private CartEntity cart;

    @Embedded
    private TimeBaseEntity timeBaseEntity;

    @JsonIgnore
    @OneToMany(mappedBy = "memberEntity",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderEntity> orderEntityList;



    public static MemberEntity DTOToEntity(MemberDTO memberDTO) {
        ModelMapper modelMapper = new ModelMapper();

        try {
            memberDTO.setPassword(BCrypt.hashpw(memberDTO.getPassword(), BCrypt.gensalt()));
        } catch (Exception ignored) {
        }
        memberDTO.setDeleteFlag("N");
        memberDTO.setRole(MemberRole.USER.getRoleName());

        return modelMapper.map(memberDTO, MemberEntity.class);
    }

}
