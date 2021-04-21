package com.hlife.qcloud.tim.uikit.config;

import java.io.Serializable;

public class ChatViewConfig implements Serializable {
    private boolean disableSendPhotoAction;//禁用发送图片
    private boolean disableCaptureAction;//禁用拍照
    private boolean disableVideoRecordAction;//禁用摄像
    private boolean disableSendFileAction;//禁用发文件
    private boolean disableSendLocationAction;//禁用发位置
    private boolean disableAudioCall = true;//禁用发实时音频
    private boolean disableVideoCall = true;//禁用发实时视频
    private boolean disableChatInput;//禁用聊天面板全部功能
    private boolean isCustomAtGroupMember;//是否开启自定义消息类型

    public void setCustomAtGroupMember(boolean customAtGroupMember) {
        isCustomAtGroupMember = customAtGroupMember;
    }

    public boolean isCustomAtGroupMember() {
        return isCustomAtGroupMember;
    }

    public boolean isDisableSendPhotoAction() {
        return disableSendPhotoAction;
    }

    public void setDisableSendPhotoAction(boolean disableSendPhotoAction) {
        this.disableSendPhotoAction = disableSendPhotoAction;
    }

    public boolean isDisableCaptureAction() {
        return disableCaptureAction;
    }

    public void setDisableCaptureAction(boolean disableCaptureAction) {
        this.disableCaptureAction = disableCaptureAction;
    }

    public boolean isDisableVideoRecordAction() {
        return disableVideoRecordAction;
    }

    public void setDisableVideoRecordAction(boolean disableVideoRecordAction) {
        this.disableVideoRecordAction = disableVideoRecordAction;
    }

    public boolean isDisableSendFileAction() {
        return disableSendFileAction;
    }

    public void setDisableSendFileAction(boolean disableSendFileAction) {
        this.disableSendFileAction = disableSendFileAction;
    }

    public boolean isDisableSendLocationAction() {
        return disableSendLocationAction;
    }

    public void setDisableSendLocationAction(boolean disableSendLocationAction) {
        this.disableSendLocationAction = disableSendLocationAction;
    }

    public boolean isDisableAudioCall() {
        return disableAudioCall;
    }

    public void setDisableAudioCall(boolean disableAudioCall) {
        this.disableAudioCall = disableAudioCall;
    }

    public boolean isDisableVideoCall() {
        return disableVideoCall;
    }

    public void setDisableVideoCall(boolean disableVideoCall) {
        this.disableVideoCall = disableVideoCall;
    }

    public void setDisableChatInput(boolean disableChatInput) {
        this.disableChatInput = disableChatInput;
    }

    public boolean isDisableChatInput() {
        return disableChatInput;
    }
}
