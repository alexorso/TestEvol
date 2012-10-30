package org.testevol.infra;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.testevol.domain.Project;
import org.testevol.domain.ProjectRepository;
import org.testevol.domain.Version;
import org.testevol.engine.domain.Execution;
import org.testevol.engine.report.ExecutionStatus;
import org.testevol.engine.util.Utils;
import org.testevol.versioncontrol.UpdateResult;
import org.testevol.versioncontrol.VersionControlSystem;

@Repository
public class ProjectRepoFileSystem implements ProjectRepository {

	private static final String EXECUTION_PROPERTIES = "execution.properties";
	private static final String EXECUTIONS_DIR = "reports";
	private File projectsDir = null;

	@Autowired
	public ProjectRepoFileSystem(
			@Value("#{testEvolProperties.projects_dir}") String dir) {
		projectsDir = new File(dir);
		if (!projectsDir.exists()) {
			projectsDir.mkdirs();
		}

	}

	public void save(Project project) throws Exception {
		if (!project.validate()) {
			throw new RuntimeException("Missing requried fields");
		}
		if (exists(project.getName())) {
			throw new RuntimeException("Project already exists");
		}

		//TODO: implement this inside version object
		VersionControlSystem versionControlSystem = project
				.getVersionControlSystem();
		File projectDir = new File(projectsDir, project.getName());
		versionControlSystem.checkout(projectDir, project.getBranchesToCheckout());
	}

	public boolean exists(String projectName) {
		File projectDir = new File(projectsDir, projectName);
		return projectDir.exists();
	}

	public Project getProject(String projectName) throws Exception{
		File f = new File(projectsDir,projectName);
		Project project = new Project();
		project.setName(projectName);
		File[] files = f.listFiles();
		List<Version> versions = new ArrayList<Version>(files.length);
		Version master = null;
		for(File dir:f.listFiles()){
			if(EXECUTIONS_DIR.equals(dir.getName())){
				continue;
			}
			Version version = new Version(dir);
			int index = version.getIndex();
			if(index == -1){//means it is the master
				master = new Version(dir);
			}
			else{
				versions.add(version);
			}
		}
		Collections.sort(versions, new Comparator<Version>() {
			@Override
			public int compare(Version version1, Version version2) {
				return new Integer(version1.getIndex()).compareTo(version2.getIndex());

			}
		});
		
		if(master!= null){
			versions.add(0, master);
		}
		
		project.setVersionsList(versions);
		return project;
	}

	public List<Project> getProjects() {
		String[] projectsName = projectsDir.list();
		List<Project> projects = new ArrayList<Project>();
		for(String projectName:projectsName){
			projects.add(new Project(projectName));
		}
		
		return projects;
	}

	public void deleteProject(String projectName) throws IOException {
		File projectDir = new File(projectsDir, projectName);
		FileUtils.deleteDirectory(projectDir);
	}

	public void deleteVersion(String projectName, String versionName) throws Exception {
		File projectDir = new File(projectsDir, projectName);
		File versionDir = new File(projectDir,versionName);
		
		Version version = new Version(versionDir);
		int versionIndex = version.getIndex();
		
		FileUtils.deleteDirectory(versionDir);
		if(versionIndex >= 0){
			for(File dir:projectDir.listFiles()){
				Version otherVersion = new Version(dir);
				if(otherVersion.getIndex() > versionIndex){
					otherVersion.setIndex(otherVersion.getIndex() - 1);
					otherVersion.saveProperties();
				}
			}			
		}
	}

	public UpdateResult updateRepo(String projectName, String versionName) throws Exception {
		File projectDir = new File(projectsDir, projectName);
		File versionDir = new File(projectDir,versionName);
		Version version = new Version(versionDir);
		return version.updateLocalFilesFromRepository();
	}

	@Override
	public Execution createExecution(String projectName, List<String> versionsToExecute) throws Exception {
		File reportDir = null;
		try{
			Project project = getProject(projectName);
			project.setVersionsToExecute(versionsToExecute);
			
			File projectDir = new File(projectsDir, project.getName());
			reportDir = Utils.getTempDir(new File(projectDir, EXECUTIONS_DIR));
			reportDir.mkdirs();
		
			String versions = "";
			for (int i = versionsToExecute.size() - 1;i>=0;i--){
				versions += versionsToExecute.get(i);
				if(i > 0){
					versions += ", ";
				}
			}
			
			Execution execution = new Execution(project, "Untitled execution", reportDir, Calendar.getInstance().getTimeInMillis(), versions, ExecutionStatus.INITIALIZED);
			execution.saveProperties(new File(reportDir, EXECUTION_PROPERTIES));
			
			return execution;

		}
		catch(Exception ex){
			if(reportDir != null && reportDir.exists()){
				FileUtils.deleteDirectory(reportDir);
			}
			throw ex;
		}
	}

	@Override
	public Execution getExecution(String projectName, String executionId) throws Exception {
		Project project = getProject(projectName);
		File executionDir = getExecutionDir(projectName, executionId);
		Properties properties = new Properties();
		properties.load(new FileInputStream(new File(executionDir, EXECUTION_PROPERTIES)));	
		String executionLog = "";
		File log = new File(executionDir, "log.txt");
		if(log.exists()){
			executionLog = FileUtils.readFileToString(log);
		}
		
		return new Execution(project, executionDir, properties).setExecutionLog(executionLog);
	}
	
	private File getExecutionDir(String projectName, String executionId){
		File projectDir = new File(projectsDir, projectName);
		File executions = new File(projectDir, EXECUTIONS_DIR);
		return new File(executions,executionId);
	}

	@Override
	public void saveExecution(String projectName, String executionId, String name, ExecutionStatus status) throws Exception {
		Execution execution = getExecution(projectName, executionId);
		if(name != null){
			execution.setName(name);	
		}
		
		if(status!= null){
			execution.setStatus(status);			
		}
		execution.saveProperties(new File(execution.getExecutionDir(), EXECUTION_PROPERTIES));
	}

	@Override
	public List<Execution> getExecutions(Project project) throws Exception {
		File projectDir = new File(projectsDir,project.getName());
		File executionsDir = new File(projectDir, EXECUTIONS_DIR);
		
		List<Execution> executions = new ArrayList<Execution>();
		if(executionsDir.exists()){
			for(File executionDir:executionsDir.listFiles()){
				Properties properties = new Properties();
				properties.load(new FileInputStream(new File(executionDir, EXECUTION_PROPERTIES)));	
				Execution execution = new Execution(project, executionDir, properties);	
				executions.add(execution);
			}
		}
		
		Collections.sort(executions, new Comparator<Execution>() {
			@Override
			public int compare(Execution execution1, Execution execution2) {
				return execution2.getCreatedAt().compareTo(execution1.getCreatedAt());//want list in descendant order!
			}
		});
		
		return executions;
	}

	@Override
	public void deleteExecution(String projectName, String id) throws IOException {
		File projectDir = new File(projectsDir, projectName);
		File executionsDir = new File(projectDir, EXECUTIONS_DIR);
		File executionDir = new File(executionsDir,id);
		
		FileUtils.deleteDirectory(executionDir);		
	}

	@Override
	public List<String> getProjectsNames() {
		return Arrays.asList(projectsDir.list());
	
	}
	
	

}
