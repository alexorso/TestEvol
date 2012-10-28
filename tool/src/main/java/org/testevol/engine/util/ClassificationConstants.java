package org.testevol.engine.util;

import java.util.HashMap;

public class ClassificationConstants {
	
	/** Map from a assert method selector to the parameter index of the expected value.
	 *  The index is -1 for those calls that do not have an expected-value parameter. */
	public static final HashMap<String, Integer> junitAssertMethods = new HashMap<String, Integer>();
	static {
		junitAssertMethods.put( "assertEquals(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", Integer.valueOf( 2 ) );
		junitAssertMethods.put( "assertEquals(Ljava/lang/Object;Ljava/lang/Object;)V", Integer.valueOf( 1 ) );
		junitAssertMethods.put( "assertEquals(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", Integer.valueOf( 2 ) );
		junitAssertMethods.put( "assertEquals(Ljava/lang/String;Ljava/lang/String;)V", Integer.valueOf( 1 ) );
		junitAssertMethods.put( "assertEquals(Ljava/lang/String;DDD)V", Integer.valueOf( 2 ) );
		junitAssertMethods.put( "assertEquals(DDD)V", Integer.valueOf( 1 ) );
		junitAssertMethods.put( "assertEquals(Ljava/lang/String;FFF)V", Integer.valueOf( 2 ) );
		junitAssertMethods.put( "assertEquals(FFF)V", Integer.valueOf( 1 ) );
		junitAssertMethods.put( "assertEquals(Ljava/lang/String;JJ)V", Integer.valueOf( 2 ) );
		junitAssertMethods.put( "assertEquals(JJ)V", Integer.valueOf( 1 ) );
		junitAssertMethods.put( "assertEquals(Ljava/lang/String;ZZ)V", Integer.valueOf( 2 ) );
		junitAssertMethods.put( "assertEquals(ZZ)V", Integer.valueOf( 1 ) );
		junitAssertMethods.put( "assertEquals(Ljava/lang/String;BB)V", Integer.valueOf( 2 ) );
		junitAssertMethods.put( "assertEquals(BB)V", Integer.valueOf( 1 ) );
		junitAssertMethods.put( "assertEquals(Ljava/lang/String;CC)V", Integer.valueOf( 2 ) );
		junitAssertMethods.put( "assertEquals(CC)V", Integer.valueOf( 1 ) );
		junitAssertMethods.put( "assertEquals(Ljava/lang/String;SS)V", Integer.valueOf( 2 ) );
		junitAssertMethods.put( "assertEquals(SS)V", Integer.valueOf( 1 ) );
		junitAssertMethods.put( "assertEquals(Ljava/lang/String;II)V", Integer.valueOf( 2 ) );
		junitAssertMethods.put( "assertEquals(II)V", Integer.valueOf( 1 ) );
		junitAssertMethods.put( "assertFalse(Ljava/lang/String;Z)V", Integer.valueOf( -1 ) );
		junitAssertMethods.put( "assertFalse(Z)V", Integer.valueOf( -1 ) );
		junitAssertMethods.put( "assertNotSame(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", Integer.valueOf( 2 ) );
		junitAssertMethods.put( "assertNotSame(Ljava/lang/Object;Ljava/lang/Object;)V", Integer.valueOf( 1 ) );
		junitAssertMethods.put( "assertNotNull(Ljava/lang/Object;)V", Integer.valueOf( -1 ) );
		junitAssertMethods.put( "assertNotNull(Ljava/lang/String;Ljava/lang/Object;)V", Integer.valueOf( -1 ) );
		junitAssertMethods.put( "assertNull(Ljava/lang/Object;)V", Integer.valueOf( -1 ) );
		junitAssertMethods.put( "assertNull(Ljava/lang/String;Ljava/lang/Object;)V", Integer.valueOf( -1 ) );
		junitAssertMethods.put( "assertSame(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", Integer.valueOf( 2 ) );
		junitAssertMethods.put( "assertSame(Ljava/lang/Object;Ljava/lang/Object;)V", Integer.valueOf( 1 ) );
		junitAssertMethods.put( "assertTrue(Ljava/lang/String;Z)V", Integer.valueOf( -1 ) );
		junitAssertMethods.put( "assertTrue(Z)V", Integer.valueOf( -1 ) );
		junitAssertMethods.put( "fail(Ljava/lang/String;)V", Integer.valueOf( -1 ) );
		junitAssertMethods.put( "fail()V", Integer.valueOf( -1 ) );
		junitAssertMethods.put( "failNotEquals(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", Integer.valueOf( 2 ) );
		junitAssertMethods.put( "failNotSame(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", Integer.valueOf( 2 ) );
		junitAssertMethods.put( "failSame(Ljava/lang/String;)V", Integer.valueOf( -1 ) );
	};
	
	public static final int TESTMOD_DELETE = -2;
	public static final int TESTMOD_IGNORE = -1;
	/** A modification to a test can be any combination of the following. These can be tested for using bit-wise OR */
	public static final int TESTMOD_ASSERT_ADD = 0x0001;
	public static final int TESTMOD_ASSERT_DEL = 0x0002;
	public static final int TESTMOD_ASSERT_MOD = 0x0004;
	public static final int TESTMOD_ASSERT_EXPVAL_MOD = 0x0008;
	public static final int TESTMOD_METHOD_CALL_ADD = 0x0010;
	public static final int TESTMOD_METHOD_CALL_DEL = 0x0020;
	public static final int TESTMOD_METHOD_CALL_MOD = 0x0040;
	public static final int TESTMOD_METHOD_CALL_PARAM_ADD = 0x0080;
	public static final int TESTMOD_METHOD_CALL_PARAM_DEL = 0x0100;
	public static final int TESTMOD_DATA_FLOW_MOD = 0x0200;
	public static final int TESTMOD_CONTROL_FLOW_MOD = 0x0400;

}
