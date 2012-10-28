package org.testevol.versioncontrol;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;


public class GitImplTest {

	@Test
	public void checkouTest() throws Exception {
		GitImpl git = new GitImpl("git://github.com/leandroshp/math.git");
		git.checkout(new File("/home/leandro/Documents/Atlanta/tmp/Versions/math"), Arrays.asList("refs/heads/v1","refs/heads/master"));
	}
	
}
