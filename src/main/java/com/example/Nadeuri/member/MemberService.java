package com.example.Nadeuri.member;

import com.example.Nadeuri.board.ImageRepository;
import com.example.Nadeuri.member.dto.MemberDTO;
import com.example.Nadeuri.member.dto.request.MemberUpdateRequest;
import com.example.Nadeuri.member.dto.request.SignupDTO;
import com.example.Nadeuri.member.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageRepository imageRepository;

    @Value("${file.local.upload.path}")
    private String uploadPath;

    public MemberDTO read(String userId, String password) {
        //userId와 password를 매개변수로 넘겨 받아 -------------------------------
        Optional<MemberEntity> foundMember= memberRepository.findByUserId(userId);
        MemberEntity member = foundMember.orElseThrow(MemberException.BAD_CREDENTIALS::get);  //데이터베이스에 존재하지 않는 경우 MemberTaskException의 BAD_CREDENTIALS 예외를 발생시키고

        if(!passwordEncoder.matches(password, member.getPassword())) {   //password가 데이터베이스의 값과 일치하지 않는 경우
            throw MemberException.BAD_CREDENTIALS.get();
        }

        return new MemberDTO(member);   //그렇지 않으면 넘겨받은 엔티티를 DTO 객체로 반환
    }

    public MemberDTO read(String userId) {
        //userId를 매개변수로 넘겨 받아 -------------------------------
        Optional<MemberEntity> foundMember= memberRepository.findByUserId(userId);
        MemberEntity member = foundMember.orElseThrow(MemberException.BAD_CREDENTIALS::get);  //데이터베이스에 존재하지 않는 경우 MemberTaskException의 BAD_CREDENTIALS 예외를 발생시키고

        return new MemberDTO(member);   //그렇지 않으면 넘겨받은 엔티티를 DTO 객체로 반환
    }

    //회원 가입
    public void signUp(SignupDTO signupDTO, final MultipartFile profileImage) {
        if (memberRepository.existsByUserId(signupDTO.getUserId())) {
            throw MemberException.DUPLICATE.get();
//            throw new DuplicateMemberException("이미 사용 중인 사용자 이름입니다.");
        }

        String imageUrl;

        if (profileImage == null || profileImage.isEmpty()) {
            imageUrl = uploadPath + "/defaultImage.png";
        } else {
            imageUrl = imageRepository.upload(profileImage);
        }
        // Password 암호화
        String encodedPassword = passwordEncoder.encode(signupDTO.getPassword());
        Role role = Role.USER;

        memberRepository.save(signupDTO.toEntity(encodedPassword, role, imageUrl));
    }

    public void updateMember(String userId, MemberUpdateRequest request, MultipartFile profileImage) {

        MemberEntity memberEntity = memberRepository.findByUserId(userId)
                .orElseThrow(() -> MemberException.NOT_FOUND.get());


        String imageUrl = (profileImage != null && !profileImage.isEmpty())
                ? imageRepository.upload(profileImage)
                : uploadPath + "/defaultImage.png";

        memberEntity.changeProfileImage(imageUrl);


        if (request.getRole() != null) {
            memberEntity.changeRole(request.getRole());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            memberEntity.changePassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            memberEntity.changeEmail(request.getEmail());
        }
        if (request.getName() != null && !request.getName().isEmpty()) {
            memberEntity.changeName(request.getName());
        }

        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            memberEntity.changeNickname(request.getNickname());
        }

        memberRepository.save(memberEntity);

    }

    public void deleteMember(String userId) {
        MemberEntity memberEntity = memberRepository.findByUserId(userId)
                .orElseThrow(() -> MemberException.NOT_FOUND.get());

        memberRepository.delete(memberEntity);

    }

    public MemberEntity findByEmail(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        return member; // MemberEntity를 MemberDTO로 변환하여 반환
    }

    public MemberEntity findByUserId(String userId) {
        MemberEntity member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("UserId not found: " + userId));
        return member; // MemberEntity를 MemberDTO로 변환하여 반환
    }

}
