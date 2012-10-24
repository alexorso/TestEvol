package org.testevol.versioncontrol;

import java.io.File;
import java.util.List;
import java.util.UUID;

public abstract class VersionControlSystem {
	
	public static String GIT = "git";

	public static VersionControlSystem getInstance(String type, String repoUrl){
		if(GIT.equals(type)){
			return new GitImpl(repoUrl);
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
		
	protected File getTempDir(){
		File dir = null;
		while(dir == null){
			dir = new File(UUID.randomUUID().toString());
			if(dir.exists()){
				dir = null;
			}			
		}
		return dir;
	}
	
	public abstract void checkout(File destinationDir, List<String> branchesToClone) throws Exception;
	public abstract UpdateResult update() throws Exception;

	
}
