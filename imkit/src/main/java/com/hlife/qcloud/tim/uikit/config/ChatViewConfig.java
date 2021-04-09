package com.hlife.qcloud.tim.uikit.config;

import java.io.Serializable;

public class ChatViewConfig implements Serializable {
    private boolean disableSendPhotoAction;
    private boolean disableCaptureAction;
    private boolean disableVideoRecordAction;
    private boolean disableSendFileAction;
    private boolean disableSendLocationAction;
    private boolean disableAudioCall = true;
    private boolean disableVideoCall = true;
    private boolean disableChatInput;
    private boolean isCustomAtGroupMember;

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
