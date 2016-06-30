package com.lsxy.framework.tenant.service;

import com.lsxy.framework.api.tenant.service.AccountService;
import com.lsxy.framework.base.AbstractService;
import com.lsxy.framework.core.exceptions.MatchMutiEntitiesException;
import com.lsxy.framework.api.base.BaseDaoInterface;
import com.lsxy.framework.core.utils.PasswordUtil;
import com.lsxy.framework.tenant.dao.AccountDao;
import com.lsxy.framework.api.tenant.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Created by Tandy on 2016/6/24.
 */
@Service
public class AccountServiceImpl extends AbstractService<Account> implements AccountService {

    @Autowired
    private AccountDao accountDao;

    @Override
    public BaseDaoInterface<Account, Serializable> getDao() {
        return accountDao;
    }


    @Override
    public Account findPersonByLoginNameAndPassword(String userLoginName, String password) throws MatchMutiEntitiesException {
        String hql = "from Account obj where obj.userName=?1 or obj.email=?2 or obj.mobile=?3";
        Account account = this.findUnique(hql, userLoginName,userLoginName,userLoginName);
        if(account != null) {
            if(!account.getPassword().equals(PasswordUtil.springSecurityPasswordEncode(password,account.getUserName()))){
                 account = null;
            }
        }
        return account;
    }
}