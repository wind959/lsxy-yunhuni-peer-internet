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
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

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
    public String getAreaIdByTelnum(String telnum){
        TelnumToLineGateway telnumToLineGateway = telnumToLineGatewayDao.findFirstByTelNumber(telnum);
        if(telnumToLineGateway == null ){
            throw new RuntimeException("数据异常，号码没有关联线路");
        }
        String lineId = telnumToLineGateway.getLineId();
        LineGateway lineGateway = lineGatewayService.findById(lineId);
        return lineGateway.getAreaId();
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
        String sql = "UPDATE db_lsxy_bi_yunhuni.tb_oc_telnum_to_linegateway SET deleted=1 WHERE line_id='"+line+"'";
        jdbcTemplate.update(sql);
    }

    @Override
    public Page<TelnumToLineGateway> getPage(Integer pageNo,Integer pageSize,String number, String isDialing, String isCalled, String isThrough) {
        String hql = " FROM TelnumToLineGateway obj WHERE 1=1 ";
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
            hql += " AND obj.number like '%"+number+"%' ";
        }
        hql += " ORDER BY obj.createTime DESC ";
        Page page = this.pageList(hql,pageNo,pageSize);
        return page;
    }

    @Override
    public void batchDelete(String[] ids) {
        String sql = " UPDATE db_lsxy_bi_yunhuni.tb_oc_telnum_to_linegateway SET deleted=1 WHERE id IN (";
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
    public void batchInsert(String id, String[] ids) {
        String sql = "INSERT INTO db_lsxy_bi_yunhuni.tb_oc_telnum_to_linegateway (id, tel_number , line_id, is_dialing,is_called,is_through,is_buy,provider,create_time,last_time,deleted,sortno,version) VALUES ";
        long times = new Date().getTime();
        Timestamp initDate = new Timestamp(times);
        for(int i=0;i<ids.length;i++){
            sql += " ( REPLACE(UUID(), '-', ''), tel_number , "+id+", is_dialing,is_called,is_through,"+1+",provider,create_time,last_time,0,"+times+",0 )";
            if(i!=ids.length-1){
                sql += " , ";
            }
        }
//                (200,'haha' , 'deng' , 'shenzhen'),
//                (201,'haha2' , 'deng' , 'GD'),
//                (202,'haha3' , 'deng' , 'Beijing');
//        id                   varchar(32) not null,
//                tel_number           varchar(32),
//                line_id              varchar(32) comment '所属线路网关',
//                is_dialing           varchar(10),
//                is_called            varchar(10),
//                is_through           varchar(10),
//                is_buy               varchar(10),
//                provider             varchar(32) comment '供应商',
//                create_time          datetime,
//                last_time            datetime,
//                deleted              int,
//        delete_time          datetime,
//        sortno               bigint,
//        version              int,
    }

}
