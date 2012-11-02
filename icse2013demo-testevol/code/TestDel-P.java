public void testConstructorMessage(){
  String msg = "message";
  MatrixIndexException ex = new MatrixIndexException(msg);
  assertEquals(msg, ex.getMessage());
}
