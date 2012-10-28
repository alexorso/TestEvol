package org.testevol.engine.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.testevol.engine.TestRunner;
import org.testevol.engine.domain.coverage.Coverage;
import org.testevol.engine.util.TrexClassLoader;

/**
 * Computes test coverage based using cobertura open source tool
 * @author Suresh Thummalapenta
 * 
 * revised date 24th Jan 2012 Rahul Pandita
 * 1. Fixed classpath related issue
 * 2. coverage related issue
 * 
 * TBD
 * 1. See what's the problem with OSX environment.
 * 2. Test with Oracle JRE Tested with Open JRE for ubuntu.
 */
public class TestCoverageDriver 
{	
	
	public static String COVERAGE_JARS = "cobertura";
	
	/**
	 * Instruments given jar file and executes test cases and outputs the coverage.
	 * Make "exclTestCases" and "inclTestCases" as null for including all test cases
	 * Make "exclTestCases" as null and "inclTestCases" as non-null to include only specific test cases
	 * Make "inclTestCases" as null and "exclTestCases" as non-null to exclude specific test cases and execute rest 
	 * Both Sets require fully-qualified names of the methods, including their class and package names
	 * 
	 * @param instrumentJar indicated wich jars need to be instrumented
	 * @param srcJarFile
	 * @param testJarFile
	 * @param excludeTestCases
	 */
	public static Coverage computeCoverage(	List<String> srcJarFiles,
												String sourceDirs[], 
												List<String> classPathJars, 
												List<String> testJarFiles, 
												Set<String> inclTestCases, 
												Set<String> exclTestCases,
												String destination,
												boolean instrument)
	{			
		try
		{

			//Instrument the files using cobertura. Don't specify destination. Source files will be overwritten
			List<String> dirsToInstrument = new ArrayList<String>();
			for (int i = 0; i < srcJarFiles.size(); i++) {
					File jarFile = new File(srcJarFiles.get(i));
					FileUtils.copyFile(jarFile, new File(jarFile.getParent(), jarFile.getName()+".NotInstrumented.jar"));
					dirsToInstrument.add(srcJarFiles.get(i));
			}			
			
			//Loading all URLs including all from class paths as well
			List<URL> allUrls = new ArrayList<URL>();
			if(classPathJars != null)
			{
				for(String classPathJar : classPathJars)
				{
					URL url = new URL( "jar:file:"+ classPathJar +"!/" );
					allUrls.add(url);
				}
			}
			
			for(String testJarFile:testJarFiles){
				URL testJarURL = new URL("jar:file:"+ testJarFile +"!/");
				allUrls.add(testJarURL);				
			}
				
			URL[] urlsToLoad = new URL[allUrls.size()];
			allUrls.toArray(urlsToLoad);
			
			TrexClassLoader clsLoader = new TrexClassLoader(urlsToLoad);

			File cobertura_ser = new File(destination, "cobertura.ser");
			
			if(instrument){
				TestCoverageDriver.instrument(null, cobertura_ser, null, null, null, null, dirsToInstrument, clsLoader);				
			}

			for(String srcJarFile : srcJarFiles)
			{
				URL url = new URL( "jar:file:"+ srcJarFile +"!/" );
				clsLoader.addJar(url);
			}
			
			//Run tests
			if(!runTests(testJarFiles, inclTestCases, exclTestCases, clsLoader))
			{
				System.err.println("Some tests not executed, coverage result may not be complete!!!");
			}

			Class ProjectData = clsLoader.findOrLoadClass("net.sourceforge.cobertura.coveragedata.ProjectData");
			ProjectData.getMethod("saveGlobalProjectDataFromFile", File.class).invoke(null,cobertura_ser);

			List<String> srcDirectories = new ArrayList<String>();
			for(String sourceDir : sourceDirs){
				srcDirectories.add(sourceDir);
			}
			
			File coverageXml = new File(destination,"coverage.xml");
			if(coverageXml.exists()){
				coverageXml.delete();
			}
			TestCoverageDriver.generateCoverageReport(cobertura_ser, destination, "xml", srcDirectories, clsLoader);
			coverageXml = new File(destination,"coverage.xml");
			Coverage coverage = Coverage.getInstance(new FileInputStream(new File(destination, "coverage.xml")));
			return coverage;
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally{
			try{
				for (int i = 0; i < srcJarFiles.size(); i++) {
					File jarFile = new File(srcJarFiles.get(i));
					jarFile.delete();
					File notIntrumentedJar = new File(jarFile.getParent(), jarFile.getName()+".NotInstrumented.jar");
					FileUtils.copyFile(notIntrumentedJar, jarFile);
					notIntrumentedJar.delete();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	private static boolean runTests( 
									List<String> testJarFiles, 
									Set<String> inclTestCases, 
									Set<String> exclTestCases,
									TrexClassLoader clsLoader) 
	{
		try
		{
			TestRunner testRunner = new TestRunner(clsLoader, exclTestCases, inclTestCases);
			List<String> classList = new ArrayList<String>();
			
			//If inclTestCases is defined we only need to put the classes
			//where those test cases are defined
			if(inclTestCases != null && !inclTestCases.isEmpty()){
				Set<String> classSet = new HashSet<String>();//use a set, as different test can be at the same class 
				for(String test:inclTestCases){
					String className = test.substring(0, test.lastIndexOf("."));
					classSet.add(className);
				}
				classList.addAll(classSet);
			}
        	for(String testJarFile:testJarFiles){
				URL testJarURL = new URL("jar:file:"+ testJarFile +"!/");
				//Getting all classes in the test jars
				Set<String> allClasses = clsLoader.getAllClassNames(testJarURL);
				if(allClasses == null)
				{
					System.err.println("Failed to load classes from testJarFile");
					return false;
				}
				classList.addAll(allClasses);
			}
            
            for(String className : classList)
			{
				try
				{
					testRunner.runTests(className);
				} catch (Exception e) {
					System.err.println("Failed to run tests of class: " + className);
				} 
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Executes a command line program
	 * @param command
	 * @return
	 */
	public static boolean executeCommandLineProgram(String command)
	{
		//Running
		try
		{
			String line;
			Process p = Runtime.getRuntime().exec(command);	
			 BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			 while ((line = input.readLine()) != null) {
			     System.out.println(line);
			 }
			 input.close();
		}
		catch(IOException ex)
		{
			System.err.println("Failed to execute the command " + command);
			ex.printStackTrace();
			return false;
		}
		
		return true;
	}
		
	public static void instrument(String basedir, File datafile, String destination, List<String> ignore,
            List<String> includeClasses, List<String> excludeClasses, List<String> instrument, TrexClassLoader classLoader) throws ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		List<String> args = new ArrayList<String>();
        /*
         * cobertura will ignore excludes if there are no includes specified, so
         * if excludes have been specified but includes haven't, put a default
         * include in the list
         */
        if (excludeClasses != null && excludeClasses.size() > 0 && (includeClasses == null || includeClasses.size() == 0)) {
            includeClasses = new ArrayList<String>(1);
            includeClasses.add(".*");
        }
        if (basedir != null && !basedir.equals("")) {
            args.add("--basedir");
            args.add(basedir);
        }
        if (datafile != null && !datafile.equals("")) {
            args.add("--datafile");
            args.add(datafile.getAbsolutePath());
        }
        if (destination != null && !destination.equals("")) {
            args.add("--destination");
            args.add(destination);
        }
        if (ignore != null) {
            for (String s : ignore) {
                args.add("--ignore");
                args.add(s);
            }
        }
        if (includeClasses != null) {
            for (String s : includeClasses) {
                args.add("--includeClasses");
                args.add(s);
            }
        }
        if (excludeClasses != null) {
            for (String s : excludeClasses) {
                args.add("--excludeClasses");
                args.add(s);
            }
        }
        args.addAll(instrument);
        String[] argsArray = args.toArray(new String[args.size()]);
        main(classLoader, "net.sourceforge.cobertura.instrument.Main", argsArray);
    }
	
	private static void main(TrexClassLoader clsLoader, String className, String[] params) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
        Class<?> cls = clsLoader.loadClass(className);
        Method meth = cls.getMethod("main", String[].class);
        meth.invoke(null, (Object) params);
	}
	

    private static void generateCoverageReport(	File datafile, 
    												String destination,
    												String format,
    												List<String> sourceDirectories,
    												TrexClassLoader classLoader) throws Exception {
        List<String> args = new ArrayList<String>();
        args.add("--datafile");
        args.add(datafile.getAbsolutePath());
        args.add("--format");
        args.add(format);
        args.add("--destination");
        args.add(destination);
        args.addAll(sourceDirectories);
        
        String[] argsArray = args.toArray(new String[args.size()]);
        main(classLoader, "net.sourceforge.cobertura.reporting.Main", argsArray);
    }
    
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
    
//    public static void CoberturaMain(String args[]) throws IOException
//	{
//		//Instrumenting
//		List<String> dirsToInstrument = new ArrayList<String>();
//		dirsToInstrument.add("C:\\Suresh\\eclipse-rcp-galileo-SR2-win32\\eclipse\\workspace\\TestNew\\bin");
//		TestCoverageDriver.instrument(null, null, 
//				"C:\\Suresh\\eclipse-rcp-galileo-SR2-win32\\eclipse\\workspace\\TestNew\\instrumented", 
//				null, null, null, dirsToInstrument);
//		
//		//Running
//		try
//		{
//			String line;
//			Process p = Runtime.getRuntime().exec("java -cp " +
//					"C:\\Suresh\\Softwares\\cobertura-1.9.4.1-bin\\cobertura-1.9.4.1\\cobertura.jar;" +
//					"C:\\Suresh\\eclipse-rcp-galileo-SR2-win32\\eclipse\\workspace\\TestNew\\instrumented;" +
//					"C:\\Suresh\\eclipse-rcp-galileo-SR2-win32\\eclipse\\workspace\\TestNew\\bin;" +
//					"C:\\Suresh\\Softwares\\junit\\junit-4.9.jar temp.Main");	
//			 BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
//			 while ((line = input.readLine()) != null) {
//			     System.out.println(line);
//			 }
//			 input.close();
//		}
//		catch(IOException ex)
//		{
//			System.err.println("Failed to execute the program!!!");
//			ex.printStackTrace();
//			return;
//		}
//		
//		//Reporting
//		try
//		{
//			List<String> srcDirectories = new ArrayList<String>();
//			srcDirectories.add("C:\\Suresh\\eclipse-rcp-galileo-SR2-win32\\eclipse\\workspace\\TestNew\\src");
//			TestCoverageDriver.generateCoverageReport("cobertura.ser", 
//					"C:\\Suresh\\eclipse-rcp-galileo-SR2-win32\\eclipse\\workspace\\TestNew\\instrumented\\report\\xml", 
//					"xml", 
//					srcDirectories);
//			
//			TestCoverageDriver.generateCoverageReport("cobertura.ser", 
//					"C:\\Suresh\\eclipse-rcp-galileo-SR2-win32\\eclipse\\workspace\\TestNew\\instrumented\\report\\html", 
//					"html", 
//					srcDirectories);
//		}
//		catch(Exception ex)
//		{
//			System.err.println("Exception occurred while generating report!!!");
//			ex.printStackTrace();
//		}
//	}
}
