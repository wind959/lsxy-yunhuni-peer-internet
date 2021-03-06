package com.lsxy.app.oc.rest.stastistic;


import com.alibaba.dubbo.config.annotation.Reference;
import com.lsxy.app.oc.base.AbstractRestController;
import com.lsxy.call.center.api.service.CallCenterConversationMemberService;
import com.lsxy.framework.api.tenant.model.Tenant;
import com.lsxy.framework.api.tenant.service.TenantService;
import com.lsxy.framework.config.SystemConfig;
import com.lsxy.framework.core.utils.*;
import com.lsxy.framework.mq.api.MQService;
import com.lsxy.framework.mq.events.portal.VoiceFileRecordSyncEvent;
import com.lsxy.framework.web.rest.RestRequest;
import com.lsxy.framework.web.rest.RestResponse;
import com.lsxy.msg.api.model.MsgSendDetail;
import com.lsxy.msg.api.model.MsgUserRequest;
import com.lsxy.msg.api.service.MsgSendDetailService;
import com.lsxy.msg.api.service.MsgUserRequestService;
import com.lsxy.yunhuni.api.app.model.App;
import com.lsxy.yunhuni.api.app.service.AppService;
import com.lsxy.yunhuni.api.file.model.VoiceFileRecord;
import com.lsxy.yunhuni.api.file.service.VoiceFileRecordService;
import com.lsxy.yunhuni.api.product.enums.ProductCode;
import com.lsxy.yunhuni.api.session.model.CallSession;
import com.lsxy.yunhuni.api.session.model.MeetingMember;
import com.lsxy.yunhuni.api.session.model.VoiceCdr;
import com.lsxy.yunhuni.api.session.service.MeetingMemberService;
import com.lsxy.yunhuni.api.session.service.VoiceCdrService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;

import static com.lsxy.yunhuni.api.product.enums.ProductCode.call_center;

/**
 * 详单查询
 * Created by zhangxb on 2016/7/6.
 */
@RequestMapping("/tenant")
@Api(value = "会话详单", description = "租户中心相关的接口" )
@RestController
public class BillDetailController extends AbstractRestController {
    private static final Logger logger = LoggerFactory.getLogger(BillDetailController.class);
    @Autowired
    VoiceCdrService voiceCdrService;
    @Autowired
    VoiceFileRecordService voiceFileRecordService;
    @Autowired
    MeetingMemberService meetingMemberService;
    @Reference(timeout=3000,check = false,lazy = true)
    private CallCenterConversationMemberService callCenterConversationMemberService;
    @Autowired
    MQService mqService;
    @Autowired
    TenantService tenantService;
    @Autowired
    AppService appService;
    @Reference(timeout=3000,check = false,lazy = true)
    private MsgSendDetailService msgSendDetailService;
    @Reference(timeout=3000,check = false,lazy = true)
    private MsgUserRequestService msgUserRequestService;
    /**
     * 会话详单查询
     * @param pageNo 第几页
     * @param pageSize 每页记录数
     * @param time 时间
     * @param appId 应用id
     * @param type 选择类型
     * @return total 总计数，page分页数
     */
    @RequestMapping(value = "/{uid}/session" ,method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "会话详单查询")
    public RestResponse call(
            @ApiParam(name = "uid",value = "用户id")
            @PathVariable String uid,
            @ApiParam(name = "type",value = "notify_call.语音通知,duo_call.双向回拨,sys_conf.会议服务,ivr_call.IVR定制服务,captcha_call.语音验证码,voice_recording.录音服务call_center呼叫中心类型")
            @RequestParam String type,
            @ApiParam(name = "time",value = "yyyy-MM-dd")
            @RequestParam(required=false) String time,
            @ApiParam(name = "time2",value = "yyyy-MM-dd")
            @RequestParam(required=false) String time2,
            @ApiParam(name = "appId",value = "应用id,当应用id为all时，表示全部")
            @RequestParam(required=false)String appId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize
    ){
        Map re = new HashMap();
        RestResponse restResponse = null;
        if(StringUtil.isNotEmpty(appId)&&StringUtil.isNotEmpty(type)){
            Date startTime = DateUtils.parseDate(time,"yyyy-MM-dd");
            Date endTime = DateUtils.parseDate(time2+" 23:59:59","yyyy-MM-dd HH:mm:ss");
            //获取分页数据
            Page page = voiceCdrService.pageList(pageNo, pageSize, type, uid, startTime,endTime, appId);
            re.put("page", page);
            if (CallSession.TYPE_VOICE_VOICECODE.equals(type)) {//语音验证码
                re.put("total", page.getTotalCount());
            } else {
                Map map = voiceCdrService.sumCost(type, uid, startTime,endTime, appId);
                re.put("total", map.get("cost"));
            }
            restResponse = RestResponse.success(re);
        }else{
            restResponse = RestResponse.failed("0","上传参数错误");
        }
        return restResponse;
    }
    @RequestMapping(value = "/{uid}/session/voice_recording" ,method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "会话详单之录音查询")
    public RestResponse voice_recording(
            @ApiParam(name = "uid",value = "用户id")
            @PathVariable String uid,
            @ApiParam(name = "type",value = "为空或者是返回的types里面的值")
            @RequestParam(required = false) String type,
            @ApiParam(name = "time",value = "yyyy-MM-dd")
            @RequestParam(required=false) String time,
            @ApiParam(name = "time2",value = "yyyy-MM-dd")
            @RequestParam(required=false) String time2,
            @ApiParam(name = "appId",value = "应用id,当应用id为空时，表示全部")
            @RequestParam(required=false)String appId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize
    ){
        Date start = null;
        Date end = null;
        try{
            start = DateUtils.parseDate(time+" 00:00:00","yyyy-MM-dd HH:mm:ss");
            end = DateUtils.parseDate(time2+" 23:59:59","yyyy-MM-dd HH:mm:ss");
        }catch (Exception e){
            return RestResponse.failed("0000","日期格式错误");
        }
        Page<Map> page = voiceFileRecordService.getPageList(pageNo,pageSize,appId,uid,type,start,end);
        Map map = voiceFileRecordService.sumAndCount(appId,uid,type,start,end);
        Map re = new HashMap();
        re.put("page",page);
        re.put("total",map);
        String serviceType = "";
        if(StringUtils.isNotEmpty(appId)) {
            App app = appService.findById(appId);
            serviceType = app.getServiceType();
        }
        re.put("types",VoiceFileRecord.getRecordType(serviceType));
        return RestResponse.success(re);
    }
    @RequestMapping(value = "/{uid}/file/download/{id}" ,method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "会话详单之录音文件下载")
    public RestResponse fileDownload( @ApiParam(name = "uid",value = "用户id")@PathVariable String uid, @ApiParam(name = "id",value = "记录id") @PathVariable String id){
        Tenant tenant = tenantService.findById(uid);
        VoiceFileRecord voiceFileRecord = voiceFileRecordService.findById(id);
        if(tenant==null||voiceFileRecord==null||!tenant.getId().equals(voiceFileRecord.getTenantId())){
            return RestResponse.failed("0000","验证失败，无法下载");
        }
        if(voiceFileRecord.getStatus()!=null&&voiceFileRecord.getStatus()==1){
            String ossUri = OssTempUriUtils.getOssTempUri(voiceFileRecord.getOssUrl());
            if(logger.isDebugEnabled()) {
                logger.debug("生成ossUri地址：[{}]", ossUri);
            }
            return RestResponse.success(ossUri);
        }
        List<VoiceFileRecord> list = voiceFileRecordService.getListBySessionId(voiceFileRecord.getSessionId());
        if(list==null||list.size()==0){
            return RestResponse.failed("0000","无对应的录音文件");
        }
        //先判断是否文件已上传，如果是的话，直接生成临时下载链接，否则
        boolean flag = false;
        for(int i=0;i<list.size();i++){
            VoiceFileRecord temp = list.get(i);
            if(temp.getStatus()==null||1!=temp.getStatus()){
                temp.setStatus(0);
                voiceFileRecordService.save(temp);
                flag=true;
                break;
            }
        }
        //发起文件上传
        if(flag) {
            mqService.publish(new VoiceFileRecordSyncEvent(tenant.getId(), voiceFileRecord.getAppId(), voiceFileRecord.getId(), VoiceFileRecordSyncEvent.TYPE_FILE));
            return RestResponse.failed("0004", id);
        }else {
            String ossUri = OssTempUriUtils.getOssTempUri(list.get(0).getOssUrl());
            return RestResponse.success(ossUri);
        }
    }

    @RequestMapping(value = "/polling/{id}",method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "轮询下载结果")
    public RestResponse fileDownloadPolling(@PathVariable String id){
        VoiceFileRecord v1 = voiceFileRecordService.findById(id);
        if(v1 != null) {
            if (v1.getStatus() != null) {
                if (v1.getStatus() == 1) {
                    String ossUri = OssTempUriUtils.getOssTempUri(v1.getOssUrl());
                    return RestResponse.success(ossUri);
                } else if (v1.getStatus() == -1) {
                    return RestResponse.failed("0001", "下载失败，请稍后重试");
                }
            }
            return RestResponse.failed("0002","查询中，请稍后重试");
        }else{
            return RestResponse.failed("0001", "下载失败，对象不存在");
        }
    }

    @RequestMapping(value = "/{uid}/cdr/download/{id}" ,method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "会话详单之CDR文件（非录音文件）下载")
    public RestResponse cdrDownload(@ApiParam(name = "uid",value = "用户id")@PathVariable String uid, @ApiParam(name = "id",value = "记录id") @PathVariable String id){
        Tenant tenant = tenantService.findById(uid);
        VoiceCdr voiceCdr = voiceCdrService.findById(id);
        if(tenant==null||voiceCdr==null||!tenant.getId().equals(voiceCdr.getTenantId())){
            return RestResponse.failed("0000","验证失败，无法下载");
        }
        List<VoiceFileRecord> list = getFile(voiceCdr);
        if(list==null||list.size()==0){
            //TODO 更新CDR
            voiceCdr.setRecording(0);
            voiceCdrService.save(voiceCdr);
            return RestResponse.failed("0401","无对应的录音文件");
        }
        //先判断是否文件已上传，如果是的话，直接生成临时下载链接，否则
        boolean flag = false;
        for(int i=0;i<list.size();i++){
            VoiceFileRecord voiceFileRecord = list.get(i);
            if(voiceFileRecord.getStatus()==null || 1!=voiceFileRecord.getStatus()){
                flag=true;
                break;
            }
        }
        //发起文件上传
        if(flag) {
            VoiceFileRecord temp = voiceFileRecordService.findById(list.get(0).getId());
            temp.setStatus(0);
            voiceFileRecordService.save(temp);
            mqService.publish(new VoiceFileRecordSyncEvent(tenant.getId(), voiceCdr.getAppId(), voiceCdr.getId(), VoiceFileRecordSyncEvent.TYPE_CDR));
            return RestResponse.failed("0004", list.get(0).getId());
        }else {
            String ossUri = OssTempUriUtils.getOssTempUri(list.get(0).getOssUrl());
            return RestResponse.success(ossUri);
        }
    }
    private List<VoiceFileRecord> getFile(VoiceCdr voiceCdr){
        //根据cdr获取业务类型，和业务id，根据业务id和业务类型获取录音文件列表，
        List list = null;
        if(voiceCdr!=null&& StringUtils.isNotEmpty(voiceCdr.getId())) {
            ProductCode p1;
            if(ProductCode.call_center_sip.name().equals(voiceCdr.getType())){
                p1 = ProductCode.call_center;
            }else {
                p1 = ProductCode.valueOf(voiceCdr.getType());
            }
            switch(p1){
                case sys_conf:{
                    //获取会议操作者
                    MeetingMember meetingMember = meetingMemberService.findBySessionId(voiceCdr.getSessionId());
                    if (meetingMember!=null) {
                        //使用会议id
                        list = voiceFileRecordService.getListBySessionId(meetingMember.getMeetingId());
                    }
                    break;
                }
                case ivr_call:{
                    //使用ivr的id
                    list = voiceFileRecordService.getListBySessionId(voiceCdr.getRelevanceId());
                    break;
                }
                case duo_call:{
                    //使用双向回拨的id
                    list = voiceFileRecordService.getListBySessionId(voiceCdr.getRelevanceId());
                    break;
                }
                case call_center:{
                    //根据sessionid获取呼叫中心交互成员，在获取呼叫中心交谈，在获取文件
                    List<String> temp = callCenterConversationMemberService.getListBySessionId(voiceCdr.getSessionId());
                    if (temp!=null&&temp.size() == 0) {
                        return null;
                    }
                    list = voiceFileRecordService.getListBySessionId( temp.toArray(new String[0]));
                    break;
                }
            }
        }
        return list;
    }
    protected String getOssTempUri(String resource){
        String host = SystemConfig.getProperty("global.oss.aliyun.endpoint.internet","http://oss-cn-beijing.aliyuncs.com");
        String accessId = SystemConfig.getProperty("global.aliyun.key","nfgEUCKyOdVMVbqQ");
        String accessKey = SystemConfig.getProperty("global.aliyun.secret","HhmxAMZ2jCrE0fTa2kh9CLXF9JPcOW");
        String resource1 = SystemConfig.getProperty("global.oss.aliyun.bucket");
        try {
            URL url = new URL(host);
            host = url.getHost();
        }catch (Exception e){}
        resource = "/"+resource1+"/"+resource;
        String result = OssTempUriUtils.getOssTempUri( accessId, accessKey, host, "GET",resource ,60);
        return result;
    }
    @RequestMapping(value = "/{uid}/session/msg/{type}" ,method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "会话详单之消息类型查询")
    public RestResponse msg(
            @ApiParam(name = "uid",value = "用户id")
            @PathVariable String uid,
            @ApiParam(name = "type",value = "短信msg_sms,闪印msg_ussd")
            @RequestParam(required = false) String type,
            @ApiParam(name = "isMass",value = "0单发1群发")
            @RequestParam(required = false) int isMass,
            @ApiParam(name = "taskName",value = "任务名")
            @RequestParam(required = false) String taskName,
            @ApiParam(name = "mobile",value = "手机号码")
            @RequestParam(required = false) String mobile,
            @ApiParam(name = "time",value = "yyyy-MM-dd")
            @RequestParam(required=false) String time,
            @ApiParam(name = "time2",value = "yyyy-MM-dd")
            @RequestParam(required=false) String time2,
            @ApiParam(name = "appId",value = "应用id,当应用id为空时，表示全部")
            @RequestParam(required=false)String appId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize
    ){
        Date start = null;
        Date end = null;
        try{
            start = DateUtils.parseDate(time+" 00:00:00","yyyy-MM-dd HH:mm:ss");
            end = DateUtils.parseDate(time2+" 23:59:59","yyyy-MM-dd HH:mm:ss");
        }catch (Exception e){
            return RestResponse.failed("0000","日期格式错误");
        }
        Page page = msgUserRequestService.getPageByCondition( pageNo,  pageSize,type, appId, start,  end,  isMass,  taskName,  mobile,uid );
        return RestResponse.success(page);
    }
    @RequestMapping(value = "/{uid}/session/msg/detail/{msgKey}" ,method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "会话详单之消息之群发详单查询")
    public RestResponse msg(
            @ApiParam(name = "uid",value = "用户id")
            @PathVariable String uid,
            @ApiParam(name = "msgKey",value = "记录标识")
            @RequestParam(required = false) String msgKey,
            @ApiParam(name = "state",value = "发送状态-1失败0等待1成功")
            @RequestParam(required = false) int state,
            @ApiParam(name = "mobile",value = "手机号码")
            @RequestParam(required = false) String mobile,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize
    ){
        Page page = msgSendDetailService.getPageByContiton( pageNo,  pageSize,msgKey,  mobile,state+"" );
        return RestResponse.success(page);
    }
    @RequestMapping(value = "/{uid}/session/msg/download/{id}" ,method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "会话详单之消息之群发详单下载")
    public String downloadMsg(
            HttpServletRequest request, HttpServletResponse response,
            @ApiParam(name = "uid",value = "用户id") @PathVariable String uid,
            @ApiParam(name = "id",value = "记录ID") @RequestParam(required = false) String id
    ){
        MsgUserRequest msgUserRequest = msgUserRequestService.findById(id);
        if(msgUserRequest != null){
            String title = "msg".equals(msgUserRequest.getSendType())?"短信":"闪印" ;
            String one = "任务名字："+msgUserRequest.getTaskName()+"  任务状态："+( msgUserRequest.getState()==MsgUserRequest.STATE_FAIL ? "任务失败":(msgUserRequest.getState()==MsgUserRequest.STATE_SUCCESS?"任务结束":"待处理"))+"  成功数："+msgUserRequest.getSuccNum()+" 失败数："+msgUserRequest.getFailNum()+" 待发数："+msgUserRequest.getPendingNum();
            String[] headers =  new String[]{"手机号码","发送结果","原因"};
            String[] values = new String[]{"mobile","state:-1=发送失败;0=未发送;1=发送失败"};
            List list = msgSendDetailService.findByMsgKey(msgUserRequest.getMsgKey());
            downloadExcel(title,one,headers,values,list,null,"cost",response);
        }
        return "";
    }
    /**
     * 导出文件
     * @param response
     */
    public <T>  void downloadExcel(String title, String one, String[] headers, String[] values, Collection<T> dataset, String pattern, String money, HttpServletResponse response) {
        try {
            Date d = new Date();
            String name = org.apache.http.client.utils.DateUtils.formatDate(d,"yyyyMMdd")+ d.getTime();
            HSSFWorkbook wb = ExportExcel.exportExcel(title,one,headers,values,dataset,pattern,money);
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-disposition", "attachment;filename="+name+".xls");
            OutputStream ouputStream = response.getOutputStream();
            wb.write(ouputStream);
            ouputStream.flush();
            ouputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
