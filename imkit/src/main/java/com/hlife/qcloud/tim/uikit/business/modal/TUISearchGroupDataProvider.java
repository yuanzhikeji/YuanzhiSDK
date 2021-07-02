package com.hlife.qcloud.tim.uikit.business.modal;

import com.hlife.qcloud.tim.uikit.utils.SearchDataUtils;
import com.tencent.imsdk.v2.V2TIMGroupInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberFullInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberSearchParam;
import com.tencent.imsdk.v2.V2TIMGroupSearchParam;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMValueCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TUISearchGroupDataProvider {
    /**
     * 群搜索
     *
     * @param searchParam 搜索参数
     *
     * @note
     * - 搜索时支持匹配群信息，支持匹配群ID、群名称，详见 @TUISearchGroupMatchField
     * - 搜索时支持匹配群成员信息，支持匹配成员ID、匹配成员昵称、匹配成员备注、匹配成员名片，详见 @TUISearchGroupMemberMatchField
     */
    public static void searchGroups(final TUISearchGroupParam searchParam, final V2TIMValueCallback<List<TUISearchGroupResult>> callback) {
        if (searchParam == null || searchParam.getKeywordList().size() == 0) {
            return;
        }

        final List<V2TIMGroupInfo> groupInfos = new ArrayList<>();
        final HashMap<String, List<V2TIMGroupMemberFullInfo>> groupMemberFullInfos= new HashMap<String, List<V2TIMGroupMemberFullInfo>>();

        //search group
        V2TIMGroupSearchParam groupSearchParam = new V2TIMGroupSearchParam();
        groupSearchParam.setKeywordList(searchParam.getKeywordList());
        groupSearchParam.setSearchGroupID(searchParam.isSearchGroupID());
        groupSearchParam.setSearchGroupName(searchParam.isSearchGroupName());
        V2TIMManager.getGroupManager().searchGroups(groupSearchParam, new V2TIMValueCallback<List<V2TIMGroupInfo>>() {
            @Override
            public void onSuccess(List<V2TIMGroupInfo> v2TIMGroupInfos) {
                for (V2TIMGroupInfo v2TIMGroupInfo : v2TIMGroupInfos) {
                    groupInfos.add(v2TIMGroupInfo);
                }

                SearchDataUtils.groupInfoFinish = true;
                SearchDataUtils.mergeGroupAndGroupMemberResult(searchParam.getKeywordList(), groupInfos, groupMemberFullInfos, callback);
            }

            @Override
            public void onError(int code, String desc) {
                SearchDataUtils.groupInfoFinish = true;
                SearchDataUtils.mergeGroupAndGroupMemberResult(searchParam.getKeywordList(), groupInfos, groupMemberFullInfos, callback);
            }
        });

        //search group member
        V2TIMGroupMemberSearchParam groupMemberSearchParam = new V2TIMGroupMemberSearchParam();
        groupMemberSearchParam.setKeywordList(searchParam.getKeywordList());
        groupMemberSearchParam.setSearchMemberUserID(searchParam.isSearchMemberUserID());
        groupMemberSearchParam.setSearchMemberNickName(searchParam.isSearchMemberNickName());
        groupMemberSearchParam.setSearchMemberNameCard(searchParam.isSearchMemberNameCard());
        groupMemberSearchParam.setSearchMemberRemark(searchParam.isSearchMemberRemark());
        V2TIMManager.getGroupManager().searchGroupMembers(groupMemberSearchParam, new V2TIMValueCallback<HashMap<String, List<V2TIMGroupMemberFullInfo>>>() {
            @Override
            public void onSuccess(HashMap<String, List<V2TIMGroupMemberFullInfo>> v2TIMGroupMemberInfoMap) {
                if (v2TIMGroupMemberInfoMap == null || v2TIMGroupMemberInfoMap.isEmpty()){
                    groupMemberFullInfos.clear();
                    SearchDataUtils.groupMemberFullInfofinish = true;
                    SearchDataUtils.mergeGroupAndGroupMemberResult(searchParam.getKeywordList(), groupInfos, groupMemberFullInfos, callback);
                    return;
                }

                for (Map.Entry<String, List<V2TIMGroupMemberFullInfo>> entry : v2TIMGroupMemberInfoMap.entrySet()) {
                    groupMemberFullInfos.put(entry.getKey(), entry.getValue());
                }

                SearchDataUtils.groupMemberFullInfofinish = true;
                SearchDataUtils.mergeGroupAndGroupMemberResult(searchParam.getKeywordList(), groupInfos, groupMemberFullInfos, callback);
            }

            @Override
            public void onError(int code, String desc) {
                SearchDataUtils.groupMemberFullInfofinish = true;
                SearchDataUtils.mergeGroupAndGroupMemberResult(searchParam.getKeywordList(), groupInfos, groupMemberFullInfos, callback);
            }
        });
    }

}
