public void testArrayOfStrings() {
  String[] target = {"Hello", "World"};
  assertEquals("[\"Hello\",\"World\"]", gson.toJson(target));
}
