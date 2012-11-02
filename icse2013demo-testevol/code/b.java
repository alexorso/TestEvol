public void testDiscardSemicolons() throws Throwable {
   Tokenizer t = new JavaTokenizer();
   SourceCode sourceCode = new SourceCode("1");
   String data = "public class Foo {private int x;}";
   Tokens tokens = new Tokens();
   sourceCode.readSource(new StringReader(data));  // Added statement
   t.tokenize(sourceCode, tokens);                 // Modified statement
   assertEquals(9, tokens.size());
}
