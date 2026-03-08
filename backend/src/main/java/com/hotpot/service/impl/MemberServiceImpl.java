package com.hotpot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotpot.common.BusinessException;
import com.hotpot.common.PageResult;
import com.hotpot.common.SecurityUtils;
import com.hotpot.dto.MemberDTO;
import com.hotpot.dto.MemberLevelDTO;
import com.hotpot.dto.MemberRechargeDTO;
import com.hotpot.entity.Member;
import com.hotpot.entity.MemberLevel;
import com.hotpot.entity.MemberRecharge;
import com.hotpot.entity.SysEmployee;
import com.hotpot.mapper.MemberLevelMapper;
import com.hotpot.mapper.MemberMapper;
import com.hotpot.mapper.MemberRechargeMapper;
import com.hotpot.mapper.SysEmployeeMapper;
import com.hotpot.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final MemberLevelMapper levelMapper;
    private final MemberRechargeMapper rechargeMapper;
    private final SysEmployeeMapper employeeMapper;

    @Override
    public PageResult<Member> list(Integer page, Integer size, String keyword, Long levelId) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Member::getName, keyword)
                    .or().like(Member::getPhone, keyword)
                    .or().like(Member::getMemberNo, keyword));
        }
        if (levelId != null) {
            wrapper.eq(Member::getLevelId, levelId);
        }
        wrapper.orderByDesc(Member::getCreatedAt);
        Page<Member> result = memberMapper.selectPage(new Page<>(page, size), wrapper);

        // 填充等级名称
        List<MemberLevel> levels = levelMapper.selectList(null);
        Map<Long, String> levelMap = levels.stream()
                .collect(Collectors.toMap(MemberLevel::getId, MemberLevel::getName));
        result.getRecords().forEach(m -> m.setLevelName(levelMap.get(m.getLevelId())));

        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Member getById(Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null) throw new BusinessException(404, "会员不存在");
        MemberLevel level = levelMapper.selectById(member.getLevelId());
        if (level != null) member.setLevelName(level.getName());
        return member;
    }

    @Override
    public void create(MemberDTO dto) {
        Long count = memberMapper.selectCount(
                new LambdaQueryWrapper<Member>().eq(Member::getPhone, dto.getPhone()));
        if (count > 0) throw new BusinessException("手机号已注册");

        Member member = new Member();
        BeanUtils.copyProperties(dto, member);
        member.setMemberNo(generateMemberNo());
        member.setLevelId(1L);
        member.setBalance(BigDecimal.ZERO);
        member.setPoints(0);
        member.setTotalConsumption(BigDecimal.ZERO);
        member.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        memberMapper.insert(member);
    }

    @Override
    public void update(Long id, MemberDTO dto) {
        Member member = memberMapper.selectById(id);
        if (member == null) throw new BusinessException(404, "会员不存在");
        if (!member.getPhone().equals(dto.getPhone())) {
            Long count = memberMapper.selectCount(
                    new LambdaQueryWrapper<Member>().eq(Member::getPhone, dto.getPhone()));
            if (count > 0) throw new BusinessException("手机号已注册");
        }
        BeanUtils.copyProperties(dto, member);
        memberMapper.updateById(member);
    }

    @Override
    public void delete(Long id) {
        memberMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void recharge(MemberRechargeDTO dto) {
        Member member = memberMapper.selectById(dto.getMemberId());
        if (member == null) throw new BusinessException("会员不存在");

        BigDecimal giftAmount = dto.getGiftAmount() != null ? dto.getGiftAmount() : BigDecimal.ZERO;
        BigDecimal totalAdd = dto.getAmount().add(giftAmount);
        member.setBalance(member.getBalance().add(totalAdd));
        memberMapper.updateById(member);

        MemberRecharge recharge = new MemberRecharge();
        recharge.setMemberId(member.getId());
        recharge.setAmount(dto.getAmount());
        recharge.setGiftAmount(giftAmount);
        recharge.setBalanceAfter(member.getBalance());
        recharge.setPaymentMethod(dto.getPaymentMethod());
        recharge.setOperatorId(SecurityUtils.getCurrentUserId());
        rechargeMapper.insert(recharge);
    }

    @Override
    public PageResult<MemberRecharge> rechargeList(Integer page, Integer size, Long memberId) {
        LambdaQueryWrapper<MemberRecharge> wrapper = new LambdaQueryWrapper<>();
        if (memberId != null) {
            wrapper.eq(MemberRecharge::getMemberId, memberId);
        }
        wrapper.orderByDesc(MemberRecharge::getCreatedAt);
        Page<MemberRecharge> result = rechargeMapper.selectPage(new Page<>(page, size), wrapper);

        List<Member> members = memberMapper.selectList(null);
        Map<Long, String> memberMap = members.stream()
                .collect(Collectors.toMap(Member::getId, Member::getName));
        List<SysEmployee> employees = employeeMapper.selectList(null);
        Map<Long, String> empMap = employees.stream()
                .collect(Collectors.toMap(SysEmployee::getId, SysEmployee::getRealName));

        result.getRecords().forEach(r -> {
            r.setMemberName(memberMap.get(r.getMemberId()));
            if (r.getOperatorId() != null) r.setOperatorName(empMap.get(r.getOperatorId()));
        });

        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public List<MemberLevel> levelList() {
        return levelMapper.selectList(new LambdaQueryWrapper<MemberLevel>().orderByAsc(MemberLevel::getMinPoints));
    }

    @Override
    public void createLevel(MemberLevelDTO dto) {
        MemberLevel level = new MemberLevel();
        BeanUtils.copyProperties(dto, level);
        levelMapper.insert(level);
    }

    @Override
    public void updateLevel(Long id, MemberLevelDTO dto) {
        MemberLevel level = levelMapper.selectById(id);
        if (level == null) throw new BusinessException(404, "等级不存在");
        BeanUtils.copyProperties(dto, level);
        levelMapper.updateById(level);
    }

    @Override
    public void deleteLevel(Long id) {
        Long count = memberMapper.selectCount(
                new LambdaQueryWrapper<Member>().eq(Member::getLevelId, id));
        if (count > 0) throw new BusinessException("该等级下还有会员，无法删除");
        levelMapper.deleteById(id);
    }

    private String generateMemberNo() {
        return "M" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%04d", (int) (Math.random() * 10000));
    }
}
