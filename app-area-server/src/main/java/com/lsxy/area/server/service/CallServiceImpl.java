package com.lsxy.area.server.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.lsxy.area.api.BusinessState;
import com.lsxy.area.api.BusinessStateService;
import com.lsxy.area.api.CallService;
import com.lsxy.area.server.AreaAndTelNumSelector;
import com.lsxy.area.server.StasticsCounter;
import com.lsxy.area.server.test.TestIncomingZB;
import com.lsxy.area.server.util.PlayFileUtil;
import com.lsxy.framework.api.tenant.model.TenantServiceSwitch;
import com.lsxy.framework.api.tenant.service.TenantServiceSwitchService;
import com.lsxy.framework.core.exceptions.api.*;
import com.lsxy.framework.core.utils.JSONUtil;
import com.lsxy.framework.core.utils.JSONUtil2;
import com.lsxy.framework.core.utils.MapBuilder;
import com.lsxy.framework.core.utils.UUIDGenerator;
import com.lsxy.framework.rpc.api.RPCCaller;
import com.lsxy.framework.rpc.api.RPCRequest;
import com.lsxy.framework.rpc.api.ServiceConstants;
import com.lsxy.framework.rpc.api.session.SessionContext;
import com.lsxy.yunhuni.api.app.model.App;
import com.lsxy.yunhuni.api.app.service.AppService;
import com.lsxy.yunhuni.api.config.service.ApiGwRedBlankNumService;
import com.lsxy.yunhuni.api.config.service.LineGatewayService;
import com.lsxy.yunhuni.api.product.enums.ProductCode;
import com.lsxy.yunhuni.api.product.service.CalCostService;
import com.lsxy.yunhuni.api.session.model.CallSession;
import com.lsxy.yunhuni.api.session.model.CaptchaCall;
import com.lsxy.yunhuni.api.session.model.NotifyCall;
import com.lsxy.yunhuni.api.session.model.VoiceCallback;
import com.lsxy.yunhuni.api.session.service.CallSessionService;
import com.lsxy.yunhuni.api.session.service.CaptchaCallService;
import com.lsxy.yunhuni.api.session.service.NotifyCallService;
import com.lsxy.yunhuni.api.session.service.VoiceCallbackService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by tandy on 16/8/18.
 */
@Service
@Component
public class CallServiceImpl implements CallService {

    private static final Logger logger = LoggerFactory.getLogger(CallServiceImpl.class);

    @Autowired(required = false)
    private StasticsCounter cs;

    @Autowired(required = false)
    private TestIncomingZB tzb;

    @Autowired
    private RPCCaller rpcCaller;

    @Autowired
    private SessionContext sessionContext;

    @Autowired
    private ApiGwRedBlankNumService apiGwRedBlankNumService;

    @Autowired
    private AppService appService;

    @Autowired
    private CalCostService calCostService;

    @Autowired
    BusinessStateService businessStateService;

    @Autowired
    LineGatewayService lineGatewayService;

    @Autowired
    VoiceCallbackService voiceCallbackService;

    @Autowired
    NotifyCallService notifyCallService;

    @Autowired
    CallSessionService callSessionService;

    @Autowired
    private PlayFileUtil playFileUtil;

    @Autowired
    private TenantServiceSwitchService tenantServiceSwitchService;

    @Autowired
    private CaptchaCallService captchaCallService;

    @Autowired
    private AreaAndTelNumSelector areaAndTelNumSelector;

    private boolean isEnableDuoCallService(String tenantId,String appId){
        try {
            TenantServiceSwitch serviceSwitch = tenantServiceSwitchService.findOneByTenant(tenantId);
            if(serviceSwitch != null && (serviceSwitch.getIsVoiceCallback() == null || serviceSwitch.getIsVoiceCallback() != 1)){
                return false;
            }
            App app = appService.findById(appId);
            if(app.getIsVoiceCallback() == null || app.getIsVoiceCallback() != 1){
                return false;
            }
        } catch (Throwable e) {
            logger.error("判断是否开启service失败",e);
            return false;
        }
        return true;
    }

    private boolean isEnableVoiceDirectlyService(String tenantId,String appId){
        try {
            TenantServiceSwitch serviceSwitch = tenantServiceSwitchService.findOneByTenant(tenantId);
            if(serviceSwitch !=null && (serviceSwitch.getIsVoiceDirectly() == null || serviceSwitch.getIsVoiceDirectly() != 1)){
                return false;
            }
            App app = appService.findById(appId);
            if(app.getIsVoiceDirectly() == null || app.getIsVoiceDirectly() != 1){
                return false;
            }
        } catch (Throwable e) {
            logger.error("判断是否开启service失败",e);
            return false;
        }
        return true;
    }


    private boolean isEnableVoiceValidateService(String tenantId,String appId){
        try {
            TenantServiceSwitch serviceSwitch = tenantServiceSwitchService.findOneByTenant(tenantId);
            if(serviceSwitch != null && (serviceSwitch.getIsVoiceValidate() == null || serviceSwitch.getIsVoiceValidate() != 1)){
                return false;
            }
            App app = appService.findById(appId);
            if(app.getIsVoiceValidate() == null || app.getIsVoiceValidate() != 1){
                return false;
            }
        } catch (Throwable e) {
            logger.error("判断是否开启service失败",e);
            return false;
        }
        return true;
    }

    @Override
    public String call(String from, String to, int maxAnswerSec, int maxRingSec) throws YunhuniApiException {

        String callid = UUIDGenerator.uuid();
        String params = "to=%s&from=%s&maxAnswerSec=%d&maxRingSec=%d&callid=%s";
        params = String.format(params, to, from, maxAnswerSec, maxRingSec, callid);

        try {
            //找到合适的区域代理

                RPCRequest rpcrequest = RPCRequest.newRequest(ServiceConstants.MN_CH_SYS_CALL, params);
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("发送SYS_CALL指令到区域:{}", rpcrequest);
                    }

                    /*发送给区域的请求次数计数*/
                    if (cs != null) cs.getSendAreaNodeRequestCount().incrementAndGet();

                    tzb.doCallZB(to,rpcrequest);

                    rpcCaller.invoke(sessionContext, rpcrequest);

                    /*呼叫API调用次数计数*/
                    if(cs!=null)cs.getSendAreaNodeSysCallCount().incrementAndGet();
                } catch (Exception e) {
                    throw new InvokeCallException(e);
                }
            return callid;
        }catch(Exception ex){
            throw new InvokeCallException(ex);
        }
    }

    @Override
    public String duoCallback(String ip,String appId,String from1,String to1,String from2,String to2,String ring_tone,Integer ring_tone_mode,
                              Integer max_dial_duration,Integer max_call_duration ,Boolean recording,Integer record_mode,String user_data) throws YunhuniApiException {
        String apiCmd = "duo_call";
        String duocCallId;
        if(apiGwRedBlankNumService.isRedNum(to1) || apiGwRedBlankNumService.isRedNum(to2)){
            throw new NumberNotAllowToCallException();
        }
        App app = appService.findById(appId);
        if(app == null){
            throw new AppNotFoundException();
        }
        String whiteList = app.getWhiteList();
        if(StringUtils.isNotBlank(whiteList.trim())){
            if(!whiteList.contains(ip)){
                throw new IPNotInWhiteListException();
            }
        }
        if(!isEnableDuoCallService(app.getTenant().getId(),appId)){
            throw new AppServiceInvalidException();
        }

        boolean isAmountEnough = calCostService.isCallTimeRemainOrBalanceEnough(apiCmd, app.getTenant().getId());
        if(!isAmountEnough){
            throw new BalanceNotEnoughException();
        }

        //TODO 获取号码
        AreaAndTelNumSelector.Selector selector = areaAndTelNumSelector.getTelnumberAndAreaId(app,true, from1,to1,from2, to2);
        String areaId = selector.getAreaId();
        String oneTelnumber = selector.getOneTelnumber();

        from1 = oneTelnumber;
        from2 = oneTelnumber;
        //TODO 获取线路IP和端口
        String lineId = selector.getLineId();
        String to1_uri = selector.getTo1Uri();
        String to2_uri = selector.getTo2Uri();

        //保存双向回拔表
        VoiceCallback voiceCallback = new VoiceCallback(from1,from2,to1_uri,to2_uri);
        voiceCallbackService.save(voiceCallback);
        duocCallId = voiceCallback.getId();
        CallSession callSession = new CallSession(CallSession.STATUS_PREPARING,app,app.getTenant(),duocCallId, ProductCode.changeApiCmdToProductCode(apiCmd).name(),oneTelnumber,to1_uri);
        CallSession callSession2 = new CallSession(CallSession.STATUS_PREPARING,app,app.getTenant(),duocCallId, ProductCode.changeApiCmdToProductCode(apiCmd).name(),oneTelnumber,to2_uri);
        callSessionService.save(callSession);
        callSessionService.save(callSession2);

        Map<String, Object> params = new MapBuilder<String, Object>()
                .putIfNotEmpty("from1_uri", from1)
                .putIfNotEmpty("to1_uri",to1_uri)
                .putIfNotEmpty("from2_uri", from2)
                .putIfNotEmpty("to2_uri",to2_uri)
                .putIfNotEmpty("max_connect_seconds",max_call_duration)
                .putIfNotEmpty("max_ring_seconds",max_dial_duration)
                .putIfNotEmpty("user_data1",duocCallId)
                .putIfNotEmpty("user_data2",duocCallId)
                .putIfNotEmpty("areaId",areaId)
                .build();

        if(StringUtils.isNotBlank(ring_tone)){
            params.put("ring_play_file",ring_tone);
            params.put("ring_play_mode",ring_tone_mode);
        }
        //录音
        if(recording != null && recording){
            //TODO 录音文件名称
            params.put("record_file ",duocCallId);
            params.put("record_mode",record_mode);
            params.put("record_format ",1);
        }

        RPCRequest rpcrequest = RPCRequest.newRequest(ServiceConstants.MN_CH_EXT_DUO_CALLBACK, params);
        try {
            Map<String,Object> data = new MapBuilder<String,Object>()
                    .put(to1_uri,callSession.getId())
                    .put(to2_uri,callSession2.getId())
                    .build();
            //将数据存到redis
            BusinessState cache = new BusinessState.Builder()
                                    .setTenantId(app.getTenant().getId())
                                    .setAppId(appId)
                                    .setId(duocCallId)
                                    .setType(apiCmd)
                                    .setUserdata(user_data)
                                    .setCallBackUrl(app.getUrl())
                                    .setAreaId(areaId)
                                    .setLineGatewayId(lineId)
                                    .setBusinessData(data)
                                    .build();

            businessStateService.save(cache);

            rpcCaller.invoke(sessionContext, rpcrequest);

            return duocCallId;
        }catch(Exception e){
            logger.error("消息发送到区域失败:{}", rpcrequest,e);
            throw new InvokeCallException(e);
        }
    }

    @Override
    public void duoCallbackCancel(String ip, String appId, String callId) throws YunhuniApiException{
        App app = appService.findById(appId);
        if(app == null){
            throw new AppNotFoundException();
        }
        String whiteList = app.getWhiteList();
        if(StringUtils.isNotBlank(whiteList.trim())){
            if(!whiteList.contains(ip)){
                throw new IPNotInWhiteListException();
            }
        }
        String areaId = areaAndTelNumSelector.getAreaId(app);
        BusinessState businessState = businessStateService.get(callId);
        Map<String, Object> params = new MapBuilder<String, Object>()
                .put("res_id",businessState.getResId())
                .put("user_data ",businessState.getId())
                .put("areaId ",areaId)
                .build();
        RPCRequest rpcrequest = RPCRequest.newRequest(ServiceConstants.MN_CH_EXT_DUO_CALLBACK_CANCEL, params);
        try {
            rpcCaller.invoke(sessionContext, rpcrequest);
        }catch(Exception e){
            logger.error("消息发送到区域失败:{}", rpcrequest,e);
            throw new InvokeCallException(e);
        }

    }

    @Override
    public String notifyCall(String ip, String appId, String from,String to,String play_file,List<List<Object>> play_content,
                             Integer repeat,Integer max_dial_duration,String user_data) throws YunhuniApiException{
        String apiCmd = BusinessState.TYPE_NOTIFY_CALL;
        String callId;
        if(apiGwRedBlankNumService.isRedNum(to)){
            throw new NumberNotAllowToCallException();
        }
        App app = appService.findById(appId);
        if(app == null){
            throw new AppNotFoundException();
        }
        String whiteList = app.getWhiteList();
        if(StringUtils.isNotBlank(whiteList.trim())){
            if(!whiteList.contains(ip)){
                throw new IPNotInWhiteListException();
            }
        }

        if(!isEnableVoiceDirectlyService(app.getTenant().getId(),appId)){
            throw new AppServiceInvalidException();
        }

        boolean isAmountEnough = calCostService.isCallTimeRemainOrBalanceEnough(apiCmd, app.getTenant().getId());
        if(!isAmountEnough){
            throw new BalanceNotEnoughException();
        }

        AreaAndTelNumSelector.Selector selector = areaAndTelNumSelector.getTelnumberAndAreaId(app, from,to);
        String areaId = selector.getAreaId();
        String oneTelnumber = selector.getOneTelnumber();

        from = oneTelnumber;
        //TODO 获取线路IP和端口
        String lineId = selector.getLineId();
        String to_uri = selector.getToUri();
        //TODO 获取线路IP和端口

        //保存语音通知
        NotifyCall notifyCall = new NotifyCall(from,to_uri);
        notifyCallService.save(notifyCall);
        callId = notifyCall.getId();
        CallSession callSession = new CallSession(CallSession.STATUS_PREPARING,app,app.getTenant(),callId, ProductCode.changeApiCmdToProductCode(apiCmd).name(),oneTelnumber,to_uri);
        callSessionService.save(callSession);

        Map<String, Object> params = new MapBuilder<String, Object>()
                .putIfNotEmpty("from_uri", from)
                .putIfNotEmpty("from_uri", from)
                .putIfNotEmpty("to_uri", to_uri)
                .putIfNotEmpty("play_repeat",repeat)
                .putIfNotEmpty("max_ring_seconds",max_dial_duration)
                .putIfNotEmpty("user_data",callId)
                .putIfNotEmpty("play_content",this.getPlayContent(app.getTenant().getId(),appId,play_file,play_content))
                .putIfNotEmpty("areaId",areaId)
                .build();

        try {
            RPCRequest rpcrequest = RPCRequest.newRequest(ServiceConstants.MN_CH_EXT_NOTIFY_CALL, params);
            Map<String,Object> data = new MapBuilder<String,Object>()
                    .put("sessionid",callSession.getId())
                    .build();
            //将数据存到redis
            BusinessState cache = new BusinessState.Builder()
                                    .setTenantId(app.getTenant().getId())
                                    .setAppId(app.getId())
                                    .setId(callId)
                                    .setType(apiCmd)
                                    .setUserdata(user_data)
                                    .setCallBackUrl(app.getUrl())
                                    .setAreaId(areaId)
                                    .setLineGatewayId(lineId)
                                    .setBusinessData(data)
                                    .build();
            businessStateService.save(cache);


            rpcCaller.invoke(sessionContext, rpcrequest);
            return callId;
        }catch(Exception ex){
            throw new InvokeCallException(ex);
        }
    }

    @Override
    @Deprecated
    public String captchaCall(String ip, String appId, String from,String to,String verify_code,
                              String max_dial_duration,String max_keys,List<String> files,String user_data) throws YunhuniApiException{
        if(apiGwRedBlankNumService.isRedNum(to)){
            throw new NumberNotAllowToCallException();
        }
        App app = appService.findById(appId);
        if(app == null){
            throw new AppNotFoundException();
        }

        String whiteList = app.getWhiteList();
        if(StringUtils.isNotBlank(whiteList.trim())){
            if(!whiteList.contains(ip)){
                throw new IPNotInWhiteListException();
            }
        }
        if(!isEnableVoiceValidateService(app.getTenant().getId(),appId)){
            throw new AppServiceInvalidException();
        }

        boolean isAmountEnough = calCostService.isCallTimeRemainOrBalanceEnough(ProductCode.captcha_call.getApiCmd(), app.getTenant().getId());
        if(!isAmountEnough){
            throw new BalanceNotEnoughException();
        }

        //TODO 获取线路IP和端口
        //TODO 待定
        String callId = UUIDGenerator.uuid();
        //TODO
        AreaAndTelNumSelector.Selector selector = areaAndTelNumSelector.getTelnumberAndAreaId(app, from,to);
        String areaId = selector.getAreaId();
        String oneTelnumber = selector.getOneTelnumber();
        String lineId = selector.getLineId();

        Map<String, Object> params = new MapBuilder<String, Object>()
                .putIfNotEmpty("to_uri",selector.getToUri())
                .putIfNotEmpty("from_uri",oneTelnumber)
                .putIfNotEmpty("max_ring_seconds",max_dial_duration)
                .putIfNotEmpty("valid_keys",verify_code)
                .putIfNotEmpty("max_keys",max_keys)
                .putIfNotEmpty("user_data",callId)
                .putIfNotEmpty("areaId",areaId)
                .build();

        files = playFileUtil.convertArray(app.getTenant().getId(),appId,files);

        if(files!= null && files .size()>0){
            Object[][] plays = new Object[][]{new Object[]{StringUtils.join(files,"|"),7,""}};
            params.put("play_content", JSONUtil2.objectToJson(plays));
        }
        try {
            //找到合适的区域代理
            RPCRequest rpcrequest = RPCRequest.newRequest(ServiceConstants.MN_CH_EXT_CAPTCHA_CALL, params);
            rpcCaller.invoke(sessionContext, rpcrequest);
            //将数据存到redis
            BusinessState cache = new BusinessState.Builder()
                    .setTenantId(app.getTenant().getId())
                    .setAppId(app.getId())
                    .setId(callId)
                    .setType("captcha_call")
                    .setCallBackUrl(app.getUrl())
                    .setUserdata(user_data)
                    .setAreaId(areaId)
                    .setLineGatewayId(lineId)
                    .setBusinessData(new MapBuilder<String,Object>()
                            .put("from",oneTelnumber)
                            .put("to",to)
                            .build())
                    .build();
            businessStateService.save(cache);
            return callId;
        }catch(Exception ex){
            throw new InvokeCallException(ex);
        }
    }

    @Override
    public String verifyCall(String ip, String appId, String from, String to, Integer maxDialDuration, String verifyCode, String playFile, Integer repeat, String userData) throws YunhuniApiException {

        if(apiGwRedBlankNumService.isRedNum(to)){
            throw new NumberNotAllowToCallException();
        }
        App app = appService.findById(appId);
        if(app == null){
            throw new AppNotFoundException();
        }

        String whiteList = app.getWhiteList();
        if(StringUtils.isNotBlank(whiteList.trim())){
            if(!whiteList.contains(ip)){
                throw new IPNotInWhiteListException();
            }
        }

        if(!isEnableVoiceValidateService(app.getTenant().getId(),appId)){
            throw new AppServiceInvalidException();
        }

        boolean isAmountEnough = calCostService.isCallTimeRemainOrBalanceEnough(ProductCode.captcha_call.getApiCmd(), app.getTenant().getId());
        if(!isAmountEnough){
            throw new BalanceNotEnoughException();
        }

        AreaAndTelNumSelector.Selector selector = areaAndTelNumSelector.getTelnumberAndAreaId(app, from,to);
        String areaId = selector.getAreaId();
        String oneTelnumber = selector.getOneTelnumber();
        String lineId = selector.getLineId();

        CaptchaCall captchaCall = new CaptchaCall();
        captchaCall.setStartTime(new Date());
        captchaCall.setEndTime(null);
        captchaCall.setFromNum(oneTelnumber);
        captchaCall.setToNum(to);
        captchaCall.setHangupSide(null);
        captchaCall.setResId(null);
        captchaCall = captchaCallService.save(captchaCall);
        String callId = captchaCall.getId();

        CallSession callSession = new CallSession();
        callSession.setStatus(CallSession.STATUS_PREPARING);
        callSession.setFromNum(oneTelnumber);
        callSession.setToNum(selector.getToUri());
        callSession.setApp(app);
        callSession.setTenant(app.getTenant());
        callSession.setRelevanceId(callId);
        callSession.setType(CallSession.TYPE_VOICE_VOICECODE);
        callSession.setResId(null);
        callSession = callSessionService.save(callSession);

        Map<String, Object> params = new MapBuilder<String, Object>()
                .putIfNotEmpty("to_uri",selector.getToUri())
                .putIfNotEmpty("from_uri",oneTelnumber)
                .putIfNotEmpty("max_ring_seconds",maxDialDuration)
                .putIfNotEmpty("play_repeat",repeat)
                .putIfNotEmpty("user_data",callId)
                .putIfNotEmpty("areaId",areaId)
                .build();

        playFile = playFileUtil.convertArray(app.getTenant().getId(),appId,playFile);

        if(StringUtils.isNotBlank(playFile) && StringUtils.isNotBlank(verifyCode)){
            Object[][] plays = new Object[][]{new Object[]{playFile,0,""},new Object[]{verifyCode,1,""}};
            params.put("play_content", JSONUtil2.objectToJson(plays));
        }else if(StringUtils.isNotBlank(playFile)){
            Object[][] plays = new Object[][]{new Object[]{playFile,0,""}};
            params.put("play_content", JSONUtil2.objectToJson(plays));
        }else if(StringUtils.isNotBlank(verifyCode)){
            Object[][] plays = new Object[][]{new Object[]{verifyCode,1,""}};
            params.put("play_content", JSONUtil2.objectToJson(plays));
        }

        try {
            //找到合适的区域代理
            RPCRequest rpcrequest = RPCRequest.newRequest(ServiceConstants.MN_CH_EXT_VERIFY_CALL, params);
            rpcCaller.invoke(sessionContext, rpcrequest);
            //将数据存到redis
            BusinessState cache = new BusinessState.Builder()
                    .setTenantId(app.getTenant().getId())
                    .setAppId(app.getId())
                    .setId(callId)
                    .setType(BusinessState.TYPE_VERIFY_CALL)
                    .setCallBackUrl(app.getUrl())
                    .setUserdata(userData)
                    .setAreaId(areaId)
                    .setLineGatewayId(lineId)
                    .setBusinessData(new MapBuilder<String,Object>()
                            .putIfNotEmpty("from",oneTelnumber)
                            .putIfNotEmpty("to",to)
                            .putIfNotEmpty("sessionid",callSession.getId())
                            .build())
                    .build();
            businessStateService.save(cache);
            return callId;
        }catch(Exception ex){
            throw new InvokeCallException(ex);
        }
    }

    /**
     * 转换成cti接口要的二维数组字符串
     * @param tenantId
     * @param appId
     * @param play_file 播放文件列表
     * @param dtos 播放文件内容
     * @return
     */
    public String getPlayContent(String tenantId,String appId,String play_file,List<List<Object>> dtos) throws PlayFileNotExistsException {
        play_file = playFileUtil.convertArray(tenantId, appId, play_file);
        if(dtos == null){
            dtos = new ArrayList<>();
        }
        if(StringUtils.isNotBlank(play_file)){
            List<Object> playFile = new ArrayList<>();
            playFile.add(play_file);
            playFile.add(7);
            playFile.add("");
            dtos.add(0,playFile);
        }
        if(dtos != null){
            for(List<Object> play:dtos){
                if(play.get(1).equals(0)){
                    play.set(0,playFileUtil.convertArray(tenantId, appId, (String) play.get(0)));
                }
            }
        }
        return JSONUtil.objectToJson(dtos);
    }

}
