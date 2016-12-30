package com.antelope.sdk.utils;

import java.util.concurrent.Exchanger;

import com.antelope.sdk.utils.CLog;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class WorkThreadExecutor {
	private String mThreadName = "WorkThreadExecutor";
    private HandlerThread mRunner;
    private Handler mHandler;
    private boolean mRunning = false;
    private Handler.Callback mCallback = null;
    private Exchanger<Boolean> mQuitExchanger = new Exchanger<Boolean>();

    public WorkThreadExecutor(String name) {
        if (name != null && !name.isEmpty()) {
        	mThreadName = name;
        }
    }

    /**
     * 处理异步任务
     *
     * @param runnable
     */
    public void executeTask(Runnable runnable) {
    	synchronized (this) {
    		if (!mRunning) {
    			return;
    		}
    	}
        mHandler.post(runnable);
    }
    
    public Object executeTaskAndWaitForResult(SyncRunnable runnable, Object obj) {
    	synchronized (this) {
    		if (!mRunning) {
    			return null;
    		}
    	}
    	MessageContext context = new MessageContext(obj);
    	context.callback = runnable;
    	mHandler.sendMessageDelayed(Message.obtain(mHandler, -1, context), 0);
    	return exchange(context.exchanger, null);
    }

    /**
     * 处理异步任务
     *
     * @param runnable
     * @param delayMillis 延迟时间
     */
    public void executeTaskDelay(Runnable runnable, long delayMillis) {
    	synchronized (this) {
    		if (!mRunning) {
    			return;
    		}
    	}
        mHandler.postDelayed(runnable, delayMillis);
    }

    /**
     * 取消异步任务
     *
     * @param runnable
     */
    public void removeTasks(Runnable runnable) {
    	synchronized (this) {
    		if (!mRunning) {
    			return;
    		}
    	}
        mHandler.removeCallbacks(runnable);
    }
    
    public void removeMessages(int message, Object object) {
    	synchronized (this) {
    		if (!mRunning) {
    			return;
    		}
    	}
    	mHandler.removeMessages(message, object);
    }
    
    public void removeTasksAndMessages(Object token) {
    	synchronized (this) {
    		if (!mRunning) {
    			return;
    		}
    	}
    	mHandler.removeCallbacksAndMessages(token);
    }
    
    /**
     * 发送消息给Handler处理
     * @param msg
     * @param delayMillis
     */
    public void sendMessageDelayed(int msg, Object obj, long delayMillis) {
    	synchronized (this) {
    		if (!mRunning) {
    			return;
    		}
    	}
    	mHandler.sendMessageDelayed(Message.obtain(mHandler, msg, obj), delayMillis);
    }
    
    public void sendMessage(int msg, Object obj) {
    	this.sendMessageDelayed(msg, obj, 0);
    }
    
    public void sendMessageDelayed(int msg, int arg1, int arg2, long delayMillis) {
    	synchronized (this) {
    		if (!mRunning) {
    			return;
    		}
    	}
    	mHandler.sendMessageDelayed(Message.obtain(mHandler, msg, arg1, arg2), delayMillis);
    }
    
    public void sendMessage(int msg, int arg1, int arg2) {
    	this.sendMessageDelayed(msg, arg1, arg2, 0);
    }
    
    public void sendMessageDelayed(int msg, int arg1, int arg2, Object obj, long delayMillis) {
    	synchronized (this) {
    		if (!mRunning) {
    			return;
    		}
    	}
    	mHandler.sendMessageDelayed(Message.obtain(mHandler, msg, arg1, arg2, obj), delayMillis);
    }
    
    public void sendMessage(int msg, int arg1, int arg2, Object obj) {
    	sendMessageDelayed(msg, arg1, arg2, obj, 0);
    }
    
    public void sendEmptyMessage(int msg) {
    	synchronized (this) {
    		if (!mRunning) {
    			return;
    		}
    	}
    	mHandler.sendEmptyMessage(msg);
    }
    
    public Object sendMessageAndWaitForResult(int msg, int arg1, int arg2) {
    	return sendMessageAndWaitForResult(msg, arg1, arg2, null);
    }
    
    public Object sendMessageAndWaitForResult(int msg, Object obj) {
    	return sendMessageAndWaitForResult(msg, 0, 0, obj);
    }
    
    public Object sendEmptyMessageAndWaitForResult(int msg) {
    	return sendMessageAndWaitForResult(msg, 0, 0, null);
    }
    
    public Object sendMessageAndWaitForResult(int msg, int arg1, int arg2, Object obj) {
    	synchronized (this) {
    		if (!mRunning) {
    			return null;
    		}
    	}
    	MessageContext context = new MessageContext(obj);
    	mHandler.sendMessageDelayed(Message.obtain(mHandler, msg, arg1, arg2, context), 0);
    	return exchange(context.exchanger, null);
    }
    
    /*
     * 扫描。基于效率考虑，如果上一次扫描未结束，将不会有任何动作。
     */
    public void start(Handler.Callback callback) {
    	synchronized (this) {
    		if (mRunning) {
    			CLog.w("already starting...");
    			return;
    		}
    		mRunning = true;
    	}
    	mCallback = callback;
    	mRunner = new HandlerThread(mThreadName);
        mRunner.setPriority(Thread.NORM_PRIORITY);
        mRunner.start();
        mHandler = new Handler(mRunner.getLooper(), mLocalCallback);
    }

    public void stop() {
    	synchronized (this) {
    		if (!mRunning) {
    			return;
    		}
    		mRunning = false;
    	}
        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(mQuitRunnable);
//        CLog.i("quitting...");
        exchange(mQuitExchanger, false);
        mHandler = null;
        mRunner = null;
    }
    
    public void release() {
    }
    
    private Runnable mQuitRunnable = new Runnable() {
		@Override
		public void run() {
//			CLog.i("quit");
			boolean r = mRunner.quit();
			exchange(mQuitExchanger, r);
		}
    };
    
    private Handler.Callback mLocalCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			MessageContext context = null;
			if (msg.obj != null && (msg.obj instanceof MessageContext)) {
				context = (MessageContext) msg.obj;
				msg.obj = context.obj;
			}
			if (msg.what == -1 && context != null && context.callback != null) {
				exchange(context.exchanger, context.callback.run(context.obj));
				return true;
			} else if (mCallback != null) {
				if (mCallback.handleMessage(msg)) {
					if (context != null) {
						exchange(context.exchanger, msg.obj);
					}
					return true;
				}
			}
			return false;
		}
	};
	
	private class MessageContext {
		Exchanger<Object> exchanger = new Exchanger<Object>();
		Object obj = null;
		SyncRunnable callback = null;
		MessageContext(Object obj) {
			this.obj = obj;
		}
	}
	
	public interface SyncRunnable {
		Object run(Object obj);
	}

	private <T> T exchange(Exchanger<T> exchanger, T value) {
		try {
			return exchanger.exchange(value);
		} catch (InterruptedException e) {
			throw new RuntimeException();
		}
	}
	
}
