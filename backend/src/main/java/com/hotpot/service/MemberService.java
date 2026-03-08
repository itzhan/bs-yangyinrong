package com.hotpot.service;

import com.hotpot.common.PageResult;
import com.hotpot.dto.MemberDTO;
import com.hotpot.dto.MemberRechargeDTO;
import com.hotpot.dto.MemberLevelDTO;
import com.hotpot.entity.Member;
import com.hotpot.entity.MemberLevel;
import com.hotpot.entity.MemberRecharge;
import java.util.List;

public interface MemberService {
    PageResult<Member> list(Integer page, Integer size, String keyword, Long levelId);
    Member getById(Long id);
    void create(MemberDTO dto);
    void update(Long id, MemberDTO dto);
    void delete(Long id);
    /** 充值 */
    void recharge(MemberRechargeDTO dto);
    /** 充值记录 */
    PageResult<MemberRecharge> rechargeList(Integer page, Integer size, Long memberId);
    /** 等级列表 */
    List<MemberLevel> levelList();
    /** 等级CRUD */
    void createLevel(MemberLevelDTO dto);
    void updateLevel(Long id, MemberLevelDTO dto);
    void deleteLevel(Long id);
}
