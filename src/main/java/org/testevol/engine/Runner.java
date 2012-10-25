package org.testevol.engine;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.testevol.domain.Version;
import org.testevol.engine.TestResult.TestOutcome;
import org.testevol.engine.util.TrexClassLoader;
import org.testevol.engine.util.Utils;

public class Runner extends Task {



    public Runner(List<org.testevol.domain.Version> versions) {
		super(versions);
		// TODO Auto-generated constructor stub
	}

	@Override
    protected String[] getGeneratedFiles() {
        String files[] = { "emptytrace.txt", "data-testslist-skipped-1.txt",
                "data-testslist-skipped-2.txt", "data-testout-*.txt",
                "data-trace-curr-*.txt" };
        return files;
    }

    @Override
    public boolean go(boolean force) throws Exception {
        if (!super.go(force)) {
            return false;
        }
        cleanUp();

        Version oldVersion = null;

        nextVersion: for (Version version : versions) {
        	//Version version = new Version(verDir);
        	
            if (oldVersion == null) {
                Utils.println("Running first version: " + version.getName());
            } else {
                Utils.println("\nRunning pair " + oldVersion.getName() + "-" + version.getName());
            }

            List<String> versionList = new ArrayList<String>();
            HashMap<String, String> versionToClasspathMap = new HashMap<String, String>();

            String classpath = null;
            
            //File binT1P1 = version.getFile("binT1P1");
            //Utils.removeAndCreate(binT1P1);
            //FileUtils.copyDirectory(version.getBinTestDir(), binT1P1);
            //FileUtils.copyDirectory(version.getBinDir(), binT1P1);
            
            // T1P1
            classpath = version.getClassPath();
            classpath += File.pathSeparator + version.getBinTestDir().getAbsolutePath();
            classpath += File.pathSeparator + version.getBinDir().getAbsolutePath();
            //classpath += File.pathSeparator + binT1P1.getAbsolutePath();
            
            versionToClasspathMap.put("T1P1", classpath);
            versionList.add("T1P1");
            
            // Consider the older version only if we are not processing the
            // first version
            if (oldVersion != null) {
                // T0P1
                classpath = Utils.getClassPathInDir(version.getDirectory());
                classpath += File.pathSeparator + oldVersion.getBinTestDir();
                classpath += File.pathSeparator + version.getBinDir();
                versionToClasspathMap.put("T0P1", classpath);
                versionList.add("T0P1");
                // alternateClasspath = this.buildAlternateClasspath(classpath,
                // verDir, oldDir);
                // T1P0
                classpath = Utils.getClassPathInDir(oldVersion.getDirectory());
                classpath += File.pathSeparator + version.getBinTestDir();
                classpath += File.pathSeparator + oldVersion.getBinDir();
                versionToClasspathMap.put("T1P0", classpath);
                versionList.add("T1P0");
                // alternateClasspath = this.buildAlternateClasspath(classpath,
                // oldDir, verDir);
            }

            File buildDir = version.getBuildDir();
            
            for (String versions : versionList) {
                // Utils.println("Processing " + versions);
                // Utils.println("Classpath "
                // + versionToClasspathMap.get(versions));
            	
                HashSet<URL> cpurls = Utils.getClassPathFromString(versionToClasspathMap.get(versions));
                TestRunner.initClassLoader(cpurls.toArray(new URL[0]));
                TrexClassLoader c = new TrexClassLoader(cpurls.toArray(new URL[0]));
                
                HashMap<TestOutcome, FileWriter> outcomeToWritersMap = new HashMap<TestOutcome, FileWriter>();
                for (TestOutcome testOutcome : TestOutcome.values()) {
                    FileWriter fw = new FileWriter(new File(buildDir,
                            "data-testout-" + testOutcome + "-" + versions
                                    + ".txt"));
                    outcomeToWritersMap.put(testOutcome, fw);
                }

                PrintStream out = new PrintStream(new File(buildDir,"data-testlog-out." + versions + ".txt"));
                PrintStream err = new PrintStream(new File(buildDir,"data-testlog-err." + versions + ".txt"));
                PrintStream origout = System.out;
                PrintStream origerr = System.err;
                System.setOut(out);
                System.setErr(err);

                try {
                    Version versionAux = null;
                    if (versions.matches("T0.*")) {
                        // Utils.println("Running for T0: " + versions);
                        //basedir = oldVersion.getBuildDir();
                        versionAux = oldVersion;
                    } else if (versions.matches("T1.*")) {
                    	// Utils.println("Running for T1: " + versions);
                        //basedir = version.getBuildDir();
                    	versionAux = version;
                    } else {
                        assert false;
                    }
                    
                    TestRunner.init(null, null);
                    List<String> classList = new ArrayList<String>();

                    String classPath = versionAux.getBinTestDir().getCanonicalPath();
                    int index = classPath.length() + 1;
                    for (File fentry : Utils.getMatchingFilesRecursively(versionAux.getBinTestDir(), ".*class$")) {
                        String absolutePath = fentry.getCanonicalPath();
                        String relativePath = absolutePath.substring(index);
                        String testClassName = relativePath.substring(0,
                                relativePath.lastIndexOf('.'))
                                .replace('/', '.');
                        classList.add(testClassName);
                	}
                    
                    for (String testClassName:classList) {                       
                        if(TestRunner.alreadyExecuted(testClassName)){
                        	continue;
                        }
                        Map<String, TestResult> testResults = TestRunner.runTests(testClassName);

                        if (testResults == null) {
                            System.out.println("\nSkipping class: "+ testClassName);
                            continue;
                        } else if (testResults.isEmpty()) {
                            outcomeToWritersMap.get(TestOutcome.COMPILATION_ERROR).write(testClassName + ".*\n");
                        } else {
                            for (String testMethod : testResults.keySet()) {
                                TestResult res = testResults.get(testMethod);
                                TestOutcome outcome = res.getTestOutcome();
                                outcomeToWritersMap.get(outcome).write(
                                        testMethod + "\n");
                            }
                        }
                    }
                } finally {
                    for (TestOutcome testOutcome : TestOutcome.values()) {
                        FileWriter fw = outcomeToWritersMap.get(testOutcome);
                        fw.close();
                        Utils.sortTextFile(new File(buildDir, "data-testout-"+ testOutcome + "-" + versions + ".txt"));
                        out.close();
                        err.close();
                        System.setOut(origout);
                        System.setErr(origerr);
                    }
                }
                
                if(versions.equals("T1P1")){
                	File assertError = new File(buildDir, "data-testout-"+ TestOutcome.ASSERT_FAILURE + "-" + versions + ".txt");
                	File compilationError = new File(buildDir, "data-testout-"+ TestOutcome.COMPILATION_ERROR+ "-" + versions + ".txt");
                	File runtimeError = new File(buildDir, "data-testout-"+ TestOutcome.RUNTIME_ERROR + "-" + versions + ".txt");
                	
                	if(assertError.length() > 0 || compilationError.length() > 0 || runtimeError.length() > 0 ){
                		Utils.println("\nTests from version "+version.getName()+" CONTAIN ERRORS!!");
                		//continue nextVersion;
                		//return false;
                	}
                } 
            }
            oldVersion = version;
        }
        
        markAsRun();
        return oldVersion != null;
    }
}
