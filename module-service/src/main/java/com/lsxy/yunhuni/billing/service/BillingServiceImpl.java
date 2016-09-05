package com.lsxy.yunhuni.billing.service;

import com.lsxy.framework.api.base.BaseDaoInterface;
import com.lsxy.framework.api.tenant.model.Tenant;
import com.lsxy.framework.api.tenant.service.TenantService;
import com.lsxy.framework.base.AbstractService;
import com.lsxy.framework.cache.manager.RedisCacheService;
import com.lsxy.framework.core.exceptions.MatchMutiEntitiesException;
import com.lsxy.framework.core.utils.DateUtils;
import com.lsxy.yunhuni.api.billing.model.Billing;
import com.lsxy.yunhuni.api.billing.service.BillingService;
import com.lsxy.yunhuni.billing.dao.BillingDao;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import static com.lsxy.yunhuni.api.billing.service.CalBillingService.AMOUNT_REDIS_MULTIPLE;
import static com.lsxy.yunhuni.api.billing.service.CalBillingService.USE_BALANCE_PREFIX;
import static javafx.scene.input.KeyCode.T;

/**
 * Created by liups on 2016/6/28.
 */
@Service
public class BillingServiceImpl extends AbstractService<Billing> implements BillingService {

    @Autowired
    private BillingDao billingDao;

    @Autowired
    private TenantService tenantService;


    @Override
    public BaseDaoInterface<Billing, Serializable> getDao() {
        return billingDao;
    }
    @Override
    public Billing findBillingByUserName(String username){
        Billing billing = null;
        Tenant tenant = tenantService.findTenantByUserName(username);
        if(tenant != null){
            billing = findBillingByTenantId(tenant.getId());
        }
        return billing;
    }

    @Override
    @Cacheable(value="billing",key = "'billing_'+#tenantId" ,unless="#result == null")
    public Billing findBillingByTenantId(String tenantId) {
        String hql = "from Billing obj where obj.tenant.id=?1";
        try {
            return this.findUnique(hql,tenantId);
        } catch (MatchMutiEntitiesException e) {
            e.printStackTrace();
            throw new RuntimeException("存在多个对应账务信息");
        }
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "entity", key = "'entity_' + #entity.id", beforeInvocation = true),
                    @CacheEvict(value = "billing", key = "'billing_' + #entity.tenant.id", beforeInvocation = true)
            },
            put = {
                    @CachePut(value = "entity", key = "'entity_' + #entity.id",unless = "#entity == null"),
                    @CachePut(value = "billing", key = "'billing_' + #entity.tenant.id",unless = "#entity == null"),
            }
    )
    @Override
    public Billing save(Billing entity) {
        return getDao().save(entity);
    }
}
