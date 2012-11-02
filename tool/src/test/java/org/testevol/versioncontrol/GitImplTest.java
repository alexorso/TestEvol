package org.testevol.versioncontrol;

import java.io.File;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.testevol.domain.RepositoryInfo;

public class GitImplTest {

	@Test
	@Ignore
	public void checkouTest() throws Exception {
		RepositoryInfo repositoryInfo = new RepositoryInfo();
		repositoryInfo.setUrl("git://github.com/testevol/google-gson.git");
		
		GitImpl git = new GitImpl(repositoryInfo);
		git.checkout(new File(
				"/tmp/gson"), Arrays
				.asList("v.1.1", "v.1.0"));
	}

	@Test
	public void getBranches() throws Exception {
		RepositoryInfo repositoryInfo = new RepositoryInfo();
		repositoryInfo.setUrl("git://github.com/testevol/google-gson.git");
		
		GitImpl git = new GitImpl(repositoryInfo);
		Assert.assertEquals(Arrays.asList("v.1.2.2","v.1.2.1","v.1.2","v.1.1","v.1.0"),
				git.getBranches());
	}

}
