public void testDistance() {
  Vector3D v1 = new Vector3D(1, -2, 3);
  Vector3D v2 = new Vector3D(-4, 2, 0);
  assertEquals(0.0,Vector3D.distance(Vector3D.MINUS_I, Vector3D.MINUS_I), 0);
  assertEquals(FastMath.sqrt(50), Vector3D.distance(v1, v2), 1.0e-12);
  assertEquals(v1.subtract(v2).getNorm(), Vector3D.distance(v1, v2), 1.0e-12);
}
