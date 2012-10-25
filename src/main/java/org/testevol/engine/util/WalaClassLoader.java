package org.testevol.engine.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeCTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.annotations.Annotation;

public class WalaClassLoader {

	private Collection<IClass> appClasses;
	private Collection<IClass> extClasses;
	private Collection<IClass> testClasses;
	private ClassHierarchy cha;
	private AnalysisScope scope;
	
	private static final String JUnitTestCase = "Ljunit/framework/TestCase";
	private static final TypeName testAnnotation = TypeName.findOrCreateClassName("org/junit", "Test");
	
	public WalaClassLoader( String walaScopeFile, String walaExclFile ) {
		load( walaScopeFile, walaExclFile );
	}
	
	private void load( String walaScopeFile, String walaExclFile ) {
		try {
			scope = makeJ2SEAnalysisScope( walaScopeFile, walaExclFile );
			cha = ClassHierarchy.make( scope );
			// build list of application and external classes
			appClasses = new ArrayList<IClass>();
			extClasses = new ArrayList<IClass>();
			testClasses = new ArrayList<IClass>();
			loadAllClasses( scope );
		}
		catch ( IOException ioe ) {
			System.err.println( "Error creating analysis scope ..." );
			ioe.printStackTrace();
			throw new RuntimeException();					
		}
		catch ( ClassHierarchyException che ) {
			System.err.println( "Error creating class hierarchy ..." );
			che.printStackTrace();
			throw new RuntimeException();								
		}
	}

	private void loadAllClasses( AnalysisScope scope ) {
		for ( IClass cls : cha ) {
			if ( isApplicationClass( scope, cls ) ) {
				try {
					if ( isTestClass( cls ) ) {
						testClasses.add( cls );
					}
					else {
						appClasses.add( cls );
					}
				}
				catch ( ClassHierarchyException che ) {
					System.err.println( "Error loading: "+cls.getName().toString() );
					che.printStackTrace();
				}
			}
			else {
				extClasses.add( cls );
			}
		}			
	}
	
	private AnalysisScope makeJ2SEAnalysisScope( String scopeFile, String exclFile ) throws IOException {
		return com.ibm.wala.core.tests.callGraph.CallGraphTestUtil.makeJ2SEAnalysisScope( scopeFile, exclFile );
	}
	
	private boolean isApplicationClass( final AnalysisScope scope, final IClass clazz ) {
	    return scope.getApplicationLoader().equals( clazz.getClassLoader().getReference() );
	}
	
	private boolean isTestClass( IClass clazz ) throws ClassHierarchyException {
		IClass superClass = clazz.getSuperclass();
		while ( superClass != null ) {
			if ( superClass.getName().toString().equals( JUnitTestCase ) ) {
				return true;
			}
			superClass = superClass.getSuperclass();
		}
		//does not have TestCase as a SuperClass
		for(IMethod method:clazz.getAllMethods()){
			if(isTestMethod(method)){
				return true;
			}
		}
		
		return false;
	}
	
	
	
	private boolean isTestMethod(IMethod method) {
		try {
			for (Annotation a : ((ShrikeCTMethod) method).getAnnotations(false)) {
				if (a.getType().getName().equals(testAnnotation)) {
					return true;
				}
			}
		} catch (InvalidClassFileException e) {
			e.printStackTrace();
		}
		return false;
	}

	public Collection<IClass> getApplicationClasses() {
		return appClasses;
	}
	
	public Collection<IClass> getTestClasses() {
		return testClasses;
	}
	
	public Collection<IClass> getExternalClasses() {
		return extClasses;
	}
	
	public ClassHierarchy getClassHierarchy() {
		return cha;
	}
	
	public AnalysisScope getAnalysisScope() {
		return scope;
	}

}
