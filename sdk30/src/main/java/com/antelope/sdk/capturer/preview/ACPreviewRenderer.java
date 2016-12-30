package com.antelope.sdk.capturer.preview;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.antelope.sdk.capturer.ACShape;
import com.antelope.sdk.capturer.ACVideoCapturer;
import com.antelope.sdk.capturer.preview.Texture2dProgram.ProgramType;
import com.antelope.sdk.utils.CLog;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.provider.UserDictionary.Words;

/**
 * @author liaolei
 * @version 创建时间：2016年10月21日 类说明
 */
public class ACPreviewRenderer implements GLSurfaceView.Renderer {
	public interface OnSurfaceTextureCreatedListener {
		void onSurfaceTextureCreated(SurfaceTexture texture);
	}

	private Texture2dProgram program;
	private Drawable2d drawable;
	private SurfaceTexture texture;
	private int textureID;
	private GlFrame frame;
	private OnSurfaceTextureCreatedListener mSurfaceCreatedListener;

	private float[] MvpMatrix = new float[16];
	private float[] texMatrix = new float[16];// 纹理矩阵

	private int mSurfaceWidth, mSurfaceHeight, mPreViewWidth, mPreviewHeight;
	private int mRotation;
	private int mShape;

	public ACPreviewRenderer(int shape, OnSurfaceTextureCreatedListener surfaceCreatedListener) {
		mShape = shape;
		mSurfaceCreatedListener = surfaceCreatedListener;
		Matrix.setIdentityM(MvpMatrix, 0);
		Matrix.setIdentityM(texMatrix, 0);
	}
	
	public void release() {
		if (program != null) {
			program.release();
			program = null;
		}
		drawable = null;
		frame = null;
		if (texture != null) {
			texture.release();
			texture = null;
		}
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//		GLES20.glDisable(GLES20.GL_DITHER);
//		GLES20.glEnable(GLES20.GL_CULL_FACE);
//		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		// 指定颜色缓存（RBGA方式）的当前清除值
		GLES20.glClearColor(0, 0, 0, 0);
//		CLog.i("test renderer surfaceCreate");
		program = new Texture2dProgram(ProgramType.TEXTURE_EXT);
		drawable = new Drawable2d(mShape);
		frame = new GlFrame(drawable, program);
		textureID = program.createTextureObject();
		texture = new SurfaceTexture(textureID);
		GlUtil.checkGlError("create texture");
		if (mSurfaceCreatedListener != null) {
			mSurfaceCreatedListener.onSurfaceTextureCreated(texture);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// 定义视口，说明视见体内的图形应该显示在屏幕上制定的矩形区域内
		GLES20.glViewport(0, 0, width, height);
		mSurfaceHeight = height;
		mSurfaceWidth = width;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		// 清除指定缓存 GL_COLOR_BUFFER_BIT颜色缓存，GL_DEPTH_BUFFER_BIT深度缓存
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		texture.updateTexImage();
		texture.getTransformMatrix(texMatrix);
		
		updateTexMatrix();
		
		frame.drawFrame(MvpMatrix, textureID, texMatrix,
				mShape == ACShape.AC_SHAPE_CIRCLE ? GLES20.GL_TRIANGLE_FAN : GLES20.GL_TRIANGLE_STRIP);
	}
	
	public void setParams(int rotation, int width, int height) {
		mRotation = rotation;
		mPreviewHeight = height;
		mPreViewWidth = width;
	}

	private void updateTexMatrix() {
		int width;
		int height;
		if (mRotation == 90 || mRotation == 270) {
			// 竖屏
			width = mPreviewHeight;
			height = mPreViewWidth;
		} else {
			// 横屏
			width = mPreViewWidth;
			height = mPreviewHeight;
		}

		int area1 = width * mSurfaceHeight;
		int area2 = height * mSurfaceWidth;
		boolean flag = area1 > area2 ? true : false;
//		CLog.i("renderer test scale : A1 > A2 :" + flag);

		// 方法1
//		Matrix.setIdentityM(MvpMatrix, 0);
//		if (mRotation != 0) {
//			// 旋转
//			Matrix.rotateM(MvpMatrix, 0, mRotation, 0, 0, -1);
//		}
//		if (area1 > area2) {
//			Matrix.scaleM(MvpMatrix, 0, 1.f, (float) area1 / (float) area2, 1.0f);
//		} else {
//			Matrix.scaleM(MvpMatrix, 0, 1.f, (float) area2 / (float) area1, 1.0f);
//		}
		
		// 方法2
		Matrix.translateM(texMatrix, 0, 0.5f, 0.5f, 0);
		if (mRotation != 0) {
			Matrix.rotateM(texMatrix, 0, mRotation, 0, 0, 1);
		}
		if (area1 > area2) {
			Matrix.scaleM(texMatrix, 0, (float) area2 / (float) area1, 1.f, 1.0f);
		} else {
			Matrix.scaleM(texMatrix, 0, 1.f, (float) area1 / (float) area2, 1.0f);
		}
		Matrix.translateM(texMatrix, 0, -0.5f, -0.5f, 0);
	}

}
