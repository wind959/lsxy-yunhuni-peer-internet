package com.lsxy.yunhuni.resourceTelenum.service;

import com.lsxy.framework.api.base.BaseDaoInterface;
import com.lsxy.framework.base.AbstractService;
import com.lsxy.yunhuni.api.resourceTelenum.model.ResourceTelenum;
import com.lsxy.yunhuni.api.resourceTelenum.service.ResourceTelenumService;
import com.lsxy.yunhuni.resourceTelenum.dao.ResourceTelenumDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 全局号码资源service
 * Created by zhangxb on 2016/7/1.
 */
@Service
public class ResourceTelenumServiceImpl extends AbstractService<ResourceTelenum> implements ResourceTelenumService {
    @Autowired
    private ResourceTelenumDao resourceTelenumDao;
    @Override
    public BaseDaoInterface<ResourceTelenum, Serializable> getDao() {
        return this.resourceTelenumDao;
    }

    @Override
    public List<String> getFreeTeleNum(int count,String areaId){
        //TODO 根据区域选择空闲的号码
        List<String> result = new ArrayList<>();
        List<ResourceTelenum> telenums = resourceTelenumDao.findFirst50ByStatus(ResourceTelenum.STATUS_FREE);
        if(telenums != null && telenums.size() > 0){
            int size = telenums.size();
            if(size <= count){
                //如果号码池中的号太少，则只取出号码池中的数量的号码即可
                for(ResourceTelenum telenum:telenums){
                    result.add(telenum.getTelNumber());
                }
            }else{
                List<Integer> ranList = new ArrayList<>();
                Random random = new Random();
                for(int i = 0;i< count;i++){
                    Integer ranNum = random.nextInt(size);
                    if(ranList.contains(ranNum)){
                        i--;
                    }else{
                        ranList.add(ranNum);
                        result.add(telenums.get(ranNum).getTelNumber());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public ResourceTelenum findByTelNumber(String telNumber) {
        return resourceTelenumDao.findByTelNumber(telNumber);
    }

    @Override
    public void cleanExpireResourceTelnum(Date expireTime) {
        resourceTelenumDao.cleanExpireResourceTelnum(expireTime);
    }

    @Override
    public String findOneFreeNumber(String areaId) {
        //TODO 根据区域获取一个空闲的号码
        ResourceTelenum resourceTelenum = resourceTelenumDao.findFirstByStatus(ResourceTelenum.STATUS_FREE);
        return resourceTelenum.getTelNumber();
    }

}
