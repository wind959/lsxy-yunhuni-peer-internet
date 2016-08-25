package com.lsxy.yunhuni.app.service;

import com.lsxy.framework.api.base.BaseDaoInterface;
import com.lsxy.framework.api.consume.model.Consume;
import com.lsxy.framework.api.consume.service.ConsumeService;
import com.lsxy.framework.api.tenant.model.Tenant;
import com.lsxy.framework.api.tenant.service.TenantService;
import com.lsxy.framework.base.AbstractService;
import com.lsxy.framework.core.utils.DateUtils;
import com.lsxy.yunhuni.api.app.model.App;
import com.lsxy.yunhuni.api.app.model.AppOnlineAction;
import com.lsxy.yunhuni.api.app.service.AppOnlineActionService;
import com.lsxy.yunhuni.api.app.service.AppService;
import com.lsxy.yunhuni.api.billing.model.Billing;
import com.lsxy.yunhuni.api.billing.service.BillingService;
import com.lsxy.yunhuni.api.config.model.Area;
import com.lsxy.yunhuni.api.config.service.AreaService;
import com.lsxy.yunhuni.api.exceptions.NotEnoughMoneyException;
import com.lsxy.yunhuni.api.exceptions.TeleNumberBeOccupiedException;
import com.lsxy.yunhuni.api.resourceTelenum.model.ResourceTelenum;
import com.lsxy.yunhuni.api.resourceTelenum.model.ResourcesRent;
import com.lsxy.yunhuni.api.resourceTelenum.service.ResourceTelenumService;
import com.lsxy.yunhuni.api.resourceTelenum.service.ResourcesRentService;
import com.lsxy.yunhuni.app.dao.AppOnlineActionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * Created by liups on 2016/7/15.
 */
@Service
public class AppOnlineActionServiceImpl extends AbstractService<AppOnlineAction> implements AppOnlineActionService {
    @Autowired
    AppOnlineActionDao appOnlineActionDao;

    @Autowired
    AppService appService;

    @Autowired
    BillingService billingService;

    @Autowired
    ConsumeService consumeService;

    @Autowired
    TenantService tenantService;

    @Autowired
    ResourceTelenumService resourceTelenumService;

    @Autowired
    ResourcesRentService resourcesRentService;

    @Autowired
    AreaService areaService;

    @Override
    public BaseDaoInterface<AppOnlineAction, Serializable> getDao() {
        return this.appOnlineActionDao;
    }

    @Override
    public AppOnlineAction findActiveActionByAppId(String appId) {
        List<AppOnlineAction> actionList = appOnlineActionDao.findByAppIdAndStatusOrderByCreateTimeDesc(appId, AppOnlineAction.STATUS_AVTIVE);
        if(actionList != null && actionList.size() > 0){
            return actionList.get(0);
        }else{
            return null;
        }
    }

    @Override
    public void actionOfSelectNum(String appId) {
        App app = appService.findById(appId);
        AppOnlineAction action = null;
        List<AppOnlineAction> actionList = appOnlineActionDao.findByAppIdAndStatusOrderByCreateTimeDesc(appId, AppOnlineAction.STATUS_AVTIVE);
        if(actionList != null && actionList.size() > 0){
            action = actionList.get(0);
        }
        //应用上线--选号
        if( app.getStatus() == App.STATUS_OFFLINE ){
            if(action != null){
                for(AppOnlineAction a:actionList){
                    a.setStatus(AppOnlineAction.STATUS_DONE);
                    this.save(a);
                }
            }
            AppOnlineAction newAction = new AppOnlineAction(null,null,null,app,AppOnlineAction.TYPE_ONLINE,AppOnlineAction.ACTION_SELECT_NUM,AppOnlineAction.STATUS_AVTIVE);
            this.save(newAction);
        }else if(action.getAction() != AppOnlineAction.ACTION_SELECT_NUM){
            throw new RuntimeException("数据错误");
        }
    }

    @Override
    public AppOnlineAction actionOfInPay(String appId, String ivr,Tenant tenant, boolean contains) {
        App app = new App();
        app.setId(appId);
        AppOnlineAction action = null;
        List<AppOnlineAction> actionList = appOnlineActionDao.findByAppIdAndStatusOrderByCreateTimeDesc(appId, AppOnlineAction.STATUS_AVTIVE);
        if(actionList != null && actionList.size() > 0){
            action = actionList.get(0);
        }
        //应用上线--正在支付
        if(action != null ){
            if(action.getAction() == AppOnlineAction.ACTION_SELECT_NUM ){
                //当上一步是应用选号中时，如果ivr属于号码池，则生成新的动作--正在支付
                if(contains){
                    for(AppOnlineAction a:actionList){
                        a.setStatus(AppOnlineAction.STATUS_DONE);
                        this.save(a);
                    }
                    //TODO 根据产品策略获取支付金额
                    BigDecimal amount = new BigDecimal(1000 + 100);
                    AppOnlineAction newAction = new AppOnlineAction(ivr, AppOnlineAction.PAY_STATUS_NOPAID,amount,app,AppOnlineAction.TYPE_ONLINE,AppOnlineAction.ACTION_PAYING,AppOnlineAction.STATUS_AVTIVE);
                    this.save(newAction);
                    return newAction;
                }else{
                    ResourceTelenum telenum = resourceTelenumService.findByTelNumber(ivr);
                    //是这个租户，则查询租用记录，有没有正在用的
                    if(telenum != null && telenum.getTenant().getId().equals(tenant.getId())){
                        ResourcesRent resourcesRent = resourcesRentService.findByResourceTelenumIdAndStatus(telenum.getId(),ResourcesRent.RENT_STATUS_UNUSED);
                        if(resourcesRent == null){
                            throw new TeleNumberBeOccupiedException("IVR号码已被占用");
                        }else if(!resourcesRent.getTenant().getId().equals(tenant.getId()) || resourcesRent.getApp() != null){
                            throw new TeleNumberBeOccupiedException("IVR号码已被占用");
                        }else{
                            //号码没被占用，创建支付动作（支付金额为0）
                            for(AppOnlineAction a:actionList){
                                a.setStatus(AppOnlineAction.STATUS_DONE);
                                this.save(a);
                            }
                            AppOnlineAction newAction = new AppOnlineAction(ivr, AppOnlineAction.PAY_STATUS_NOPAID,new BigDecimal(0),app,AppOnlineAction.TYPE_ONLINE,AppOnlineAction.ACTION_PAYING,AppOnlineAction.STATUS_AVTIVE);
                            this.save(newAction);
                            return newAction;
                        }
                    }else{
                        throw new RuntimeException("数据错误");
                    }
                }
            }else if(action.getAction() == AppOnlineAction.ACTION_PAYING){
                //当应用正处于正在支付时，反回当前动作
                return action;
            }else{
                throw new RuntimeException("数据错误");
            }
        }else{
            throw new RuntimeException("数据错误");
        }
    }

    @Override
    public AppOnlineAction actionOfOnline(String userName, String appId) throws NotEnoughMoneyException{
        App app = appService.findById(appId);
        AppOnlineAction action = null;
        List<AppOnlineAction> actionList = appOnlineActionDao.findByAppIdAndStatusOrderByCreateTimeDesc(appId, AppOnlineAction.STATUS_AVTIVE);
        if(actionList != null && actionList.size() > 0){
            action = actionList.get(0);
        }
        //应用上线--支付完成
        if(action != null ){
            if(action.getAction() == AppOnlineAction.ACTION_PAYING){
                //当上一步是应用正在支付中时，如果余额足够，则生成新的动作--上线
                Tenant tenant = tenantService.findTenantByUserName(userName);
                Billing billing = billingService.findBillingByTenantId(tenant.getId());
                if(billing.getBalance().compareTo(action.getAmount()) >= 0){
                    //当应用有ivr功能时，绑定IVR号码绑定
                    //判断ivr号码是否被占用
                    if(app.getIsIvrService() != null && app.getIsIvrService() == 1){
                        this.bindIvrToApp(app, action.getTelNumber(), tenant);
                    }
                    //当支付金额为0时，既上线不用支付，就不用插入消费记录，否则插入消费记录
                    if(action.getAmount().compareTo(new BigDecimal(0)) == 1){
                        //支付扣费，并插入消费记录
                        this.pay(appId, action.getAmount(), tenant, billing);
                    }
                    //将上一步设为已支付和完成
                    for(AppOnlineAction a:actionList){
                        a.setStatus(AppOnlineAction.STATUS_DONE);
                        if(a.getPayStatus() == AppOnlineAction.PAY_STATUS_NOPAID){
                            a.setPayStatus(AppOnlineAction.PAY_STATUS_PAID);
                        }
                        this.save(a);
                    }
                    //先成新的动作--生成新的动作--上线中
                    AppOnlineAction newAction = new AppOnlineAction(null,null,null,
                            app,AppOnlineAction.TYPE_ONLINE,AppOnlineAction.ACTION_ONLINE,AppOnlineAction.STATUS_AVTIVE);
                    this.save(newAction);
                    //应用状态改为上线
                    app.setStatus(App.STATUS_ONLINE);
                    appService.save(app);
                    return newAction;
                }else{
                    throw new NotEnoughMoneyException("余额不足");
                }
            }else if(action.getAction() == AppOnlineAction.ACTION_ONLINE){
                //当应用正处于已经上线状态时，反回当前动作
                return action;
            }else{
                throw new RuntimeException("数据错误");
            }
        }else{
            throw new RuntimeException("数据错误");
        }
    }

    /**
     * 绑定Ivr号到应用
     * @param app
     * @param ivr
     * @param tenant
     */
    private void bindIvrToApp(App app, String ivr, Tenant tenant) {
        ResourceTelenum resourceTelenum = resourceTelenumService.findByTelNumber(ivr);
        if(resourceTelenum.getStatus() == null || resourceTelenum.getStatus()== ResourceTelenum.STATUS_FREE){
            //生成新的号码租用关系
            this.newResourcesRent(app, tenant, resourceTelenum);
        }else if(resourceTelenum.getStatus()== ResourceTelenum.STATUS_RENTED){
            //如果号码已被租用,则根据租用关系进行判断并处理
            this.alterResourcesRent(app, tenant, resourceTelenum);
        }else{
            //如果ivr号码被占用，则抛出异常
            throw new TeleNumberBeOccupiedException("IVR号码已被占用");
        }
        //绑定应用与区域的关系
        app.setArea(resourceTelenum.getLine().getArea());
    }

    /**
     * 支付扣费，并插入消费记录
     * @param appId
     * @param amount
     * @param tenant
     * @param billing
     */
    private void pay(String appId, BigDecimal amount, Tenant tenant, Billing billing) {
        //TODO 调用扣费接口
        billing.setBalance(billing.getBalance().subtract(amount));
        billingService.save(billing);
        //插入消费记录
        Consume consume = new Consume(new Date(),"应用上线",amount,"应用上线",appId,tenant);
        consumeService.save(consume);
    }

    /**
     * 如果号码已被租用,则根据租用关系进行判断并处理
     * @param app 应用
     * @param tenant 租户
     * @param resourceTelenum 号码资源
     */
    private void alterResourcesRent(App app, Tenant tenant, ResourceTelenum resourceTelenum) {
        //如果号码被租用
        //租用是不是这个租户
        if(!resourceTelenum.getTenant().getId().equals(tenant.getId())){
            throw new TeleNumberBeOccupiedException("IVR号码已被占用");
        }else{
            //是这个租户，则查询租用记录，有没有正在用的
            ResourcesRent resourcesRent = resourcesRentService.findByResourceTelenumIdAndStatus(resourceTelenum.getId(),ResourcesRent.RENT_STATUS_UNUSED);
            if(resourcesRent == null){
                throw new TeleNumberBeOccupiedException("IVR号码已被占用");
            }else if(!resourcesRent.getTenant().getId().equals(tenant.getId()) || resourcesRent.getApp() != null){
                throw new TeleNumberBeOccupiedException("IVR号码已被占用");
            }else{
                resourcesRent.setApp(app);
                resourcesRent.setRentStatus(ResourcesRent.RENT_STATUS_USING);
                resourcesRentService.save(resourcesRent);
            }
        }
    }

    /**
     * 生成新的号码租用关系
     * @param app 应用
     * @param tenant 租户
     * @param resourceTelenum 号码资源
     */
    private void newResourcesRent(App app, Tenant tenant, ResourceTelenum resourceTelenum) {
        //保存号码资源
        resourceTelenum.setTenant(tenant);
        resourceTelenum.setStatus(ResourceTelenum.STATUS_RENTED);
        resourceTelenumService.save(resourceTelenum);
        // 保存号码租用关系
        Date date = new Date();
        String nextMonth = DateUtils.getNextMonth(DateUtils.getDate(date, "yyyy-MM"), "yyyy-MM");
        Date expireDateTem = DateUtils.parseDate(nextMonth, "yyyy-MM");    //号码到期时间
        Date expireDate = new Date(expireDateTem.getTime() -1);    //号码到期时间设为下个月一号的时间戳减1
        ResourcesRent resourcesRent = new ResourcesRent(tenant,app,resourceTelenum,"号码资源",ResourcesRent.RESTYPE_TELENUM,new Date(),expireDate,ResourcesRent.RENT_STATUS_USING);
        resourcesRentService.save(resourcesRent);
    }

    @Override
    public AppOnlineAction actionOfDirectOnline(String userName, String appId) {
        App app = appService.findById(appId);
        if(app.getIsIvrService() == null || app.getIsIvrService() == 0){
            //应用上线--直接上线
            AppOnlineAction action = null;
            List<AppOnlineAction> actionList = appOnlineActionDao.findByAppIdAndStatusOrderByCreateTimeDesc(appId, AppOnlineAction.STATUS_AVTIVE);
            if(actionList != null && actionList.size() > 0){
                action = actionList.get(0);
            }
            if( app.getStatus() == App.STATUS_OFFLINE ){
                if(action != null){
                    for(AppOnlineAction a:actionList){
                        a.setStatus(AppOnlineAction.STATUS_DONE);
                        this.save(a);
                    }
                }
                AppOnlineAction newAction = new AppOnlineAction(null,null,null,app,AppOnlineAction.TYPE_ONLINE,AppOnlineAction.ACTION_ONLINE,AppOnlineAction.STATUS_AVTIVE);
                this.save(newAction);
                //应用状态改为上线
                app.setStatus(App.STATUS_ONLINE);
                //应用绑定区域
                app.setArea(areaService.getOneAvailableArea());
                appService.save(app);
                return newAction;
            }else if(app.getStatus() == App.STATUS_ONLINE ){
                return action;
            }else{
                throw new RuntimeException("数据错误");
            }
        }else{
            throw new RuntimeException("数据错误");
        }
    }

    @Override
    public AppOnlineAction resetIvr(String userName, String appId) {
        App app = appService.findById(appId);
        AppOnlineAction action = null;
        List<AppOnlineAction> actionList = appOnlineActionDao.findByAppIdAndStatusOrderByCreateTimeDesc(appId, AppOnlineAction.STATUS_AVTIVE);
        if(actionList != null && actionList.size() > 0){
            action = actionList.get(0);
        }
        //应用上线--支付完成
        if(action != null ) {
            if (action.getAction() == AppOnlineAction.ACTION_PAYING) {
                //将上一步设为已完成
                for(AppOnlineAction a:actionList){
                    a.setStatus(AppOnlineAction.STATUS_DONE);
                    this.save(a);
                }
                //插入一条取消支付的动作（状态为已完成）
                AppOnlineAction resetAction = new AppOnlineAction(null,null,null,app,AppOnlineAction.TYPE_ONLINE,AppOnlineAction.ACTION_CANCEL_PAY,AppOnlineAction.STATUS_DONE);
                this.save(resetAction);
                //插入一条正在选号的动作
                AppOnlineAction newAction = new AppOnlineAction(null,null,null,app,AppOnlineAction.TYPE_ONLINE,AppOnlineAction.ACTION_SELECT_NUM,AppOnlineAction.STATUS_AVTIVE);
                this.save(newAction);
                return newAction;
            }else{
                throw new RuntimeException("数据错误");
            }
        }else{
            throw new RuntimeException("数据错误");
        }
    }

    @Override
    public App offline(String appId) {
        App app = appService.findById(appId);
        if(app!= null && app.getStatus() == App.STATUS_ONLINE){
            List<AppOnlineAction> actionList = appOnlineActionDao.findByAppIdAndStatusOrderByCreateTimeDesc(appId, AppOnlineAction.STATUS_AVTIVE);
            if(actionList != null && actionList.size() > 0){
                //将上一步设为已完成
                for(AppOnlineAction a:actionList){
                    a.setStatus(AppOnlineAction.STATUS_DONE);
                    this.save(a);
                }
            }

            //生成新的动作
            AppOnlineAction newAction = new AppOnlineAction(null,null,null,app,AppOnlineAction.TYPE_OFFLINE,AppOnlineAction.ACTION_OFFLINE,AppOnlineAction.STATUS_AVTIVE);
            this.save(newAction);
            //应用状态改为下线
            app.setStatus(App.STATUS_OFFLINE);
            appService.save(app);
            if(app.getIsIvrService() != null && app.getIsIvrService() ==1){
                //当应用有ivr功能时，改变IVR号码的租用关系
                ResourcesRent rent = resourcesRentService.findByAppId(app.getId());
                if(rent != null){
                    rent.setRentStatus(ResourcesRent.RENT_STATUS_UNUSED);
                    rent.setApp(null);
                    resourcesRentService.save(rent);
                }
            }
            return app;
        }else{
            throw new RuntimeException("数据错误");
        }
    }

    @Override
    public void resetAppOnlineAction(String appId) {
        List<AppOnlineAction> actionList = appOnlineActionDao.findByAppIdAndStatusOrderByCreateTimeDesc(appId, AppOnlineAction.STATUS_AVTIVE);
        if(actionList != null && actionList.size() > 0){
            for(AppOnlineAction action : actionList){
                action.setStatus(AppOnlineAction.STATUS_DONE);
                this.save(action);
            }
        }
    }

}
