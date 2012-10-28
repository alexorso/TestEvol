package org.testevol.engine;

import com.ibm.wala.ssa.SSAInvokeInstruction;

public class AssertInstruction {
	
	private String assertSel;
	private SSAInvokeInstruction invInst;
	
	public AssertInstruction( SSAInvokeInstruction invInst ) {
		this.invInst = invInst;
		this.assertSel = invInst.getDeclaredTarget().getSelector().toString();
	}

	public String getAssertSelector() {
		return assertSel;
	}

	public SSAInvokeInstruction getInvInst() {
		return invInst;
	}

}
