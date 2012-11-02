public void test1900Previous() {
  Year current = new Year(1900);
  Year previous = (Year) current.previous();
  assertNull(previous);
}
