import java.io.File;

import org.testevol.domain.Version;



public class Test {

	@org.junit.Test
	public void test() {
		try {
			Version version = new Version(new File("/home/leandro/Documents/Atlanta/workspace/testevol-web/testevol-web/tool/tmp/projects/Gson/v.1.0"));
			version.setUp(new File("/home/leandro/Documents/Atlanta/workspace/testevol-web/testevol-web/tool/config"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
