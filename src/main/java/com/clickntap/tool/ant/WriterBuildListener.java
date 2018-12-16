package com.clickntap.tool.ant;

import java.io.IOException;
import java.io.Writer;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;

public class WriterBuildListener implements BuildListener {
	private Writer out;

	public WriterBuildListener(Writer out) {
		this.out = out;
	}

	public void buildStarted(BuildEvent event) {
	}

	public void buildFinished(BuildEvent event) {
	}

	public void targetStarted(BuildEvent event) {
	}

	public void targetFinished(BuildEvent event) {
	}

	public void taskStarted(BuildEvent event) {
	}

	public void taskFinished(BuildEvent event) {
	}

	public void messageLogged(BuildEvent event) {
		if (event.getPriority() == 1) {
			try {
				System.out.println(event.getMessage());
				out.write(event.getMessage() + "\n");
			} catch (IOException e) {
			}
		}
	}
}
