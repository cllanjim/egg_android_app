package com.antelope.sdk.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class StreamPacketPipe {
	private ByteBuffer mBuffer = null;
	private ByteBuffer mSizeBuffer = ByteBuffer.allocateDirect(4);
	private Object mBufferFence = new Object();
	private Semaphore mSemaphore = new Semaphore(0);
	private int mBufferCapacity = 0;
	private int mWriteDataPosition = 0;
	private int mDataSize = 0;
	private int mReadDataPosition = 0;
	private int mWaitAvailableSize = 0;
	private int mMaxBufferSize = -1;
	
	public StreamPacketPipe(int maxBufferSize) {
		mMaxBufferSize = maxBufferSize;
		mSizeBuffer.order(ByteOrder.BIG_ENDIAN);
	}

	public boolean allocate(int capacity) {
		if (capacity <= 0 || capacity > mMaxBufferSize) {
			return false;
		}
		synchronized (mBufferFence) {
			ByteBuffer newBuffer;
			try {
				newBuffer = ByteBuffer.allocateDirect(capacity);
			} catch (OutOfMemoryError e) {
				return false;
			}
			if (mBuffer != null && mDataSize > 0 && mDataSize <= capacity) {
				newBuffer.limit(mDataSize);
				readData(newBuffer);
				mWriteDataPosition = mDataSize;
				mReadDataPosition = 0;
			} else {
				mWriteDataPosition = 0;
				mDataSize = 0;
				mReadDataPosition = 0;
			}
			mBuffer = newBuffer;
			mBufferCapacity = capacity;
		}
		return true;
	}
	
	public void release() {
		mBuffer = null;
		mSizeBuffer = null;
		mBufferFence = null;
		mSemaphore = null;
		mBufferCapacity = 0;
		mWriteDataPosition = 0;
		mDataSize = 0;
		mReadDataPosition = 0;
	}
	
	public void cancel() {
		synchronized (mBufferFence) {
			mBufferFence.notifyAll();
		}
		mSemaphore.release();
	}
	
	public void clear() {
		synchronized (mBufferFence) {
			mWriteDataPosition = 0;
			mDataSize = 0;
			mReadDataPosition = 0;
		}
		mSemaphore.drainPermits();
	}
	
	public int capacity() {
		synchronized (mBufferFence) {
			return mBufferCapacity;
		}
	}
	
	public int remaining() {
		synchronized (mBufferFence) {
			return mBufferCapacity - mDataSize;
		}
	}
	
	public int cache() {
		synchronized (mBufferFence) {
			return mDataSize;
		}
	}
	
	public boolean write(ByteBuffer buffer, int timeout) {
		if (buffer == null || buffer.remaining() == 0) {
			return false;
		}
		boolean result = false;
		int size = buffer.remaining();
		synchronized (mBufferFence) {
			int totalSize = mDataSize + size + 4;
			if (timeout != 0) {
				if (totalSize > mBufferCapacity) {
					mWaitAvailableSize = size + 4;
					try {
						if (timeout < 0) {
							mBufferFence.wait();
						} else {
							mBufferFence.wait(timeout);
						}
					} catch (InterruptedException e) {
					}
					mWaitAvailableSize = 0;
					totalSize = mDataSize + size + 4;
				}
			}
			if (totalSize <= mBufferCapacity) {
				mSizeBuffer.clear();
				mSizeBuffer.putInt(0, size);
				writeData(mSizeBuffer);
				increaseWriteDataPosition(4);
				writeData(buffer);
				increaseWriteDataPosition(size);
				result = true;
			}
		}
		if (result) {
			mSemaphore.release();
		}
		return result;
	}
	
	public ByteBuffer read(ByteBuffer buffer, int timedout) {
		try {
			if (timedout < 0) {
				mSemaphore.acquire();
			} else if (timedout == 0) {
				if (!mSemaphore.tryAcquire()) {
					return null;
				}
			} else {
				if (!mSemaphore.tryAcquire(timedout, TimeUnit.MILLISECONDS)) {
					return null;
				}
			}
		} catch (InterruptedException e) {
			return null;
		}
		ByteBuffer result = null;
		int dataSize;
		synchronized (mBufferFence) {
			if (mDataSize > 0) {
				mSizeBuffer.clear();
				readData(mSizeBuffer);
				increaseReadDataPosition(4);
				dataSize = mSizeBuffer.getInt(0);
				if (buffer == null || dataSize > buffer.remaining()) {
					if (buffer.isDirect()) {
						buffer = ByteBuffer.allocateDirect(dataSize);
					} else {
						buffer = ByteBuffer.allocate(dataSize);
					}
				} else {
					buffer.limit(buffer.position() + dataSize);
				}
				readData(buffer);
				increaseReadDataPosition(dataSize);
				result = buffer;
				if (mWaitAvailableSize > 0) {
					if (mWaitAvailableSize + mDataSize <= mBufferCapacity) {
						mBufferFence.notify();
					}
				}
			}
		}
		return result;
	}
	
	public ByteBuffer read(ByteBuffer buffer) {
		return read(buffer, -1);
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
