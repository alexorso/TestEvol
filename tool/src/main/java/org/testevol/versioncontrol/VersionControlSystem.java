package org.testevol.versioncontrol;

import java.io.File;
import java.util.List;

import org.testevol.domain.RepositoryInfo;

public abstract class VersionControlSystem {
	
	public static String GIT = "git";
	
	public static VersionControlSystem getInstance(RepositoryInfo repositoryInfo){
		if(GIT.equals(repositoryInfo.getType())){
			return new GitImpl(repositoryInfo);
		}
		
		return null;
	}
	
	public static VersionControlSystem getInstance(String type, File workingCopy){
		if(GIT.equals(type)){
			return new GitImpl(workingCopy);
		}
		
		return null;
	}
	
	public abstract List<String> getBranches() throws Exception;	
	public abstract void checkout(File destinationDir, List<String> branchesToClone) throws Exception;
	public abstract UpdateResult update() throws Exception;

	
}
