package org.testevol.engine.driver;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.pmd.cpd.CPD;
import net.sourceforge.pmd.cpd.JavaLanguage;
import net.sourceforge.pmd.cpd.Match;

import org.testevol.engine.domain.CloneAnalyser;


public class CopyAndPasteDetectorDriver{
	private CPD cpd;
	private File file1;
	
	private List<String> clones;
	
	public CopyAndPasteDetectorDriver(	int minimumTileSize,
										File file1, 
										File file2) throws IOException {
		cpd = new CPD(minimumTileSize, new JavaLanguage());
		cpd.add(file1);
		cpd.add(file2);
		this.file1 = file1;
	}

	public void run() throws ParseException, IOException {
		cpd.go();
		Iterator<Match> matches = cpd.getMatches();
		
		List<Region> regionsFile1 = new ArrayList<Region>();
		List<Region> regionsFile2 = new ArrayList<Region>();
		
		while(matches.hasNext()){
			Match match = matches.next();
			
			String fileContainer1 = match.getFirstMark().getTokenSrcID();
			String fileContainer2 = match.getSecondMark().getTokenSrcID();
			
			//Avoid comparing files from the same version
			if(!fileContainer1.equals(fileContainer2)){
				int firstLine = match.getFirstMark().getBeginLine();
				int endLine = firstLine + match.getLineCount() - 1;
				regionsFile1.add(new Region(firstLine, endLine));
				
				firstLine = match.getSecondMark().getBeginLine();
				endLine = firstLine + match.getLineCount() - 1;
				regionsFile2.add(new Region(firstLine, endLine));				
			}
		}
		
		clones = new ArrayList<String>();
		List<MethodDeclaration> methodDeclarationsFile1 = getMethodDeclarations(file1);
		for(MethodDeclaration methodDeclaration:methodDeclarationsFile1) {
			 Region methodRegion = new Region(methodDeclaration.getBeginLine(), methodDeclaration.getEndLine());
			 for(Region region:regionsFile1){
				 if(region.contains(methodRegion)){
					 String methodName = methodDeclaration.getName();
					 methodName = removeSeparator(methodName);
					 clones.add(methodName);
				 }
			 }
		}
	}
	
	private String removeSeparator(String methodName){
		return methodName.replaceAll(""+CloneAnalyser.SEPARATOR+"", ".");
	}
	
	public List<String> getClonesInFile1() {
		return clones;
	}

	public List<MethodDeclaration> getMethodDeclarations(File file) throws ParseException, IOException{
			CompilationUnit cu = JavaParser.parse(file);
			MethodVisitor methodVisitor = new MethodVisitor();
			methodVisitor.visit(cu, null);
			return methodVisitor.getMethods();
	}

	
	class Region{
		
		private int begin;
		private int end;
		
		Region(int begin, int end){
			this.begin = begin;
			this.end = end;
		}
		
		public boolean contains(Region region){
			return this.begin <= region.begin &&
				   this.end >= region.end;
		}
	}	
	
	private static class MethodVisitor extends VoidVisitorAdapter {

		List<MethodDeclaration> methods;
		
		public MethodVisitor() {
			methods= new ArrayList<MethodDeclaration>();
		}
		
		public List<MethodDeclaration> getMethods() {
			return methods;
		}
		
		@Override
		public void visit(MethodDeclaration n, Object arg) {
			methods.add(n);
		}
	}
}
