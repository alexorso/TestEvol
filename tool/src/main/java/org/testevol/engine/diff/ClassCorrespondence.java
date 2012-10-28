package org.testevol.engine.diff;

import java.util.Collection;
import java.util.LinkedList;

import com.ibm.wala.classLoader.IClass;

public class ClassCorrespondence {
	
	private Collection<Pair<IClass>> allPairs;
	private Collection<Pair<IClass>> matchedPairs;
	private Collection<IClass> firstVersionClasses;
	private Collection<IClass> secondVersionClasses;
	private Collection<IClass> deletedClasses;
	private Collection<IClass> addedClasses;
	
	public ClassCorrespondence( Collection<IClass> first, Collection<IClass> second ) {
		firstVersionClasses = new LinkedList<IClass>();
		firstVersionClasses.addAll( first );
		secondVersionClasses = new LinkedList<IClass>();
		secondVersionClasses.addAll( second );
		allPairs = new LinkedList<Pair<IClass>>();
		matchedPairs = new LinkedList<Pair<IClass>>();
		deletedClasses = new LinkedList<IClass>();
		addedClasses = new LinkedList<IClass>();
		load();
	}
	
	private void load() {
		Collection<IClass> unmatchedV1 = new LinkedList<IClass>();
		unmatchedV1.addAll( firstVersionClasses );
		Collection<IClass> unmatchedV2 = new LinkedList<IClass>();
		unmatchedV2.addAll( secondVersionClasses );
		LinkedList<IClass> matchedV1 = new LinkedList<IClass>();
		for ( IClass cls1 : unmatchedV1 ) {
			for ( IClass cls2 : unmatchedV2 ) {
				if ( cls1.getName().equals( cls2.getName() ) ) {
					matchedPairs.add( new Pair<IClass>( cls1, cls2 ) );
					matchedV1.add( cls1 );
					unmatchedV2.remove( cls2 );
					break;
				}
			}
		}
		unmatchedV1.removeAll( matchedV1 );
		deletedClasses.addAll( unmatchedV1 );
		addedClasses.addAll( unmatchedV2 );
		allPairs.addAll( matchedPairs );
		for ( IClass cls : deletedClasses ) {
			allPairs.add( new Pair<IClass>( cls, null ) );
		}
		for ( IClass cls : addedClasses ) {
			allPairs.add( new Pair<IClass>( null, cls ) );
		}		
	}
	
	public Collection<IClass> getVersionOneClasses() {
		return firstVersionClasses;
	}
	
	public Collection<IClass> getVersionTwoClasses() {
		return secondVersionClasses;
	}
	
	public Collection<Pair<IClass>> getAllClassPairs() {
		return allPairs;
	}
	
	public Collection<Pair<IClass>> getMatchedClassPairs() {
		return matchedPairs;
	}
	
	public Collection<IClass> getDeletedClasses() {
		return deletedClasses;
	}
	
	public Collection<IClass> getAddedClasses() {
		return addedClasses;
	}

}
