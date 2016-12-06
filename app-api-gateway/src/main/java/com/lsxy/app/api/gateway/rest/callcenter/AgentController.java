package com.lsxy.app.api.gateway.rest.callcenter;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lsxy.app.api.gateway.response.ApiGatewayResponse;
import com.lsxy.app.api.gateway.rest.AbstractAPIController;
import com.lsxy.app.api.gateway.rest.callcenter.vo.AgentSkillOptsVO;
import com.lsxy.app.api.gateway.rest.callcenter.vo.AgentSkillVO;
import com.lsxy.app.api.gateway.rest.callcenter.vo.AgentVO;
import com.lsxy.call.center.api.model.CallCenterAgent;
import com.lsxy.call.center.api.service.CallCenterAgentService;
import com.lsxy.framework.core.exceptions.api.RequestIllegalArgumentException;
import com.lsxy.framework.core.exceptions.api.YunhuniApiException;
import com.lsxy.framework.core.utils.BeanUtils;
import com.lsxy.framework.core.utils.Page;
import com.lsxy.yunhuni.api.app.model.App;
import com.lsxy.yunhuni.api.app.service.AppService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by liups on 2016/11/15.
 */
@RestController
public class AgentController extends AbstractAPIController {
    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);

    @Reference(timeout=3000,check = false,lazy = true)
    private CallCenterAgentService callCenterAgentService;
    @Autowired
    AppService appService;

    @RequestMapping(value = "/{account_id}/callcenter/agent",method = RequestMethod.POST)
    public ApiGatewayResponse login(HttpServletRequest request, @RequestBody CallCenterAgent agent, @RequestHeader("AppID") String appId) throws YunhuniApiException {
        App app = appService.findById(appId);
        agent.setAppId(appId);
        agent.setTenantId(app.getTenant().getId());
        callCenterAgentService.login(agent);
        return ApiGatewayResponse.success();
    }

    @RequestMapping(value = "/{account_id}/callcenter/agent/{agent_name}",method = RequestMethod.DELETE)
    public ApiGatewayResponse logout(HttpServletRequest request, @RequestHeader("AppID") String appId,
                                          @PathVariable("agent_name") String agentName,@RequestParam(value = "force",required = false) boolean force) throws YunhuniApiException {
        App app = appService.findById(appId);
        callCenterAgentService.logout(app.getTenant().getId(),appId,agentName,force);
        return ApiGatewayResponse.success();
    }

    @RequestMapping(value = "/{account_id}/callcenter/agent/{agent_name}/keepalive",method = RequestMethod.GET)
    public ApiGatewayResponse keepAlive(HttpServletRequest request, @RequestHeader("AppID") String appId,
                                          @PathVariable("agent_name") String agentName) throws YunhuniApiException {
        callCenterAgentService.keepAlive(appId,agentName);
        return ApiGatewayResponse.success();
    }

    @RequestMapping(value = "/{account_id}/callcenter/agent/{agent_name}",method = RequestMethod.GET)
    public ApiGatewayResponse get(HttpServletRequest request, @RequestHeader("AppID") String appId,
                                   @PathVariable("agent_name") String agentName) throws YunhuniApiException {
        CallCenterAgent agent = callCenterAgentService.get(appId,agentName);
        AgentVO agentVO = getAgentVO(agent);
        return ApiGatewayResponse.success(agentVO);
    }

    /**
     * 转成AgentVO字段
     * @param agent
     * @return
     */
    private AgentVO getAgentVO(CallCenterAgent agent) {
        AgentVO agentVO = new AgentVO();
        try {
            List<AgentSkillVO> skillVOs = new ArrayList<>();
            BeanUtils.copyProperties(agentVO,agent);
            List skills = agentVO.getSkills();
            skills.stream().forEach(skill ->{
                AgentSkillVO skillVO = new AgentSkillVO();
                try {
                    BeanUtils.copyProperties(skillVO,skill);
                    skillVOs.add(skillVO);
                } catch (Exception e) {
                }
            });
            agentVO.setSkills(skillVOs);
        } catch (Exception e) {
        }
        return agentVO;
    }

    @RequestMapping(value = "/{account_id}/callcenter/agent",method = RequestMethod.GET)
    public ApiGatewayResponse page(HttpServletRequest request, @RequestHeader("AppID") String appId,
                                   @RequestParam(defaultValue = "1",required = false) Integer  pageNo,
                                   @RequestParam(defaultValue = "20",required = false)  Integer pageSize) throws YunhuniApiException {
        Page page  = callCenterAgentService.getPage(appId,pageNo,pageSize);
        List<AgentVO> agentVOs = new ArrayList<>();
        List<CallCenterAgent> result = page.getResult();
        result.stream().forEach(agent -> agentVOs.add(getAgentVO(agent)));
        page.setResult(agentVOs);
        return ApiGatewayResponse.success(page);
    }

    @RequestMapping(value = "/{account_id}/callcenter/agent/{agent_name}/extension",method = RequestMethod.POST)
    public ApiGatewayResponse extension(HttpServletRequest request, @RequestHeader("AppID") String appId,
                                        @PathVariable("agent_name") String agentName,@RequestBody Map map) throws YunhuniApiException {
        String extensionId = (String) map.get("id");
        callCenterAgentService.extension(appId,agentName,extensionId);
        return ApiGatewayResponse.success();
    }

    @RequestMapping(value = "/{account_id}/callcenter/agent/{agent_name}/state",method = RequestMethod.POST)
    public ApiGatewayResponse state(HttpServletRequest request, @RequestHeader("AppID") String appId,
                                        @PathVariable("agent_name") String agentName,@RequestBody Map map) throws YunhuniApiException {
        String state = (String) map.get("state");
        //TODO 校验数据有效性
        if(StringUtils.isBlank(state)){
            throw new RequestIllegalArgumentException();
        }
        if(state.equals("busy") || state.equals("away") || state.equals("idle") || state.startsWith("busy/") || state.startsWith("away/")){
            callCenterAgentService.state(appId,agentName,state);
            return ApiGatewayResponse.success();
        }else{
            throw new RequestIllegalArgumentException();
        }
    }

    @RequestMapping(value = "/{account_id}/callcenter/agent/{agent_name}/skills",method = RequestMethod.POST)
    public ApiGatewayResponse skills(HttpServletRequest request, @RequestHeader("AppID") String appId,
                                    @PathVariable("agent_name") String agentName,@RequestBody AgentSkillOptsVO skillOpts) throws YunhuniApiException {
        //TODO 校验数据有效性
        if(skillOpts.getOpts() == null || skillOpts.getOpts().size() == 0){
            throw new RequestIllegalArgumentException();
        }
        App app = appService.findById(appId);
        callCenterAgentService.skills(app.getTenant().getId(),appId,agentName,skillOpts.getOpts());
        return ApiGatewayResponse.success();
    }

}
