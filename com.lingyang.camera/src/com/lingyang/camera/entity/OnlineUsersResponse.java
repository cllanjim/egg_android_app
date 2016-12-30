package com.lingyang.camera.entity;

import java.util.List;

/**
 * 文件名: OnlineUsersResponse
 * 描    述: [该类的简要描述]
 * 创建人: dushu
 * 创建时间: 2016/3/3
 */
public class OnlineUsersResponse extends BaseResponse {
    private static final long serialVersionUID = 1L;
    public List<CallContactResponse.CallContact> online_users;

    public List<CallContactResponse.CallContact> getOnline_users() {
        return online_users;
    }
}
