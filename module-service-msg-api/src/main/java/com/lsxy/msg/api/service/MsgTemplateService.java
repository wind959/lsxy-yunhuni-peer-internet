package com.lsxy.msg.api.service;

import com.lsxy.framework.api.base.BaseService;
import com.lsxy.framework.core.utils.Page;
import com.lsxy.msg.api.model.MsgTemplate;

/**
 * Created by liups on 2017/3/1.
 */
public interface MsgTemplateService extends BaseService<MsgTemplate> {
    MsgTemplate createTemplate(MsgTemplate msgTemplate);

    Page<MsgTemplate> getPageForGW(String appId, String subaccountId, Integer pageNo, Integer pageSize);

    MsgTemplate findByTempIdForGW(String appId, String subaccountId, String tempId);
}
