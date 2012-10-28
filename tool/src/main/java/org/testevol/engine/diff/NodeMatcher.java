package org.testevol.engine.diff;

import java.util.Collection;
import java.util.LinkedList;

import org.testevol.engine.util.MethodInfo;

import com.ibm.wala.shrikeBT.BinaryOpInstruction;
import com.ibm.wala.shrikeBT.ConditionalBranchInstruction;
import com.ibm.wala.shrikeBT.UnaryOpInstruction;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAArrayLengthInstruction;
import com.ibm.wala.ssa.SSAArrayLoadInstruction;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSACheckCastInstruction;
import com.ibm.wala.ssa.SSAComparisonInstruction;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAConversionInstruction;
import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAGotoInstruction;
import com.ibm.wala.ssa.SSAInstanceofInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSALoadIndirectInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSASwitchInstruction;
import com.ibm.wala.ssa.SSAThrowInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.TypeReference;

public class NodeMatcher {

	private MethodInfo methodInfo1;
	private MethodInfo methodInfo2;
	
	public NodeMatcher( MethodInfo mInfo1, MethodInfo mInfo2 ) {
		methodInfo1 = mInfo1;
		methodInfo2 = mInfo2;
	}
	
	public boolean compareNodes( ISSABasicBlock node1, ISSABasicBlock node2 ) {
		// entry, exit nodes match
		if ( node1.isEntryBlock() && node2.isEntryBlock() ) {
			return true;
		}
		if ( node1.isExitBlock() && node2.isExitBlock() ) {
			return true;
		}
		
		// build list of instructions
		SSAInstruction[] instList1 = getInstructions( node1 );
		SSAInstruction[] instList2 = getInstructions( node2 );

		// match instructions, in order, up to the smaller length of the two lists
		int len = ( instList1.length < instList2.length ) ? instList1.length : instList2.length;
		for ( int i = 0; i < len; i++ ) {
			SSAInstruction inst1 = instList1[i];
			SSAInstruction inst2 = instList2[i];
			//System.err.println( "                  "+i+". "+inst1+" : "+inst2 );
			if ( !matchSSAInstruction( inst1, inst2 ) ) {
				return false;
			}
		}
		return true;
	}
	
	private SSAInstruction[] getInstructions( ISSABasicBlock node ) {
		LinkedList<SSAInstruction> instrList = new LinkedList<SSAInstruction>();
		for ( SSAInstruction instr : node ) {
			instrList.add(instr);
		}
		return instrList.toArray( new SSAInstruction[0] );
	}

	/**
	 * Matches two SSA instructions of the same type.
	 * @param instr1
	 * @param instr2
	 * @return true is instructions match; false otherwise
	 */
	private boolean matchSSAInstruction( SSAInstruction inst1, SSAInstruction inst2 ) {
		// if different types of instructions, return false
		if ( !inst1.getClass().getName().equals( inst2.getClass().getName() ) ) {
			return false;
		}
		// instructions are of the same types
		//System.err.println( "                -- "+inst1+" | "+inst2 );
		
		// match definitions and uses
		if ( !matchDefsAndUses( inst1, inst2 ) ) {
			return false;
		}
		
		// Don't match exception types

		// match specific types of instructions
		if ( inst1 instanceof SSAArrayLengthInstruction ) {
			return matchSSAArrayLengthInstruction( (SSAArrayLengthInstruction)inst1, (SSAArrayLengthInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSAArrayLoadInstruction ) {
			return matchSSAArrayLoadInstruction( (SSAArrayLoadInstruction)inst1, (SSAArrayLoadInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSAArrayStoreInstruction ) {
			return matchSSAArrayStoreInstruction( (SSAArrayStoreInstruction)inst1, (SSAArrayStoreInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSABinaryOpInstruction ) {
			return matchSSABinaryOpInstruction( (SSABinaryOpInstruction)inst1, (SSABinaryOpInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSACheckCastInstruction ) {
			return matchSSACheckCastInstruction( (SSACheckCastInstruction)inst1, (SSACheckCastInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSAComparisonInstruction ) {
			return matchSSAComparisonInstruction( (SSAComparisonInstruction)inst1, (SSAComparisonInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSAConditionalBranchInstruction ) {
			return matchSSAConditionalBranchInstruction( (SSAConditionalBranchInstruction)inst1, (SSAConditionalBranchInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSAConversionInstruction ) {
			return matchSSAConversionInstruction( (SSAConversionInstruction)inst1, (SSAConversionInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSAGetInstruction ) {
			return matchSSAGetInstruction( (SSAGetInstruction)inst1, (SSAGetInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSAGetCaughtExceptionInstruction ) {
			return matchSSAGetCaughtInstruction( (SSAGetCaughtExceptionInstruction)inst1, (SSAGetCaughtExceptionInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSAGotoInstruction ) {
			return matchSSAGotoInstruction( (SSAGotoInstruction)inst1, (SSAGotoInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSAInstanceofInstruction ) {
			return matchSSAInstanceofInstruction( (SSAInstanceofInstruction)inst1, (SSAInstanceofInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSAInvokeInstruction ) {
			return matchSSAInvokeInstruction( (SSAInvokeInstruction)inst1, (SSAInvokeInstruction)inst2 );
		}
		
//		if ( inst1 instanceof SSALoadClassInstruction ) {
//			return matchSSALoadClassInstruction( (SSALoadClassInstruction)inst1, (SSALoadClassInstruction)inst2 );
//		}
		
		if ( inst1 instanceof SSALoadIndirectInstruction ) {
			return matchSSALoadIndirectInstruction((SSALoadIndirectInstruction)inst1, (SSALoadIndirectInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSAMonitorInstruction ) {
			return matchSSAMonitorInstruction( (SSAMonitorInstruction)inst1, (SSAMonitorInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSANewInstruction ) {
			return matchSSANewInstruction( (SSANewInstruction)inst1, (SSANewInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSAPutInstruction ) {
			return matchSSAPutInstruction( (SSAPutInstruction)inst1, (SSAPutInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSAReturnInstruction ) {
			return matchSSAReturnInstruction( (SSAReturnInstruction)inst1, (SSAReturnInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSASwitchInstruction ) {
			return matchSSASwitchInstruction( (SSASwitchInstruction)inst1, (SSASwitchInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSAThrowInstruction ) {
			return matchSSAThrowInstruction( (SSAThrowInstruction)inst1, (SSAThrowInstruction)inst2 );
		}
		
		if ( inst1 instanceof SSAUnaryOpInstruction ) {
			return matchSSAUnaryOpInstruction( (SSAUnaryOpInstruction)inst1, (SSAUnaryOpInstruction)inst2 );
		}
		return true;
	}

	private boolean matchSSAArrayLengthInstruction( SSAArrayLengthInstruction inst1, SSAArrayLengthInstruction inst2 ) {
		// no instruction-specific matching required; defs/uses already matched
		return true;
	}

	private boolean matchSSAArrayLoadInstruction( SSAArrayLoadInstruction inst1, SSAArrayLoadInstruction inst2 ) {
		// no instruction-specific matching required; defs/uses already matched
		return true;
	}

	private boolean matchSSAArrayStoreInstruction( SSAArrayStoreInstruction inst1, SSAArrayStoreInstruction inst2 ) {
		// no instruction-specific matching required; defs/uses already matched
		return true;
	}

	private boolean matchSSABinaryOpInstruction( SSABinaryOpInstruction inst1, SSABinaryOpInstruction inst2 ) {
		//System.err.println( "                -- Matching binary op "+inst1+" | "+inst2 );
		// return false if different binary operators
		// ADD, AND, DIV, MUL, OR, REM, SUB, XOR
		// SHL, SHR, USHL
		BinaryOpInstruction.IOperator inst1Op = inst1.getOperator();
		BinaryOpInstruction.IOperator inst2Op = inst2.getOperator();
		//System.err.println( "                   "+inst1Op.toString()+" "+inst2Op.toString() );
		if ( !inst1Op.toString().equals( inst2Op.toString() ) ) {
			return false;
		}		
		return true;
	}
	
	private boolean matchSSACheckCastInstruction( SSACheckCastInstruction inst1, SSACheckCastInstruction inst2 ) {
		// match get declared result type
		if ( !matchTypeReference( inst1.getDeclaredResultType(), inst2.getDeclaredResultType() ) ) {
			return false;
		}
		// getResult, getVal: getResult returns def, getVal returns use --- def/use already matched
		return true;
	}

	private boolean matchSSAComparisonInstruction( SSAComparisonInstruction inst1, SSAComparisonInstruction inst2 ) {
		// return false if opcodes don't match
		if ( inst1.getOperator().equals(inst2.getOperator())) {
			return false;
		}
		return true;
	}

	private boolean matchSSAConditionalBranchInstruction( SSAConditionalBranchInstruction inst1, SSAConditionalBranchInstruction inst2 ) {
		// return false if different conditional operators
		// EQ, GE, GT, LE, LT, NE
		ConditionalBranchInstruction.IOperator inst1Op = inst1.getOperator();
		ConditionalBranchInstruction.IOperator inst2Op = inst2.getOperator();
		//System.err.println( "                   "+inst1Op.toString()+" "+inst2Op.toString() );
		if ( !inst1Op.toString().equals( inst2Op.toString() ) ) {
			return false;
		}
		// match getType
		if ( !matchTypeReference( inst1.getType(), inst2.getType() ) ) {
			return false;
		}
		// object or integer comparison (is it required after def/use match)
		if ( ( inst1.isObjectComparison() && inst2.isIntegerComparison() ) ||
			 ( inst1.isIntegerComparison() && inst2.isObjectComparison() ) ) {
			return false;
		}
		return true;
	}

	private boolean matchSSAConversionInstruction( SSAConversionInstruction inst1, SSAConversionInstruction inst2 ) {
		// match from types
		if ( !matchTypeReference( inst1.getFromType(), inst2.getFromType() ) ) {
			return false;
		}
		// match to types
		return matchTypeReference( inst1.getToType(), inst2.getToType() );
	}

	private boolean matchSSAGetCaughtInstruction( SSAGetCaughtExceptionInstruction inst1, SSAGetCaughtExceptionInstruction inst2 ) {
		// getException: returns def --- def already matched
		//System.err.println( "                -- Matching catch "+inst1+" | "+inst2 );
		return true;
	}

	private boolean matchSSAGetInstruction( SSAGetInstruction inst1, SSAGetInstruction inst2 ) {
		FieldReference fref1 = inst1.getDeclaredField();
		FieldReference fref2 = inst2.getDeclaredField();
		if ( fref1 != null && fref2 != null ) {
			if ( matchFieldReference( fref1, fref2 ) ) {
				return true;
			}
		}
		return false;
	}

	private boolean matchSSAGotoInstruction( SSAGotoInstruction inst1, SSAGotoInstruction inst2 ) {
		// TODO: goto instructions match only if their successors match
		return true;
	}

	private boolean matchSSAInstanceofInstruction( SSAInstanceofInstruction inst1, SSAInstanceofInstruction inst2 ) {
		// match getCheckedType
		return matchTypeReference( inst1.getCheckedType(), inst2.getCheckedType() );
	}

	private boolean matchSSAInvokeInstruction( SSAInvokeInstruction inst1, SSAInvokeInstruction inst2 ) {
		//System.err.println( "                -- Matching invoke "+inst1+" | "+inst2 );		
		// match parameters: matched as uses
		// getDeclaredTarget
		String decl1 = inst1.getDeclaredTarget().getSignature();
		String decl2 = inst2.getDeclaredTarget().getSignature();
		//System.err.println( "                   target1 = "+decl1+" target2 = "+decl2 );
		if ( !decl1.equals( decl2 ) ) {
			return false;
		}
		// getDeclaredResultType: not required because signature has been matched
		// getReceiver: returns (1) call site, which should not be matched, or (2) use, which has already been matched
		// getInvocationCode: returns call site, which should not be matched
		// getReturnValue: returns either -1 or def --- neither needs to be matched
		return true;
	}

	private boolean matchSSALoadIndirectInstruction( SSALoadIndirectInstruction inst1, SSALoadIndirectInstruction inst2 ) {
		// match getLoadedClass
		return matchTypeReference( inst1.getLoadedType(), inst2.getLoadedType());
	}
	
//	private boolean matchSSALoadClassInstruction( SSALoadClassInstruction inst1, SSALoadClassInstruction inst2 ) {
//		// match getLoadedClass
//		return matchTypeReference( inst1.getLoadedClass(), inst2.getLoadedClass() );
//	}

	private boolean matchSSAMonitorInstruction( SSAMonitorInstruction inst1, SSAMonitorInstruction inst2 ) {
		// no instruction-specific matching required
		return true;
	}

	private boolean matchSSANewInstruction( SSANewInstruction inst1, SSANewInstruction inst2 ) {
		// match getConcreteType
		if ( !matchTypeReference( inst1.getConcreteType(), inst2.getConcreteType() ) ) {
			return false;
		}
		// getNewSite: returns program counter, which should not be matched
		return true;
	}

	private boolean matchSSAPutInstruction( SSAPutInstruction inst1, SSAPutInstruction inst2 ) {
		FieldReference fref1 = inst1.getDeclaredField();
		FieldReference fref2 = inst2.getDeclaredField();
		if ( fref1 != null && fref2 != null ) {
			if ( matchFieldReference( fref1, fref2 ) ) {
				return true;
			}
		}
		return false;
	}

	private boolean matchSSAReturnInstruction( SSAReturnInstruction inst1, SSAReturnInstruction inst2 ) {
		// getResult: not required because uses have been matched
		// returnsVoid: not required because number of uses have been matched
		// TODO: returns match only if their predecessors match (i.e., the same value is being returned). Without this check,
		// returns can be matched incorrectly
		return true;
	}

	private boolean matchSSASwitchInstruction( SSASwitchInstruction inst1, SSASwitchInstruction inst2 ) {
		// getCaseAndLabels
		// getDefault
		// getTarget
		// only uses need to be matched: if case and labels, default, or target differ, these would be accounted for when the CFG
		// traversal follows like-labeled out edges from switch (the changes would be recorded at those statements)
		return true;
	}
	
	private boolean matchSSAThrowInstruction( SSAThrowInstruction inst1, SSAThrowInstruction inst2 ) {
		// getException: not required because use has been matched
		return true;
	}
	
	private boolean matchSSAUnaryOpInstruction( SSAUnaryOpInstruction inst1, SSAUnaryOpInstruction inst2 ) {
		// check unary operator; required
		// only one operator: NEG
		UnaryOpInstruction.IOperator inst1Op = inst1.getOpcode();
		UnaryOpInstruction.IOperator inst2Op = inst2.getOpcode();
		//System.err.println( "                   "+inst1Op.toString()+" "+inst2Op.toString() );
		if ( !inst1Op.toString().equals( inst2Op.toString() ) ) {
			return false;
		}		
		return true;
	}
	
	private boolean matchTypeReference( TypeReference typeRef1, TypeReference typeRef2 ) {
		String type1name = typeRef1.getName().toString();
		String type2name = typeRef2.getName().toString();
		if ( !type1name.equals( type2name ) ) {
			return false;
		}
		return true;
	}
	
	private boolean matchFieldReference( FieldReference fieldRef1, FieldReference fieldRef2 ) {
		if ( !fieldRef1.getSignature().equals( fieldRef2.getSignature() ) ) {
			return false;
		}
		return true;
	}
	
	private boolean matchDefsAndUses( SSAInstruction inst1, SSAInstruction inst2 ) {
		// return false if different number of definitions or uses
		if ( inst1.getNumberOfDefs() != inst2.getNumberOfDefs() ||
				inst1.getNumberOfUses() != inst2.getNumberOfUses() ) {
			return false;
		}
		// match variable names for definitions and uses
		Collection<String> inst1Defs = methodInfo1.getDefinitions( inst1 );
		Collection<String> inst2Defs = methodInfo2.getDefinitions( inst2 );
		if ( !matchVarNames( inst1Defs, inst2Defs ) ) {
			return false;
		}
		Collection<String> inst1Uses = methodInfo1.getUses( inst1 );
		Collection<String> inst2Uses = methodInfo2.getUses( inst2 );
		return matchVarNames( inst1Uses, inst2Uses );
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
	
}
