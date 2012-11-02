public void testNullField() throws Exception {
  try {
    new FieldAttributes(Foo.class, null);
    fail("Field parameter can not be null");
  } catch (NullPointerException expected) { }
}
