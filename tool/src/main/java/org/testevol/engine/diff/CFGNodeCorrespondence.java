package org.testevol.engine.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.testevol.engine.util.MethodInfo;

import com.ibm.wala.cfg.Util;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACFG.ExceptionHandlerBasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.TypeReference;

public class CFGNodeCorrespondence {

	private Collection<ISSABasicBlock> v1Nodes;
	private Collection<ISSABasicBlock> v2Nodes;
	private Collection<Pair<ISSABasicBlock>> matchedNodes;
	private Collection<ISSABasicBlock> v1UnmatchedNodes;
	private Collection<ISSABasicBlock> v2UnmatchedNodes;
	
	private MethodInfo methodInfo1;
	private MethodInfo methodInfo2;
	private NodeMatcher nodeMatcher;
	
	private SSACFG cfg1;
	private SSACFG cfg2;
	
	private Collection<ISSABasicBlock> v1VisitedNodes;
	private Collection<ISSABasicBlock> v2VisitedNodes;
	
	private Pair<ISSABasicBlock> firstDifferingNodePair;

	public CFGNodeCorrespondence( IMethod method1, IMethod method2 ) {
		// load method info, which creates CFG and IR for the method
		methodInfo1 = new MethodInfo( method1 );
		methodInfo2 = new MethodInfo( method2 );
		load();
	}
	
	public CFGNodeCorrespondence( CGNode cgNode1, CGNode cgNode2 ) {
		// load method info, which creates CFG and IR for the method
		methodInfo1 = new MethodInfo( cgNode1 );
		methodInfo2 = new MethodInfo( cgNode2 );
		load();
	}	
	
	private void load() {
		v1Nodes = new ArrayList<ISSABasicBlock>();
		v2Nodes = new ArrayList<ISSABasicBlock>();
		matchedNodes = new ArrayList<Pair<ISSABasicBlock>>();
		v1UnmatchedNodes = new ArrayList<ISSABasicBlock>();
		v2UnmatchedNodes = new ArrayList<ISSABasicBlock>();
		
		cfg1 = methodInfo1.getCFG();
		cfg2 = methodInfo2.getCFG();
		if ( cfg1 == null || cfg2 == null ) {
			return;
		}
		// initialize CFG nodes
		for ( ISSABasicBlock node : cfg1 ) {
			v1Nodes.add( node );
		}		
		for ( ISSABasicBlock node : cfg2 ) {
			v2Nodes.add( node );
		}
		v1UnmatchedNodes.addAll( v1Nodes );
		v2UnmatchedNodes.addAll( v2Nodes );
		
		// create node matcher
		nodeMatcher = new NodeMatcher( methodInfo1, methodInfo2 );
		
		// traverse CFGs starting at each entry node and match nodes
		traverseCFGs();
	}
	
	private void traverseCFGs() {
		v1VisitedNodes = new LinkedList<ISSABasicBlock>();
		v2VisitedNodes = new LinkedList<ISSABasicBlock>();
		
		// traverse CFGs from successors of the entry nodes
		ISSABasicBlock entryNode1 = cfg1.entry();
		ISSABasicBlock entryNode2 = cfg2.entry();
		markNodesAsMatched( entryNode1, entryNode2 );
		ISSABasicBlock entry1succ = cfg1.getNormalSuccessors( entryNode1 ).iterator().next();
		ISSABasicBlock entry2succ = cfg2.getNormalSuccessors( entryNode2 ).iterator().next();
		traverseCFGsStartingAtNode( new Pair<ISSABasicBlock>( entry1succ, entry2succ ) );
		
		// traverse paths starting at catch nodes
		Collection<Pair<ISSABasicBlock>> catchPairs = getCatchPairs();
		for ( Pair<ISSABasicBlock> catchPair : catchPairs ) {
			traverseCFGsStartingAtNode( catchPair );
		}

	}
	
	private void traverseCFGsStartingAtNode( Pair<ISSABasicBlock> startPair ) {
		LinkedList<Pair<ISSABasicBlock>> worklist = new LinkedList<Pair<ISSABasicBlock>>();
		addToWorklist( startPair, worklist );
		while ( !worklist.isEmpty() ) {
			// remove node pair from end
			Pair<ISSABasicBlock> wlPair = worklist.removeLast();
			ISSABasicBlock node1 = wlPair.getFirst();
			ISSABasicBlock node2 = wlPair.getSecond();
			// mark nodes as visited
			v1VisitedNodes.add( node1 );
			v2VisitedNodes.add( node2 );
			
			// if either node is matched, continue to the next pair
			if ( !v1UnmatchedNodes.contains( node1 ) || !v2UnmatchedNodes.contains( node2 ) ) {
				if ( !v1UnmatchedNodes.contains( node1 ) ) {
					if ( getInstructionCountForNode( node2 ) == 0 ) { // if other node is empty, removed if from the unmatched set
						v2UnmatchedNodes.remove( node2 );
					}
				}
				if ( !v2UnmatchedNodes.contains( node2 ) ) {
					if ( getInstructionCountForNode( node1 ) == 0 ) {
						v1UnmatchedNodes.remove( node1 );
					}
				}
				continue;
			}

			// skip empty nodes
			if ( getInstructionCountForNode( node1 ) == 0 && getInstructionCountForNode( node2 ) == 0 ) {
				Collection<Pair<ISSABasicBlock>> succPairs = getCorresondingSuccNodes( node1, node2 );
				for ( Pair<ISSABasicBlock> succPair : succPairs ) {
					addToWorklist( succPair, worklist );
				}
				v1UnmatchedNodes.remove( node1 );
				v2UnmatchedNodes.remove( node2 );
				continue;
			}
			else if ( getInstructionCountForNode( node1 ) == 0 ) {
				Collection<ISSABasicBlock> node1Succs = cfg1.getNormalSuccessors( node1 );
				if ( node1Succs.size() == 1 ) { // succs should either be 1 or 0
					// visit node 2 again, this time with successor of node 1
					ISSABasicBlock node1Succ = node1Succs.iterator().next();
					v2VisitedNodes.remove( node2 );
					addToWorklist( new Pair<ISSABasicBlock>( node1Succ, node2 ), worklist );	
				}
				// remove the empty node from unmatched nodes, so that it is not counted as a deleted node
				v1UnmatchedNodes.remove( node1 );
				continue;
			}
			else if ( getInstructionCountForNode( node2 ) == 0 ) {
				Collection<ISSABasicBlock> node2Succs = cfg2.getNormalSuccessors( node2 );
				if ( node2Succs.size() == 1 ) { // succs should either be 1 or 0
					// visit node 1 again, this time with successor of node 2
					ISSABasicBlock node2Succ = node2Succs.iterator().next();
					v1VisitedNodes.remove( node1 );
					addToWorklist( new Pair<ISSABasicBlock>( node1, node2Succ ), worklist );
				}
				// remove the empty node from unmatched nodes, so that it is not counted as an added node
				v2UnmatchedNodes.remove( node2 );
				continue;
			}

			boolean nodesMatch = nodeMatcher.compareNodes( node1, node2 );
			if ( nodesMatch ) {
				markNodesAsMatched( node1, node2 );
			}
			else { // found a pair of nodes that don't match
				firstDifferingNodePair = new Pair<ISSABasicBlock>( node1, node2 );
				break;
			}

			// continue traversal from unvisited matching successors of the two nodes
			Collection<Pair<ISSABasicBlock>> succPairs = getCorresondingSuccNodes( node1, node2 );
			for ( Pair<ISSABasicBlock> succPair : succPairs ) {
				addToWorklist( succPair, worklist );
			}

		}
	}
	
	private void addToWorklist( Pair<ISSABasicBlock> nodePair, LinkedList<Pair<ISSABasicBlock>> worklist ) {
		ISSABasicBlock node1 = nodePair.getFirst();
		ISSABasicBlock node2 = nodePair.getSecond();
		if ( !v1VisitedNodes.contains( node1 ) || !v2VisitedNodes.contains( node2 ) ) {
			worklist.addLast( nodePair );
		}
	}
	
	private void markNodesAsMatched( ISSABasicBlock node1, ISSABasicBlock node2 ) {
		matchedNodes.add( new Pair<ISSABasicBlock>( node1, node2 ) );
		v1UnmatchedNodes.remove( node1 );
		v2UnmatchedNodes.remove( node2 );		
	}
	
	private Collection<Pair<ISSABasicBlock>> getCatchPairs() {
		Collection<Pair<ISSABasicBlock>> catchPairs = new LinkedList<Pair<ISSABasicBlock>>();
		Collection<ExceptionHandlerBasicBlock> catchNodes1 = methodInfo1.getCatchBlocks();
		Collection<ExceptionHandlerBasicBlock> catchNodes2 = methodInfo2.getCatchBlocks();
		// find nodes in catch list 1 that have exactly one match in list 2
		Collection<ExceptionHandlerBasicBlock> um1 = new LinkedList<ExceptionHandlerBasicBlock>();
		for ( ExceptionHandlerBasicBlock cn : catchNodes1 ) {
			if ( getMatchingCatchNodes( cn, catchNodes2 ).size() == 1 ) {
				um1.add( cn );
			}
		}
		// find nodes in catch list 2 that have exactly one match in list 1
		Collection<ExceptionHandlerBasicBlock> um2 = new LinkedList<ExceptionHandlerBasicBlock>();
		for ( ExceptionHandlerBasicBlock cn : catchNodes2 ) {
			if ( getMatchingCatchNodes( cn, catchNodes1 ).size() == 1 ) {
				um2.add( cn );
			}
		}
		// add pairs from the unique match lists
		for ( ExceptionHandlerBasicBlock c1 : um1 ) {
			Collection<ExceptionHandlerBasicBlock> matchingCatches = getMatchingCatchNodes( c1, um2 );
			// invariant: matching catches = 0 or 1
			if ( matchingCatches.size() == 1 ) {
				catchPairs.add( new Pair<ISSABasicBlock>( c1, matchingCatches.iterator().next() ) );
			}
		}
		// add to catchPairs only if there is a unique matching catch 
		return catchPairs;
	}

	private Collection<ExceptionHandlerBasicBlock> getMatchingCatchNodes( ExceptionHandlerBasicBlock cNode, Collection<ExceptionHandlerBasicBlock> cnList ) {
		Collection<ExceptionHandlerBasicBlock> matchingCatches = new LinkedList<ExceptionHandlerBasicBlock>();
		Collection<TypeReference> eList1 = getExcpList( cNode.getCaughtExceptionTypes() );
		for ( ExceptionHandlerBasicBlock cn : cnList ) {
			Collection<TypeReference> eList2 = getExcpList( cn.getCaughtExceptionTypes() );
			LinkedList<TypeReference> matchingTypes1 = new LinkedList<TypeReference>();
			LinkedList<TypeReference> matchingTypes2 = new LinkedList<TypeReference>();
			for ( TypeReference e1 : eList1 ) {
				for ( TypeReference e2 : eList2 ) {
					if ( e1.getName().toString().equals( e2.getName().toString() ) ) {
						matchingTypes1.add( e1 );
						matchingTypes2.add( e2 );
					}
				}
			}
			if ( matchingTypes1.containsAll( eList1 ) && matchingTypes2.containsAll( eList2 ) ) {
				matchingCatches.add( cn );
			}
		}
		return matchingCatches;
	}
	
	private Collection<TypeReference> getExcpList( Iterator<TypeReference> iter ) {
		LinkedList<TypeReference> list = new LinkedList<TypeReference>();
		while ( iter.hasNext() ) {
			list.add( iter.next() );
		}
		return list;
	}
	
	private Collection<Pair<ISSABasicBlock>> getCorresondingSuccNodes( ISSABasicBlock node1, ISSABasicBlock node2 ) {
		Collection<Pair<ISSABasicBlock>> succs = new LinkedList<Pair<ISSABasicBlock>>();
		
		if ( node1.getLastInstructionIndex() >= 0 && node2.getLastInstructionIndex() >= 0 ) {
			// if statements: return successors along matching outedges
			boolean isNode1Conditional = Util.endsWithConditionalBranch( cfg1, node1 );
			boolean isNode2Conditional = Util.endsWithConditionalBranch( cfg2, node2 );
			if ( isNode1Conditional && isNode2Conditional ) {
				// add true successors
				ISSABasicBlock trueSucc1 = Util.getTakenSuccessor( cfg1, node1 );
				ISSABasicBlock trueSucc2 = Util.getTakenSuccessor( cfg2, node2 );
				succs.add( new Pair<ISSABasicBlock>( trueSucc1, trueSucc2 ) );
				// add false successors
				ISSABasicBlock falseSucc1 = Util.getNotTakenSuccessor( cfg1, node1 );
				ISSABasicBlock falseSucc2 = Util.getNotTakenSuccessor( cfg2, node2 );
				succs.add( new Pair<ISSABasicBlock>( falseSucc1, falseSucc2 ) );
				return succs;
			}		
			// switch statements: return successors along matching outedges
			boolean isNode1Switch = Util.endsWithSwitch( cfg1, node1 );
			boolean isNode2Switch = Util.endsWithSwitch( cfg2, node2 );
			if ( isNode1Switch && isNode2Switch ) {
				// build a map from switch labels to corresponding succ nodes
				HashMap<Integer,ISSABasicBlock> switchMap1 = new HashMap<Integer,ISSABasicBlock>();
				ISSABasicBlock switch1DefaultSucc = null;
				for ( ISSABasicBlock switchSucc1 : cfg1.getNormalSuccessors( node1 ) ) {
					if ( Util.isSwitchDefault( cfg1, node1, switchSucc1 ) ) {
						switch1DefaultSucc = switchSucc1;
					}
					else {
						int label1 = Util.getSwitchLabel( cfg1, node1, switchSucc1 );
						switchMap1.put( new Integer( label1 ), switchSucc1 );
					}
				}
				HashMap<Integer,ISSABasicBlock> switchMap2 = new HashMap<Integer,ISSABasicBlock>();
				ISSABasicBlock switch2DefaultSucc = null;
				for ( ISSABasicBlock switchSucc2 : cfg2.getNormalSuccessors( node2 ) ) {
					if ( Util.isSwitchDefault( cfg2, node2, switchSucc2 ) ) {
						switch2DefaultSucc = switchSucc2;
					}
					else {
						int label2 = Util.getSwitchLabel( cfg2, node2, switchSucc2 );
						switchMap2.put( new Integer( label2 ), switchSucc2 );
					}
				}
				// add pairs with matching labels to succs
				for ( Integer label1 : switchMap1.keySet() ) {
					for ( Integer label2 : switchMap2.keySet() ) {
						if ( label1.intValue() == label2.intValue() ) {
							succs.add( new Pair<ISSABasicBlock>( switchMap1.get( label1 ), switchMap2.get( label2 ) ) );
						}
					}
				}
				// add default successors
				if ( switch1DefaultSucc != null && switch2DefaultSucc != null ) {
					succs.add( new Pair<ISSABasicBlock>( switch1DefaultSucc, switch2DefaultSucc ) );
				}
				return succs;
			}
		}
		
		// get exceptional successors for throw nodes only
/*		if ( node1.getLastInstructionIndex() > 0 ) {
			if ( node1.getLastInstruction() instanceof SSAThrowInstruction ) {
				succs1.addAll( cfg1.getExceptionalSuccessors( node1 ) );
			}
		}
		if ( node2.getLastInstructionIndex() > 0 ) {
			if ( node2.getLastInstruction() instanceof SSAThrowInstruction ) {
				succs2.addAll( cfg2.getExceptionalSuccessors( node2 ) );
			}
		}
		*/

		// return each pair of successors
		for ( ISSABasicBlock succ1 : cfg1.getNormalSuccessors( node1 ) ) {
			for ( ISSABasicBlock succ2 : cfg2.getNormalSuccessors( node2 ) ) {
				succs.add( new Pair<ISSABasicBlock>( succ1, succ2 ) );
			}
		}
		return succs;
	}
	
	private int getInstructionCountForNode( ISSABasicBlock node ) {
		int count = 0;
		for ( SSAInstruction inst : node ) {
			count++;
		}
		return count;
	}
		
	public IMethod getFirstMethod() {
		return methodInfo1.getMethod();
	}
	
	public Collection<ISSABasicBlock> getMethodOneNodes() {
		return v1Nodes;
	}
	
	public MethodInfo getMethodOneInfo() {
		return methodInfo1;
	}

	public IMethod getSecondMethod() {
		return methodInfo2.getMethod();
	}
	
	public MethodInfo getMethodTwoInfo() {
		return methodInfo2;
	}
	
	public Collection<ISSABasicBlock> getMethodTwoNodes() {
		return v2Nodes;
	}
	
	public boolean isMethodModified() {
/*		if ( v1UnmatchedNodes.size() > 0 || v2UnmatchedNodes.size() > 0 ) {
			return true;
		}
		return false;
*/		return ( firstDifferingNodePair != null );
	}
	
	public Pair<ISSABasicBlock> getDifferingNodePair() {
		return firstDifferingNodePair;
	}
	
	public Collection<Pair<ISSABasicBlock>> getMatchedNodePairs() {
		return matchedNodes;
	}	

	public Collection<ISSABasicBlock> getMethodOneUnmatchedNodes() {
		return v1UnmatchedNodes;
	}

	public Collection<ISSABasicBlock> getMethodTwoUnmatchedNodes() {
		return v2UnmatchedNodes;
	}
}
