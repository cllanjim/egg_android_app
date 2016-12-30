package com.lingyang.camera.framework;

import com.lingyang.camera.entity.ResponseError;

public interface BaseCallBack<T extends Object> {

    void error(ResponseError object);

    void success(T t);
}
