package org.testevol.engine.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sourceforge.cobertura.coveragedata.ProjectData;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.sonatype.guice.bean.containers.Main;
import org.testevol.engine.TestRunner;
import org.testevol.engine.domain.coverage.Coverage;
import org.testevol.engine.util.TrexClassLoader;
import org.testevol.engine.util.Utils;

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
	private static ExecutorService executor = Executors.newFixedThreadPool(1);
	
	
	public static void main(String[] args) throws Exception {
		TestCoverageDriver.generateCoverageReport("cobertura.ser", "teste", "xml", new ArrayList<String>());
	}

	public static final String TEMP_DIR = "CoberturaTemp";
	
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
											List<String> testJarFile, 
											Set<String> inclTestCases, 
											Set<String> exclTestCases,
											String destination,
											boolean instrument)
	{			
		try
		{

			//Instrument the files using cobertura. Don't specify destination. Source files will be overwritten
			List<String> dirsToInstrument = new ArrayList<String>();

			System.out.print("Caution: source jar files ");
			for (int i = 0; i < srcJarFiles.size(); i++) {
					File jarFile = new File(srcJarFiles.get(i));
					FileUtils.copyFile(jarFile, new File(jarFile.getParent(), jarFile.getName()+".NotInstrumented.jar"));
					System.out.print(srcJarFiles.get(i) + ", ");					
					dirsToInstrument.add(srcJarFiles.get(i));
			}
			System.out.println(" will be instrumented!!!");

			if(instrument){
				TestCoverageDriver.instrument(null, null, null, null, null, null, dirsToInstrument);				
			}
			
			//Run tests
			if(!runTests(srcJarFiles, classPathJars, testJarFile, inclTestCases, exclTestCases))
			{
				System.err.println("Some tests not executed, coverage result may not be complete!!!");
			}
			//Flush cobertura data
			ProjectData.saveGlobalProjectData();
			
			//Generate report
			try
			{
				List<String> srcDirectories = new ArrayList<String>();
				for(String sourceDir : sourceDirs){
					srcDirectories.add(sourceDir);
				}
				
				File coverageXml = new File(destination,"coverage.xml");
				if(coverageXml.exists()){
					coverageXml.delete();
				}
				
				
				TestCoverageDriver.generateCoverageReport("cobertura.ser", destination, "xml", srcDirectories);
				
				coverageXml = new File(destination,"coverage.xml");
								
				Coverage coverage = Coverage.getInstance(new FileInputStream(new File(destination, "coverage.xml")));
				return coverage;
			}
			catch(Exception ex)
			{
				System.err.println("Exception occurred while generating report!!!");
				ex.printStackTrace();
				return null;
			}
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
	
	private static boolean runTests(List<String> srcJarFiles, 
									List<String> classPathJars, 
									List<String> testJarFiles, 
									Set<String> inclTestCases, 
									Set<String> exclTestCases) 
	{
		try
		{
			//Loading all URLs including all from class paths as well
			List<URL> allUrls = new ArrayList<URL>();
			for(String srcJarFile : srcJarFiles)
			{
				URL url = new URL( "jar:file:"+ srcJarFile +"!/" );
				allUrls.add(url);
			}
			
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
			
		
			TestRunner.init(exclTestCases, inclTestCases);
			TestRunner.initClassLoader(urlsToLoad);
			
			TrexClassLoader clsLoader = new TrexClassLoader(urlsToLoad);			
			
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
					clsLoader = new TrexClassLoader(urlsToLoad);
					Thread.currentThread().setContextClassLoader(clsLoader);
					TestRunner.runTests(className);
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
	 * Runs selected tests
	 * @param className
	 * @param inclTests
	 * @param exclTests
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 */
	public static void runSelectedTests(	TrexClassLoader clsLoader,
															String className, 
															Set<String> inclTests,
															Set<String> exclTests )
			throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		
		final Class<?> klass = clsLoader.loadClass(className);
		if(klass.isInterface() || Modifier.isAbstract( klass.getModifiers() ))
			return;
				
        Method beforeClassMethod = null;
        Method afterClassMethod = null;
        for (Method m : klass.getMethods()) {
            if (m.isAnnotationPresent(AfterClass.class)) {
            	afterClassMethod = m;
            } else if (m.isAnnotationPresent(BeforeClass.class)) {
            	beforeClassMethod = m;
            }
        }
        // invoke the setup method
        if (beforeClassMethod != null) {
        	beforeClassMethod.setAccessible(true);
        	beforeClassMethod.invoke(null);
        }
        
		for (final Method method : klass.getDeclaredMethods() ) {			
			if ( Utils.isIgnoredMethod( method ) ) {
				continue;
			}
			else if ( !Utils.isTestMethod( method ) ) {
				continue;
			}			
			
			String methodName = method.getName();
			String fullyQualifidName = klass.getName() + "." + methodName;
			
			if(exclTests != null && exclTests.contains(fullyQualifidName))
				continue;

			if(inclTests == null || inclTests.contains(fullyQualifidName)) {
				Utils.println("\n**** Running test for coverage:"+fullyQualifidName+ " ****\n");	
				TestRunner.execMethod(method, klass, TestRunner.createInstance(klass), null);
			}
		}
		// invoke the setup method
        if (afterClassMethod != null) {
        	afterClassMethod.setAccessible(true);
        	afterClassMethod.invoke(null);
        }
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
		
	public static void instrument(String basedir, String datafile, String destination, List<String> ignore,
            List<String> includeClasses, List<String> excludeClasses, List<String> instrument) {
		
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
            args.add(datafile);
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
        Main.main(args.toArray(new String[args.size()]));
    }
	
	

    private static void generateCoverageReport(String datafile, String destination, String format,
            List<String> sourceDirectories) throws Exception {
        List<String> args = new ArrayList<String>();
        args.add("--datafile");
        args.add(datafile);
        args.add("--format");
        args.add(format);
        args.add("--destination");
        args.add(destination);
        args.addAll(sourceDirectories);
        net.sourceforge.cobertura.reporting.Main.main(args.toArray(new String[args.size()]));
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
    
    public static void CoberturaMain(String args[]) throws IOException
	{
		//Instrumenting
		List<String> dirsToInstrument = new ArrayList<String>();
		dirsToInstrument.add("C:\\Suresh\\eclipse-rcp-galileo-SR2-win32\\eclipse\\workspace\\TestNew\\bin");
		TestCoverageDriver.instrument(null, null, 
				"C:\\Suresh\\eclipse-rcp-galileo-SR2-win32\\eclipse\\workspace\\TestNew\\instrumented", 
				null, null, null, dirsToInstrument);
		
		//Running
		try
		{
			String line;
			Process p = Runtime.getRuntime().exec("java -cp " +
					"C:\\Suresh\\Softwares\\cobertura-1.9.4.1-bin\\cobertura-1.9.4.1\\cobertura.jar;" +
					"C:\\Suresh\\eclipse-rcp-galileo-SR2-win32\\eclipse\\workspace\\TestNew\\instrumented;" +
					"C:\\Suresh\\eclipse-rcp-galileo-SR2-win32\\eclipse\\workspace\\TestNew\\bin;" +
					"C:\\Suresh\\Softwares\\junit\\junit-4.9.jar temp.Main");	
			 BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			 while ((line = input.readLine()) != null) {
			     System.out.println(line);
			 }
			 input.close();
		}
		catch(IOException ex)
		{
			System.err.println("Failed to execute the program!!!");
			ex.printStackTrace();
			return;
		}
		
		//Reporting
		try
		{
			List<String> srcDirectories = new ArrayList<String>();
			srcDirectories.add("C:\\Suresh\\eclipse-rcp-galileo-SR2-win32\\eclipse\\workspace\\TestNew\\src");
			TestCoverageDriver.generateCoverageReport("cobertura.ser", 
					"C:\\Suresh\\eclipse-rcp-galileo-SR2-win32\\eclipse\\workspace\\TestNew\\instrumented\\report\\xml", 
					"xml", 
					srcDirectories);
			
			TestCoverageDriver.generateCoverageReport("cobertura.ser", 
					"C:\\Suresh\\eclipse-rcp-galileo-SR2-win32\\eclipse\\workspace\\TestNew\\instrumented\\report\\html", 
					"html", 
					srcDirectories);
		}
		catch(Exception ex)
		{
			System.err.println("Exception occurred while generating report!!!");
			ex.printStackTrace();
		}
	}
}
