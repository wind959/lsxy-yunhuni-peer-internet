package com.lsxy.app.portal.rest.console.account;

import com.lsxy.framework.api.tenant.model.Account;
import com.lsxy.framework.api.tenant.model.RealnameCorp;
import com.lsxy.framework.api.tenant.model.RealnamePrivate;
import com.lsxy.framework.api.tenant.model.Tenant;
import com.lsxy.framework.config.SystemConfig;
import com.lsxy.framework.web.rest.RestRequest;
import com.lsxy.framework.web.rest.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.apache.zookeeper.ZooDefs.OpCode.auth;

/**
 * Created by zhangxb on 2016/6/24.
 * 实名认证
 */
@Controller
@RequestMapping("/console/account/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private  static final Integer AUTH_WAIT = 100;//个人认证等待中
    private  static final Integer  AUTH_NO= 0;//未认证
    private  static final Integer AUTH_COMPANY_FAIL = -2;//企业认证失败
    private  static final Integer AUTH_COMPANY_SUCESS = 2;//企业认证成功
    private  static final Integer AUTH_ONESELF_FAIL = -1;//个人认证失败
    private  static final Integer AUTH_ONESELF_SUCESS = 1;//个人认证成功
    private static final String UPLOAD_TYPE_FILE = "file";//文件上传类型之file
    private static final String UPLOAD_TYPE_OSS = "oss";//文件上传类型之oss
    private static final Integer AUTH_COMPANY=1;//认证类型-企业认证
    private static final Integer AUTH_ONESELF=0;//认证类型-个人认证
    private static final String IS_FALSE = "-1";//表示失败
    private static final String IS_TRUE = "1";//表示成功
    private String restPrefixUrl = SystemConfig.getProperty("portal.restful.url");
    /**
     * 实名认证首页
     * @param request
     * @return
     */
    @RequestMapping("/index" )
    public ModelAndView index(HttpServletRequest request){
        ModelAndView mav = new ModelAndView();
        //TODO 获取实名认证的状态
        String userName = "user001";
        //调resr接口
        String url = restPrefixUrl + "/rest/account/findByUserName";
        String token = "1234";
        Map map = new HashMap();
        map.put("userName",userName);
        RestResponse<Account> restResponse = RestRequest.buildSecurityRequest(token).post(url,map, Account.class);
        Account account = restResponse.getData();
        Tenant tenant = account.getTenant();
        if(tenant==null){//未实名认证
            // 未实名认证
            mav.setViewName("/console/account/auth/index");
        }else {
            int authStatus  = tenant.getIsRealAuth();
            if(AUTH_NO==authStatus) {
                mav.setViewName("/console/account/auth/index");
            }if (AUTH_WAIT ==authStatus ) {
                //审核中
                mav.setViewName("/console/account/auth/wait");
            } else if (AUTH_ONESELF_SUCESS == authStatus) {
                //TODO 个人实名认证
                mav.setViewName("/console/account/auth/sucess");
            } else if (AUTH_COMPANY_SUCESS == authStatus) {
                //TODO 企业实名认证
                mav.setViewName("/console/account/auth/sucess");
            } else if (AUTH_ONESELF_FAIL == authStatus) {
                //TODO 个人实名认证失败
                mav.addObject("msg","身份证与名称不符合，请重新提交资料认证");
                mav.setViewName("/console/account/auth/fail");
            } else if (AUTH_COMPANY_FAIL == authStatus) {
                //TODO 企业实名认证失败
                mav.addObject("msg","上传资料不符合要求，请重新提交资料认证");
                mav.setViewName("/console/account/auth/fail");
            }else{
                // 未实名认证
                mav.setViewName("/console/account/auth/index");
            }
        }
        return mav;
    }

    /**
     * 上次文件方法
     * @param file
     * @return
     */
    private String UploadFile(MultipartFile file){
        String tempPath ="";
        try{
            //实际上传文件地址，可根据需要从配置获取
            String svaeType = SystemConfig.getProperty("portal.realauth.resource.upload.mode");
            String savePath ="";
            if(UPLOAD_TYPE_FILE.equals(svaeType)){
                //上传到文件到指定位置
                savePath = SystemConfig.getProperty("portal.realauth.resource.upload.file.path");
                tempPath = savePath + file.getOriginalFilename();
                file.transferTo(new File(tempPath));
            }else if(UPLOAD_TYPE_OSS.equals(svaeType)) {
                //TODO OSS上创
            }
        }catch(Exception e){

        }
        return tempPath;
    }

    /**
     * 实名认证方法
     * @param request
     * @param authVo
     * @param type
     * @param multipartfiles
     * @return
     */
    @RequestMapping(value="/edit" ,method = RequestMethod.POST)
    public ModelAndView edit(HttpServletRequest request,AuthVo authVo,String type, @RequestParam("file") MultipartFile[] multipartfiles){

        //对上次文件进行处理
        if (null != multipartfiles && multipartfiles.length > 0) {
            if(Integer.valueOf(type)==0){
                authVo.setIdPhoto(UploadFile(multipartfiles[0]));
            }else if(Integer.valueOf(type)==1){
                authVo.setType01Prop01(UploadFile(multipartfiles[1]));
                authVo.setType03Prop02(UploadFile(multipartfiles[2]));
            }
        }

        String userName = "user001";
        String token = "1234";
        String  status = IS_TRUE;//默认操作成功
        if(Integer.valueOf(type)==AUTH_ONESELF){
            String url = restPrefixUrl + "/rest/account/auth/privateAuth";
            Map map = getRealnamePrivaateParams(authVo,userName,AUTH_WAIT);
            RestResponse<RealnamePrivate> restResponse = RestRequest.buildSecurityRequest(token).post(url,map, RealnamePrivate.class);
            RealnamePrivate realnamePrivate = restResponse.getData();
            if(realnamePrivate==null){
                status = IS_FALSE;
            }
        }else if(Integer.valueOf(type)==AUTH_COMPANY){
            //调resr接口
            String url = restPrefixUrl + "/rest/account/auth/corpAuth";
            Map map = getRealnameCorpParams(authVo,userName,AUTH_WAIT);
            RestResponse<RealnameCorp> restResponse = RestRequest.buildSecurityRequest(token).post(url,map, RealnameCorp.class);
            RealnameCorp realnameCorp = restResponse.getData();
            if(realnameCorp==null){
                status = IS_FALSE;
            }
        }
        if(IS_TRUE.equals(status)){
            return  new ModelAndView("redirect:/console/account/auth/index");
        }else{
            ModelAndView mav = new ModelAndView();
            mav.addObject("msg","操作失败，请稍后重试");
            mav.setViewName("/console/account/auth/index");
            return mav;
        }


    }

    /**
     * 组装个人实名认证的信息
     * @param authVo 实名认证vo对象
     * @param userName 用户名
     * @return
     */
    private Map getRealnamePrivaateParams(AuthVo authVo,String userName,int status){
        Map map = new HashMap();
        map.put("userName",userName);//用户账号
        map.put("status",status);//状态
        map.put("name",authVo.getPrivateName());//姓名
        map.put("idNumber", authVo.getIdNumber());//身份证号
        map.put("idPhoto",authVo.getIdPhoto());//身份证照片
        map.put("idType", authVo.getIdType());//证件类型
        return map;
    }
    private Map getRealnameCorpParams(AuthVo authVo,String userName,int status){
        Map map = new HashMap();
        map.put("userName",userName);//用户账号
        map.put("status",status);//状态
        map.put("corpName",authVo.getCorpName());//企业名称
        map.put("addr",authVo.getAddr());//企业地址
        map.put("fieldCode",authVo.getFieldCode());//所属行业
        map.put("authType",authVo.getAuthType());//认证类型
        map.put("type01Prop01",authVo.getType01Prop01());//[一照一码]营业执照照片
        map.put("type01Prop02",authVo.getType01Prop02());//[一照一码]统一社会信用代码
        map.put("type02Prop01",authVo.getType02Prop01());//[三证合一]注册号
        map.put("type02Prop02",authVo.getType02Prop02());//[三证合一]税务登记证号
        map.put("type03Prop02",authVo.getType03Prop02());//[三证分离]税务登记证照片
        return map;
    }
}
