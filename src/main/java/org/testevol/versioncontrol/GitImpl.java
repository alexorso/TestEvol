package org.testevol.versioncontrol;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.Ref;
import org.testevol.domain.Version;
import org.testevol.engine.util.Utils;

public class GitImpl extends VersionControlSystem {

	private String url;
	private File workingCopy;

	public GitImpl(String url) {
		this.url = url;
	}
	
	public GitImpl(File workingCopy) {
		this.workingCopy = workingCopy;
	}

	@Override
	public List<String> getBranches() throws Exception {

		File dir = Utils.getTempDir();
		List<String> branches = new ArrayList<String>();

		try {
			Git git = Git.cloneRepository().setURI(url).setDirectory(dir)
					.setNoCheckout(true).call();

			for (Ref ref : git.lsRemote().setHeads(true).call()) {
				branches.add(ref.getName());
			}
		} finally {
			if (dir != null) {
				FileUtils.deleteDirectory(dir);
			}
		}
		return branches;
	}

	@Override
	public void checkout(File destinationDir, List<String> branchesToClone)
			throws Exception {
		if(!destinationDir.exists()){
			destinationDir.mkdirs();
		}
		File dir = Utils.getTempDir();
		try {
			int branchIndex=0;
			for(String branch:branchesToClone){
				String branchDirName = branch.lastIndexOf("/") != -1? branch.substring(branch.lastIndexOf("/") + 1):branch;
				File branchDdir = new File(dir,branchDirName);
				Git.cloneRepository().setURI(url).setDirectory(branchDdir)
						.setBranch(branch).call();
				
				branchIndex = branchDirName.equals("master")?-1:branchIndex;
				
				File versionDir = new File(destinationDir,branchDirName);
				branchDdir.renameTo(versionDir);
				Version version = new Version(versionDir);
				version.setIndex(branchIndex);
				version.saveProperties();
				if(branchIndex != -1){
					branchIndex++;
				}
			}
			
		} finally {
			if (dir != null) {
				FileUtils.deleteDirectory(dir);
			}
		}
	}

	@Override
	public UpdateResult update() throws Exception {
		PullResult pullResult = Git.open(workingCopy).pull().call();
		return new UpdateResult(pullResult.isSuccessful(), pullResult.getMergeResult().getMergeStatus() + "\n" + pullResult.getFetchResult().getMessages());
	}

}
