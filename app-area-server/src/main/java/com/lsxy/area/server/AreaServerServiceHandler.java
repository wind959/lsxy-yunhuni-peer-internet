package com.lsxy.area.server;

import com.lsxy.area.server.test.TestIncomingZB;
import com.lsxy.framework.rpc.api.RPCCaller;
import com.lsxy.framework.rpc.api.RPCRequest;
import com.lsxy.framework.rpc.api.RPCResponse;
import com.lsxy.framework.rpc.api.ServiceConstants;
import com.lsxy.framework.rpc.api.server.AbstractServiceHandler;
import com.lsxy.framework.rpc.api.server.Session;
import com.lsxy.framework.web.rest.RestRequest;
import com.lsxy.framework.web.rest.RestResponse;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by tandy on 16/8/8.
 */
@Component
public class AreaServerServiceHandler extends AbstractServiceHandler {


    private static final Logger logger = LoggerFactory.getLogger(AreaServerServiceHandler.class);

    @Autowired(required = false)
    private StasticsCounter sc;

    @Autowired
    private RPCCaller rpcCaller;

    @Autowired(required = false)
    private TestIncomingZB tzb;

    @Override
    public RPCResponse handleService(RPCRequest request, Session session) {

        if(sc != null) sc.getReceivedAreaNodeRequestCount().incrementAndGet();

        if(request.getName().equals(ServiceConstants.CH_MN_CTI_EVENT)){
            /*收到CTI事件次数*/
            if(sc != null)  sc.getReceivedAreaNodeCTIEventCount().incrementAndGet();

            return process_CH_MN_CTI_EVENT(request,session);
        }

        if(request.getName().equals(ServiceConstants.MN_CH_TEST_ECHO)){
            return process_MN_CH_TEST_ECHO(request,session);
        }


        RPCResponse response = RPCResponse.buildResponse(request);
        response.setMessage(RPCResponse.STATE_OK);
        return response;
    }

    private RPCResponse process_MN_CH_TEST_ECHO(RPCRequest request, Session session) {
        RPCResponse response = RPCResponse.buildResponse(request);
        response.setTimestamp(request.getTimestamp());
        return response;
    }

    /**
     * 处理CH_MN_CTI_EVENT CTI事件监听
     * 有CTI事件触发时,需要将事件告诉用户,让用户告知接下来如何处理
     * @param request
     * @param session
     * @return
     */
    private RPCResponse process_CH_MN_CTI_EVENT(RPCRequest request, Session session) {



        /*临时逻辑,如果收到是 incoming 事件  则概率性的挂断或接通
        sys.call.on_dial_completed
        sys.call.on_incoming
        sys.call.on_released
        */
        String method = (String) request.getParameter("method");
        if(logger.isDebugEnabled()){
            logger.debug("收到CTI事件:{}-----{}" , method,request.getParamMap());
        }

        if(method.equals("sys.call.on_incoming")){
            tzb.receivedIncoming(request);
            if(sc != null)  sc.getReceivedAreaNodeInComingEventCount().incrementAndGet();
            if(logger.isDebugEnabled()){
                logger.debug("<<<<<<INCOMING>>>>>>>");
            }
            RPCRequest sendRequest = randomRequest(request);
            try {
                if(logger.isDebugEnabled()){
                    logger.debug(">>>>>>>>发送指令到CTI:{}",sendRequest);
                }

                if(sendRequest!=null){
                      /*发送给区域的请求次数计数*/
                    if(sc!=null) sc.getSendAreaNodeRequestCount().incrementAndGet();
                    rpcCaller.invoke(session,sendRequest);
                }

            } catch (Exception e) {
                logger.error("发送区域的指令出现异常,指令发送失败:{}",sendRequest);
            }
        }else{
//            if(logger.isDebugEnabled()){
//                logger.debug("不是 INCOMING.........");
//            }
        }
        return null;
    }

    /**
     * 随机创建拒绝或挂断请求
     * @param request
     * @return
     */
    private RPCRequest randomRequest(RPCRequest request) {
//        if(logger.isDebugEnabled()){
//            logger.debug("请求远程接口参数:{}",request.getParamMap());
//        }
//
//        RestResponse<String> response = RestRequest.buildRequest().post("http://101.200.73.13:3000/incoming",request.getParamMap());
//
//        RPCRequest requestX = null;
//        if(response != null && response.isSuccess()){
//            String param = response.getData();
//            if(sc != null){
//                /*接听 挂机  次数计数*/
//                if(param.indexOf("sys.call.answer")>=0){
//                    if(sc != null)  sc.getSendAreaNodeSysAnswerCount().incrementAndGet();
//                }else if(param.indexOf("sys.call.drop")>=0){
//                    if(sc != null)  sc.getSendAreaNodeSysDropCount().incrementAndGet();
//                }
//            }
//            requestX = RPCRequest.newRequest(ServiceConstants.MN_CH_CTI_API,param);
//        }else {
//            logger.error("请求用户接口发生异常,用户接口没有READY!!!!!!!!");
//        }


//        int radom = RandomUtils.nextInt(0,100);
//        String  param = null;
////        if(radom % 2 == 0){
////            param = "method=sys.call.drop&res_id="+request.getParameter("res_id")+"&cause=603";
////            if(logger.isDebugEnabled()){
////                logger.debug("这个是挂断指令:{}",param);
////            }
////        }else{
////            param = "method=sys.call.answer&res_id="+request.getParameter("res_id")+"&max_answer_seconds=5&user_data=1234";
////            if(logger.isDebugEnabled()){
////                logger.debug("这个是接通指令:{}",param);
////            }
////        }
       String  param = "method=sys.call.drop&res_id="+request.getParameter("res_id")+"&cause=603";
        /*挂机指令计数*/
        if(sc != null) sc.getSendAreaNodeSysDropCount().incrementAndGet();
        RPCRequest requestX = RPCRequest.newRequest(ServiceConstants.MN_CH_CTI_API,param);

        return requestX;
    }

//    public static void main(String[] args) {
//        String url = "http://101.200.73.13:3000/incoming";
//        RestRequest request = RestRequest.buildRequest();
//        RestResponse<String> response = request.get(url,String.class);
//        System.out.println(response.getData());
//
//    }
}
