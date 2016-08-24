package com.lsxy.framework.consume.service;

import com.lsxy.framework.api.base.BaseDaoInterface;
import com.lsxy.framework.api.consume.model.Consume;
import com.lsxy.framework.api.consume.service.ConsumeService;
import com.lsxy.framework.api.tenant.model.Tenant;
import com.lsxy.framework.api.tenant.service.TenantService;
import com.lsxy.framework.base.AbstractService;
import com.lsxy.framework.consume.dao.ConsumeDao;
import com.lsxy.framework.core.utils.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * 消费记录ServiceImpl
 * Created by zhangxb on 2016/7/8.
 */
@Service
public class ConsumeServiceImpl extends AbstractService<Consume> implements ConsumeService {
    @Autowired
    ConsumeDao consumeDao;
    @Autowired
    TenantService tenantService;
    @Autowired
    EntityManager em;

    @Override
    public BaseDaoInterface<Consume, Serializable> getDao() {
        return consumeDao;
    }

    @Override
    public Page<Consume> pageList(String userName,Integer pageNo, Integer pageSize,String startTime,String endTime) {
        Tenant tenant = tenantService.findTenantByUserName(userName);
        String hql = "from Consume obj where obj.tenant.id=?1 and ( DATE_FORMAT(obj.dt,'%Y-%m')<=?2 and DATE_FORMAT(obj.dt,'%Y-%m')>=?3 )  ORDER BY obj.dt";
        Page<Consume> page = this.pageList(hql,pageNo,pageSize,tenant.getId(),endTime,startTime);
        return page;
    }

    @Override
    public Page<Consume> pageListByTenantAndDate(String tenantId, Integer year, Integer month, Integer pageNo, Integer pageSize) {
        int start = (pageNo-1)*pageSize;
        String db_name = "db_lsxy_base";//year +"-" + month;
        String countsql = "select count(1) from "+db_name+".tb_base_consume where tenant_id=:tenant";
        Query countQuery = em.createNativeQuery(countsql);
        countQuery.setParameter("tenant",tenantId);
        long total = ((BigInteger)countQuery.getSingleResult()).longValue();
        if(total == 0){
            return new Page<>(start,total,pageSize,null);
        }
        String pagesql = "select * from "+db_name+".tb_base_consume where tenant_id=:tenant";
        Query pageQuery = em.createNativeQuery(pagesql,Consume.class);
        pageQuery.setParameter("tenant",tenantId);
        pageQuery.setMaxResults(pageSize);
        pageQuery.setFirstResult(start);
        return new Page<>(start,total,pageSize,pageQuery.getResultList());
    }
}
