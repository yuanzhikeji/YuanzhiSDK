package com.work.api.open;


import com.http.network.RequestParams;
import com.http.network.listener.OnResultDataListener;
import com.http.network.task.ConnectDataTask;
import com.work.api.open.contacts.ModeApi;
import com.work.api.open.model.AddApplyStaticsReq;
import com.work.api.open.model.GetImageConfigResp;
import com.work.api.open.model.GetTenantGroupListReq;
import com.work.api.open.model.BaseReq;
import com.work.api.open.model.BaseResp;
import com.work.api.open.model.CheckToolTokenReq;
import com.work.api.open.model.CheckToolTokenResp;
import com.work.api.open.model.CreateGroupReq;
import com.work.api.open.model.CreateGroupResp;
import com.work.api.open.model.DestroyGroupReq;
import com.work.api.open.model.GetCarWebViewUrlResp;
import com.work.api.open.model.GetCityListResp;
import com.work.api.open.model.GetFriendByMobileResp;
import com.work.api.open.model.GetGroupMsgReq;
import com.work.api.open.model.GetGroupMsgResp;
import com.work.api.open.model.GetTenantGroupListResp;
import com.work.api.open.model.GetToolListByUserIdResp;
import com.work.api.open.model.GetToolTokenReq;
import com.work.api.open.model.GetToolTokenResp;
import com.work.api.open.model.GetUserByParamReq;
import com.work.api.open.model.GetUserByParamResp;
import com.work.api.open.model.GetUserListByMobilesReq;
import com.work.api.open.model.GetUserListByMobilesResp;
import com.work.api.open.model.GetVersionResp;
import com.work.api.open.model.GroupMemberResp;
import com.work.api.open.model.InviteUserReq;
import com.work.api.open.model.LoginReq;
import com.work.api.open.model.LoginResp;
import com.work.api.open.model.RegisterReq;
import com.work.api.open.model.SendGroupMessageReq;
import com.work.api.open.model.SendMessageReq;
import com.work.api.open.model.SendSmsReq;
import com.work.api.open.model.SysUserReq;
import com.work.api.open.model.UpdateMobileReq;
import com.work.api.open.model.UploadResp;
import com.work.util.SLog;
import com.work.util.SharedUtils;

import java.io.File;

/**
 * Created by Administrator on 2019/4/2
 * Description
 */

public class Yz extends ApiClient {

    private static Yz INSTANCE;
    public static Yz getSession(){
        return INSTANCE==null?INSTANCE = new Yz():INSTANCE;
    }
    /**
     * ???????????????
     */
    public void sendSms(SendSmsReq sendSmsReq, OnResultDataListener onResultDataListener){
        requestPost(ModeApi.sendSms,sendSmsReq,new BaseResp(),onResultDataListener);
    }
    /**
     * ??????
     */
    public void register(RegisterReq registerReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.register,registerReq,new LoginResp(),onResultDataListener);
    }
    /**
     * ????????????
     */
    public void resetPwd(RegisterReq registerReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.resetPwd,registerReq,new BaseResp(),onResultDataListener);
    }
    /**
     * ??????
     */
    public void login(LoginReq loginReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.login,loginReq,new LoginResp(),onResultDataListener);
    }
    /**
     * ????????????
     */
    public void update(RegisterReq registerReq,OnResultDataListener onResultDataListener,Object... objects){
        requestPost(ModeApi.update,registerReq,new BaseResp(),onResultDataListener,objects);
    }
    /**
     * ????????????
     */
    public void updatePwd(RegisterReq registerReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.updatePwd,registerReq,new BaseResp(),onResultDataListener);
    }
    /**
     * ????????????????????????
     */
    public void getUserByUserId(LoginReq loginReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.getUserByUserId,loginReq,new LoginResp(),onResultDataListener);
    }
    /**
     * ??????????????????????????????
     */
    public void getUserByMobile(LoginReq loginReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.getUserByMobile,loginReq,new LoginResp(),onResultDataListener);
    }
    /**
     * ????????????
     */
    public void getUserByParam(GetUserByParamReq getUserByParamReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.getUserByParam,getUserByParamReq,new GetUserByParamResp(),onResultDataListener);
    }
    /**
     * ???????????????
     */
    public void updateMobile(UpdateMobileReq registerReq, OnResultDataListener onResultDataListener){
        requestPost(ModeApi.updateMobile,registerReq,new BaseResp(),onResultDataListener);
    }
    /**
     * ?????????
     */
    public void getToolListByUserId(OnResultDataListener onResultDataListener){
        requestPost(ModeApi.getToolListByUserId,new BaseReq(),new GetToolListByUserIdResp(),onResultDataListener);
    }
    /**
     * ?????????
     */
    public void createGroup(CreateGroupReq createGroupReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.createGroup,createGroupReq,new CreateGroupResp(),onResultDataListener);
    }
    /**
     * ???????????????
     */
//    public void updateGroupName(CreateGroupReq createGroupReq,OnResultDataListener onResultDataListener){
//        requestPost(ModeApi.updateGroupName,createGroupReq,new CreateGroupResp(),onResultDataListener);
//    }
    /**
     * ???????????????
     */
//    public void addGroupUser(CreateGroupReq createGroupReq,OnResultDataListener onResultDataListener){
//        requestPost(ModeApi.addGroupUser,createGroupReq,new GroupMemberResp(),onResultDataListener);
//    }
    /**
     * ?????????????????????
     */
    public void deleteGroupUser(CreateGroupReq createGroupReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.deleteGroupUser,createGroupReq,new GroupMemberResp(),onResultDataListener);
    }
    /**
     * ????????????
     */
    public void destroyGroup(DestroyGroupReq destroyGroupReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.destroyGroupdestroyGroup,destroyGroupReq,new BaseResp(),onResultDataListener);
    }
    /**
     * ???????????????
     */
    public void getGroupMsg(GetGroupMsgReq getGroupMsgReq, OnResultDataListener onResultDataListener){
        requestPost(ModeApi.getGroupMsg,getGroupMsgReq,new GetGroupMsgResp(),onResultDataListener);
    }
//    /**
//     * ???????????????
//     */
//    public void updateGroup(CreateGroupReq createGroupReq,OnResultDataListener onResultDataListener){
//        requestPost(ModeApi.updateGroup,createGroupReq,new BaseResp(),onResultDataListener);
//    }
    /**
     * ??????????????????
     */
    public void getVersion(OnResultDataListener onResultDataListener){
        requestPost(ModeApi.getVersion,new BaseReq(),new GetVersionResp(),onResultDataListener);
    }
    /**
     * ??????token
     */
    public void getToolToken(GetToolTokenReq getToolTokenReq,OnResultDataListener onResultDataListener,Object... objects){
        requestPost(ModeApi.getToolToken,getToolTokenReq,new GetToolTokenResp(),onResultDataListener,objects);
    }
    /**
     * ??????????????????
     */
    public void getFriendByMobile(GetUserByParamReq getUserByParamReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.getFriendByMobile,getUserByParamReq,new GetFriendByMobileResp(),onResultDataListener);
    }
    /**
     * ????????????
     */
    public void sysUser(SysUserReq sysUserReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.sysUser,sysUserReq,new LoginResp(),onResultDataListener);
    }
    /**
     * ????????????
     */
    public void addApplyStatics(AddApplyStaticsReq addApplyStaticsReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.addApplyStatics,addApplyStaticsReq,new BaseResp(),onResultDataListener);
    }
    /**
     * ??????JSKey
     */
    public void checkToolToken(CheckToolTokenReq checkToolTokenReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.checkToolToken,checkToolTokenReq,new CheckToolTokenResp(),onResultDataListener);
    }
    /**
     * ?????????????????????
     */
    public void getUserListByMobiles(GetUserListByMobilesReq getUserListByMobilesReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.getUserListByMobiles,getUserListByMobilesReq,new GetUserListByMobilesResp(),onResultDataListener);
    }
    /**
     * ????????????
     */
    public void inviteUser(InviteUserReq inviteUserReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.inviteUser,inviteUserReq,new BaseResp(),onResultDataListener);
    }
    /**
     * ????????????
     */
    public void getCityList(OnResultDataListener onResultDataListener){
        requestPost(ModeApi.getCityList,new BaseReq(),new GetCityListResp(),onResultDataListener);
    }
    /**
     * ??????oss??????
     */
    public void getImageConfig(OnResultDataListener onResultDataListener,Object... objects){
        requestPost(ModeApi.getStsToken,new BaseReq(),new GetImageConfigResp(),onResultDataListener,objects);
    }
    /**
     * ??????????????????
     */
    public void sendMessage(SendMessageReq sendMessageReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.sendTextFormToMsg,sendMessageReq,new BaseResp(),onResultDataListener);
    }
    /**
     * ????????????????????????
     */
    public void sendCustomGroupTextMsg(SendGroupMessageReq sendGroupMessageReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.sendCustomGroupTextMsg,sendGroupMessageReq,new BaseResp(),onResultDataListener);
    }
    /**
     * ????????????
     */
    public void getTenantGroupList(GetTenantGroupListReq getTenantGroupListReq,OnResultDataListener onResultDataListener){
        requestPost(ModeApi.getTenantGroupList,getTenantGroupListReq,new GetTenantGroupListResp(),onResultDataListener);
    }
    /**
     * ??????????????????
     */
    public void getCarWebViewUrl(String url,OnResultDataListener onResultDataListener){
        RequestParams params = new RequestParams();
        params.url = url;
        params.onResultDataListener = onResultDataListener;
        params.req = new BaseReq();
        params.resp = new GetCarWebViewUrlResp();
        ConnectDataTask dataTask = new ConnectDataTask(params);
        dataTask.doGet();
    }
    /**
     * ????????????
     */
    public void upload(String path,OnResultDataListener onResultDataListener){
        RequestParams params = new RequestParams();
        params.url = clientModelUrl(ModeApi.upload);
        params.onResultDataListener = onResultDataListener;
        params.req = new BaseReq();
        params.resp = new UploadResp();
        if(SLog.debug)SLog.e("Upload File:"+path);
        params.addFileParam("file",new File(path));
        String token = SharedUtils.getString("userToken");
        params.addTextParam("token",token);
        params.addHeader("token",token);
        ConnectDataTask dataTask = new ConnectDataTask(params);
        dataTask.uploadFile();
    }
}
