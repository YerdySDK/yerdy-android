package com.yerdy.services.core;

import java.util.TimerTask;

import android.os.SystemClock;
import android.util.Log;

public class YRDTimerTask {

	private TimerTask _task;
	private Runnable _run;
	private long _start;
	private long _remaining;
	private boolean _ran = false;
	private boolean _running = false;
	
	public YRDTimerTask(long delay, Runnable runnable) {
		_remaining = delay;
		_run = runnable;
	}
	
	public void start(final YRDTaskScheduler scheduler) {
		if(!_ran && !_running) {
			final YRDTimerTask me = this;
			_task = new TimerTask() {
				@Override
				public void run() {
					_ran = true;
					if(_run != null)
						_run.run();
					_task.cancel();
					scheduler.dequeue(me);
				}
			};
			_start = SystemClock.elapsedRealtime();
			scheduler.getTimer().schedule(_task, _remaining);
		}
	}
	
	public boolean cancel() {
		_ran = true;
		return _task.cancel();
	}
	
	public boolean pause() {
		long delta = _remaining - (SystemClock.elapsedRealtime() - _start);
		_remaining = (delta > 0)?(delta):(0);
		Log.i("Remaining", "time: " + _remaining);
		return _task.cancel();
	}
	
	public boolean isExpired() {
		if(_ran || _task.scheduledExecutionTime() > 0)
			return true;
		else
			return false;
	}
	
	public void setRunnable(Runnable run) {
		_run = run;
	}
}
