import java.io.File;

import org.testevol.domain.Version;



public class Test {

	@org.junit.Test
	public void test() {
		try {
			Version version = new Version(new File("/home/leandro/TestEvol/tool/tmp/projects/Google-gson/v.1.0/"));
			version.setUp(new File("/home/leandro/TestEvol/tool/config/"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
