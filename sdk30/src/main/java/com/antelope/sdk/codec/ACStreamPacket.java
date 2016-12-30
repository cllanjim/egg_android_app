package com.antelope.sdk.codec;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
* @author liaolei 
* @version 创建时间：2016年9月8日 
* 类说明
*/
public class ACStreamPacket {
	public ByteBuffer buffer = null;
	public int offset = 0;
	public int size = 0;
	/**
	 * 详细定义请参考{@link ACFrameType}
	 */
	public int type = 0;
	public long timestamp = 0;
	
	public int getEmptyPacketSize() {
		return 20;
	}
	
	public int getPacketSize() {
		return this.size + this.getEmptyPacketSize();
	}
	
	public void writeTo(ByteBuffer buffer, int offset) {
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.clear();
		buffer.position(offset);
		buffer.putInt(0);
		buffer.putInt(this.size);
		buffer.putInt(this.type);
		buffer.putLong(this.timestamp);
		if (this.buffer != null && this.size > 0 && this.size <= buffer.remaining()) {
			this.buffer.limit(this.offset + this.size);
			this.buffer.position(this.offset);
			buffer.put(this.buffer);
		}
	}
	
	public void readFrom(ByteBuffer buffer, int offset, boolean copyData) {
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.clear();
		buffer.position(offset);
		this.offset = buffer.getInt();
		this.size = buffer.getInt();
		this.type = buffer.getInt();
		this.timestamp = buffer.getLong();
		if (this.size > 0 && this.size <= buffer.remaining()) {
			if (copyData) {
				if (this.buffer == null || this.buffer.capacity() <this.size) {
					this.buffer = ByteBuffer.allocateDirect(this.size);
				} else {
					this.buffer.clear();
				}
				buffer.limit(buffer.position() + this.offset + this.size);
				this.buffer.put(buffer);
			} else {
				this.offset += buffer.position();
				this.buffer = buffer;
			}
		}
	}
	
}
