package com.lsxy.call.center.service;

import com.lsxy.call.center.api.model.Channel;
import com.lsxy.call.center.api.model.Condition;
import com.lsxy.call.center.api.service.ChannelService;
import com.lsxy.call.center.api.service.ConditionService;
import com.lsxy.call.center.dao.ConditionDao;
import com.lsxy.call.center.states.lock.ModifyConditionLock;
import com.lsxy.call.center.utils.ExpressionUtils;
import com.lsxy.framework.api.base.BaseDaoInterface;
import com.lsxy.framework.base.AbstractService;
import com.lsxy.framework.cache.manager.RedisCacheService;
import com.lsxy.framework.mq.api.AbstractMQEvent;
import com.lsxy.framework.mq.api.MQService;
import com.lsxy.framework.mq.events.callcenter.CreateConditionEvent;
import com.lsxy.framework.mq.events.callcenter.DeleteConditionEvent;
import com.lsxy.framework.mq.events.callcenter.ModifyConditionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import java.io.Serializable;

/**
 * Created by zhangxb on 2016/10/21.
 */
@Service
@com.alibaba.dubbo.config.annotation.Service
public class ConditionServiceImpl extends AbstractService<Condition> implements ConditionService {

    private static final Logger logger = LoggerFactory.getLogger(ConditionServiceImpl.class);

    @Autowired
    private ConditionDao conditionDao;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private MQService mqService;

    @Autowired
    private RedisCacheService redisCacheService;

    @Override
    public BaseDaoInterface<Condition, Serializable> getDao() {
        return conditionDao;
    }

    @Override
    public Condition save(Condition condition){
        if(condition == null){
            throw new NullPointerException();
        }
        if(condition.getTenantId() == null){
            throw new IllegalArgumentException("tenantId 不能为null");
        }
        if(condition.getAppId() == null){
            throw new IllegalArgumentException("appId 不能为null");
        }
        if(condition.getChannelId() == null){
            throw new IllegalArgumentException("channelId 不能为null");
        }
        if(!ExpressionUtils.validSortExpression(condition.getSortExpression())){
            throw new IllegalArgumentException("sort expression 错误");
        }
        if(!ExpressionUtils.validWhereExpression(condition.getWhereExpression())){
            throw new IllegalArgumentException("where expression 错误");
        }
        //通道是否存在
        Channel channel = channelService.findById(condition.getChannelId());
        if(channel == null){
            throw new IllegalArgumentException("channel 不存在");
        }
        if(condition.getId() != null){
            Condition oldCondition = this.findById(condition.getId());
            if(oldCondition == null){
                throw new IllegalArgumentException("condition 不存在");
            }
            if(!oldCondition.getTenantId().equals(condition.getTenantId())){
                throw new IllegalArgumentException("condition 不存在");
            }
            if(!oldCondition.getAppId().equals(condition.getAppId())){
                throw new IllegalArgumentException("condition 不存在");
            }
            ModifyConditionLock lock = new ModifyConditionLock(redisCacheService,condition.getId());
            if(!lock.lock()){
                throw new java.lang.IllegalStateException("系统繁忙");
            }
            try{
                boolean modify_where = false;
                boolean modify_sort = false;
                boolean modify_priority = false;

                if(condition.getWhereExpression() != null){
                    modify_where = !(oldCondition.getWhereExpression() == null?"":oldCondition.getWhereExpression())
                            .equals(condition.getWhereExpression());
                    oldCondition.setWhereExpression(condition.getWhereExpression());
                }
                if(condition.getSortExpression() != null){
                    modify_sort = !(oldCondition.getSortExpression() == null?"":oldCondition.getSortExpression())
                            .equals(condition.getSortExpression());
                    oldCondition.setSortExpression(condition.getSortExpression());
                }
                if(condition.getPriority() != null){
                    modify_priority = Integer.compare(oldCondition.getPriority() == null ? 0 : oldCondition.getPriority()
                            ,condition.getPriority()) != 0;
                    oldCondition.setPriority(condition.getPriority());
                }
                if(condition.getQueueTimeout() != null){
                    oldCondition.setQueueTimeout(condition.getQueueTimeout());
                }
                if(condition.getFetchTimeout() != null){
                    oldCondition.setFetchTimeout(condition.getFetchTimeout());
                }
                if(condition.getRemark() != null){
                    oldCondition.setRemark(condition.getRemark());
                }
                condition = oldCondition;
                condition = super.save(condition);
                //修改条件事件
                AbstractMQEvent event = new ModifyConditionEvent(condition.getId(),condition.getTenantId(),condition.getAppId(),
                        modify_where,modify_sort,modify_priority);
                mqService.publish(event);
            }catch (Throwable t){
                lock.unlock();
                throw t;
            }
        }else{
            condition = super.save(condition);
            ModifyConditionLock lock = new ModifyConditionLock(redisCacheService,condition.getId());
            if(!lock.lock()){
                throw new java.lang.IllegalStateException("系统繁忙");
            }
            try{
                //创建条件事件
                AbstractMQEvent event = new CreateConditionEvent(condition.getId(),condition.getTenantId(),condition.getAppId());
                mqService.publish(event);
            }catch (Throwable t){
                lock.unlock();
                throw t;
            }
        }
        return condition;
    }

    @Override
    public void delete(String tenantId,String appId,String conditionId){
        Condition condition = this.findOne(tenantId,appId,conditionId);
        ModifyConditionLock lock = new ModifyConditionLock(redisCacheService,condition.getId());
        if(!lock.lock()){
            throw new java.lang.IllegalStateException("系统繁忙");
        }
        try {
            this.delete(conditionId);
            AbstractMQEvent event = new DeleteConditionEvent(condition.getId(),
                    condition.getTenantId(),condition.getAppId(),condition.getChannelId());
            mqService.publish(event);
        } catch (Throwable t) {
            lock.unlock();
            throw new RuntimeException(t);
        }
    }

    @Override
    public Condition findOne(String tenantId,String appId,String conditionId){
        if(conditionId == null){
            throw new IllegalArgumentException("conditionId 不能为null");
        }
        if(tenantId == null){
            throw new IllegalArgumentException("tenantId 不能为null");
        }
        if(appId == null){
            throw new IllegalArgumentException("appId 不能为null");
        }
        Condition condition = this.findById(conditionId);
        if(condition == null){
            throw new IllegalArgumentException("condition 不存在");
        }
        if(!tenantId.equals(condition.getTenantId())){
            throw new IllegalArgumentException("condition 不存在");
        }
        if(!appId.equals(condition.getAppId())){
            throw new IllegalArgumentException("condition 不存在");
        }
        return condition;
    }

    @Override
    public List<Condition> getAll(String tenantId, String appId){
        if(tenantId == null){
            throw new IllegalArgumentException("tenantId 不能为null");
        }
        if(appId == null){
            throw new IllegalArgumentException("appId 不能为null");
        }

        return this.conditionDao.findByTenantIdAndAppId(tenantId,appId);
    }

    @Override
    public List<Condition> getAll(String tenantId, String appId,String channelId){
        if(tenantId == null){
            throw new IllegalArgumentException("tenantId 不能为null");
        }
        if(appId == null){
            throw new IllegalArgumentException("appId 不能为null");
        }
        if(channelId == null){
            throw new IllegalArgumentException("channelId 不能为null");
        }
        return this.conditionDao.findByTenantIdAndAppIdAndChannelId(tenantId,appId,channelId);
    }
}