package com.lingyang.camera.entity;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * 文件名: UnShareCamera
 * 描    述: [该类的简要描述]
 * 创建人: 杜舒
 * 创建时间: 2016/7/5
 */
public class UnShareCamera implements Serializable{
    public UnShareCamera() {
    }

    private static final long serialVersionUID = 1L;
    public String cid = "";
    public String cname = "";
    public String nickname = "";
    public String message = "";

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
