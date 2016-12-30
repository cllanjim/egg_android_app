package com.lingyang.base.utils.http;

import com.lingyang.base.utils.JacksonUtils;

public class JsonParser implements IDataParser{

	private Class parserClass;
	/**
	 * 是否是列表
	 */
	private boolean isList;
	
	/**
	 * 
	 * @param parseClass 解析类
	 * @param isList 是否解析为列表
	 */
	public JsonParser(Class parseClass, boolean isList) {
		this.parserClass = parseClass;
		this.isList = isList;
	}
	
	public JsonParser(Class parseClass) {
		this(parseClass, false);
	}
	
	@Override
	public Object parseData(String data) {
		if(isList){
			return JacksonUtils.shareJacksonUtils().parseJson2List(data, parserClass);
		}else{
			return JacksonUtils.shareJacksonUtils().parseJson2Obj(data, parserClass);	
		}
	}
}

