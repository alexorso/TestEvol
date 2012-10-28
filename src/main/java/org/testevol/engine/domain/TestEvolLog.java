package org.testevol.engine.domain;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.FileUtils;

public class TestEvolLog {

	private File log;
	
	public TestEvolLog(File log) throws IOException {
		if(!log.exists()){
			log.createNewFile();
		}
		this.log = log;
	}

	public void log(String logMessage) throws IOException {
		FileUtils.writeStringToFile(log, logMessage+"<br/>", true);		
	}
	
	public void logStrong(String logMessage) throws IOException {
		log("<strong>"+logMessage+"</strong>");		
	}	
	public void logError(String logMessage) throws IOException {
		log("<span class='log-error'>"+logMessage+"</span>");		
	}

	public void addLine() throws IOException {
		log("");		
	}
	
	public void addLineSeparator(int numberOfSeparators) throws IOException {
		StringWriter writer = new StringWriter();
		for (int i = 0; i < numberOfSeparators; i++) {
			writer.append("&ndash;&nbsp;");
		}
		log(writer.toString());
	}
}
