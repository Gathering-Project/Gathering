package nbc_final.gathering.domain.user.controller;

import lombok.RequiredArgsConstructor;
import nbc_final.gathering.domain.user.repository.MemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberRepository memberRepository;

    @PostMapping
    public ResponseEntity createMember(@RequestParam String username) {
        return ResponseEntity.ok(memberRepository.save(new Member(username)));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity getMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(memberRepository.findById(memberId));
    }

    @GetMapping("/members")
    public ResponseEntity getMembers() {
        return ResponseEntity.ok(memberRepository.findAll());
    }
}
