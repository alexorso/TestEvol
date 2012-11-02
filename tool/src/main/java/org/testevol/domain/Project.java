package org.testevol.domain;

import java.util.List;

import org.testevol.versioncontrol.VersionControlSystem;

public class Project {

	private String name;
	private RepositoryInfo repositoryInfo;
	private List<Version> versionsList;
	private List<String> versionsToExecute;
	private List<String> branchesToCheckout;
	private boolean includeCoverageAnalysis;

	public Project() {}

	
	public Project(String name) {
		this.name = name;
	}

	public List<String> getBranchesToCheckout() {
		return branchesToCheckout;
	}
	
	public String getName() {
		return name;
	}
	
	public RepositoryInfo getRepositoryInfo() {
		return repositoryInfo;
	}

	public String getUrl() {
		return repositoryInfo.getUrl();
	}
	
	public VersionControlSystem getVersionControlSystem(){
		return VersionControlSystem.getInstance(repositoryInfo);
	}
	
	public List<Version> getVersionsList() {
		return versionsList;
	}

	public List<String> getVersionsToExecute() {
		return versionsToExecute;
	}
	
	public boolean isIncludeCoverageAnalysis() {
		return includeCoverageAnalysis;
	}
	
	public void setBranchesToCheckout(List<String> branchesToCheckout) {
		this.branchesToCheckout = branchesToCheckout;
	}

	public void setIncludeCoverageAnalysis(boolean includeCoverageAnalysis) {
		this.includeCoverageAnalysis = includeCoverageAnalysis;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setRepositoryInfo(RepositoryInfo repositoryInfo) {
		this.repositoryInfo = repositoryInfo;
	}
	
	public void setVersionsList(List<Version> versionsList) {
		this.versionsList = versionsList;
	}
	
	public void setVersionsToExecute(List<String> versionsToExecute) {
		this.versionsToExecute = versionsToExecute;
	}
	
	public boolean validate() {
		if(getName() == null || getName().trim().isEmpty()){
			return false;
		}
		if(getRepositoryInfo() == null){
			return false;
		}
		String vcs = getRepositoryInfo().getType();
		if(vcs == null || vcs.trim().isEmpty()){
			return false;
		}
		if(getUrl() == null || getUrl().trim().isEmpty()){
			return false;
		}	
		if((versionsList == null || versionsList.size() < 2) &&
		   (branchesToCheckout == null || branchesToCheckout.size() < 2)){
			return false;
		}
		
		return true;
	}

}
