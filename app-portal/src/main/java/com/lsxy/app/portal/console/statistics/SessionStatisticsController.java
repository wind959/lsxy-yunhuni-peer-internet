package com.lsxy.app.portal.console.statistics;

import com.lsxy.app.portal.base.AbstractPortalController;
import com.lsxy.framework.api.statistics.model.ConsumeDay;
import com.lsxy.framework.api.statistics.model.ConsumeMonth;
import com.lsxy.framework.api.statistics.model.VoiceCdrDay;
import com.lsxy.framework.api.statistics.model.VoiceCdrMonth;
import com.lsxy.framework.config.SystemConfig;
import com.lsxy.framework.core.utils.DateUtils;
import com.lsxy.framework.web.rest.RestRequest;
import com.lsxy.framework.web.rest.RestResponse;
import com.lsxy.yunhuni.api.app.model.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 会话统计
 * Created by zhangxb on 2016/7/8.
 */
@Controller
@RequestMapping("/console/statistics/session")
public class SessionStatisticsController extends AbstractPortalController {
    private static final Logger logger = LoggerFactory.getLogger(SessionStatisticsController.class);
    private String restPrefixUrl = SystemConfig.getProperty("portal.rest.api.url");
    /**
     * 消费统计首页
     * @param request
     * @return
     */
    @RequestMapping("/index")
    public ModelAndView index(HttpServletRequest request){
        ModelAndView mav = new ModelAndView();
        mav.addObject("appList",getAppList(request).getData());
        Date date = new Date();
        mav.addObject("startTimeMonth", DateUtils.formatDate(date,"yyyy-MM"));
        mav.addObject("startTimeYear", DateUtils.formatDate(date,"yyyy"));
        mav.setViewName("/console/statistics/session/index");
        return mav;
    }
    /**
     * 获取租户下的全部应用
     * @param request
     * @return
     */
    private RestResponse getAppList(HttpServletRequest request){
        String token = getSecurityToken(request);
        String uri = restPrefixUrl +   "/rest/app/list";
        return RestRequest.buildSecurityRequest(token).getList(uri, App.class);
    }

    /**
     * 异步获取图表显示信息
     * @param request
     * @param type 类型 日统计，月统计
     * @param startTime 统计时间
     * @param appId 应用
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public List list(HttpServletRequest request,String type,String startTime,String appId){
        List list = new ArrayList();
        List tempConsumeList = getConsumeList(request,type,appId,startTime);
        List tempVoiceCdrList = getVoiceCdrList(request,type,appId,startTime);
        list.add(getArrays(tempConsumeList));
        list.add(getArrays(tempVoiceCdrList));
        return list;
    }
    /**
     * 获取通话记录（session）的List
     * @param request
     * @param appId 应用id
     * @param startTime 开始时间
     * @param type 统计类型
     * @return
     */
    private List getVoiceCdrList(HttpServletRequest request,String type,String appId,String startTime){
        String token = getSecurityToken(request);
        String uri = restPrefixUrl +   "/rest/voice_cdr_"+type+"/list?tenantId={1}&appId={2}&startTime={3}";
        Class clazz = VoiceCdrDay.class;
        if(ConsumeStatisticsVo.TYPE_MONTH.equals(type)){
            clazz = VoiceCdrMonth.class;
        }
        appId = "-1".equals(appId)?null:appId;
        String tenantId = getCurrentAccount(request).getTenant().getId();
        return (List)RestRequest.buildSecurityRequest(token).getList(uri, clazz,tenantId,appId,startTime).getData();
    }
    /**
     * 获取消费List
     * @param request
     * @param appId 应用id
     * @param startTime 开始时间
     * @param type 统计类型
     * @return
     */
    private List getConsumeList(HttpServletRequest request,String type,String appId,String startTime){
        String token = getSecurityToken(request);
        String uri = restPrefixUrl +   "/rest/consume_"+type+"/list?tenantId={1}&appId={2}&startTime={3}";
        Class clazz = ConsumeDay.class;
        if(ConsumeStatisticsVo.TYPE_MONTH.equals(type)){
            clazz = ConsumeMonth.class;
        }
        appId = "-1".equals(appId)?null:appId;
        String tenantId = getCurrentAccount(request).getTenant().getId();
        return (List)RestRequest.buildSecurityRequest(token).getList(uri, clazz,tenantId,appId,startTime).getData();
    }
    /**
     * 获取列表数据
     * @param list 待处理的list
     * @return
     */
    private Object[] getArrays(List list) {
        Object[] list1 = new Object[list.size()];
        for(int i=0;i<list.size();i++){
            Object obj = list.get(i);
            if(obj instanceof ConsumeMonth){
                list1[i]=((ConsumeMonth)obj).getSumAmount().doubleValue();
            }else if(obj instanceof VoiceCdrMonth){
                list1[i]=((VoiceCdrMonth)obj).getAmongCall();
            }else if(obj instanceof ConsumeDay){
                list1[i]=((ConsumeDay)obj).getSumAmount().doubleValue();
            }else if(obj instanceof VoiceCdrDay){
                list1[i]=((VoiceCdrDay)obj).getAmongCall();
            }
        }
        return list1;
    }

}
