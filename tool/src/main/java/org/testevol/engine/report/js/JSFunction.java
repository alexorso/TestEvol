package org.testevol.engine.report.js;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class JSFunction {
	private String name;
	private List<String> statements;
	
	public JSFunction(String name) {
		this.name = name;
		statements = new ArrayList<String>();

	}
	
	public String toString(){
		return createJSFunction();
	}
	
	public JSFunction addStatement(String statement){
		statements.add(statement);
		return this;
	}
	
	private String createJSFunction() {
		StringWriter jsFunction = new StringWriter();
		jsFunction.append("function " + name + "(){");
		for(String statement:statements){
			jsFunction.append(statement);
			jsFunction.append(statement.endsWith(";")?"":";");
		}
		jsFunction.append("}");
		return jsFunction.toString();
	}


}
