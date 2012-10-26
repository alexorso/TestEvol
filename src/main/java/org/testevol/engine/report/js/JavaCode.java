package org.testevol.engine.report.js;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JavaCode {
	
	private String className;
	private Map<String, String> methods;
	
	public JavaCode(String className, File file) throws IOException{
		methods = new HashMap<String, String>();
		this.className = className;
		load(file);
	}
	
	private void load(File javaFile) throws IOException{
		FileInputStream in = new FileInputStream(javaFile);

		CompilationUnit cu;
		try {
			// parse the file
			cu = JavaParser.parse(in);

			// visit and print the methods names
			MethodVisitor methodVisitor = new MethodVisitor();
			methodVisitor.visit(cu, null);		
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			in.close();
		}
	}
	
	public String getMethodCode(String methodName){
		return methods.get(methodName);
	}
	
	class MethodVisitor extends VoidVisitorAdapter {

		private StringBuffer code; 
		
		public MethodVisitor() {
			this.code = new StringBuffer();
		}
		
		@Override
		public void visit(MethodDeclaration n, Object arg) {
			JavaCode.this.methods.put(className + "."+n.getName(), n.toString());
		}
	}

}
