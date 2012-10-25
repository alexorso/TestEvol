package org.testevol.engine.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.slicer.GetCaughtExceptionStatement;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACFG.ExceptionHandlerBasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.MethodReference;

public class MethodInfo {

	IMethod method;
	SSACFG cfg;
	IR ir;
	SymbolTable symTab;
	private HashMap<SSAInstruction,Integer> instToPCMap;
	
	public MethodInfo( IMethod m ) {
		method = m;
		ir = getIR( method );
	    if ( ir != null ) {
			symTab = ir.getSymbolTable();
	    	load();
	    }
	}
	
	public MethodInfo( CGNode cgNode ) {
		ir = cgNode.getIR();
		symTab = ir.getSymbolTable();
		method = cgNode.getMethod();
		load();
	}
	
	private void load() {
	    cfg = ir.getControlFlowGraph();
		instToPCMap = new HashMap<SSAInstruction, Integer>();
		SSAInstruction[] inst = cfg.getInstructions();
		for ( int i = 0; i < inst.length; i++ ) {
			if ( inst[i] != null ) {
				instToPCMap.put( inst[i], cfg.getProgramCounter( i ) );
			}
		}
	}
	
	public IMethod getMethod() {
		return method;
	}
	
	public SSACFG getCFG() {
		return cfg;
	}
	
	public IR getIR() {
		return ir;
	}
	
	public int getBytecodeOffset( SSAInstruction inst ) {
		if ( instToPCMap.containsKey( inst ) ) {
			return instToPCMap.get( inst );
		}
		return -1;
	}
	
	public int getBytecodeOffset( Statement stmt ) {
		SSAInstruction inst = getSSAInstruction( stmt );
		if ( inst != null ) {
			return getBytecodeOffset( inst );
		}
		return -1;
	}
	
	public int getSourceLineNumber( SSAInstruction inst ) throws InvalidClassFileException {
		int bytecodeOffset = getBytecodeOffset( inst );
		if ( bytecodeOffset != -1 ) {
			return method.getLineNumber( bytecodeOffset );
		}
		return -1;
	}
	
	public int getSourceLineNumber( Statement stmt ) throws InvalidClassFileException {
		SSAInstruction inst = getSSAInstruction( stmt );
		if ( inst != null ) {
			return getSourceLineNumber( inst );
		}
		return -1;
	}
	
	public int getStartLineNumber() throws InvalidClassFileException {
		int line = Integer.MAX_VALUE;
		SSAInstruction[] instList = ir.getInstructions();
		for ( SSAInstruction inst : instList ) {
			if ( inst != null ) {
				int srcLine = getSourceLineNumber( inst ); 
				if ( srcLine > 0 && srcLine < line ) {
					line = srcLine;
				}
			}
		}
		return line;
	}
	
	public int getEndLineNumber() throws InvalidClassFileException {
		int line = -1;
		SSAInstruction[] instList = ir.getInstructions();
		for ( SSAInstruction inst : instList ) {
			if ( inst != null ) {
				int srcLine = getSourceLineNumber( inst ); 
				if ( srcLine > line ) {
					line = srcLine;
				}
			}
		}
		return line;
	}
	private SSAInstruction getSSAInstruction( Statement stmt ) {
		SSAInstruction inst = null;
		if ( stmt instanceof NormalStatement ) {
			inst = ((NormalStatement)stmt).getInstruction();
		}
		else if ( stmt instanceof GetCaughtExceptionStatement ) {
			inst = ((GetCaughtExceptionStatement)stmt).getInstruction();
		}
		return inst;
	}
	
	public Collection<String> getDefinitions( SSAInstruction inst ) {
		ArrayList<String> defs = new ArrayList<String>();
		getVarsForInst( inst, inst.getDef(), defs );
		return defs;
	}
	
	public Collection<String> getUses( SSAInstruction inst ) {
		ArrayList<String> uses = new ArrayList<String>();
		if ( ir == null ) {
			return uses;
		}
		for ( int i = 0; i < inst.getNumberOfUses(); i++ ) {
			getVarsForInst( inst, inst.getUse( i ), uses );
		}
		return uses;
	}
	
	public boolean hasConstantUse( SSAInstruction inst ) {
		for ( int i = 0; i < inst.getNumberOfUses(); i++ ) {
			if ( symTab.isConstant( inst.getUse( i ) ) ) {
				return true;
			}
		}
		return false;
	}

	public LinkedList<String> getConstantUses( SSAInstruction inst ) {
		LinkedList<String> constUses = new LinkedList<String>();
		for ( int i = 0; i < inst.getNumberOfUses(); i++ ) {
			int valNum = inst.getUse( i );
			String constVal = getConstant( valNum );
			if ( constVal != null ) {
				constUses.add( constVal );
			}
		}
		return constUses;
	}
	
	public boolean isConstantVariable( SSAInstruction inst, int valueNum ) {
		if ( symTab.isConstant( valueNum ) ) {
			return true;
		}
		return false;
	}
	
	public Collection<SSAInvokeInstruction> getAssertCalls() {
		ArrayList<SSAInvokeInstruction> assertCalls = new ArrayList<SSAInvokeInstruction>();
		for ( SSAInstruction inst : cfg.getInstructions() ) {
			if ( inst instanceof SSAInvokeInstruction ) {
				SSAInvokeInstruction invInst = (SSAInvokeInstruction)inst;
				if ( isAssertCall( invInst ) ) {
					assertCalls.add( invInst );
				}
			}
		}
		return assertCalls;
	}
	
	public Collection<SSAInvokeInstruction> getCallInstructions() {
		ArrayList<SSAInvokeInstruction> callInst = new ArrayList<SSAInvokeInstruction>();
		for ( SSAInstruction inst : cfg.getInstructions() ) {
			if ( inst instanceof SSAInvokeInstruction ) {
				SSAInvokeInstruction invInst = (SSAInvokeInstruction)inst;
				if ( !isAssertCall( invInst ) ) {
					callInst.add( invInst );
				}
			}
		}
		return callInst;
	}
	
	public Collection<ExceptionHandlerBasicBlock> getCatchBlocks() {
		ArrayList<ExceptionHandlerBasicBlock> cb = new ArrayList<ExceptionHandlerBasicBlock>();
		for ( ISSABasicBlock node : cfg ) {
			if ( node.isCatchBlock() ) {
				cb.add( (ExceptionHandlerBasicBlock)node );
			}
		}
		return cb;
	}
	
	private boolean isAssertCall( SSAInvokeInstruction invInst ) {
		MethodReference calleeRef = invInst.getCallSite().getDeclaredTarget();
		String calleeSel = calleeRef.getSelector().toString();
		for ( String assertSel : ClassificationConstants.junitAssertMethods.keySet() ) {
			if ( assertSel.endsWith( calleeSel ) ) {
				return true;
			}
		}
		return false;		
	}
	
	
	
	private void getVarsForInst( SSAInstruction inst, int vn, ArrayList<String> vars ) {
		SSAInstruction[] instArr = cfg.getInstructions();
		if ( vn > -1 ) {
			if ( symTab.isStringConstant( vn ) ) {
				vars.add( symTab.getConstantValue( vn ).toString() );
				return;
			}
			if ( symTab.isIntegerConstant( vn ) ) {
				vars.add( symTab.getConstantValue( vn ).toString() );
				return;
			}			
			if ( symTab.isLongConstant( vn ) ) {
				vars.add( symTab.getConstantValue( vn ).toString() );
				return;
			}			
			if ( symTab.isFloatConstant( vn ) ) {
				vars.add( symTab.getConstantValue( vn ).toString() );
				return;
			}			
			if ( symTab.isDoubleConstant( vn ) ) {
				vars.add( symTab.getConstantValue( vn ).toString() );
				return;
			}			
		}
		for ( int i = 0; i < instArr.length; i++ ) {
			SSAInstruction ssaInst = (SSAInstruction)instArr[i];
			if ( ssaInst == inst ) {
				String[] names = ir.getLocalNames( i, vn );
				if ( names != null ) {
					for ( String name : names ) {
						if ( name != null ) {
							vars.add( name );
						}
					}
				}
				return;
			}
		}
	}
	
	private String getConstant( int vn ) {
		if ( symTab.isStringConstant( vn ) ) {
			return symTab.getConstantValue( vn ).toString();
		}
		if ( symTab.isIntegerConstant( vn ) ) {
			return symTab.getConstantValue( vn ).toString();
		}			
		if ( symTab.isLongConstant( vn ) ) {
			return symTab.getConstantValue( vn ).toString();
		}			
		if ( symTab.isFloatConstant( vn ) ) {
			return symTab.getConstantValue( vn ).toString();
		}			
		if ( symTab.isDoubleConstant( vn ) ) {
			return symTab.getConstantValue( vn ).toString();
		}			
		return null;
	}
	
	public static IR getIR( IMethod method ) {
		AnalysisCache cache = new AnalysisCache();
		return cache.getSSACache().findOrCreateIR(method, Everywhere.EVERYWHERE, SSAOptions.defaultOptions() );
	}
}
