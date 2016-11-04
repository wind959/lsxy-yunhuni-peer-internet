package com.lsxy.area.server.event.handler.call;

import com.lsxy.area.api.BusinessState;
import com.lsxy.area.api.BusinessStateService;
import com.lsxy.area.server.event.EventHandler;
import com.lsxy.area.server.service.ivr.IVRActionService;
import com.lsxy.area.server.util.NotifyCallbackUtil;
import com.lsxy.framework.core.utils.MapBuilder;
import com.lsxy.framework.rpc.api.RPCRequest;
import com.lsxy.framework.rpc.api.RPCResponse;
import com.lsxy.framework.rpc.api.event.Constants;
import com.lsxy.framework.rpc.api.session.Session;
import com.lsxy.framework.rpc.exceptions.InvalidParamException;
import com.lsxy.yunhuni.api.app.model.App;
import com.lsxy.yunhuni.api.app.service.AppService;
import com.lsxy.yunhuni.api.session.model.CallSession;
import com.lsxy.yunhuni.api.session.service.CallSessionService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by liuws on 2016/8/29.
 */
@Component
public class Handler_EVENT_SYS_CALL_ON_RELEASE extends EventHandler{

    private static final Logger logger = LoggerFactory.getLogger(Handler_EVENT_SYS_CALL_ON_RELEASE.class);

    @Autowired
    private AppService appService;

    @Autowired
    private BusinessStateService businessStateService;

    @Autowired
    private NotifyCallbackUtil notifyCallbackUtil;

    @Autowired
    private IVRActionService ivrActionService;

    @Autowired
    private CallSessionService callSessionService;

    @Override
    public String getEventName() {
        return Constants.EVENT_SYS_CALL_ON_RELEASE;
    }

    /**
     * 处理呼叫结束事件
     * @param request
     * @param session
     * @return
     */
    @Override
    public RPCResponse handle(RPCRequest request, Session session) {
        RPCResponse res = null;
        Map<String,Object> params = request.getParamMap();
        if(MapUtils.isEmpty(params)){
            throw new InvalidParamException("request params is null");
        }
        String call_id = (String)params.get("user_data");

        if(StringUtils.isBlank(call_id)){
            throw new InvalidParamException("call_id is null");
        }

        BusinessState state = businessStateService.get(call_id);
        if(state == null){
            throw new InvalidParamException("businessstate is null");
        }
        businessStateService.delete(call_id);
        if(StringUtils.isBlank(state.getAppId())){
            throw new InvalidParamException("没有找到对应的app信息appId={}",state.getAppId());
        }
        App app = appService.findById(state.getAppId());
        if(app == null){
            throw new InvalidParamException("没有找到对应的app信息appId={}",state.getAppId());
        }

        if(logger.isDebugEnabled()){
            logger.debug("call_id={},state={}",call_id,state);
        }
        Long begin_time = null;
        Long end_time = null;
        Long answer_time = null;
        if(params.get("begin_time") != null){
            begin_time = (Long.parseLong(params.get("begin_time").toString())) * 1000;
        }
        if(params.get("end_time") != null){
            end_time = (Long.parseLong(params.get("end_time").toString())) * 1000;
        }
        if(params.get("answer_time") != null){
            answer_time = (Long.parseLong(params.get("answer_time").toString())) * 1000;
        }

        //发送呼叫结束通知
        if(StringUtils.isNotBlank(app.getUrl())){
            Map<String,Object> notify_data = new MapBuilder<String,Object>()
                    .putIfNotEmpty("event","ivr.call_end")
                    .putIfNotEmpty("id",call_id)
                    .putIfNotEmpty("begin_time",begin_time)
                    .putIfNotEmpty("answer_time",answer_time)
                    .putIfNotEmpty("end_time",end_time)
                    .putIfNotEmpty("end_by",params.get("dropped_by"))
                    .putIfNotEmpty("cause",params.get("cause"))
                    .putIfNotEmpty("user_data",state.getUserdata())
                    .build();
            notifyCallbackUtil.postNotify(app.getUrl(),notify_data,3);
        }

        //更新会话记录状态
        CallSession callSession = callSessionService.findById((String)state.getBusinessData().get("sessionid"));
        if(callSession != null){
            callSession.setStatus(CallSession.STATUS_OVER);
            callSessionService.save(callSession);
        }

        //通过ivr 拨号发起的呼叫在被叫方结束后 要继续ivr
        if(state.getType().equalsIgnoreCase("ivr_dial")){
            String ivr_call_id = null;
            if(state.getBusinessData() != null){
                ivr_call_id = (String)state.getBusinessData().get("ivr_call_id");
            }
            if(StringUtils.isNotBlank(ivr_call_id)){
                ivrActionService.doAction(ivr_call_id);
            }
        }
        return res;
    }
}
