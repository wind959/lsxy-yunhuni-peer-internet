package com.lsxy.yunhuni.session.service;

import com.lsxy.framework.api.base.BaseDaoInterface;
import com.lsxy.yunhuni.api.statistics.model.VoiceCdrDay;
import com.lsxy.yunhuni.api.statistics.model.VoiceCdrHour;
import com.lsxy.yunhuni.api.statistics.service.VoiceCdrDayService;
import com.lsxy.yunhuni.api.statistics.service.VoiceCdrHourService;
import com.lsxy.framework.base.AbstractService;
import com.lsxy.framework.core.utils.BeanUtils;
import com.lsxy.framework.core.utils.DateUtils;
import com.lsxy.framework.core.utils.Page;
import com.lsxy.framework.core.utils.StringUtil;
import com.lsxy.utils.StatisticsUtils;
import com.lsxy.yunhuni.api.session.model.CallSession;
import com.lsxy.yunhuni.api.session.model.VoiceCdr;
import com.lsxy.yunhuni.api.session.service.CallSessionService;
import com.lsxy.yunhuni.api.session.service.VoiceCdrService;
import com.lsxy.yunhuni.session.dao.VoiceCdrDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.lsxy.framework.core.utils.DateUtils.getPreDate;

/**
 * Created by zhangxb on 2016/7/19.
 */
@Service
public class VoiceCdrServiceImpl extends AbstractService<VoiceCdr> implements  VoiceCdrService{
    @Autowired
    private VoiceCdrDao voiceCdrDao;
    @Override
    public BaseDaoInterface<VoiceCdr, Serializable> getDao() {
        return voiceCdrDao;
    }
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CallSessionService callSessionService;
    @Autowired
    private VoiceCdrHourService voiceCdrHourService;
    @Autowired
    private VoiceCdrDayService voiceCdrDayService;

    @Override
    public Page<VoiceCdr> pageList(Integer pageNo,Integer pageSize, String type,String tenantId, String time, String appId) {
        Date date1 = DateUtils.parseDate(time,"yyyy-MM-dd");
        Date date2 = DateUtils.parseDate(time+" 23:59:59","yyyy-MM-dd HH:mm:ss");
        String sql = "from db_lsxy_bi_yunhuni.tb_bi_voice_cdr where "+ StatisticsUtils.getSqlIsNull2(tenantId,appId,type)+ " deleted=0 and   last_time BETWEEN ? and ?";
        String sqlCount = "select count(1) "+sql;
        Integer totalCount = jdbcTemplate.queryForObject(sqlCount,Integer.class,new Object[]{date1,date2});
        sql = "select "+StringUtil.sqlName(VoiceCdr.class)+sql+" order by call_start_dt desc limit ?,?";
        pageNo--;
        List rows = jdbcTemplate.queryForList(sql,new Object[]{date1,date2,pageNo*pageSize,pageSize});
        List list = new ArrayList();
        for(int i=0;i<rows.size();i++){
            VoiceCdr voiceCdr = new VoiceCdr();
            try {
                BeanUtils.copyProperties(voiceCdr,rows.get(i));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            list.add(voiceCdr);
        }
        Page<VoiceCdr> page = new Page((pageNo)*pageSize+1,totalCount,pageSize,list);
        return page;
    }

    @Override
    public Map sumCost( String type ,String tenantId, String time, String appId) {
        Date date1 = DateUtils.parseDate(time,"yyyy-MM-dd");
        Date date2 = DateUtils.parseDate(time+" 23:59:59","yyyy-MM-dd HH:mm:ss");
        String costType = " SUM(cost) as cost";
        if(CallSession.TYPE_VOICE_RECORDING.equals(type)){
            costType = " sum(record_size) as size,sum(cost) as money ";
        }
        String sql = "select "+costType+" from db_lsxy_bi_yunhuni.tb_bi_voice_cdr  where "+ StatisticsUtils.getSqlIsNull2(tenantId,appId,type)+ " deleted=0  and last_time BETWEEN ? and ? ";
        Map result = this.jdbcTemplate.queryForMap(sql,new Object[]{date1,date2});
        return result;
    }


    @Override
    public Map currentRecordStatistics(String appId){
        VoiceCdrHour voiceCdrHour = null;
        VoiceCdrDay voiceCdrDay = null;
        Date date = new Date();
        Long currentSession = callSessionService.currentCallSessionCount(appId);
        String currentHourStr = DateUtils.formatDate(date, "yyyy-MM-dd HH");
        Date currentHour = DateUtils.parseDate(currentHourStr, "yyyy-MM-dd HH");
        currentHour = DateUtils.getPrevHour(currentHour);
        voiceCdrHour = voiceCdrHourService.findByAppIdAndTime(appId,currentHour);
        if(voiceCdrHour == null){
            Date lastHour = DateUtils.getPrevHour(currentHour);
            voiceCdrHour = voiceCdrHourService.findByAppIdAndTime(appId,lastHour);
        }

        String currentDayStr = DateUtils.formatDate(date, "yyyy-MM-dd");
        Date currentDay = DateUtils.parseDate(currentDayStr, "yyyy-MM-dd");
        currentDay = DateUtils.getPreDate(currentDay);
        voiceCdrDay = voiceCdrDayService.findByAppIdAndTime(appId,currentDay);
        if(voiceCdrDay == null){
            Date lastDay = DateUtils.getPreDate(currentDay);
            voiceCdrDay = voiceCdrDayService.findByAppIdAndTime(appId,lastDay);
        }

        Map result = new HashMap();
        result.put("hourCount",voiceCdrHour == null ? 0 : voiceCdrHour.getAmongCall());
        result.put("dayCount",voiceCdrDay == null ? 0 : voiceCdrDay.getAmongCall());
        result.put("currentSession",currentSession);
        return result;
    }

}
