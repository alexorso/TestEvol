package org.testevol.domain;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import junit.framework.Assert;

import org.apache.maven.cli.MavenCli;
import org.junit.Test;

public class VersionTest {
	
	@Test
	public void checkConfiguration() throws Exception{
		Version version = new Version(new File("/home/leandro/Documents/Atlanta/tmp/Versions/math/v1"));
		version.setUp(null);
		Assert.assertEquals("1.6", version.getJavaVersion());
	}
	
	//@Test
	public void test() {
		MavenCli maven = new MavenCli();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		maven.doMain(new String[]{"clean","dependency:copy-dependencies"}, "/home/leandro/Documents/Atlanta/tmp/Versions/math/v1", ps, ps);
		maven.doMain(new String[]{"clean"}, "/home/leandro/Documents/Atlanta/tmp/Versions/math/v1", System.out, System.out);
		
		System.out.println(baos);
		System.out.println(baos.toString().contains("BUILD SUCCESS"));
		
	}

}
