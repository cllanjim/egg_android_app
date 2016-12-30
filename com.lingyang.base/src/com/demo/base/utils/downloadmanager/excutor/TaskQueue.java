package com.lingyang.base.utils.downloadmanager.excutor;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TaskQueue<T> extends AbstractQueue<T> implements BlockingQueue<T>{

	private final static String TAG = "TaskQueue";
	
	private final static int DEFAULT_MAX_RUNNING_SIZE = 5;
	
    private final ReentrantLock takeLock = new ReentrantLock();

    private final ReentrantLock putLock = new ReentrantLock();
    
	private LinkedList<T> runningQueue;
	private LinkedList<T> pendingQueue;
	
    private final Condition notEmpty = takeLock.newCondition();
    private final Condition notFull = putLock.newCondition();
    
	private int maxRunningSize = DEFAULT_MAX_RUNNING_SIZE;
	
	public TaskQueue() {
		runningQueue = new LinkedList<T>();
		pendingQueue = new LinkedList<T>();
	}
	
	public List<T> getRunningQueue() {
		fullyLock();
		try {
			List<T> list = new ArrayList<T>();
			Iterator<T> it = runningQueue.iterator();
			while(it.hasNext()) {
				T t = it.next();
				list.add(t);
			}
			return list;
		} finally {
			fullyUnlock();
		}
	}
	
	public List<T> getPendingQueue(){
		fullyLock();
		try {
			List<T> list = new ArrayList<T>();
			Iterator<T> it = pendingQueue.iterator();
			while(it.hasNext()) {
				T t = it.next();
				list.add(t);
			}
			return list;
		} finally {
			fullyUnlock();
		}
	}
	
	public T getPendingTask(T obj) {
		fullyLock();
		try {
			Iterator<T> it = pendingQueue.iterator();
			while(it.hasNext()) {
				T t = it.next();
				if (obj.equals(t)) {
					return t;
				}
			}
			return null;
		} finally {
			fullyUnlock();
		}
	}
	
	public boolean removeQueue(T t) {
		final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
        	boolean isSuc = runningQueue.remove(t);
        	if (! isSuc) {
        		isSuc = pendingQueue.remove(t);
        	} else {
        		signalNotEmpty();
        	}
        	return isSuc;
        } finally {
            takeLock.unlock();
        }
	}
	
	
	public List<T> clearTask() {
		List<T> list = new ArrayList<T>();
		fullyLock();
		try {
			list.addAll(runningQueue);
			list.addAll(pendingQueue);
			runningQueue.clear();
			pendingQueue.clear();
		} finally {
			fullyUnlock();
		}
		return list;
	}

	public boolean removeRunningTask(T t) {
		final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
        	boolean isSuc = runningQueue.remove(t);
        	signalNotEmpty();
        	return isSuc;
        } finally {
            takeLock.unlock();
        }
	}
	
	@Override
	public T take() throws InterruptedException {
		T x;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            while (runningQueue.size() >= maxRunningSize
            		|| pendingQueue.size() == 0) {
                notEmpty.await();
            }
            if (pendingQueue.size() == 0) {
            	return null;
            }
            x = pendingQueue.remove();
            runningQueue.add(x);
        	notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
        signalNotFull();
        return x;
	}
	
	@Override
	public boolean offer(T e) {
		if (e == null) throw new NullPointerException();
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
    		pendingQueue.add(e);
            notFull.signal();
        } finally {
            putLock.unlock();
        }
        signalNotEmpty();
        return true;
	}
	
	public void insertFirst(T e) {
		if (e == null) throw new NullPointerException();
		fullyLock();
        try {
        	pendingQueue.remove(e);
    		pendingQueue.addFirst(e);
            notFull.signal();
        } finally {
        	fullyUnlock();
        }
        signalNotEmpty();
	}
	
	@Override
	public T peek() {
		final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
        	return pendingQueue.peek();
        } finally {
            takeLock.unlock();
        }
	}
	
	@Override
	public boolean contains(Object object) {
		fullyLock();
		try {
			if (runningQueue.contains(object)) {
				return true;
			}
			if (pendingQueue.contains(object)) {
				return true;
			}
		} finally {
			fullyUnlock();
		}
		return false;
	}
	
	public boolean containsRunning(Object object) {
		fullyLock();
		try {
			if (runningQueue.contains(object)) {
				return true;
			}
		} finally {
			fullyUnlock();
		}
		return false;
	}
	
	public boolean containsPending(Object object) {
		fullyLock();
		try {
			if (pendingQueue.contains(object)) {
				return true;
			}
		} finally {
			fullyUnlock();
		}
		return false;
	}

	
	@Override
	public T poll() {
		throw new RuntimeException("not support method");
	}

	@Override
	public void put(Object e) throws InterruptedException {
		throw new RuntimeException("not support method");
	}

	@Override
	public boolean offer(Object e, long timeout, TimeUnit unit)
			throws InterruptedException {
		throw new RuntimeException("not support method");
	}

	@Override
	public T poll(long timeout, TimeUnit unit) throws InterruptedException {
		throw new RuntimeException("not support method");
	}

	@Override
	public int remainingCapacity() {
		throw new RuntimeException("not support method");
	}

	@Override
	public int drainTo(Collection c) {
		throw new RuntimeException("not support method");
	}

	@Override
	public int drainTo(Collection c, int maxElements) {
		throw new RuntimeException("not support method");
	}

	@Override
	public Iterator iterator() {
		throw new RuntimeException("not support method");
	}

	@Override
	public int size() {
		fullyLock();
		try {
			return getTotalSize();
		} finally {
			fullyUnlock();
		}
	}
	
	@Override
	public boolean isEmpty() {
		fullyLock();
		try {
			return getTotalSize() > 0 ? false : true;
		} finally {
			fullyUnlock();
		}
	}

	public int getRunningSize() {
		fullyLock();
		try {
			return runningQueue.size();
		} finally {
			fullyUnlock();
		}
	}
	
	public int getPendingSize() {
		fullyLock();
		try {
			return pendingQueue.size();
		} finally {
			fullyUnlock();
		}
	}
	
	private int getTotalSize(){
		return runningQueue.size() + pendingQueue.size();
	}
	
	private T removeTask() {
		T x = null;
		if (runningQueue.size() > 0) {
    		x = runningQueue.remove();
    	} else if (pendingQueue.size() > 0) {
    		x = pendingQueue.remove();
    	}
		return x;
	}
	
	void fullyLock() {
        putLock.lock();
        takeLock.lock();
    }

    void fullyUnlock() {
        takeLock.unlock();
        putLock.unlock();
    }
    
    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    private void signalNotFull() {
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            notFull.signal();
        } finally {
            putLock.unlock();
        }
    }

	public int getMaxRunningSize() {
		return maxRunningSize;
	}

	public void setMaxRunningSize(int maxRunningSize) {
		this.maxRunningSize = maxRunningSize;
	}
}

