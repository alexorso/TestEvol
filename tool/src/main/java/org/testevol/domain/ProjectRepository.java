package org.testevol.domain;

import java.io.IOException;
import java.util.List;

import org.testevol.engine.domain.Execution;
import org.testevol.engine.report.ExecutionStatus;
import org.testevol.versioncontrol.UpdateResult;

public interface ProjectRepository {

	public void save(Project project, String user) throws Exception;
	public boolean exists(String projectName, String user) throws Exception;
	public Project getProject(String projectName, String user) throws Exception;
	public List<Project> getProjects(String user) throws Exception;
	public List<String> getProjectsNames(String user) throws Exception;
	public void deleteProject(String projectName, String user) throws IOException;
	public void deleteVersion(String projectName, String version, String user) throws Exception;
	public UpdateResult updateRepo(String projectName, String version, String user) throws Exception;
	public Execution createExecution(String projectName, List<String> versionsToExecute, String user) throws Exception;
	public Execution getExecution(String projectName, String id, String user) throws Exception;
	public void saveExecution(String projectName, String id, String name, ExecutionStatus status, String user) throws Exception;
	public List<Execution> getExecutions(Project project, String user) throws Exception;
	public void deleteExecution(String projectName, String id, String user) throws IOException;
	public void updateVersionSettings(VersionSettings versionSettings, String user) throws Exception;
	
}
