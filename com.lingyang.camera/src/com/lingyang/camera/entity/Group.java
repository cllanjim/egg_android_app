package com.lingyang.camera.entity;

import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 实体组
 * 
 * @param <T>
 */
public class Group<T extends CameraType> extends ArrayList<T> implements CameraType, Serializable {

	/**
     *
     */
	private static final long serialVersionUID = 5595829955115868700L;

	private int ItemCount;

	private int statusCode;
	private int errorCode;
	private String errorMsg;

	public int getItemCount() {
		return ItemCount;
	}

	public void setItemCount(int itemCount) {
		ItemCount = itemCount;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public Group<T> setStatusCode(int statusCode) {
		this.statusCode = statusCode;
		return this;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public Group<T> setErrorCode(int errorCode) {
		this.errorCode = errorCode;
		return this;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public Group<T> setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
		return this;
	}

	@Override
	public boolean add(T object) {
		return super.add(object);
	}

	public void addCommonCam(Camera camera) {
		this.add((T) camera);
	}

	@Override
	public boolean addAll(Collection<? extends T> collection) {
		return super.addAll(collection);
	}

	public void addAllCommon(Group<?> common) {
		this.addAll((Collection<? extends T>) common);
	}

}
