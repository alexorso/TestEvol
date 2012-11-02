public void testGetMinY() {
  TimeSeries s1 = new TimeSeries("S1");
  assertTrue(Double.isNaN(s1.getMinY()));
  s1.add(new Year(2008), 1.1);
  assertEquals(1.1, s1.getMinY(), EPSILON);
  s1.add(new Year(2009), 2.2);
  assertEquals(1.1, s1.getMinY(), EPSILON);
  s1.add(new Year(2002), -1.1);
  assertEquals(-1.1, s1.getMinY(), EPSILON);
}
