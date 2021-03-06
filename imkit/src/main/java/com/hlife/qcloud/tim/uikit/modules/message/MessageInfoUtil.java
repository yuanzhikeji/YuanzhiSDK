package com.hlife.qcloud.tim.uikit.modules.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.base.IBaseInfo;
import com.hlife.qcloud.tim.uikit.base.TUIChatControllerListener;
import com.hlife.qcloud.tim.uikit.base.TUIKitListenerManager;
import com.hlife.qcloud.tim.uikit.business.message.CustomFileMessage;
import com.hlife.qcloud.tim.uikit.utils.TUIKitConstants;
import com.http.network.task.ObjectMapperFactory;
import com.tencent.imsdk.v2.V2TIMCustomElem;
import com.tencent.imsdk.v2.V2TIMDownloadCallback;
import com.tencent.imsdk.v2.V2TIMElem;
import com.tencent.imsdk.v2.V2TIMFaceElem;
import com.tencent.imsdk.v2.V2TIMFileElem;
import com.tencent.imsdk.v2.V2TIMGroupChangeInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberChangeInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfo;
import com.tencent.imsdk.v2.V2TIMGroupTipsElem;
import com.tencent.imsdk.v2.V2TIMImageElem;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMSoundElem;
import com.tencent.imsdk.v2.V2TIMTextElem;
import com.tencent.imsdk.v2.V2TIMVideoElem;
import com.hlife.qcloud.tim.uikit.utils.DateTimeUtil;
import com.hlife.qcloud.tim.uikit.utils.FileUtil;
import com.hlife.qcloud.tim.uikit.utils.ImageUtil;
import com.hlife.qcloud.tim.uikit.utils.IMKitConstants;
import com.work.util.SLog;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.hlife.qcloud.tim.uikit.utils.IMKitConstants.BUSINESS_ID_CUSTOM_CARD;
import static com.hlife.qcloud.tim.uikit.utils.IMKitConstants.BUSINESS_ID_CUSTOM_FILE;
import static com.hlife.qcloud.tim.uikit.utils.IMKitConstants.BUSINESS_ID_CUSTOM_LOCATION;

public class MessageInfoUtil {

    /**
     * ????????????????????????
     *
     * @param message ????????????
     * @return
     */
    public static MessageInfo buildTextMessage(String message) {
        MessageInfo info = new MessageInfo();
        V2TIMMessage v2TIMMessage = V2TIMManager.getMessageManager().createTextMessage(message);
        info.setExtra(message);
        info.setMsgTime(System.currentTimeMillis() / 1000);
        info.setSelf(true);
        info.setTimMessage(v2TIMMessage);
        info.setFromUser(V2TIMManager.getInstance().getLoginUser());
        info.setMsgType(MessageInfo.MSG_TYPE_TEXT);
        return info;
    }

    public static MessageInfo buildTextAtMessage(List<String> atUserList, String message) {
        MessageInfo info = new MessageInfo();
        V2TIMMessage v2TIMMessage = V2TIMManager.getMessageManager().createTextAtMessage(message, atUserList);

        info.setExtra(message);
        info.setMsgTime(System.currentTimeMillis() / 1000);
        info.setSelf(true);
        info.setTimMessage(v2TIMMessage);
        info.setFromUser(V2TIMManager.getInstance().getLoginUser());
        info.setMsgType(MessageInfo.MSG_TYPE_TEXT);
        return info;
    }

    /**
     * ????????????????????????????????????
     *
     * @param groupId  ?????????????????????????????????id
     * @param faceName ???????????????
     * @return
     */
    public static MessageInfo buildCustomFaceMessage(int groupId, String faceName) {
        MessageInfo info = new MessageInfo();
        V2TIMMessage v2TIMMessage = V2TIMManager.getMessageManager().createFaceMessage(groupId, faceName.getBytes());

        info.setExtra("[???????????????]");
        info.setMsgTime(System.currentTimeMillis() / 1000);
        info.setSelf(true);
        info.setTimMessage(v2TIMMessage);
        info.setFromUser(V2TIMManager.getInstance().getLoginUser());
        info.setMsgType(MessageInfo.MSG_TYPE_CUSTOM_FACE);
        return info;
    }

    /**
     * ????????????????????????
     *
     * @param uri        ??????URI
     * @param compressed ????????????
     * @return
     */
    public static MessageInfo buildImageMessage(final Uri uri, boolean compressed) {
        final MessageInfo info = new MessageInfo();
        String path = FileUtil.getPathFromUri(uri);
        V2TIMMessage v2TIMMessage = V2TIMManager.getMessageManager().createImageMessage(path);

        info.setDataUri(uri);
        int[] size = ImageUtil.getImageSize(uri);
        info.setDataPath(path);
        info.setImgWidth(size[0]);
        info.setImgHeight(size[1]);
        info.setSelf(true);
        info.setTimMessage(v2TIMMessage);
        info.setExtra("[??????]");
        info.setMsgTime(System.currentTimeMillis() / 1000);
        info.setFromUser(V2TIMManager.getInstance().getLoginUser());
        info.setMsgType(MessageInfo.MSG_TYPE_IMAGE);
        return info;
    }

    /**
     * ????????????????????????
     *
     * @param imgPath   ?????????????????????
     * @param videoPath ????????????
     * @param width     ????????????
     * @param height    ????????????
     * @param duration  ???????????????
     * @return
     */
    public static MessageInfo buildVideoMessage(String imgPath, String videoPath, int width, int height, long duration) {
        MessageInfo info = new MessageInfo();
        V2TIMMessage v2TIMMessage = V2TIMManager.getMessageManager().createVideoMessage(videoPath, "mp4", (int) duration / 1000, imgPath);

        Uri videoUri = Uri.fromFile(new File(videoPath));
        info.setSelf(true);
        info.setImgWidth(width);
        info.setImgHeight(height);
        info.setDataPath(imgPath);
        info.setDataUri(videoUri);
        info.setTimMessage(v2TIMMessage);
        info.setExtra("[??????]");
        info.setMsgTime(System.currentTimeMillis() / 1000);
        info.setFromUser(V2TIMManager.getInstance().getLoginUser());
        info.setMsgType(MessageInfo.MSG_TYPE_VIDEO);
        return info;
    }

    /**
     * ????????????????????????
     *
     * @param recordPath ????????????
     * @param duration   ???????????????
     * @return
     */
    public static MessageInfo buildAudioMessage(String recordPath, int duration) {
        MessageInfo info = new MessageInfo();
        V2TIMMessage v2TIMMessage = V2TIMManager.getMessageManager().createSoundMessage(recordPath, duration / 1000);

        info.setDataPath(recordPath);
        info.setSelf(true);
        info.setTimMessage(v2TIMMessage);
        info.setExtra("[??????]");
        info.setMsgTime(System.currentTimeMillis() / 1000);
        info.setFromUser(V2TIMManager.getInstance().getLoginUser());
        info.setMsgType(MessageInfo.MSG_TYPE_AUDIO);
        return info;
    }

    /**
     * ????????????????????????
     *
     * @param fileUri ????????????
     * @return
     */
    public static MessageInfo buildFileMessage(Uri fileUri) {
        String filePath = FileUtil.getPathFromUri(fileUri);
        File file = new File(filePath);
        if (file.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath,options);
            if(options.outWidth != -1){//?????????
                return buildImageMessage(fileUri,true);
            }

            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(filePath);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
            if(mimeType!=null && mimeType.contains("video")){//?????????
                Bitmap firstFrame = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
                String imagePath = FileUtil.saveBitmap("JCamera", firstFrame);
                long duration = 0;
                try {
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(filePath);
                    duration = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                }catch (Exception ignore){}
                return buildVideoMessage(imagePath,filePath,firstFrame.getWidth(),firstFrame.getHeight(),duration);
            }
            MessageInfo info = new MessageInfo();
            V2TIMMessage v2TIMMessage = V2TIMManager.getMessageManager().createFileMessage(filePath, file.getName());

            info.setDataPath(filePath);
            info.setSelf(true);
            info.setTimMessage(v2TIMMessage);
            info.setExtra("[??????]");
            info.setMsgTime(System.currentTimeMillis() / 1000);
            info.setFromUser(V2TIMManager.getInstance().getLoginUser());
            info.setMsgType(MessageInfo.MSG_TYPE_FILE);
            return info;
        }
        return null;
    }
    /**
     * ??????????????????
     */
    public static MessageInfo buildLocationMessage(String data,double longitude,double latitude){
        MessageInfo info = new MessageInfo();
        V2TIMMessage v2TIMMessage = V2TIMManager.getMessageManager().createLocationMessage(data,longitude,latitude);
        info.setSelf(true);
        info.setTimMessage(v2TIMMessage);
        info.setExtra("[??????]");
        info.setMsgTime(System.currentTimeMillis() / 1000);
        info.setFromUser(V2TIMManager.getInstance().getLoginUser());
        info.setMsgType(MessageInfo.MSG_TYPE_LOCATION);
        return info;
    }
    /**
     * ???????????????????????????
     *
     * @param data ?????????????????????????????????????????????
     *             desc???????????????????????????
     * @return
     */
    public static MessageInfo buildCustomMessage(String data,String desc) {
        MessageInfo info = new MessageInfo();
        V2TIMMessage v2TIMMessage = V2TIMManager.getMessageManager().createCustomMessage(data.getBytes(),desc,desc.getBytes());

        info.setSelf(true);
        info.setTimMessage(v2TIMMessage);
        info.setMsgTime(System.currentTimeMillis() / 1000);
        info.setMsgType(MessageInfo.MSG_TYPE_CUSTOM);
        info.setFromUser(V2TIMManager.getInstance().getLoginUser());
        info.setExtra("[???????????????]");
        return info;
    }

    /**
     * ????????????????????????????????????
     *
     * @param customMessage ????????????
     * @return
     */
    public static V2TIMMessage buildGroupCustomMessage(String customMessage) {
        return V2TIMManager.getMessageManager().createCustomMessage(customMessage.getBytes());
    }

    /**
     * ???SDK?????????bean???????????????TUIKit?????????bean??????
     *
     * @param timMessages SDK????????????bean??????
     * @param isGroup     ??????????????????
     * @return
     */
    public static List<MessageInfo> TIMMessages2MessageInfos(List<V2TIMMessage> timMessages, boolean isGroup) {
        if (timMessages == null) {
            return null;
        }
        List<MessageInfo> messageInfos = new ArrayList<>();
        for (int i = 0; i < timMessages.size(); i++) {
            V2TIMMessage timMessage = timMessages.get(i);
            List<MessageInfo> info = TIMMessage2MessageInfos(timMessage);
            if (info != null) {
                messageInfos.addAll(info);
            }
        }
        return messageInfos;
    }

    /**
     * ???SDK?????????bean?????????TUIKit?????????bean
     *
     * @param timMessage SDK????????????bean
     * @return
     */
    public static List<MessageInfo> TIMMessage2MessageInfos(V2TIMMessage timMessage) {
        if (timMessage == null || timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_HAS_DELETED || timMessage.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_NONE) {
            return null;
        }
        List<MessageInfo> list = new ArrayList<>();
        final MessageInfo msgInfo = createMessageInfo(timMessage);
        if (msgInfo != null) {
            list.add(msgInfo);
        }
        return list;
    }

    public static MessageInfo TIMMessage2MessageInfo(V2TIMMessage timMessage) {
        if (timMessage == null || timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_HAS_DELETED || timMessage.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_NONE) {
            return null;
        }
        return createMessageInfo(timMessage);
    }

    public static boolean isOnlineIgnoredDialing(byte[] data) {
        try {
            String str = new String(data, StandardCharsets.UTF_8);
            MessageCustom custom = new Gson().fromJson(str, MessageCustom.class);
            if (custom != null
                    && TextUtils.equals(custom.businessID, MessageCustom.BUSINESS_ID_AV_CALL)
                    && custom.version <= IMKitConstants.version) {
                return true;
            }
            return false;
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isTyping(byte[] data) {
        try {
            String str = new String(data, StandardCharsets.UTF_8);
            MessageTyping typing = new Gson().fromJson(str, MessageTyping.class);
            if (typing != null
                    && typing.userAction == MessageTyping.TYPE_TYPING
                    && TextUtils.equals(typing.actionParam, MessageTyping.EDIT_START)) {
                return true;
            }
            return false;
        } catch (Exception ignore) {
        }
        return false;
    }
    public static MessageInfo createMessageInfo(V2TIMMessage timMessage) {
        if (timMessage == null
                || timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_HAS_DELETED
                || timMessage.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_NONE) {
            return null;
        }

        Context context = TUIKit.getAppContext();
        if (context == null){
            return new MessageInfo();
        }

        final MessageInfo msgInfo;

        int type = timMessage.getElemType();
        if (type == V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM) {
            msgInfo = createCustomMessageInfo(timMessage, context);
        } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_GROUP_TIPS) {
            msgInfo = createGroupTipsMessageInfo(timMessage, context);
        } else {
            msgInfo = createNormalMessageInfo(timMessage, context, type);
        }

        if (msgInfo == null) {
            return null;
        }

        if (timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_LOCAL_REVOKED) {
            msgInfo.setStatus(MessageInfo.MSG_STATUS_REVOKE);
            msgInfo.setMsgType(MessageInfo.MSG_STATUS_REVOKE);
            if (msgInfo.isSelf()) {
                msgInfo.setExtra(context.getString(R.string.revoke_tips_you));
            } else if (msgInfo.isGroup()) {
                String message = TUIKitConstants.covert2HTMLString(msgInfo.getFromUser());
                msgInfo.setExtra(message + context.getString(R.string.revoke_tips));
            } else {
                msgInfo.setExtra(context.getString(R.string.revoke_tips_other));
            }
        } else {
            if (msgInfo.isSelf()) {
                if (timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_SEND_FAIL) {
                    msgInfo.setStatus(MessageInfo.MSG_STATUS_SEND_FAIL);
                } else if (timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_SEND_SUCC) {
                    msgInfo.setStatus(MessageInfo.MSG_STATUS_SEND_SUCCESS);
                } else if (timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_SENDING) {
                    msgInfo.setStatus(MessageInfo.MSG_STATUS_SENDING);
                }
            }
        }
        return msgInfo;
    }

    private static MessageInfo createCustomMessageInfo(V2TIMMessage timMessage, Context context) {
        for(TUIChatControllerListener chatListener : TUIKitListenerManager.getInstance().getTUIChatListeners()) {
            IBaseInfo IBaseInfo = chatListener.createCommonInfoFromTimMessage(timMessage);
            if (IBaseInfo instanceof MessageInfo) {
                return (MessageInfo) IBaseInfo;
            }
        }
        MessageInfo msgInfo = new MessageInfo();
        setMessageInfoCommonAttributes(msgInfo, timMessage);
        V2TIMCustomElem customElem = timMessage.getCustomElem();
        String data = "";
        if(customElem.getData()!=null){
            data = new String(customElem.getData());
        }
//        if(SLog.debug)SLog.i("message custom:"+data);
        if (data.equals(MessageCustom.BUSINESS_ID_GROUP_CREATE)) {
            // ??????4.7??????????????? tuikit
            msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_CREATE);
            String message = TUIKitConstants.covert2HTMLString(
                    TextUtils.isEmpty(msgInfo.getGroupNameCard())
                            ? msgInfo.getFromUser()
                            : msgInfo.getGroupNameCard()) + context.getString(R.string.create_group);
            msgInfo.setExtra(message);
        } else {
            if (isTyping(customElem.getData())) {
                // ?????????????????????????????????????????????????????????
                return null;
            }
            String content = context.getString(R.string.custom_msg);
            msgInfo.setMsgType(MessageInfo.MSG_TYPE_CUSTOM);
            msgInfo.setExtra(content);
            Gson gson = new Gson();
            MessageCustom messageCustom;
            try {
                messageCustom = gson.fromJson(data, MessageCustom.class);
                String businessId = messageCustom.businessID;
                if (!TextUtils.isEmpty(businessId) && messageCustom.businessID.equals(MessageCustom.BUSINESS_ID_GROUP_CREATE)) {
                    msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_CREATE);
                    String message = IMKitConstants.covert2HTMLString(messageCustom.opUser) + messageCustom.content;
                    msgInfo.setExtra(message);
                } else if(!TextUtils.isEmpty(businessId) && messageCustom.businessID.equals(BUSINESS_ID_CUSTOM_CARD)){
                    msgInfo.setExtra("[??????]");
                } else if(!TextUtils.isEmpty(businessId) && messageCustom.businessID.equals(BUSINESS_ID_CUSTOM_FILE)){
                    CustomFileMessage message = ObjectMapperFactory.getObjectMapper().json2Model(data,CustomFileMessage.class);
                    msgInfo.setExtra("[??????]"+message.getFileName());
                }else if(!TextUtils.isEmpty(businessId) && messageCustom.businessID.equals(BUSINESS_ID_CUSTOM_LOCATION)){//??????????????????????????????????????????
                    msgInfo.setExtra("[??????]");
                    msgInfo.setMsgType(MessageInfo.MSG_TYPE_LOCATION);
                }
            } catch (Exception ignore) {
            }
        }
        return msgInfo;
    }

    private static MessageInfo createGroupTipsMessageInfo(V2TIMMessage timMessage, Context context) {
        MessageInfo msgInfo = new MessageInfo();
        setMessageInfoCommonAttributes(msgInfo, timMessage);

        V2TIMGroupTipsElem groupTipElem = timMessage.getGroupTipsElem();
        int tipsType = groupTipElem.getType();
        StringBuilder user = new StringBuilder();
        if (groupTipElem.getMemberList().size() > 0) {
            List<V2TIMGroupMemberInfo> v2TIMGroupMemberInfoList = groupTipElem.getMemberList();
            for (int i = 0; i < v2TIMGroupMemberInfoList.size(); i++) {
                V2TIMGroupMemberInfo v2TIMGroupMemberInfo = v2TIMGroupMemberInfoList.get(i);
                if (i == 0) {
                    if (!TextUtils.isEmpty(v2TIMGroupMemberInfo.getNameCard())) {
                        user.append(v2TIMGroupMemberInfo.getNameCard());
                    } else if (!TextUtils.isEmpty(v2TIMGroupMemberInfo.getNickName())) {
                        user.append(v2TIMGroupMemberInfo.getNickName());
                    } else {
                        user.append(v2TIMGroupMemberInfo.getUserID());
                    }
                } else {
                    if (i == 2 && v2TIMGroupMemberInfoList.size() > 3) {
                        user.append(TUIKit.getAppContext().getString(R.string.etc));
                        break;
                    } else {
                        user.append("???");
                        if (!TextUtils.isEmpty(v2TIMGroupMemberInfo.getNameCard())) {
                            user.append(v2TIMGroupMemberInfo.getNameCard());
                        } else if (!TextUtils.isEmpty(v2TIMGroupMemberInfo.getNickName())) {
                            user.append(v2TIMGroupMemberInfo.getNickName());
                        } else {
                            user.append(v2TIMGroupMemberInfo.getUserID());
                        }
                    }
                }
            }
        } else {
            if(!TextUtils.isEmpty(groupTipElem.getOpMember().getNameCard())){
                user.append(groupTipElem.getOpMember().getNameCard());
            }else if(!TextUtils.isEmpty(groupTipElem.getOpMember().getNickName())){
                user.append(groupTipElem.getOpMember().getNickName());
            }else{
                user.append(groupTipElem.getOpMember().getUserID());
            }
        }
        String message = TUIKitConstants.covert2HTMLString(user.toString());
        if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_JOIN) {
            msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_JOIN);
            message = message + context.getString(R.string.join_group);
        }
        if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_INVITE) {
            msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_JOIN);
            message = message + context.getString(R.string.invite_joined_group);
        }
        if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_QUIT) {
            msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_QUITE);
            message = message + context.getString(R.string.quit_group);
        }
        if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_KICKED) {
            msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_KICK);
            message = message + context.getString(R.string.kick_group_tip);
        }
        if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_SET_ADMIN) {
            msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
            message = message + context.getString(R.string.be_group_manager);
        }
        if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_CANCEL_ADMIN) {
            msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
            message = message + context.getString(R.string.cancle_group_manager);
        }
        if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_GROUP_INFO_CHANGE) {
            List<V2TIMGroupChangeInfo> modifyList = groupTipElem.getGroupChangeInfoList();
            for (int i = 0; i < modifyList.size(); i++) {
                V2TIMGroupChangeInfo modifyInfo = modifyList.get(i);
                int modifyType = modifyInfo.getType();
                if (modifyType == V2TIMGroupChangeInfo.V2TIM_GROUP_INFO_CHANGE_TYPE_NAME) {
                    msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NAME);
                    message = message + context.getString(R.string.modify_group_name_is) + "\"" + modifyInfo.getValue() + "\"";
                } else if (modifyType == V2TIMGroupChangeInfo.V2TIM_GROUP_INFO_CHANGE_TYPE_NOTIFICATION) {
                    msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
                    message = message + context.getString(R.string.modify_notice) + "\"" + modifyInfo.getValue() + "\"";
                } else if (modifyType == V2TIMGroupChangeInfo.V2TIM_GROUP_INFO_CHANGE_TYPE_OWNER) {
                    msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
                    message = message + context.getString(R.string.move_owner) + "\"" + modifyInfo.getValue() + "\"";
                } else if (modifyType == V2TIMGroupChangeInfo.V2TIM_GROUP_INFO_CHANGE_TYPE_FACE_URL) {
                    msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
                    message = message + context.getString(R.string.modify_group_avatar);
                } else if (modifyType == V2TIMGroupChangeInfo.V2TIM_GROUP_INFO_CHANGE_TYPE_INTRODUCTION) {
                    msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
                    message = message + context.getString(R.string.modify_notice) + "\"" + modifyInfo.getValue() + "\"";
                }
                if (i < modifyList.size() - 1) {
                    message = message + "???";
                }
            }
        }
        if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_MEMBER_INFO_CHANGE) {
            List<V2TIMGroupMemberChangeInfo> modifyList = groupTipElem.getMemberChangeInfoList();
            if (modifyList.size() > 0) {
                long shutupTime = modifyList.get(0).getMuteTime();
                if (shutupTime > 0) {
                    msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
                    message = message + context.getString(R.string.banned) + "\"" + DateTimeUtil.formatSeconds(shutupTime) + "\"";
                } else {
                    msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
                    message = message + context.getString(R.string.cancle_banned);
                }
            }
        }
        if (TextUtils.isEmpty(message)) {
            return null;
        }
        msgInfo.setExtra(message);
        return msgInfo;
    }

    private static MessageInfo createNormalMessageInfo(V2TIMMessage timMessage, Context context, int type) {
        final MessageInfo msgInfo = new MessageInfo();
        setMessageInfoCommonAttributes(msgInfo, timMessage);
        if (type == V2TIMMessage.V2TIM_ELEM_TYPE_TEXT) {
            V2TIMTextElem txtEle = timMessage.getTextElem();
            msgInfo.setExtra(txtEle.getText());
        } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_FACE) {
            V2TIMFaceElem faceElem = timMessage.getFaceElem();
            if (faceElem.getIndex() < 1 || faceElem.getData() == null) {
                return null;
            }
            msgInfo.setExtra(context.getString(R.string.custom_emoji));
        } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_SOUND) {
            V2TIMSoundElem soundElemEle = timMessage.getSoundElem();
            if (msgInfo.isSelf()) {
                msgInfo.setDataPath(soundElemEle.getPath());
            } else {
                final String path = TUIKitConstants.RECORD_DOWNLOAD_DIR + soundElemEle.getUUID();
                File file = new File(path);
                if (!file.exists()) {
                    soundElemEle.downloadSound(path, new V2TIMDownloadCallback() {
                        @Override
                        public void onProgress(V2TIMElem.V2ProgressInfo progressInfo) {
                            long currentSize = progressInfo.getCurrentSize();
                            long totalSize = progressInfo.getTotalSize();
                            int progress = 0;
                            if (totalSize > 0) {
                                progress = (int) (100 * currentSize / totalSize);
                            }
                            if (progress > 100) {
                                progress = 100;
                            }
                        }

                        @Override
                        public void onError(int code, String desc) {
                        }

                        @Override
                        public void onSuccess() {
                            msgInfo.setDataPath(path);
                        }
                    });
                } else {
                    msgInfo.setDataPath(path);
                }
            }
            msgInfo.setExtra(context.getString(R.string.audio_extra));
        } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_IMAGE) {
            V2TIMImageElem imageEle = timMessage.getImageElem();
            String localPath = imageEle.getPath();
            if (msgInfo.isSelf() && !TextUtils.isEmpty(localPath)) {
                msgInfo.setDataPath(localPath);
                int size[] = ImageUtil.getImageSize(localPath);
                msgInfo.setImgWidth(size[0]);
                msgInfo.setImgHeight(size[1]);
            }
            //??????????????????????????????????????????????????????????????????
            else {
                List<V2TIMImageElem.V2TIMImage> imgs = imageEle.getImageList();
                for (int i = 0; i < imgs.size(); i++) {
                    V2TIMImageElem.V2TIMImage img = imgs.get(i);
                    if (img.getType() == V2TIMImageElem.V2TIM_IMAGE_TYPE_THUMB) {
                        final String path = ImageUtil.generateImagePath(img.getUUID(), V2TIMImageElem.V2TIM_IMAGE_TYPE_THUMB);
                        msgInfo.setImgWidth(img.getWidth());
                        msgInfo.setImgHeight(img.getHeight());
                        File file = new File(path);
                        if (file.exists()) {
                            msgInfo.setDataPath(path);
                        }
                    }
                }
            }
            msgInfo.setExtra(context.getString(R.string.picture_extra));
        } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_VIDEO) {
            V2TIMVideoElem videoEle = timMessage.getVideoElem();
            if (msgInfo.isSelf() && !TextUtils.isEmpty(videoEle.getSnapshotPath())) {
                int size[] = ImageUtil.getImageSize(videoEle.getSnapshotPath());
                msgInfo.setImgWidth(size[0]);
                msgInfo.setImgHeight(size[1]);
                msgInfo.setDataPath(videoEle.getSnapshotPath());
                msgInfo.setDataUri(FileUtil.getUriFromPath(videoEle.getVideoPath()));
            } else {
                final String videoPath = TUIKitConstants.VIDEO_DOWNLOAD_DIR + videoEle.getVideoUUID();
                Uri uri = Uri.parse(videoPath);
                msgInfo.setDataUri(uri);
                msgInfo.setImgWidth((int) videoEle.getSnapshotWidth());
                msgInfo.setImgHeight((int) videoEle.getSnapshotHeight());
                final String snapPath = TUIKitConstants.IMAGE_DOWNLOAD_DIR + videoEle.getSnapshotUUID();
                //????????????????????????,?????????????????????
                if (new File(snapPath).exists()) {
                    msgInfo.setDataPath(snapPath);
                }
            }

            msgInfo.setExtra(context.getString(R.string.video_extra));
        } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_FILE) {
            V2TIMFileElem fileElem = timMessage.getFileElem();
            String filename = fileElem.getUUID();
            if (TextUtils.isEmpty(filename)) {
                filename = System.currentTimeMillis() + fileElem.getFileName();
            }
            final String path = TUIKitConstants.FILE_DOWNLOAD_DIR + filename;
            File file = new File(path);
            if (file.exists()) {
                if (msgInfo.isSelf()) {
                    msgInfo.setStatus(MessageInfo.MSG_STATUS_SEND_SUCCESS);
                } else {
                    msgInfo.setStatus(MessageInfo.MSG_STATUS_DOWNLOADED);
                }
                msgInfo.setDataPath(path);
            } else {
                if (msgInfo.isSelf()) {
                    if (TextUtils.isEmpty(fileElem.getPath())) {
                        msgInfo.setStatus(MessageInfo.MSG_STATUS_UN_DOWNLOAD);
                        msgInfo.setDataPath(path);
                    } else {
                        file = new File(fileElem.getPath());
                        if (file.exists()) {
                            msgInfo.setStatus(MessageInfo.MSG_STATUS_SEND_SUCCESS);
                            msgInfo.setDataPath(fileElem.getPath());
                        } else {
                            msgInfo.setStatus(MessageInfo.MSG_STATUS_UN_DOWNLOAD);
                            msgInfo.setDataPath(path);
                        }
                    }
                } else {
                    msgInfo.setStatus(MessageInfo.MSG_STATUS_UN_DOWNLOAD);
                    msgInfo.setDataPath(path);
                }
            }
            msgInfo.setExtra(context.getString(R.string.file_extra));
        } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_MERGER) {
            // ??????????????????
            msgInfo.setExtra("[????????????]");
        }
        msgInfo.setMsgType(TIMElemType2MessageInfoType(type));
        return msgInfo;
    }
//    public static MessageInfo createMessageInfo(V2TIMMessage timMessage) {
//        if (timMessage == null
//                || timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_HAS_DELETED
//                || timMessage.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_NONE) {
//            SLog.e("ele2MessageInfo parameters error");
//            return null;
//        }
//        final MessageInfo msgInfo = new MessageInfo();
//        boolean isGroup = !TextUtils.isEmpty(timMessage.getGroupID());
//        String sender = timMessage.getSender();
//        msgInfo.setTimMessage(timMessage);
//        msgInfo.setGroup(isGroup);
//        msgInfo.setId(timMessage.getMsgID());
//        msgInfo.setPeerRead(timMessage.isPeerRead());
//        msgInfo.setFromUser(sender);
//        if (isGroup) {
//            if (!TextUtils.isEmpty(timMessage.getNameCard())) {
//                msgInfo.setGroupNameCard(timMessage.getNameCard());
//            }
//        }
//        msgInfo.setMsgTime(timMessage.getTimestamp());
//        msgInfo.setSelf(sender.equals(V2TIMManager.getInstance().getLoginUser()));
//
//        int type = timMessage.getElemType();
//        if (type == V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM) {
//            V2TIMCustomElem customElem = timMessage.getCustomElem();
//            String data = "";
//            if(customElem.getData()!=null){
//                data = new String(customElem.getData());
//            }
//            if (data.equals(MessageCustom.BUSINESS_ID_GROUP_CREATE)) {
//                // ??????4.7??????????????? tuikith
//                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_CREATE);
//                String message =  (TextUtils.isEmpty(msgInfo.getTimMessage().getNameCard())?msgInfo.getTimMessage().getNickName():msgInfo.getTimMessage().getNameCard())+ " ???????????????";
//                msgInfo.setExtra(message);
//            }
//            else {
//                if (isTyping(customElem.getData())) {
//                    // ?????????????????????????????????????????????????????????
//                    return null;
//                }
//                if(SLog.debug)SLog.i( "message info util custom data:" + data);
//                String content = "[???????????????]";
//                msgInfo.setMsgType(MessageInfo.MSG_TYPE_CUSTOM);
//                msgInfo.setExtra(content);
//                Gson gson = new Gson();
//                MessageCustom messageCustom;
//                try {
//                    messageCustom = gson.fromJson(data, MessageCustom.class);
//                    String businessId = messageCustom.businessID;
//                    if (!TextUtils.isEmpty(businessId) && messageCustom.businessID.equals(MessageCustom.BUSINESS_ID_GROUP_CREATE)) {
//                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_CREATE);
//                        String message = IMKitConstants.covert2HTMLString(messageCustom.opUser) + messageCustom.content;
//                        msgInfo.setExtra(message);
//                    } else if(!TextUtils.isEmpty(businessId) && messageCustom.businessID.equals(BUSINESS_ID_CUSTOM_CARD)){
//                        msgInfo.setExtra("[??????]");
//                    } else if(!TextUtils.isEmpty(businessId) && messageCustom.businessID.equals(BUSINESS_ID_CUSTOM_FILE)){
//                        CustomFileMessage message = ObjectMapperFactory.getObjectMapper().json2Model(data,CustomFileMessage.class);
//                        msgInfo.setExtra("[??????]"+message.getFileName());
//                    }else if(!TextUtils.isEmpty(businessId) && messageCustom.businessID.equals(BUSINESS_ID_CUSTOM_LOCATION)){//??????????????????????????????????????????
//                        msgInfo.setExtra("[??????]");
//                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_LOCATION);
//                    } else {
//                        CallModel callModel = CallModel.convert2VideoCallData(timMessage);
//                        if (callModel != null) {
//                            String senderShowName = timMessage.getSender();
//                            if (!TextUtils.isEmpty(timMessage.getNameCard())) {
//                                senderShowName = timMessage.getNameCard();
//                            } else if (!TextUtils.isEmpty(timMessage.getFriendRemark())) {
//                                senderShowName = timMessage.getFriendRemark();
//                            } else if (!TextUtils.isEmpty(timMessage.getNickName())) {
//                                senderShowName = timMessage.getNickName();
//                            }
//                            switch (callModel.action) {
//                                case CallModel.VIDEO_CALL_ACTION_DIALING:
//                                    content = isGroup ? ("\"" + senderShowName + "\"" + "???????????????") : ("????????????");
//                                    if(!isGroup){
//                                        msgInfo.setCallType(callModel.callType);
//                                    }
//                                    break;
//                                case CallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL:
//                                    content = isGroup ? "???????????????" : "????????????";
//                                    if(!isGroup){
//                                        msgInfo.setCallType(callModel.callType);
//                                    }
//                                    break;
//                                case CallModel.VIDEO_CALL_ACTION_LINE_BUSY:
//                                    content = isGroup ? ("\"" + senderShowName + "\"" + "??????") : "????????????";
//                                    if(!isGroup){
//                                        msgInfo.setCallType(callModel.callType);
//                                    }
//                                    break;
//                                case CallModel.VIDEO_CALL_ACTION_REJECT:
//                                    content = isGroup ? ("\"" + senderShowName + "\"" + "???????????????") : "????????????";
//                                    if(!isGroup){
//                                        msgInfo.setCallType(callModel.callType);
//                                    }
//                                    break;
//                                case CallModel.VIDEO_CALL_ACTION_SPONSOR_TIMEOUT:
//                                    if (isGroup && callModel.invitedList != null && callModel.invitedList.size() == 1
//                                            && callModel.invitedList.get(0).equals(timMessage.getSender())) {
//                                        content = "\"" + senderShowName + "\"" + "?????????";
//                                    } else {
////                                        StringBuilder inviteeShowStringBuilder = new StringBuilder();
//                                        if (callModel.invitedList != null && callModel.invitedList.size() > 0) {
////                                            for (String invitee : callModel.invitedList) {
////                                                inviteeShowStringBuilder.append(invitee).append("???");
////                                            }
////                                            if (inviteeShowStringBuilder.length() > 0) {
////                                                inviteeShowStringBuilder.delete(inviteeShowStringBuilder.length() - 1, inviteeShowStringBuilder.length());
////                                            }
//                                            content = isGroup ? ("?????????\"" + callModel.invitedList.size() + "\"" + "????????????") : "?????????";
//                                        }
////                                        content = isGroup ? ("\"" + inviteeShowStringBuilder.toString() + "\"" + "?????????") : "?????????";
//                                    }
//                                    break;
//                                case CallModel.VIDEO_CALL_ACTION_ACCEPT:
//                                    content = isGroup ? ("\"" + senderShowName + "\"" + "?????????") : "?????????";
//                                    if(!isGroup){
//                                        msgInfo.setCallType(callModel.callType);
//                                    }
//                                    break;
//                                case CallModel.VIDEO_CALL_ACTION_HANGUP:
//                                    content = isGroup ? "???????????????" : "??????????????????????????????" + DateTimeUtil.formatSecondsTo00(callModel.duration);
//                                    if(!isGroup){
//                                        msgInfo.setCallType(callModel.callType);
//                                    }
//                                    break;
//                                default:
//                                    content = "???????????????????????????";
//                                    break;
//                            }
//                            if (isGroup) {
//                                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_AV_CALL_NOTICE);
//                            }else{
//                                msgInfo.setMsgType(MessageInfo.MSG_TYPE_TEXT);
//                            }
//                            msgInfo.setExtra(content);
//                        }
//                    }
//                } catch (Exception e) {
//                    SLog.e( "invalid json: " + data + ", exception:" + e);
//                }
//            }
//        } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_GROUP_TIPS) {
//            V2TIMGroupTipsElem groupTipElem = timMessage.getGroupTipsElem();
//            int tipsType = groupTipElem.getType();
//            StringBuilder user = new StringBuilder();
//            if (groupTipElem.getMemberList().size() > 0) {
//                List<V2TIMGroupMemberInfo> v2TIMGroupMemberInfoList = groupTipElem.getMemberList();
//                for (int i = 0; i < v2TIMGroupMemberInfoList.size(); i++) {
//                    V2TIMGroupMemberInfo v2TIMGroupMemberInfo = v2TIMGroupMemberInfoList.get(i);
//
//                    if (i == 0) {
//                        if(!TextUtils.isEmpty(v2TIMGroupMemberInfo.getNameCard())){
//                            user.append(v2TIMGroupMemberInfo.getNameCard());
//                        }else if(!TextUtils.isEmpty(v2TIMGroupMemberInfo.getNickName())){
//                            user.append(v2TIMGroupMemberInfo.getNickName());
//                        }else{
//                            user.append(v2TIMGroupMemberInfo.getUserID());
//                        }
//                    } else {
//                        if (i == 2 && v2TIMGroupMemberInfoList.size() > 3) {
//                            user.append("???");
//                            break;
//                        } else {
//                            user.append("???");
//                            if(!TextUtils.isEmpty(v2TIMGroupMemberInfo.getNameCard())){
//                                user.append(v2TIMGroupMemberInfo.getNameCard());
//                            }else if(!TextUtils.isEmpty(v2TIMGroupMemberInfo.getNickName())){
//                                user.append(v2TIMGroupMemberInfo.getNickName());
//                            }else{
//                                user.append(v2TIMGroupMemberInfo.getUserID());
//                            }
//                        }
//                    }
//                }
//
//            } else {
//                if(!TextUtils.isEmpty(groupTipElem.getOpMember().getNameCard())){
//                    user.append(groupTipElem.getOpMember().getNameCard());
//                }else if(!TextUtils.isEmpty(groupTipElem.getOpMember().getNickName())){
//                    user.append(groupTipElem.getOpMember().getNickName());
//                }else{
//                    user.append(groupTipElem.getOpMember().getUserID());
//                }
//            }
//            StringBuilder message = new StringBuilder(IMKitConstants.covert2HTMLString(user.toString()));
//            if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_JOIN) {
//                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_JOIN);
//                message.append("????????????");
//            }
//            if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_INVITE) {
//                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_JOIN);
//                message.append("???????????????");
//            }
//            if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_QUIT) {
//                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_QUITE);
//                message.append("????????????");
//            }
//            if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_KICKED) {
//                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_KICK);
//                message.append("???????????????");
//            }
//            if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_SET_ADMIN) {
//                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
//                message.append("??????????????????");
//            }
//            if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_CANCEL_ADMIN) {
//                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
//                message.append("??????????????????");
//            }
//            if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_GROUP_INFO_CHANGE) {
//                List<V2TIMGroupChangeInfo> modifyList = groupTipElem.getGroupChangeInfoList();
//                for (int i = 0; i < modifyList.size(); i++) {
//                    V2TIMGroupChangeInfo modifyInfo = modifyList.get(i);
//                    int modifyType = modifyInfo.getType();
//                    if (modifyType == V2TIMGroupChangeInfo.V2TIM_GROUP_INFO_CHANGE_TYPE_NAME) {
//                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NAME);
//                        message.append("??????????????????\"").append(modifyInfo.getValue()).append("\"");
//                    } else if (modifyType == V2TIMGroupChangeInfo.V2TIM_GROUP_INFO_CHANGE_TYPE_NOTIFICATION) {
//                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
//                        message.append("??????????????????\"").append(modifyInfo.getValue()).append("\"");
//                    } else if (modifyType == V2TIMGroupChangeInfo.V2TIM_GROUP_INFO_CHANGE_TYPE_OWNER) {
//                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
////                        message.append("???????????????\"").append(modifyInfo.getValue()).append("\"");
//                        message.append("???????????????");
//                    } else if (modifyType == V2TIMGroupChangeInfo.V2TIM_GROUP_INFO_CHANGE_TYPE_FACE_URL) {
//                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
//                        message.append("??????????????????");
//                    } else if (modifyType == V2TIMGroupChangeInfo.V2TIM_GROUP_INFO_CHANGE_TYPE_INTRODUCTION) {
//                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
//                        message.append("??????????????????\"").append(modifyInfo.getValue()).append("\"");
//                    }
//                    if (i < modifyList.size() - 1) {
//                        message.append("???");
//                    }
//                }
//            }
//            if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_MEMBER_INFO_CHANGE) {
//                List<V2TIMGroupMemberChangeInfo> modifyList = groupTipElem.getMemberChangeInfoList();
//                if (modifyList.size() > 0) {
//                    long shutupTime = modifyList.get(0).getMuteTime();
//                    if (shutupTime > 0) {
//                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
//                        message.append("?????????\"").append(DateTimeUtil.formatSeconds(shutupTime)).append("\"");
//                    } else {
//                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
//                        message.append("???????????????");
//                    }
//                }
//            }
//            if (TextUtils.isEmpty(message.toString())) {
//                return null;
//            }
//            msgInfo.setExtra(message.toString());
//        } else {
//            if (type == V2TIMMessage.V2TIM_ELEM_TYPE_TEXT) {
//                V2TIMTextElem txtEle = timMessage.getTextElem();
//                msgInfo.setExtra(txtEle.getText());
//            } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_FACE) {
//                V2TIMFaceElem faceElem = timMessage.getFaceElem();
//                if (faceElem.getIndex() < 1 || faceElem.getData() == null) {
//                    return null;
//                }
//                msgInfo.setExtra("[???????????????]");
//
//
//            } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_SOUND) {
//                V2TIMSoundElem soundElemEle = timMessage.getSoundElem();
//                if (msgInfo.isSelf()) {
//                    msgInfo.setDataPath(soundElemEle.getPath());
//                } else {
//                    final String path = IMKitConstants.RECORD_DOWNLOAD_DIR + soundElemEle.getUUID();
//                    File file = new File(path);
//                    if (!file.exists()) {
//                        soundElemEle.downloadSound(path, new V2TIMDownloadCallback() {
//                            @Override
//                            public void onProgress(V2TIMElem.V2ProgressInfo progressInfo) {
//                                long currentSize = progressInfo.getCurrentSize();
//                                long totalSize = progressInfo.getTotalSize();
//                                int progress = 0;
//                                if (totalSize > 0) {
//                                    progress = (int) (100 * currentSize / totalSize);
//                                }
//                                if (progress > 100) {
//                                    progress = 100;
//                                }
//                            }
//
//                            @Override
//                            public void onError(int code, String desc) {
//                            }
//
//                            @Override
//                            public void onSuccess() {
//                                msgInfo.setDataPath(path);
//                            }
//                        });
//                    } else {
//                        msgInfo.setDataPath(path);
//                    }
//                }
//                msgInfo.setExtra("[??????]");
//            } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_IMAGE) {
//                V2TIMImageElem imageEle = timMessage.getImageElem();
//                String localPath = imageEle.getPath();
//                if (msgInfo.isSelf() && !TextUtils.isEmpty(localPath)) {
//                    msgInfo.setDataPath(localPath);
//                    int[] size = ImageUtil.getImageSize(localPath);
//                    msgInfo.setImgWidth(size[0]);
//                    msgInfo.setImgHeight(size[1]);
//                }
//                //??????????????????????????????????????????????????????????????????
//                else {
//                    List<V2TIMImageElem.V2TIMImage> imgs = imageEle.getImageList();
//                    for (int i = 0; i < imgs.size(); i++) {
//                        V2TIMImageElem.V2TIMImage img = imgs.get(i);
//                        if (img.getType() == V2TIMImageElem.V2TIM_IMAGE_TYPE_THUMB) {
//                            final String path = IMKitConstants.IMAGE_DOWNLOAD_DIR + img.getUUID();
//                            msgInfo.setImgWidth(img.getWidth());
//                            msgInfo.setImgHeight(img.getHeight());
//                            File file = new File(path);
//                            if (file.exists()) {
//                                msgInfo.setDataPath(path);
//                            }
//                        }
//                    }
//                }
//                msgInfo.setExtra("[??????]");
//            } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_VIDEO) {
//                V2TIMVideoElem videoEle = timMessage.getVideoElem();
//                if (msgInfo.isSelf() && !TextUtils.isEmpty(videoEle.getSnapshotPath())) {
//                    int[] size = ImageUtil.getImageSize(videoEle.getSnapshotPath());
//                    msgInfo.setImgWidth(size[0]);
//                    msgInfo.setImgHeight(size[1]);
//                    msgInfo.setDataPath(videoEle.getSnapshotPath());
//                    msgInfo.setDataUri(FileUtil.getUriFromPath(videoEle.getVideoPath()));
//                } else {
//                    final String videoPath = IMKitConstants.VIDEO_DOWNLOAD_DIR + videoEle.getVideoUUID();
//                    Uri uri = Uri.parse(videoPath);
//                    msgInfo.setDataUri(uri);
//                    msgInfo.setImgWidth((int) videoEle.getSnapshotWidth());
//                    msgInfo.setImgHeight((int) videoEle.getSnapshotHeight());
//                    final String snapPath = IMKitConstants.IMAGE_DOWNLOAD_DIR + videoEle.getSnapshotUUID();
//                    //????????????????????????,?????????????????????
//                    if (new File(snapPath).exists()) {
//                        msgInfo.setDataPath(snapPath);
//                    }
//                }
//
//                msgInfo.setExtra("[??????]");
//            } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_FILE) {
//                V2TIMFileElem fileElem = timMessage.getFileElem();
//                String filename = fileElem.getUUID();
//                if (TextUtils.isEmpty(filename)) {
//                    filename = System.currentTimeMillis() + fileElem.getFileName();
//                }
//                final String path = IMKitConstants.FILE_DOWNLOAD_DIR + filename;
//                File file = new File(path);
//                if (file.exists()) {
//                    if (msgInfo.isSelf()) {
//                        msgInfo.setStatus(MessageInfo.MSG_STATUS_SEND_SUCCESS);
//                    } else {
//                        msgInfo.setStatus(MessageInfo.MSG_STATUS_DOWNLOADED);
//                    }
//                    msgInfo.setDataPath(path);
//                } else {
//                    if (msgInfo.isSelf()) {
//                        if (TextUtils.isEmpty(fileElem.getPath())) {
//                            msgInfo.setStatus(MessageInfo.MSG_STATUS_UN_DOWNLOAD);
//                            msgInfo.setDataPath(path);
//                        } else {
//                            file = new File(fileElem.getPath());
//                            if (file.exists()) {
//                                msgInfo.setStatus(MessageInfo.MSG_STATUS_SEND_SUCCESS);
//                                msgInfo.setDataPath(fileElem.getPath());
//                            } else {
//                                msgInfo.setStatus(MessageInfo.MSG_STATUS_UN_DOWNLOAD);
//                                msgInfo.setDataPath(path);
//                            }
//                        }
//                    } else {
//                        msgInfo.setStatus(MessageInfo.MSG_STATUS_UN_DOWNLOAD);
//                        msgInfo.setDataPath(path);
//                    }
//                }
//                msgInfo.setExtra("[??????]");
//            }else if(type == V2TIMMessage.V2TIM_ELEM_TYPE_LOCATION){
//                msgInfo.setExtra("[??????]");
//            }
//            msgInfo.setMsgType(TIMElemType2MessageInfoType(type));
//        }
//
//        if (timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_LOCAL_REVOKED) {
//            msgInfo.setStatus(MessageInfo.MSG_STATUS_REVOKE);
//            msgInfo.setMsgType(MessageInfo.MSG_STATUS_REVOKE);
//            if (msgInfo.isSelf()) {
//                msgInfo.setExtra("????????????????????????");
//            } else if (msgInfo.isGroup()) {
//                String name = msgInfo.getGroupNameCard();
//                if(TextUtils.isEmpty(name)){
//                    name = msgInfo.getTimMessage().getNameCard();
//                }
//                if(TextUtils.isEmpty(name)){
//                    name = msgInfo.getTimMessage().getNickName();
//                }
//                String message = IMKitConstants.covert2HTMLString(name);
//                msgInfo.setExtra(message + "?????????????????????");
//            } else {
//                msgInfo.setExtra("???????????????????????????");
//            }
//        } else {
//            if (msgInfo.isSelf()) {
//                if (timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_SEND_FAIL) {
//                    msgInfo.setStatus(MessageInfo.MSG_STATUS_SEND_FAIL);
//                } else if (timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_SEND_SUCC) {
//                    msgInfo.setStatus(MessageInfo.MSG_STATUS_SEND_SUCCESS);
//                } else if (timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_SENDING) {
//                    msgInfo.setStatus(MessageInfo.MSG_STATUS_SENDING);
//                }
//            }
//        }
//        return msgInfo;
//    }

    public static void setMessageInfoCommonAttributes(MessageInfo msgInfo, V2TIMMessage timMessage) {
        if (msgInfo == null) {
            return;
        }
        boolean isGroup = !TextUtils.isEmpty(timMessage.getGroupID());
        String sender = timMessage.getSender();
        if (TextUtils.isEmpty(sender)) {
            sender = V2TIMManager.getInstance().getLoginUser();
        }
        msgInfo.setTimMessage(timMessage);
        msgInfo.setGroup(isGroup);
        msgInfo.setId(timMessage.getMsgID());
        msgInfo.setPeerRead(timMessage.isPeerRead());
        msgInfo.setFromUser(sender);
        if (isGroup) {
            if (!TextUtils.isEmpty(timMessage.getNameCard())) {
                msgInfo.setGroupNameCard(timMessage.getNameCard());
            }
        }
        msgInfo.setMsgTime(timMessage.getTimestamp());
        msgInfo.setSelf(sender.equals(V2TIMManager.getInstance().getLoginUser()));
    }

    private static int TIMElemType2MessageInfoType(int type) {
        switch (type) {
            case V2TIMMessage.V2TIM_ELEM_TYPE_TEXT:
                return MessageInfo.MSG_TYPE_TEXT;
            case V2TIMMessage.V2TIM_ELEM_TYPE_IMAGE:
                return MessageInfo.MSG_TYPE_IMAGE;
            case V2TIMMessage.V2TIM_ELEM_TYPE_SOUND:
                return MessageInfo.MSG_TYPE_AUDIO;
            case V2TIMMessage.V2TIM_ELEM_TYPE_VIDEO:
                return MessageInfo.MSG_TYPE_VIDEO;
            case V2TIMMessage.V2TIM_ELEM_TYPE_FILE:
                return MessageInfo.MSG_TYPE_FILE;
            case V2TIMMessage.V2TIM_ELEM_TYPE_LOCATION:
                return MessageInfo.MSG_TYPE_LOCATION;
            case V2TIMMessage.V2TIM_ELEM_TYPE_FACE:
                return MessageInfo.MSG_TYPE_CUSTOM_FACE;
            case V2TIMMessage.V2TIM_ELEM_TYPE_GROUP_TIPS:
                return MessageInfo.MSG_TYPE_TIPS;
        }

        return -1;
    }
}
