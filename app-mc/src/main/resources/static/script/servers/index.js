function startServer(){
    var btnStartServer = $(this);
    var host = btnStartServer.attr("host");
    var app = btnStartServer.attr("app");
    var url = ctx + "admin/server/start?host="+host+"&app="+app;

    $.get( url, function( data ) {
        if(data && data.success){
            alert('启动成功');
        }else{
            alert('启动失败:' + data.errorMsg);
        }
        location.reload();
    });

}
function updateServer(){
    var btnStartServer = $(this);
    var host = btnStartServer.attr("host");
    var app = btnStartServer.attr("app");
    var url = ctx + "admin/server/update?host="+host+"&app="+app;

    $.get( url, function( data ) {
        if(data && data.success){
            alert('更新成功');
        }else{
            alert('更新失败:'+data.errorMsg);
        }
        location.reload();
    });

}

function stopServer(){
    var btnStartServer = $(this);
    var host = btnStartServer.attr("host");
    var app = btnStartServer.attr("app");
    var url = ctx + "admin/server/stop?host="+host+"&app="+app;

    $.get( url, function( data ) {
        if(data && data.success){
            alert('操作成功');
        }else{
            alert('操作失败');
        }
        location.reload();
    });
}

$(function() {
    $(".btnStartServer").bind("click",startServer);
    $(".btnUpdateServer").bind("click",updateServer);
    $(".btnStopServer").bind("click",stopServer);

});