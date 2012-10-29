package org.testevol.domain;

import java.io.IOException;
import java.util.List;

import org.testevol.engine.domain.Execution;
import org.testevol.engine.report.ExecutionStatus;
import org.testevol.versioncontrol.UpdateResult;

public interface ProjectRepository {

	public void save(Project project) throws Exception;
	public boolean exists(String projectName);
	public Project getProject(String projectName) throws Exception;
	public List<Project> getProjects();
	public List<String> getProjectsNames();
	public void deleteProject(String projectName) throws IOException;
	public void deleteVersion(String projectName, String version) throws Exception;
	public UpdateResult updateRepo(String projectName, String version) throws Exception;
	public Execution createExecution(String projectName, List<String> versionsToExecute) throws Exception;
	public Execution getExecution(String projectName, String id) throws Exception;
	public void saveExecution(String projectName, String id, String name, ExecutionStatus status) throws Exception;
	public List<Execution> getExecutions(Project project) throws Exception;
	public void deleteExecution(String projectName, String id) throws IOException;
	
}
