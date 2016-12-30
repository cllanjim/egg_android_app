package com.antelope.sdk.capturer.preview;
/**
* @author liaolei 
* @version 创建时间：2016年10月21日 
* 类说明
*/
public class GlFrame {
	
	private Drawable2d drawable;
	private Texture2dProgram program;

	public GlFrame(Drawable2d d,Texture2dProgram p){
		drawable=d;
		program=p;
	}
	
	public void changeProgram(Texture2dProgram p){
		program.release();
		program=p;
	}
	
	public void release(){
		if(program!=null){
			program.release();
			program=null;
		}
	}
	
	public void drawFrame(float[] mvpMatrix,int textureId,float[] texMatrix,int mode){
		
		program.draw(mvpMatrix, drawable.getVertexArray(), 0, drawable.getVertexCount(),
				drawable.getCoordsPerVertex(), drawable.getVertexStride(), texMatrix, drawable.getTexCoordArray(), 
				textureId, drawable.getTexCoordStride(),mode);
		
	}
}
