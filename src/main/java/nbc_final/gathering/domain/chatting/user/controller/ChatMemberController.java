package nbc_final.gathering.domain.chatting.user.controller;

import lombok.RequiredArgsConstructor;

import nbc_final.gathering.domain.chatting.user.entity.ChatMember;


import nbc_final.gathering.domain.chatting.user.repository.ChatMemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class ChatMemberController {

    private final ChatMemberRepository memberRepository;

    @PostMapping
    public ResponseEntity createMember(@RequestParam String username) {
        return ResponseEntity.ok(memberRepository.save(new ChatMember(username)));
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
