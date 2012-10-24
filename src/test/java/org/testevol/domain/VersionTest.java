package org.testevol.domain;

import java.io.File;

import org.apache.maven.cli.MavenCli;
import org.junit.Test;

public class VersionTest {
	
	@Test
	public void checkConfiguration(){
		Version version = new Version(new File("/home/leandro/Documents/Atlanta/tmp/Versions/math/v1"));
	}
	
	//@Test
	public void test() {
		MavenCli maven = new MavenCli();
		maven.doMain(new String[]{"clean","dependency:copy-dependencies"}, "/home/leandro/Documents/Atlanta/tmp/Versions/math/v1", System.out, System.out);
		maven.doMain(new String[]{"clean"}, "/home/leandro/Documents/Atlanta/tmp/Versions/math/v1", System.out, System.out);
	
	}

}
