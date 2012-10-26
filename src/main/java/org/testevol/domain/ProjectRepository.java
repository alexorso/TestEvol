package org.testevol.domain;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.testevol.versioncontrol.UpdateResult;

public interface ProjectRepository {

	public void save(Project project) throws Exception;
	public boolean exists(String projectName);
	public Project getProject(String projectName) throws Exception;
	public List<Project> getProjects();
	public void deleteProject(String projectName) throws IOException;
	public void deleteVersion(String projectName, String version) throws Exception;
	public UpdateResult updateRepo(String projectName, String version) throws Exception;
	public File createReportDir(Project project);
	
}
