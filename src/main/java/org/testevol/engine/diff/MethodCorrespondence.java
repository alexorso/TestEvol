package org.testevol.engine.diff;

import java.util.Collection;
import java.util.LinkedList;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;

public class MethodCorrespondence {
	
	private Collection<Pair<IMethod>> allPairs;
	private Collection<Pair<IMethod>> matchedPairs;
	private Collection<IMethod> firstVersionMethods;
	private Collection<IMethod> secondVersionMethods;
	private Collection<IMethod> deletedMethods;
	private Collection<IMethod> addedMethods;
	private IClass class1;
	private IClass class2;
	
	public MethodCorrespondence( IClass class1, IClass class2 ) {
		this.class1 = class1;
		this.class2 = class2;
		firstVersionMethods = class1.getDeclaredMethods();
		secondVersionMethods = class2.getDeclaredMethods();
		allPairs = new LinkedList<Pair<IMethod>>();
		matchedPairs = new LinkedList<Pair<IMethod>>();
		deletedMethods = new LinkedList<IMethod>();
		addedMethods = new LinkedList<IMethod>();
		load();
	}
	
	private void load() {
		Collection<IMethod> unmatchedV1 = new LinkedList<IMethod>();
		unmatchedV1.addAll( firstVersionMethods );
		Collection<IMethod> unmatchedV2 = new LinkedList<IMethod>();
		unmatchedV2.addAll( secondVersionMethods );
		LinkedList<IMethod> matchedV1 = new LinkedList<IMethod>();
		for ( IMethod method1 : unmatchedV1 ) {
			for ( IMethod method2 : unmatchedV2 ) {
				if ( method1.getSignature().equals( method2.getSignature() ) ) {
					matchedPairs.add( new Pair<IMethod>( method1, method2 ) );
					matchedV1.add( method1 );
					unmatchedV2.remove( method2 );
					break;
				}
			}
		}
		unmatchedV1.removeAll( matchedV1 );
		deletedMethods.addAll( unmatchedV1 );
		addedMethods.addAll( unmatchedV2 );
		allPairs.addAll( matchedPairs );
		for ( IMethod cls : deletedMethods ) {
			allPairs.add( new Pair<IMethod>( cls, null ) );
		}
		for ( IMethod cls : addedMethods ) {
			allPairs.add( new Pair<IMethod>( null, cls ) );
		}		
	}
	
	public Collection<IMethod> getVersionOneMethods() {
		return firstVersionMethods;
	}
	
	public Collection<IMethod> getVersionTwoMethods() {
		return secondVersionMethods;
	}
	
	public Collection<Pair<IMethod>> getAllMethodPairs() {
		return allPairs;
	}
	
	public Collection<Pair<IMethod>> getMatchedMethodPairs() {
		return matchedPairs;
	}
	
	public Collection<IMethod> getDeletedMethods() {
		return deletedMethods;
	}
	
	public Collection<IMethod> getAddedMethods() {
		return addedMethods;
	}
	
	public IClass getFirstClass() {
		return class1;
	}
	
	public IClass getSecondClass() {
		return class2;
	}

}
