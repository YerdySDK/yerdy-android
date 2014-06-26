package com.yerdy.services.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class YRDTaskScheduler {

	private List<YRDTimerTask> _queue = new ArrayList<YRDTimerTask>();
	private Timer _timer;
	
	public YRDTaskScheduler() {
		_timer = new Timer("YRDTaskScheduler");
	}
	
	public void queueAndStart(YRDTimerTask task) {
		_queue.add(task);
		task.start(this);
	}
	
	public Timer getTimer() {
		return _timer;
	}

	public void dequeueAll() {
		pause();
		_queue.clear();
	}

	public void dequeue(YRDTimerTask me) {
		_timer.purge();
		_queue.remove(me);
	}
	
	public void pause() {
		List<YRDTimerTask> removalList = new ArrayList<YRDTimerTask>();
		for(YRDTimerTask task : _queue) {
			task.pause();
			if(task.isExpired())
				removalList.add(task);
		}
		
		for(YRDTimerTask task : removalList)
		{
			_queue.remove(task);
		}
		_timer.purge();
	}
	
	public void resume() {
		for(YRDTimerTask task : _queue) {
			task.start(this);
		}
	}
	
}
