package com.work.api.open.model;

import com.work.api.open.model.client.OpenData;

import java.util.List;

/**
 * Created by tangyx
 * Date 2020/12/27
 * email tangyx@live.com
 */

public class GetUserListByMobilesResp extends BaseResp{

    private List<OpenData> data;

    public List<OpenData> getData() {
        return data;
    }
}
