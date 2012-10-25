package org.testevol.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.testevol.engine.diff.Pair;
import org.testevol.engine.util.ClassificationConstants;
import org.testevol.engine.util.MethodInfo;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.MethodReference;

public class TestMethodDifferencer {
	
	private MethodInfo methodInfo1;
	private MethodInfo methodInfo2;
	private ArrayList<AssertInstruction> addedAsserts;
	private ArrayList<AssertInstruction> deletedAsserts;
	private ArrayList<Pair<AssertInstruction>> modifiedAsserts;
	private ArrayList<Pair<AssertInstruction>> modifiedAssertExpVals;
	private ArrayList<SSAInvokeInstruction> addedCalls;
	private ArrayList<SSAInvokeInstruction> deletedCalls;
	private ArrayList<Pair<SSAInvokeInstruction>> modifiedCalls;
	private ArrayList<Pair<SSAInvokeInstruction>> paramAddedCalls;
	private ArrayList<Pair<SSAInvokeInstruction>> paramDeletedCalls;
	
	private int changeType;
	
	public TestMethodDifferencer( IMethod method1, IMethod method2 ) {
		this.methodInfo1 = new MethodInfo( method1 );
		this.methodInfo2 = new MethodInfo( method2 );
		addedAsserts = new ArrayList<AssertInstruction>();
		deletedAsserts = new ArrayList<AssertInstruction>();
		modifiedAsserts = new ArrayList<Pair<AssertInstruction>>();
		modifiedAssertExpVals = new ArrayList<Pair<AssertInstruction>>();
		addedCalls = new ArrayList<SSAInvokeInstruction>();
		deletedCalls = new ArrayList<SSAInvokeInstruction>();
		modifiedCalls = new ArrayList<Pair<SSAInvokeInstruction>>();
		paramAddedCalls = new ArrayList<Pair<SSAInvokeInstruction>>();
		paramDeletedCalls = new ArrayList<Pair<SSAInvokeInstruction>>();

		// check whether asserts have been modified
		checkAssertModifications();
		
		// check method modifications
		checkMethodModifications();
	}

	private void checkAssertModifications() {
		Collection<SSAInvokeInstruction> assertList1 = methodInfo1.getAssertCalls();
		Collection<SSAInvokeInstruction> assertList2 = methodInfo2.getAssertCalls();

		// check for exact matches
		ArrayList<SSAInvokeInstruction> exactMatchList1 = new ArrayList<SSAInvokeInstruction>();
		for ( SSAInvokeInstruction assert1 : assertList1 ) {
			SSAInvokeInstruction matchingInst2 = null;
			for ( SSAInvokeInstruction assert2 : assertList2 ) {
				if ( isExactInvokeMatch( assert1, assert2 ) ) {
					exactMatchList1.add( assert1 );
					matchingInst2 = assert2;
					break;
				}
			}
			if ( matchingInst2 != null ) {
				assertList2.remove( matchingInst2 );
			}
		}
		assertList1.removeAll( exactMatchList1 );
		
		// check for partial matches for the remaining assertions
		ArrayList<SSAInvokeInstruction> partialMatchList1 = new ArrayList<SSAInvokeInstruction>();
		for ( SSAInvokeInstruction assert1 : assertList1 ) {
			SSAInvokeInstruction matchingInst2 = null;
			for ( SSAInvokeInstruction assert2 : assertList2 ) {
				int partialMatchType = getPartialAssertMatchType( assert1, assert2 );
				if ( partialMatchType == 0 ) {
					continue;
				}
				partialMatchList1.add( assert1 );
				matchingInst2 = assert2;
				AssertInstruction aInst1 = new AssertInstruction( assert1 );
				AssertInstruction aInst2 = new AssertInstruction( matchingInst2 );
				if ( (partialMatchType & ClassificationConstants.TESTMOD_ASSERT_EXPVAL_MOD) != 0 ) {
					modifiedAssertExpVals.add( new Pair<AssertInstruction>( aInst1, aInst2 ) );
				}
				else {
					modifiedAsserts.add( new Pair<AssertInstruction>( aInst1, aInst2 ) );
				}
				break;
			}
			if ( matchingInst2 != null ) {
				assertList2.remove( matchingInst2 );
			}
		}
		assertList1.removeAll( partialMatchList1 );
		
		// remaining asserts in list 1 are deleted asserts
		for ( SSAInvokeInstruction inst : assertList1 ) {
			deletedAsserts.add( new AssertInstruction( inst ) );
		}
		// remaining asserts in list 2 are added asserts
		for ( SSAInvokeInstruction inst : assertList2 ) {
			addedAsserts.add( new AssertInstruction( inst ) );
		}
		if ( modifiedAsserts.size() > 0 ) {
			changeType = changeType | ClassificationConstants.TESTMOD_ASSERT_MOD;
		}
		if ( modifiedAssertExpVals.size() > 0 ) {
			changeType = changeType | ClassificationConstants.TESTMOD_ASSERT_EXPVAL_MOD;
		}
		if ( deletedAsserts.size() > 0 ) {
			changeType = changeType | ClassificationConstants.TESTMOD_ASSERT_DEL;
		}
		if ( addedAsserts.size() > 0 ) {
			changeType = changeType | ClassificationConstants.TESTMOD_ASSERT_ADD;
		}
	}
	
	private boolean isExactInvokeMatch( SSAInvokeInstruction invInst1, SSAInvokeInstruction invInst2 ) {
		if ( !invInst1.getDeclaredTarget().getSignature().equals( invInst2.getDeclaredTarget().getSignature() ) ) {
			return false;
		}
		// return false if different number uses
		if ( invInst1.getNumberOfUses() != invInst2.getNumberOfUses() ) {
			return false;
		}
		// match variable names for uses
		Collection<String> inst1Uses = methodInfo1.getUses( invInst1 );
		Collection<String> inst2Uses = methodInfo2.getUses( invInst2 );
		return matchVarNames( inst1Uses, inst2Uses );
	}

	private int getPartialAssertMatchType( SSAInvokeInstruction invInst1, SSAInvokeInstruction invInst2 ) {
		if ( !invInst1.getDeclaredTarget().getSignature().equals( invInst2.getDeclaredTarget().getSignature() ) ) {
			return 0;
		}
		// isPartialMatch() is called after isExactMatch. Therefore, at this point we know that some
		// of the parameter values don't match.
		// Check whether constant values differ: these would be changes in the expected values
		// However, this does not detect indirect changes to expected values: this occurs when the expected
		// value is a variable and the computation of the value of that variable is modified via
		// method-sequence modification or data modification
		LinkedList<String> inst1ConstUses = methodInfo1.getConstantUses( invInst1 );
		String assertSel1 = invInst1.getDeclaredTarget().getSelector().toString();
		String expVal1 = null;
		if ( inst1ConstUses.size() == ClassificationConstants.junitAssertMethods.get( assertSel1 ) ) {
			expVal1 = inst1ConstUses.getLast();
		}
		LinkedList<String> inst2ConstUses = methodInfo2.getConstantUses( invInst2 );
		String assertSel2 = invInst2.getDeclaredTarget().getSelector().toString();
		String expVal2 = null;
		if ( inst2ConstUses.size() == ClassificationConstants.junitAssertMethods.get( assertSel2 ) ) {
			expVal2 = inst2ConstUses.getLast();
		}
		if ( expVal1 != null && expVal2 != null && expVal1 != expVal2 ) {
			return ClassificationConstants.TESTMOD_ASSERT_EXPVAL_MOD;
		}
		return ClassificationConstants.TESTMOD_ASSERT_MOD;
	}

	private void checkMethodModifications() {
		Collection<SSAInvokeInstruction> callList1 = methodInfo1.getCallInstructions();
		Collection<SSAInvokeInstruction> callList2 = methodInfo2.getCallInstructions();

		// check for exact matches
		ArrayList<SSAInvokeInstruction> exactMatchList1 = new ArrayList<SSAInvokeInstruction>();
		for ( SSAInvokeInstruction call1 : callList1 ) {
			SSAInvokeInstruction matchingInst2 = null;
			for ( SSAInvokeInstruction call2 : callList2 ) {
				if ( isExactInvokeMatch( call1, call2 ) ) {
//				if ( call1.getDeclaredTarget().getSignature().equals( call2.getDeclaredTarget().getSignature() ) ) {
					exactMatchList1.add( call1 );
					matchingInst2 = call2;
					break;
				}
			}
			if ( matchingInst2 != null ) {
				callList2.remove( matchingInst2 );
			}
		}
		callList1.removeAll( exactMatchList1 );
		// for the remaining calls, check for param-add, param-del, and mod classifications
		ArrayList<SSAInvokeInstruction> modMatchList1 = new ArrayList<SSAInvokeInstruction>();
		for ( SSAInvokeInstruction call1 : callList1 ) {
			SSAInvokeInstruction matchingInst2 = null;
			for ( SSAInvokeInstruction call2 : callList2 ) {
				int modMatchType = getCallMatchType( call1, call2 );
				if ( modMatchType == 0 ) {
					continue;
				}
				modMatchList1.add( call1 );
				matchingInst2 = call2;
				Pair<SSAInvokeInstruction> matchedCallPair = new Pair<SSAInvokeInstruction>( call1, call2 );
				if ( (modMatchType & ClassificationConstants.TESTMOD_METHOD_CALL_PARAM_ADD) != 0 ) {
					paramAddedCalls.add( matchedCallPair );
				}
				if ( (modMatchType & ClassificationConstants.TESTMOD_METHOD_CALL_PARAM_DEL) != 0 ) {
					paramDeletedCalls.add( matchedCallPair );
				}
				else {
					modifiedCalls.add( matchedCallPair );
				}
				break;
			}
			if ( matchingInst2 != null ) {
				callList2.remove( matchingInst2 );  // removed modified-match call from list 2
			}
		}
		callList1.removeAll( modMatchList1 );  // remove all modified-match calls from list1
		// the remaining calls in list 1 are deleted calls
		deletedCalls.addAll( callList1 );
		// the remaining calls in list 2 are added calls
		addedCalls.addAll( callList2 );
		if ( modifiedCalls.size() > 0 ) {
			changeType = changeType | ClassificationConstants.TESTMOD_METHOD_CALL_MOD;
		}
		if ( paramAddedCalls.size() > 0 ) {
			changeType = changeType | ClassificationConstants.TESTMOD_METHOD_CALL_PARAM_ADD;
		}
		if ( paramDeletedCalls.size() > 0 ) {
			changeType = changeType | ClassificationConstants.TESTMOD_METHOD_CALL_PARAM_DEL;
		}
		if ( addedCalls.size() > 0 ) {
			changeType = changeType | ClassificationConstants.TESTMOD_METHOD_CALL_ADD;
		}
		if ( deletedCalls.size() > 0 ) {
			changeType = changeType | ClassificationConstants.TESTMOD_METHOD_CALL_DEL;
		}
	}
	
	private int getCallMatchType( SSAInvokeInstruction invInst1, SSAInvokeInstruction invInst2 ) {
		MethodReference callee1 = invInst1.getDeclaredTarget();
		MethodReference callee2 = invInst2.getDeclaredTarget();
		if ( callee1.getSignature().equals( callee2.getSignature() ) ) {
			return ClassificationConstants.TESTMOD_METHOD_CALL_MOD;
		}
		if ( !callee1.getDeclaringClass().getName().toString().equals( callee2.getDeclaringClass().getName().toString() ) ||
				!callee1.getName().toString().equals( callee2.getName().toString() ) ) {
			return 0;
		}
		// declaring class and method names match, but signatures don't match
		// check whether the parameter lists match
		LinkedList<String> callee1ParamTypes = getParameters( callee1 );
		LinkedList<String> callee2ParamTypes = getParameters( callee2 );
		LinkedList<String> exactMatchList1 = new LinkedList<String>();
		for ( String paramType1 : callee1ParamTypes ) {
			String matchingParam2 = null;
			for ( String paramType2 : callee2ParamTypes ) {
				if ( paramType1.equals( paramType2 ) ) {
					matchingParam2 = paramType2;
					exactMatchList1.add( paramType1 );
					break;
				}
			}
			if ( matchingParam2 != null ) {
				callee2ParamTypes.remove( matchingParam2 );
			}
		}
		callee1ParamTypes.removeAll( exactMatchList1 );
		if ( callee1ParamTypes.size() > 0 && callee2ParamTypes.size() == 0 ) {
			return ClassificationConstants.TESTMOD_METHOD_CALL_PARAM_DEL;
		}
		else if ( callee1ParamTypes.size() == 0 && callee2ParamTypes.size() > 0 ) {
			return ClassificationConstants.TESTMOD_METHOD_CALL_PARAM_ADD;
		}
		return ClassificationConstants.TESTMOD_METHOD_CALL_MOD;
	}
	
	private LinkedList<String> getParameters( MethodReference methodRef ) {
		LinkedList<String> params = new LinkedList<String>();
		int numParams = methodRef.getNumberOfParameters();
		for ( int i = 0; i < numParams; i++ ) {
			params.add( methodRef.getParameterType( i ).getName().toString() );
		}
		return params;
	}
	
	private boolean matchVarNames( Collection<String> varList1, Collection<String> varList2 ) {
		if ( varList1.size() == 0 && varList2.size() == 0 ) {
			return true;
		}
		LinkedList<String> unmatchedVars2 = new LinkedList<String>();
		unmatchedVars2.addAll( varList2 );
		for ( String var1 : varList1 ) {
			boolean found = false;
			for ( String var2 : varList2 ) {
				if ( var1.equals( var2 ) ) {
					found = true;
					unmatchedVars2.remove( var2 );
					break;
				}
			}
			if ( !found ) {
				return false; // var 1 not matched
			}
		}
		if ( unmatchedVars2.size() > 0 ) {
			return false; // some vars in list 2 not matched
		}
		return true;
	}

	public int getChangeType() {
		return this.changeType;
	}
	
	public Collection<AssertInstruction> getAddedAsserts() {
		return this.addedAsserts;
	}
	
	public Collection<AssertInstruction> getDeletedAsserts() {
		return this.deletedAsserts;
	}
	
	public Collection<Pair<AssertInstruction>> getModifiedAsserts() {
		return this.modifiedAsserts;
	}
	
	public Collection<Pair<AssertInstruction>> getModifiedExpectedValueAsserts() {
		return this.modifiedAssertExpVals;
	}
	
	public Collection<SSAInvokeInstruction> getAddedCalls() {
		return this.addedCalls;
	}
	
	public Collection<SSAInvokeInstruction> getDeletedCalls() {
		return this.deletedCalls;
	}
	
	public Collection<Pair<SSAInvokeInstruction>> getModifiedCalls() {
		return this.modifiedCalls;
	}
	
	public Collection<Pair<SSAInvokeInstruction>> getParamAddedCalls() {
		return this.paramAddedCalls;
	}
	
	public Collection<Pair<SSAInvokeInstruction>> getParamDeletedCalls() {
		return this.paramDeletedCalls;
	}
	
}
