package com.lingyang.base.utils.db;

public class DataColumn {
	public String name;
	public DataType type;
	public Object defValue;
	public boolean defCanNull = true;
	public boolean unique;
	public boolean isPrimary;
	
	/**
	 * 
	 * [构造简要说明]
	 * @param name 字段名称
	 * @param type 字段类型
	 * @param defValue 字段是否默认初始值
	 * @param defCanNull 字段是否可为空
	 */
	public DataColumn(String name, DataType type, Object defValue, boolean defCanNull) {
		this(name, type, defValue, defCanNull, false);
	}
	
	/**
     * 
     * [构造简要说明]
     * @param name 字段名称
     * @param type 字段类型
     * @param defValue 字段是否默认初始值
     * @param defCanNull 字段是否可为空
     * @param unique 是否唯一约束
     */
    public DataColumn(String name, DataType type, Object defValue, boolean defCanNull, boolean unique) {
        this.name = name;
        this.type = type;
        this.defValue = defValue;
        this.defCanNull = defCanNull;
        this.unique = unique;
    }
	
	public enum DataType {
		INTEGER, TEXT, BLOB, TIMESTAMP, NULL, CURRENT_TIMESTAMP
	}
}
