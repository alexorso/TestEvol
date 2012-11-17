package org.testevol.engine.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.cli.MavenCli;

public class TestevolMavenCli {

	public static String exec(String[] goals, File workingDir) {
		MavenCli maven = new MavenCli();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		maven.doMain(goals, workingDir.getAbsolutePath(), ps, ps);
		return baos.toString();
	}

	public static String execMavenProcess(String[] goals, File workingDir) throws Exception {
		List<String> args = new ArrayList<String>();
		args.add("mvn");
		for (String goal : goals) {
			args.add(goal);
		}

		ProcessBuilder processBuilder = new ProcessBuilder(args).directory(workingDir);
		processBuilder.redirectErrorStream();
		Process process = processBuilder.start();

		String line;
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((line = br.readLine()) != null) {
			//System.out.println(line);
			IOUtils.write(line+"\n", baos);
		}
		
		process.waitFor();
		//System.out.println(baos.toString());
		return baos.toString();
	}

}
