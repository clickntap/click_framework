package com.clickntap.tool.ant;

import java.io.File;
import java.util.Map;

import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

public class AntRunner {

	private Project project;

	private String target;

	public AntRunner(File file) {
		project = new Project();
		project.init();
		ProjectHelper.getProjectHelper().parse(project, file);
		setTarget(project.getDefaultTarget());
	}

	public AntRunner(String file) {
		this(new File(file));
	}

	public Map execute(String target) {
		setTarget(target);
		return execute();
	}

	public Map execute(BuildListener listener) {
		project.addBuildListener(listener);
		return execute();
	}

	public Map execute() {
		project.executeTarget(target);
		return project.getProperties();
	}

	public void setParameter(String key, String value) {
		project.setProperty(key, value);
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
}
