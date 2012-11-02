public void testParameter(){
  MatrixIndexException ex = 
	new MatrixIndexException(INDEX_OUT_OF_RANGE, 12, 0, 5);
  assertEquals(12, ex.getArguments()[0]);
  assertEquals(0,  ex.getArguments()[1]);
  assertEquals(5,  ex.getArguments()[2]);
}
