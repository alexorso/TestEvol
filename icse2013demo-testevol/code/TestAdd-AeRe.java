public void testZeroVectors() {
  assertEquals(0, new ArrayRealVector(new double[0]).getDimension());
  assertEquals(0, new ArrayRealVector(new double[0], true).getDimension());
  assertEquals(0, new ArrayRealVector(new double[0], false).getDimension());
}
