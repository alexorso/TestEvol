package org.testevol.versioncontrol;

import java.io.File;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

public class GitImplTest {

	//@Test
	public void checkouTest() throws Exception {
		GitImpl git = new GitImpl("git://github.com/leandroshp/math.git");
		git.checkout(new File(
				"/home/leandro/Documents/Atlanta/tmp/Versions/math"), Arrays
				.asList("refs/heads/v1", "refs/heads/master"));
	}

	@Test
	public void getBranches() throws Exception {
		GitImpl git = new GitImpl("git://github.com/testevol/google-gson.git");
		Assert.assertEquals(Arrays.asList("refs/heads/master",
				"refs/heads/v.1.1.1", "refs/heads/v.1.1", "refs/heads/v.1.0"),
				git.getBranches());
	}

}
