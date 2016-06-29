package com.lsxy.framework.api.tenant.service;

import com.lsxy.framework.api.base.BaseService;
import com.lsxy.framework.api.tenant.model.Account;
import com.lsxy.framework.core.exceptions.MatchMutiEntitiesException;

public interface AccountService extends BaseService<Account> {

    /**
     * 根据登陆用户名查询账号信息
     * @param userLoginName  用户登录名称可以是会员名  email 或手机号
     * @param password 用户密码  使用passwordutils.springpasswordencode方法并使用username作为盐值进行加密的密码
     * @return
     */
    Account findPersonByLoginNameAndPassword(String userLoginName, String password) throws MatchMutiEntitiesException;

    /**
     * 根据用户名获取用户对象
     * @param userName 用户名
     * @return
     */
    Account findByUserName(String userName);



}
