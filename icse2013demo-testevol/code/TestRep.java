public void testNullField() throws Exception {
  try {
    new FieldAttributes(null);
    fail("Field parameter can not be null");
  } catch (NullPointerException expected) { }
}
