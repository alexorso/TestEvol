package org.testevol.engine.domain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.testevol.domain.Project;
import org.testevol.engine.report.ExecutionStatus;

public class Execution {

	private String id;
	private String name;
	private File executionDir;
	private long createdAt;
	private String versions;
	private ExecutionStatus status;
	private Project project;
	private String executionLog;
	
	public Execution(Project project, String name, File executionDir,
			long createdAt, String versions, ExecutionStatus status) {
		super();
		this.id = executionDir.getName();
		this.name = name;
		this.executionDir = executionDir;
		this.createdAt = createdAt;
		this.versions = versions;
		this.status = status;
		this.project = project;
	}

	public Execution(Project project, File executionDir, Properties properties) {
		this(project, properties.getProperty("name"), executionDir, Long
				.parseLong(properties.getProperty("created-at")), properties
				.getProperty("versions"), ExecutionStatus.fromCode(properties
				.getProperty("status")));

	}

	public Project getProject() {
		return project;
	}

	public String getName() {
		return name;
	}

	public Long getCreatedAt() {
		return createdAt;
	}

	public String getVersions() {
		return versions;
	}

	public ExecutionStatus getStatus() {
		return status;
	}

	public File getExecutionDir() {
		return executionDir;
	}
	
	public String getId() {
		return id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setStatus(ExecutionStatus status) {
		this.status = status;
	}
	
	public Execution setExecutionLog(String executionLog) {
		this.executionLog = executionLog;
		return this;
	}
	
	public String getExecutionLog() {
		return executionLog;
	}

	public Properties getProperties() {
		Properties properties = new Properties();
		properties.setProperty("name", getName());
		properties.setProperty("created-at", String.valueOf(getCreatedAt()));
		properties.setProperty("versions", getVersions());
		properties.setProperty("status", getStatus().getCode());

		return properties;
	}
	
	public void saveProperties(File file) throws FileNotFoundException, IOException{
		Properties properties = getProperties();
		properties.store(new FileOutputStream(file), "Execution Properties");			

		
	}

	public File getCSVReport() {
		return new File(executionDir,"results.txt");
	}
}
