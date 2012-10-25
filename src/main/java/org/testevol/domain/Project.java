package org.testevol.domain;

import java.util.List;

import org.testevol.versioncontrol.VersionControlSystem;

import static org.testevol.versioncontrol.VersionControlSystem.GIT;

public class Project {

	private String name;
	private String vcs;
	private String gitUrl;
	private List<Version> versionsList;
	private List<String> versionsToExecute;
	private List<String> branchesToCheckout;
	
	public Project() {
		// TODO Auto-generated constructor stub
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

	public String getVcs() {
		return vcs;
	}

	public void setVcs(String vcs) {
		this.vcs = vcs;
	}

	public String getGitUrl() {
		return gitUrl;
	}
	
	public String getUrl() {
		if(GIT.equals(vcs)){
			return getGitUrl();	
		}
		return null;
	}

	public void setGitUrl(String gitUrl) {
		this.gitUrl = gitUrl;
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
		return VersionControlSystem.getInstance(getVcs(), getUrl());
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
		String vcs = getVcs();
		if(vcs == null || vcs.trim().isEmpty()){
			return false;
		}
		if(GIT.equals(vcs)){
			if(getGitUrl() == null || getGitUrl().trim().isEmpty()){
				return false;
			}	
		}
		if((versionsList == null || versionsList.size() < 2) &&
		   (branchesToCheckout == null || branchesToCheckout.size() < 2)){
			return false;
		}
		
		return true;
	}

}
