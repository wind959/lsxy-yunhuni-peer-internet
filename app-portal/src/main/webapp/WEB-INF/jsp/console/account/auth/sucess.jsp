<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@include file="/inc/import.jsp" %>
<!DOCTYPE html>
<html>

<!-- header -->
<head>
    <%@include file="/inc/meta.jsp" %>

</head>
<body>
<section class="vbox">
<%@include file="/inc/headerNav.jsp"%>
<section class='aside-section'>
    <section class="hbox stretch">
        <!-- .aside -->
        <aside class="bg-Green lter aside hidden-print" id="nav"><%@include file="/inc/leftMenu.jsp"%></aside>
        <!-- /.aside -->

        <section id="content">
            <section class="hbox stretch">
                <!-- 如果没有三级导航 这段代码注释-->
                <aside class="bg-green lter aside-sm hidden-print ybox" id="subNav">
                    <section class="vbox">
                        <div class="wrapper header"><span class="margin_lr"></span><span class="margin_lr border-left">&nbsp;基本资料</span>
                        </div>
                        <section class="scrollable">
                            <div class="slim-scroll">
                                <!-- nav -->
                                <nav class="hidden-xs">
                                    <ul class="nav">
                                        <li>
                                            <div class="aside-li-a">
                                                <a href="${ctx}/console/account/safety/index">安全设置</a>
                                            </div>
                                        </li>
                                        <li>
                                            <div class="aside-li-a ">
                                                <a href="${ctx}/console/account/index">基本资料</a>
                                            </div>
                                        </li>
                                        <li>
                                            <div class="aside-li-a active">
                                                <a href="${ctx}/console/account/auth/index">实名认证</a>
                                            </div>
                                        </li>
                                    </ul>
                                </nav>
                            </div>
                        </section>
                    </section>
                </aside>
                <aside>
                    <section class="vbox xbox">
                        <!-- 如果没有三级导航 这段代码注释-->
                        <div class="head-box"> <a href="#subNav" data-toggle="class:hide"> <i class="fa fa-angle-left text"></i> <i class="fa fa-angle-right text-active"></i> </a> </div>
                        <section class=" w-f personal-auth">
                            <div class="wrapper header">
                                <span class="border-left">&nbsp;实名认证</span>
                                <div>
                                    <div style="margin-top:140px;width:100%;margin-left:-150px;text-align:center;">
                                        <c:if test="${status == 1}">
                                            <h2>个人实名认证完成</h2>
                                        </c:if>
                                        <c:if test="${status == 2}">
                                            <h2>公司实名认证完成</h2>
                                        </c:if>
                                    </div>
                                    <div class="auth authsuccess thumbnail" style="margin-top:0px;height: 230px;">
                                        <img class="fl" src="${resPrefixUrl }/images/personal/pass.png" width="190px">
                                        <div class="caption fr">
                                            <c:if test="${status == 1}">
                                                <ul>
                                                    <li>认证类型：个人认证</li>
                                                    <li>真实姓名：${name}</li>
                                                    <li>证件类型：
                                                        <c:if test="${idType == '0'}">身份证</c:if>
                                                        <c:if test="${idType == '1'}">护照</c:if>
                                                    </li>
                                                    <li>证件号码：${idNumber}</li>
                                                    <li>认证时间：${time}</li>
                                                </ul>
                                            </c:if>
                                            <c:if test="${status == 2}">
                                                <ul>
                                                    <li>认证类型：公司认证</li>
                                                    <li>公司名称：${name}</li>
                                                    <li>办公地址：${addr}</li>
                                                    <li>所属行业：${industry}</li>
                                                    <li>申请人：${proposer}</li>
                                                    <li>证件类型：
                                                        <c:if test="${authType == '0'}">三证合一（一照一码）</c:if>
                                                        <c:if test="${authType == '1'}">三证合一</c:if>
                                                        <c:if test="${authType == '2'}">三证分离</c:if>
                                                    </li>
                                                    <c:if test="${authType == '0'}">
                                                        <li>统一社会信用代码：${type01Prop02}</li>
                                                    </c:if>
                                                    <c:if test="${authType == '1'}">
                                                        <li>注册号：${type02Prop01}</li>
                                                        <li>税务登记号：${type02Prop02}</li>
                                                    </c:if>
                                                    <c:if test="${authType == '2'}">
                                                        <li>税务登记号：${type03Prop01}</li>
                                                        <li>营业执照号：${type03Prop03}</li>
                                                    </c:if>
                                                    <li>认证时间：${time}</li>
                                                </ul>
                                            </c:if>
                                        </div>
                                        <c:if test="${status == 1}">
                                            <div class="fl" style="margin-top: 40px;">
                                                <a href="/console/account/auth/index?upgrade=true" class="btn btn-primary btn-form">
                                                    升级公司认证
                                                </a>&nbsp;&nbsp;个人认证完成后，还可进行公司认证
                                            </div>
                                        </c:if>
                                    </div>
                                </div>

                            </div>
                        </section>
                    </section>
                </aside>
            </section>
        </section>
    </section>
</section>
</section>
<%@include file="/inc/footer.jsp"%>

<script type="text/javascript" src='${resPrefixUrl }/js/personal/auth.js'></script>
</body>
</html>