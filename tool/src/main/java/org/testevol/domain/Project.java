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
	
	public boolean isIncludeCoverageAnalysis() {
		return includeCoverageAnalysis;
	}

	public void setIncludeCoverageAnalysis(boolean includeCoverageAnalysis) {
		this.includeCoverageAnalysis = includeCoverageAnalysis;
	}

	public Project() {
	}
	
	public Project(String name) {
		super();
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getUrl() {
		return repositoryInfo.getUrl();
	}
	
	public List<Version> getVersionsList() {
		return versionsList;
	}

	public void setVersionsList(List<Version> versionsList) {
		this.versionsList = versionsList;
	}
	
	public void setBranchesToCheckout(List<String> branchesToCheckout) {
		this.branchesToCheckout = branchesToCheckout;
	}
	
	public List<String> getBranchesToCheckout() {
		return branchesToCheckout;
	}

	public VersionControlSystem getVersionControlSystem(){
		return VersionControlSystem.getInstance(repositoryInfo);
	}
	
	public void setRepositoryInfo(RepositoryInfo repositoryInfo) {
		this.repositoryInfo = repositoryInfo;
	}
	
	public RepositoryInfo getRepositoryInfo() {
		return repositoryInfo;
	}
	
	public void setVersionsToExecute(List<String> versionsToExecute) {
		this.versionsToExecute = versionsToExecute;
	}
	
	public List<String> getVersionsToExecute() {
		return versionsToExecute;
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
