package com.clickntap.platform;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import com.clickntap.hub.App;
import com.clickntap.tool.f.F;

public class FExecutor {

	private App app;
	private F f;
	private ThreadPoolExecutor executor;
	private int n;

	public FExecutor(F f, App app) {
		this.f = f;
		this.app = app;
		this.n = 1;
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(16);
		executor.setThreadFactory(new ThreadFactory() {
			public Thread newThread(Runnable r) {
				FThread thread = new FThread(r);
				thread.setName("F (" + (n++) + ")");
				try {
					thread.init(f);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return thread;
			}
		});
	}

	public void execute(FTask task) {
		executor.execute(task);
		while (!task.isDone()) {
			try {
				Thread.sleep(1);
			} catch (Exception e) {
			}
		}
	}

	public F getF() {
		return f;
	}

	public App getApp() {
		return app;
	}

}
