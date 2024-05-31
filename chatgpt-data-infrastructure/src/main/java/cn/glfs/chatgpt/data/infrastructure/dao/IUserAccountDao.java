package cn.glfs.chatgpt.data.infrastructure.dao;


import cn.glfs.chatgpt.data.infrastructure.po.UserAccountPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 用户账户DAO
 * @create 2023-10-03 16:39
 */
@Mapper
public interface IUserAccountDao {

    /**
     * 根据用户id,有剩余额度并且用户可用将用户额度-1
     * @param openid
     * @return
     */
    int subAccountQuota(String openid);

    /**
     * 根据用户id查询用户信息
     * @param openid
     * @return
     */
    UserAccountPO queryUserAccount(String openid);

    void insert(UserAccountPO userAccountPOReq);

    /**
     * 根据用户id更新剩余额度和总额度和更新时间
     * @param userAccountPOReq
     * @return
     */
    int addAccountQuota(UserAccountPO userAccountPOReq);
}
