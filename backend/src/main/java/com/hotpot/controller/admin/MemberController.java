package com.hotpot.controller.admin;

import com.hotpot.common.PageResult;
import com.hotpot.common.Result;
import com.hotpot.dto.MemberDTO;
import com.hotpot.dto.MemberLevelDTO;
import com.hotpot.dto.MemberRechargeDTO;
import com.hotpot.entity.Member;
import com.hotpot.entity.MemberLevel;
import com.hotpot.entity.MemberRecharge;
import com.hotpot.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public Result<PageResult<Member>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long levelId) {
        return Result.success(memberService.list(page, size, keyword, levelId));
    }

    @GetMapping("/{id}")
    public Result<Member> getById(@PathVariable Long id) {
        return Result.success(memberService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody MemberDTO dto) {
        memberService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MemberDTO dto) {
        memberService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return Result.success();
    }

    @PostMapping("/recharge")
    public Result<Void> recharge(@Valid @RequestBody MemberRechargeDTO dto) {
        memberService.recharge(dto);
        return Result.success();
    }

    @GetMapping("/recharges")
    public Result<PageResult<MemberRecharge>> rechargeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long memberId) {
        return Result.success(memberService.rechargeList(page, size, memberId));
    }

    @GetMapping("/levels")
    public Result<List<MemberLevel>> levelList() {
        return Result.success(memberService.levelList());
    }

    @PostMapping("/levels")
    public Result<Void> createLevel(@Valid @RequestBody MemberLevelDTO dto) {
        memberService.createLevel(dto);
        return Result.success();
    }

    @PutMapping("/levels/{id}")
    public Result<Void> updateLevel(@PathVariable Long id, @Valid @RequestBody MemberLevelDTO dto) {
        memberService.updateLevel(id, dto);
        return Result.success();
    }

    @DeleteMapping("/levels/{id}")
    public Result<Void> deleteLevel(@PathVariable Long id) {
        memberService.deleteLevel(id);
        return Result.success();
    }
}
