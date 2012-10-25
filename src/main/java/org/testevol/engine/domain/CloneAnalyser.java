package org.testevol.engine.domain;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testevol.domain.Version;

public class CloneAnalyser {
	
	public static String SEPARATOR = "_____";
	
	private String name;
	private Version version;
	private List<String> classes;
	private Map<String,Set<String>> tests;
	
	public CloneAnalyser(String name, Version version){
		this.name = name;
		classes = new ArrayList<String>();
		tests = new HashMap<String, Set<String>>();
		this.version = version;
	}

	public void addTests(Set<String> tests){
		for(String test:tests){
			String classOfTheTest = test.substring(0,test.lastIndexOf("."));
			if(!classes.contains(classOfTheTest)){
				classes.add(classOfTheTest);
			}
			Set<String> testsOfClass = this.tests.get(classOfTheTest);
			if(testsOfClass == null){
				testsOfClass = new HashSet<String>();
				this.tests.put(classOfTheTest, testsOfClass);
			}
			testsOfClass.add(test.substring(test.lastIndexOf(".") + 1,test.length()));
		}
	}
	
	public List<String> getClasses() {
		return classes;
	}
	
	public File createFile() throws ParseException, IOException{
		
		File file = new File(version.getBinTestDir(),name+".java");
		if(file.exists()){
			file.delete();
		}
		FileOutputStream fos = new FileOutputStream(file);
		for(String clazz:classes){
				FileInputStream in = new FileInputStream(new File(version.getTestsSourceDir(),clazz.replaceAll("[.]", File.separator)+".java"));

				CompilationUnit cu;
				try {
					// parse the file
					cu = JavaParser.parse(in);
					// visit and print the methods names
					MethodVisitor methodVisitor = new MethodVisitor(clazz,tests.get(clazz));
					methodVisitor.visit(cu, null);				
					fos.write(methodVisitor.getCode().getBytes());

				} 
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					in.close();
				}
				
		}
		
		fos.close();
		return new File(version.getBinTestDir(),name+".java");
	}
	
	private static class MethodVisitor extends VoidVisitorAdapter {

		private String name;
		private StringBuffer code;
		private Set<String> relevantMethods; 
		
		public MethodVisitor(String name, Set<String> relevantMethods) {
			this.name = name.replaceAll("[.]", SEPARATOR);
			this.relevantMethods = relevantMethods;
			this.code = new StringBuffer();
		}
		
		@Override
		public void visit(MethodDeclaration n, Object arg) {
			// here you can access the attributes of the method.
			// this method will be called for all methods in this
			// CompilationUnit, including inner class methods
			if(relevantMethods.contains(n.getName())){
				code.append(System.getProperty("line.separator"));
				code.append("public void "+name+SEPARATOR+n.getName()+"()");
				code.append(n.getBody().toString());
			}
		}
		
		public String getCode(){
			StringBuffer auxCode = new StringBuffer();
			auxCode.append("public class "+name+"{");
			auxCode.append(code.toString());
			auxCode.append(System.getProperty("line.separator"));
			auxCode.append("}");
			return auxCode.toString();
		}
	}

	
}
