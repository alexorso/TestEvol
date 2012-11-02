public void testDiscardSemicolons() throws Throwable {
   Tokenizer t = new JavaTokenizer();
   SourceCode sourceCode = new SourceCode("1");
   String data = "public class Foo {private int x;}";
   Tokens tokens = new Tokens();
   t.tokenize(sourceCode, tokens, new StringReader(data));  // Broken statement
   assertEquals(9, tokens.size());
}
