package org.testevol.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.testevol.engine.diff.CFGNodeCorrespondence;
import org.testevol.engine.diff.ClassCorrespondence;
import org.testevol.engine.diff.MethodCorrespondence;
import org.testevol.engine.diff.Pair;
import org.testevol.engine.util.MethodInfo;
import org.testevol.engine.util.WalaClassLoader;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.TypeName;

public class JavaDifferencer {
	
	protected WalaClassLoader classLoader1;
	protected WalaClassLoader classLoader2;
	private Collection<IClass> app1Classes;
	private Collection<IClass> app2Classes;
	private Collection<IMethod> app1Methods;
	private Collection<IMethod> app2Methods;
	private ClassCorrespondence classCorr;
	private Collection<IClass> addedClasses;
	private Collection<IClass> deletedClasses;
	private Collection<Pair<IClass>> modifiedClasses;
	private Collection<Pair<IClass>> unmodifiedClasses;
	private Collection<IMethod> addedMethods;
	private Collection<IMethod> deletedMethods;
	private Collection<Pair<IMethod>> modifiedMethods;
	private HashMap<IMethod, MethodInfo> mInfoMap;

	public JavaDifferencer( String configFile1, String configFile2, String exclFile ) {
		classLoader1 = new WalaClassLoader( configFile1, exclFile );
		classLoader2 = new WalaClassLoader( configFile2, exclFile );
		app1Classes = classLoader1.getApplicationClasses();
		app2Classes = classLoader2.getApplicationClasses();
		app1Methods = null;
		app2Methods = null;
		addedClasses = new ArrayList<IClass>();
		deletedClasses = new ArrayList<IClass>();
		modifiedClasses = new ArrayList<Pair<IClass>>();
		unmodifiedClasses = new ArrayList<Pair<IClass>>();
		addedMethods = new ArrayList<IMethod>();
		deletedMethods = new ArrayList<IMethod>();
		modifiedMethods = new ArrayList<Pair<IMethod>>();
		mInfoMap = new HashMap<IMethod, MethodInfo>();
		load();
	}
	
	private void load() {
	
		classCorr = getClassCorrespondence();
		addedClasses.addAll(  classCorr.getAddedClasses() );
		for ( IClass cls : classCorr.getAddedClasses() ) {
			addedMethods.addAll( cls.getDeclaredMethods() );
			for ( IMethod method : cls.getDeclaredMethods() ) {
				addToMethodMap( method );
			}
		}
		deletedClasses.addAll( classCorr.getDeletedClasses() );
		for ( IClass cls : classCorr.getDeletedClasses() ) {
			for ( IMethod method : cls.getDeclaredMethods() ) {
				addToMethodMap( method );
			}
		}
		Collection<Pair<IClass>> matchedClasses = classCorr.getMatchedClassPairs();
		for ( Pair<IClass> matchedCls : matchedClasses ) {
			boolean isClsMod = false;
			MethodCorrespondence methodCorr = new MethodCorrespondence( matchedCls.getFirst(), matchedCls.getSecond() );
			for ( IMethod method : methodCorr.getAddedMethods() ) {
				addedMethods.add( method );
				addToMethodMap( method );
				isClsMod = true;
			}
			for ( IMethod method : methodCorr.getDeletedMethods() ) {
				deletedMethods.add( method );
				addToMethodMap( method );
				isClsMod = true;
			}
			Collection<Pair<IMethod>> matchedMethods = methodCorr.getMatchedMethodPairs();
			for ( Pair<IMethod> matchedMeth : matchedMethods ) {
				IMethod method1 = matchedMeth.getFirst();
				IMethod method2 = matchedMeth.getSecond();
				CFGNodeCorrespondence nodeCorr = new CFGNodeCorrespondence( method1, method2 );
				if ( nodeCorr.isMethodModified() ) {
					modifiedMethods.add( matchedMeth );
					isClsMod = true;
				}
				addToMethodMap( nodeCorr.getMethodOneInfo() );
				addToMethodMap( nodeCorr.getMethodTwoInfo() );
			}
			if ( isClsMod ) {
				modifiedClasses.add( matchedCls );
			}
			else {
				unmodifiedClasses.add( matchedCls );
			}
		}
	}
	
	protected ClassCorrespondence getClassCorrespondence() {
		return new ClassCorrespondence( classLoader1.getApplicationClasses(), classLoader2.getApplicationClasses() );
	}

	private void addToMethodMap( IMethod method ) {
		if ( !mInfoMap.containsKey( method ) ) {
			mInfoMap.put( method, new MethodInfo( method ) );
		}
	}
	
	private void addToMethodMap( MethodInfo mInfo ) {
		if ( !mInfoMap.containsKey( mInfo.getMethod() ) ) {
			mInfoMap.put( mInfo.getMethod(), mInfo );
		}
	}
	
	public Collection<IClass> getAddedClasses() {
		return addedClasses;
	}

	public Collection<IClass> getDeletedClasses() {
		return deletedClasses;
	}

	public Collection<Pair<IClass>> getModifiedClasses() {
		return modifiedClasses;
	}

	public Collection<Pair<IClass>> getUnmodifiedClasses() {
		return unmodifiedClasses;
	}

	public Collection<IMethod> getAddedMethods() {
		return addedMethods;
	}
	
	public Collection<IMethod> getAllAddedMethods() throws ClassHierarchyException {
		Set<IMethod> addedMethods = new HashSet<IMethod>();
		addedMethods.addAll(this.addedMethods);
        for(IClass addedClazz:getAddedClasses()){
        	addedMethods.addAll(addedClazz.getAllMethods());
        }
		
		return addedMethods;
	}

	public Collection<IMethod> getDeletedMethods() {
		return deletedMethods;
	}

	public Collection<Pair<IMethod>> getModifiedMethods() {
		return modifiedMethods;
	}
	
	public int getMethodStartLineNumber( IMethod method ) {
		try {
			if ( mInfoMap.containsKey( method ) ) {
				return mInfoMap.get( method ).getStartLineNumber();
			}
			return 0;
		}
		catch ( InvalidClassFileException icfe ) {
			return 0;
		}
	}

	public int getMethodStartLineNumber( String methodSig ) {
		try {
			for ( IMethod method : mInfoMap.keySet() ) {
				if ( method.getSignature().equals( methodSig ) ) {
					return mInfoMap.get( method ).getStartLineNumber();
				}
			}
			return 0;
		}
		catch ( InvalidClassFileException icfe ) {
			return 0;
		}
	}
	
	/**
	 * This method returns all the modified methods represented by
	 * its first line number and its declaring class. So, for
	 * example if a class had a modified method called m1, and
	 * its first line is line 10 the set will contain the following element
	 * "class_name,10" -> name of the class ","(comma) method's first line number
	 * 
	 * 
	 * @return
	 */
	public Set<String> getSetOfLinesRelatedToModifiedMethods() {
		Set<String> lineNumbersOfModifiedMethods = new HashSet<String>();
		for(Pair<IMethod> pair:getModifiedMethods()){
			int getLineNumberOfFirstLineOfMethod = getMethodStartLineNumber(pair.getSecond());				
			lineNumbersOfModifiedMethods.add(getClassName(pair.getSecond().getDeclaringClass())+","+getLineNumberOfFirstLineOfMethod);
		}
		return lineNumbersOfModifiedMethods;
	}
	
	public Set<String> getSetOfLinesRelatedToAddedMethods() {
		Set<String> lineNumbersOfAddedMethods = new HashSet<String>();
		for(IMethod method:getAddedMethods()){
			int getLineNumberOfFirstLineOfMethod=0;
			try {
				getLineNumberOfFirstLineOfMethod = method.getLineNumber(1);// getMethodStartLineNumber(method);
			} catch (Exception e) {}				
			lineNumbersOfAddedMethods.add(getClassName(method.getDeclaringClass())+","+getLineNumberOfFirstLineOfMethod);
		}
		return lineNumbersOfAddedMethods;
	}

	private String getClassName(IClass clazz){
		TypeName typeName = clazz.getName();
		return typeName.getPackage().toString().replaceAll("/", ".")+ "." + typeName.getClassName();
	}
	
	public int getMethodEndLineNumber( IMethod method ) {
		try {
			if ( mInfoMap.containsKey( method ) ) {
				return mInfoMap.get( method ).getEndLineNumber();
			}
			return 0;
		}
		catch ( InvalidClassFileException icfe ) {
			return 0;
		}
	}

	public int getMethodEndLineNumber( String methodSig ) {
		try {
			for ( IMethod method : mInfoMap.keySet() ) {
				if ( method.getSignature().equals( methodSig ) ) {
					return mInfoMap.get( method ).getEndLineNumber();
				}
			}
			return 0;
		}
		catch ( InvalidClassFileException icfe ) {
			return 0;
		}
	}
	
	public Collection<IClass> getApp1Classes() {
		return app1Classes;
	}

	public Collection<IClass> getApp2Classes() {
		return app2Classes;
	}
	
	public Collection<IMethod> getApp1Methods() {
		if ( app1Methods == null ) {
			app1Methods = new ArrayList<IMethod>();
			for ( IClass cls : app1Classes ) {
				app1Methods.addAll( cls.getDeclaredMethods() );
			}
		}
		return app1Methods;
	}
	
	public Collection<IMethod> getApp2Methods() {
		if ( app2Methods == null ) {
			app2Methods = new ArrayList<IMethod>();
			for ( IClass cls : app2Classes ) {
				app2Methods.addAll( cls.getDeclaredMethods() );
			}
		}
		return app2Methods;
	}
	
}
