package com.lsxy.yunhuni.api.statistics.service;

import com.lsxy.framework.api.base.BaseService;
import com.lsxy.yunhuni.api.statistics.model.ApiCallDay;
import com.lsxy.yunhuni.api.statistics.model.ApiCallMonth;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * api调用小时统计service
 * Created by zhangxb on 2016/8/1.
 */
public interface ApiCallMonthService extends BaseService<ApiCallMonth> {
    /**
     * 根据当前时间，进行统计
     * @param date1 时间yyyy-MM-dd
     * @param day1 第几天 1-31
     * @param date2 前一天的时间 yyyy-MM-dd
     * @param day2 前一天是第几天 1-31
     * @param select 组合groupby条件
     */
    public void monthStatistics(Date date1, int day1, Date date2, int day2, String[] select,String[] all) throws SQLException;

    /**
     * 获取某个租户某月的api调用次数
     * @param d
     * @return
     */
    public long getInvokeCountByDateAndTenant(Date d, String tenant,String appId);

    /**
     * 获取用户某时间的列表数据
     * @param tenantId 对于租户
     * @param appId 应用id
     * @param type 消费类型
     * @param startTime 时间
     * @param endTime 结束时间
     * @return
     */
    List<ApiCallMonth> list(Object tenantId, Object appId, Object type, Date startTime, Date endTime);
}
