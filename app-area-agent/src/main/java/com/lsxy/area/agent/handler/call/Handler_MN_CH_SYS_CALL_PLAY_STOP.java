package com.lsxy.area.agent.handler.call;

import com.lsxy.app.area.cti.RpcError;
import com.lsxy.app.area.cti.RpcResultListener;
import com.lsxy.area.agent.cti.CTIClientContext;
import com.lsxy.area.agent.cti.CTINode;
import com.lsxy.framework.rpc.api.RPCRequest;
import com.lsxy.framework.rpc.api.RPCResponse;
import com.lsxy.framework.rpc.api.ServiceConstants;
import com.lsxy.framework.rpc.api.handler.RpcRequestHandler;
import com.lsxy.framework.rpc.api.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by liuws on 2016/8/27.
 */
@Component
public class Handler_MN_CH_SYS_CALL_PLAY_STOP extends RpcRequestHandler{

    private static final Logger logger = LoggerFactory.getLogger(Handler_MN_CH_SYS_CALL_PLAY_STOP.class);

    @Autowired
    private CTIClientContext cticlientContext;

    @Override
    public String getEventName() {
        return ServiceConstants.MN_CH_SYS_CALL_PLAY_STOP;
    }

    @Override
    public RPCResponse handle(RPCRequest request, Session session) {
        try {
            Map<String, Object> params = request.getParamMap();
            String call_id = (String)params.get("user_data");
            String res_id = (String)params.get("res_id");
            CTINode cticlient = cticlientContext.getAvalibleNode(res_id);
            cticlient.operateResource( res_id, "sys.call.play_stop", params, new RpcResultListener(){
                @Override
                protected void onResult(Object o) {
                    if(logger.isDebugEnabled()){
                        logger.debug("调用sys.call.play_stop成功call_id={},result={}",call_id,o);
                    }
                }

                @Override
                protected void onError(RpcError rpcError) {
                    logger.error("调用sys.call.play_stop失败call_id={},result={}",call_id,rpcError);
                }

                @Override
                protected void onTimeout() {
                    logger.error("调用sys.call.play_stop超时call_id={}",call_id);
                }
            });
        } catch (Throwable e) {
            logger.error("调用资源操作失败",e);
        }
        return null;
    }
}
