package org.testevol.infra;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.testevol.domain.Project;
import org.testevol.domain.ProjectRepository;
import org.testevol.domain.Version;
import org.testevol.versioncontrol.UpdateResult;
import org.testevol.versioncontrol.VersionControlSystem;

@Repository
public class ProjectRepoFileSystem implements ProjectRepository {

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
		for(File dirName:f.listFiles()){
			Version version = new Version(dirName);
			int index = version.getIndex();
			if(index == -1){//means it is the master
				master = new Version(dirName);
			}
			else{
				versions.add(index,version);
			}
		}
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

}
