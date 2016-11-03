package com.lsxy.yunhuni.resourceTelenum.service;

import com.lsxy.framework.api.base.BaseDaoInterface;
import com.lsxy.framework.base.AbstractService;
import com.lsxy.framework.core.utils.Page;
import com.lsxy.yunhuni.api.config.model.LineGateway;
import com.lsxy.yunhuni.api.config.service.LineGatewayService;
import com.lsxy.yunhuni.api.resourceTelenum.model.TelnumToLineGateway;
import com.lsxy.yunhuni.api.resourceTelenum.service.TelnumToLineGatewayService;
import com.lsxy.yunhuni.resourceTelenum.dao.TelnumToLineGatewayDao;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by liups on 2016/9/2.
 */
@Service
public class TelnumToLineGatewayServiceImpl extends AbstractService<TelnumToLineGateway> implements TelnumToLineGatewayService{
    @Autowired
    TelnumToLineGatewayDao telnumToLineGatewayDao;

    @Autowired
    LineGatewayService lineGatewayService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Override
    public BaseDaoInterface<TelnumToLineGateway, Serializable> getDao() {
        return this.telnumToLineGatewayDao;
    }


    @Override
    public  List<TelnumToLineGateway> getDialingLinesByNumber(String number) {
        return telnumToLineGatewayDao.findDialingLine(number);
    }

    @Override
    public LineGateway getCalledLineByNumber(String number) {
        TelnumToLineGateway ttg = telnumToLineGatewayDao.findFirstByTelNumberAndIsCalled(number,"1");
        if(ttg != null){
            return lineGatewayService.findById(ttg.getLineId());
        }else{
            return null;
        }
    }

    @Override
    public void deleteByLineId(String line) {
        String sql = "UPDATE db_lsxy_bi_yunhuni.tb_oc_telnum_to_linegateway SET deleted=1 WHERE deleted=0 AND line_id='"+line+"'";
        jdbcTemplate.update(sql);
    }

    @Override
    public void deleteByTelnum(String telnum) {
        String sql = "UPDATE db_lsxy_bi_yunhuni.tb_oc_telnum_to_linegateway SET deleted=1 WHERE deleted=0 AND tel_number='"+telnum+"'";
        jdbcTemplate.update(sql);
    }

    @Override
    public Page<TelnumToLineGateway> getPage(Integer pageNo,Integer pageSize,String line,String number, String isDialing, String isCalled, String isThrough) {
        String hql = " FROM TelnumToLineGateway obj WHERE obj.deleted=0  ";
        if(StringUtils.isNotEmpty(line)){
            hql += " AND obj.lineId='"+line+"' ";
        }
        if(StringUtils.isNotEmpty(isDialing)){
            hql += " AND obj.isDialing='"+isDialing+"' ";
        }
        if(StringUtils.isNotEmpty(isCalled)){
            hql += " AND obj.isCalled='"+isCalled+"' ";
        }
        if(StringUtils.isNotEmpty(isThrough)){
            hql += " AND obj.isThrough='"+isThrough+"' ";
        }
        if(StringUtils.isNotEmpty(number)){
            hql += " AND obj.telNumber like '%"+number+"%' ";
        }
        hql += " ORDER BY obj.createTime DESC ";
        Page page = this.pageList(hql,pageNo,pageSize);
        return page;
    }

    @Override
    public Page<TelnumToLineGateway> getIsNotNullPage(Integer pageNo, Integer pageSize, String isNotNull, String number) {
        String hql = " FROM TelnumToLineGateway obj WHERE obj.deleted=0  ";
        if(StringUtils.isNotEmpty(isNotNull)){
            hql += " AND obj.lineId<>'"+isNotNull+"' ";
        }
        if(StringUtils.isNotEmpty(number)){
            hql += " AND obj.telNumber = '"+number+"' ";
        }
        hql += " ORDER BY obj.createTime DESC ";
        Page page = this.pageList(hql,pageNo,pageSize);
        return page;
    }

    @Override
    public void batchDelete(String line,String[] ids) {
        String sql = " UPDATE db_lsxy_bi_yunhuni.tb_oc_telnum_to_linegateway SET deleted=1 WHERE line_id='"+line+"' AND id IN (";
        for(int i=0;i<ids.length;i++){
            sql+= " '"+ids[i]+"' ";
            if(i!=ids.length-1){
                sql+=" , ";
            }
        }
        sql += " ) ";
        jdbcTemplate.update(sql);
    }

    @Override
    public void batchInsert(String id,String provider, String[] ids) {
        String sql = "INSERT INTO db_lsxy_bi_yunhuni.tb_oc_telnum_to_linegateway (id, tel_number , line_id, is_dialing,is_called,is_through,is_buy,provider,create_time,last_time,deleted,sortno,version) VALUES ";
        long times = new Date().getTime();
        Timestamp initDate = new Timestamp(times);
        ArrayList list = new ArrayList();
        for(int i=0;i<ids.length;i++){
            sql += " ( REPLACE(UUID(), '-', ''), '"+ids[i]+"' , '"+id+"', 0,0,1,"+1+",'"+provider+"',?,?,0,"+times+",0 ) ";
            if(i!=ids.length-1){
                sql += " , ";
            }
            list.add(initDate);
            list.add(initDate);
        }
        jdbcTemplate.update(sql,new PreparedStatementSetter(){
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                for(int i=0;i<list.size();i++){
                    ps.setObject(i+1,list.get(i));
                }
            }
        });
    }

    @Override
    public Map getTelnumCall(String telnum,String line) {
        String sql = " SELECT CONVERT(IFNULL(SUM(is_dialing),0),SIGNED) AS isDialing,CONVERT(IFNULL(SUM(is_through),0),SIGNED) AS isThrough, CONVERT(IFNULL(SUM(is_called),0),SIGNED) AS isCalled  FROM  db_lsxy_bi_yunhuni.tb_oc_telnum_to_linegateway WHERE deleted=0 ";
        if(StringUtils.isNotEmpty(line)){
            sql += " AND line_id<>'"+line+"' ";
        }
        Map map =  jdbcTemplate.queryForMap(sql);
        return map;
    }

    @Override
    public List<String> getTelnumByLineId(String line) {
        String sql = " SELECT DISTINCT tel_number  FROM  db_lsxy_bi_yunhuni.tb_oc_telnum_to_linegateway WHERE deleted=0 AND line_id='"+line+"' ";
        List<String> list = jdbcTemplate.queryForList(sql,String.class);
        return list;
    }

    @Override
    public void updateIsThrough(String line, String isThrough) {
        String sql = " UPDATE db_lsxy_bi_yunhuni.tb_oc_telnum_to_linegateway SET is_through='"+isThrough+"' WHERE  line_id='"+line+"' ";
        jdbcTemplate.update(sql);
    }

    @Override
    public TelnumToLineGateway findByTelNumberAndLineId(String telNumber, String lineId) {
        return telnumToLineGatewayDao.findByTelNumberAndLineId(telNumber,lineId);
    }

    @Override
    public void updateTelnum(String telnum1,String telnum) {
        String sql = "UPDATE db_lsxy_bi_yunhuni.tb_oc_telnum_to_linegateway SET tel_number='"+telnum+"' WHERE deleted=0 AND tel_number='"+telnum1+"'";
        jdbcTemplate.update(sql);
    }

}
