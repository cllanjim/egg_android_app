package com.antelope.sdk.utils;

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

public class RawFramePipe {
	private ByteBuffer mBuffer = null;
	private Object mBufferFence = new Object();
	private Semaphore mSemaphore = new Semaphore(0);
	private int mBufferCapacity = 0;
	private int mWriteDataPosition = 0;
	private int mDataSize = 0;
	private int mReadDataPosition = 0;
	
	public boolean allocate(int capacity) {
		if (capacity <= 0) {
			return false;
		}
		synchronized (mBufferFence) {
			ByteBuffer newBuffer = ByteBuffer.allocateDirect(capacity);
			if (mBuffer != null && mDataSize > 0) {
				newBuffer.limit(mDataSize);
				readData(newBuffer);
				mWriteDataPosition = mDataSize;
				mReadDataPosition = 0;
			}
			mBuffer = newBuffer;
			mBufferCapacity = capacity;
		}
		return true;
	}
	
	public void release() {
		mBuffer = null;
		mBufferFence = null;
		mSemaphore = null;
		mBufferCapacity = 0;
		mWriteDataPosition = 0;
		mDataSize = 0;
		mReadDataPosition = 0;
	}
	
	public void cancel() {
		mSemaphore.release();
	}
	
	public boolean write(ByteBuffer buffer) {
		if (buffer == null || buffer.remaining() == 0) {
			return false;
		}
		boolean result = false;
		int size = buffer.remaining();
		synchronized (mBufferFence) {
			if (mDataSize + size <= mBufferCapacity) {
				writeData(buffer);
				increaseWriteDataPosition(size);
				result = true;
			}
		}
		if (result) {
			mSemaphore.release(size);
		}
		return result;
	}
	
	public boolean read(ByteBuffer buffer) {
		if (buffer == null || buffer.remaining() == 0) {
			return false;
		}
		int dataSize = buffer.remaining();
		try {
			mSemaphore.acquire(dataSize);
		} catch (InterruptedException e) {
			return false;
		}
		boolean result = false;
		synchronized (mBufferFence) {
			if (mDataSize >= dataSize) {
				readData(buffer);
				increaseReadDataPosition(dataSize);
				result = true;
			}
		}
		return result;
	}
	
	private void writeData(ByteBuffer buffer) {
		int size = buffer.remaining();
		mBuffer.clear();
		mBuffer.position(mWriteDataPosition);
		if (mWriteDataPosition + size > mBufferCapacity) {
			int limit = buffer.limit();
			buffer.limit(buffer.position() + mBuffer.remaining());
			mBuffer.put(buffer);
			mBuffer.rewind();
			buffer.limit(limit);
		}
		mBuffer.put(buffer);
	}
	
	private void readData(ByteBuffer buffer) {
		int offset = buffer.position();
		int size = buffer.remaining();
		mBuffer.clear();
		mBuffer.position(mReadDataPosition);
		if (mReadDataPosition + size > mBufferCapacity) {
			int left = size - mBuffer.remaining();
			buffer.put(mBuffer);
			mBuffer.limit(left);
			mBuffer.rewind();
		} else {
			mBuffer.limit(mReadDataPosition + size);
		}
		buffer.put(mBuffer);
		buffer.position(offset);
	}
	
	private void increaseWriteDataPosition(int size) {
		if (mWriteDataPosition + size > mBufferCapacity) {
			mWriteDataPosition = size - (mBufferCapacity - mWriteDataPosition);
		} else {
			mWriteDataPosition += size;
		}
		mDataSize += size;
	}
	
	private void increaseReadDataPosition(int size) {
		if (mReadDataPosition + size > mBufferCapacity) {
			mReadDataPosition = size - (mBufferCapacity - mReadDataPosition);
		} else {
			mReadDataPosition += size;
		}
		mDataSize -= size;
	}
	
}
