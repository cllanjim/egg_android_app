package com.lingyang.base.utils.http;

/**
 * [数据解析借口]
 * 
 */
public interface IDataParser {

	/**
	 * [解析数据]<br/>
	 * @param data 需要解析的数据
	 * @return Object 解析后的数据
	 */
	Object parseData(String data);
} 

