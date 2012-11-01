package org.testevol.versioncontrol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.testevol.domain.RepositoryInfo;
import org.testevol.domain.Version;
import org.testevol.engine.util.Utils;

public class GitImpl extends VersionControlSystem {

	private String url;
	private File workingCopy;
	private String username;
	private String password;

	public GitImpl(RepositoryInfo repositoryInfo) {
		this.url = repositoryInfo.getUrl();
		this.username = repositoryInfo.getUsername();
		this.password = repositoryInfo.getPassword();
	}
	
	public GitImpl(File workingCopy) {
		this.workingCopy = workingCopy;
	}

	@Override
	public List<String> getBranches() throws Exception {

		File dir = Utils.getTempDir();
		List<String> tags = new ArrayList<String>();
		try {
			CloneCommand cloneCommand = Git.cloneRepository().setURI(url).setDirectory(dir).setNoCheckout(true);
			
			if(username != null && !username.trim().isEmpty() &&
			   password != null){
				cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
			}
			
			
			Git git = cloneCommand.call();
			
			for(Ref ref:git.tagList().call()){
				String name = ref.getName();
				if(name.contains("/")){
					name = name.substring(name.lastIndexOf("/")+1);
				}
				tags.add(name);
			}
			Collections.reverse(tags);
		} finally {
			if (dir != null) {
				FileUtils.deleteDirectory(dir);
			}
		}
		return tags;
	}

	@Override
	public void checkout(File destinationDir, List<String> branchesToClone)
			throws Exception {
		File dir = Utils.getTempDir();
		try {
			int branchIndex=0;
			CredentialsProvider credentialsProvider = null;
			if(username != null && !username.trim().isEmpty() &&
					   password != null){
				credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);		
			}

			for(String branch:branchesToClone){
				deleteDirContents(dir);
				
				CloneCommand cloneCommand = Git.cloneRepository().setURI(url).setDirectory(dir).setNoCheckout(true);
				
				if(credentialsProvider != null){
					cloneCommand.setCredentialsProvider(credentialsProvider);
				}
				
				Git git = cloneCommand.call();
				
				String branchDirName = branch.lastIndexOf("/") != -1? branch.substring(branch.lastIndexOf("/") + 1):branch;
				File versionDir = new File(destinationDir,branchDirName);

				git.checkout().setName(branch).call();
				
				branchIndex = branchDirName.equals("master")?-1:branchIndex;
				FileUtils.copyDirectory(dir, versionDir);

				Version version = new Version(versionDir);
				version.setIndex(branchIndex);
				version.saveProperties();
				if(branchIndex != -1){
					branchIndex++;
				}
			}
		} 
		finally {
			if (dir != null) {
				FileUtils.deleteDirectory(dir);
			}
		}
	}

	private void deleteDirContents(File dir) throws IOException{
		if(dir.listFiles() == null) return;
		for(File entry:dir.listFiles()){
			if(entry.isDirectory()){
				FileUtils.deleteDirectory(entry);
			}
			else{
				entry.delete();
			}
		}
	}
	
	@Override
	public UpdateResult update() throws Exception {
		PullResult pullResult = Git.open(workingCopy).pull().call();
		return new UpdateResult(pullResult.isSuccessful(), pullResult.getMergeResult().getMergeStatus() + "\n" + pullResult.getFetchResult().getMessages());
	}

}
