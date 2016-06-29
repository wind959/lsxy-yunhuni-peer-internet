package com.lsxy.yunhuni.recordedvoice.service;

import com.lsxy.framework.core.persistence.BaseDaoInterface;
import com.lsxy.framework.core.service.AbstractService;
import com.lsxy.yuhuni.recordedvoice.model.RecordedVoiceFile;
import com.lsxy.yuhuni.recordedvoice.service.RecordedVoiceFileService;
import com.lsxy.yunhuni.recordedvoice.dao.RecordedVoiceFileDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Created by liups on 2016/6/29.
 */
@Service
public class RecordedVoiceFileServiceImpl extends AbstractService<RecordedVoiceFile> implements RecordedVoiceFileService {
    @Autowired
    RecordedVoiceFileDao recordedVoiceFileDao;
    @Override
    public BaseDaoInterface<RecordedVoiceFile, Serializable> getDao() {
        return this.recordedVoiceFileDao;
    }

}
