package org.testevol.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;
import org.testevol.domain.Version;
import org.testevol.engine.domain.TestEvolLog;
import org.testevol.engine.util.TrexClassLoader;
import org.testevol.engine.util.Utils;

import com.sun.org.apache.xalan.internal.xsltc.compiler.CompilerException;

/**
 * @author orso
 * 
 */
public class Compiler extends Task {

	/**
	 * List expected to be ordered
	 */
    public Compiler(List<Version> versions, TestEvolLog log) {
        super(versions, log);
    }


    @Override
    public boolean go() throws Exception {
    	log.logStrong("Starting Compiler...");
    	for (Version version : versions) {
        	
        	if (!version.getSourceDir().exists()) {
        		log.logError("Source directory \""+version.getSourceDir()+"\" not found for version " + version.getName());
                throw new RuntimeException("Source directory \""+version.getSourceDir()+"\" not found for version " + version.getName());
            }            
                        
        	log.log("Compiling version "+version.getName());
            compileSrc(version);
            if(version.getTestsSourceDir().exists()){
                compileTests(version);            	
            }

            FileWriter testslist = new FileWriter(version.getTestslistFile());
            
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

            File targetdir = null;

            JarOutputStream target = null;
            JarOutputStream codejar = new JarOutputStream(new FileOutputStream(version.getCodeJar()), manifest);
            JarOutputStream testsjar = new JarOutputStream(new FileOutputStream(version.getTestsJar()), manifest);

            File binTmpDir = version.getBinTmpDir();
            File binTestTmpDir = version.getBinTestTmpDir();
           
            File srcResourcesDir = version.getSrcResourcesDir();
            File testResourcesDir = version.getTestResourcesDir();
            
            String binTmpDirCanonicalPath = binTmpDir.getCanonicalPath();
            int binTmpDirCanonicalPathLength = binTmpDirCanonicalPath.length();

            String binTestTmpDirCanonicalPath = binTestTmpDir.getCanonicalPath();
            int binTestTmpDirCanonicalPathLength = binTestTmpDirCanonicalPath.length();

            
            String classpath = version.getClassPath();
            HashSet<URL> cpurls = Utils.getClassPathFromString(classpath);
            TrexClassLoader tcl = new TrexClassLoader(cpurls.toArray(new URL[0]));
            tcl.addJar(binTmpDir.toURI().toURL());
            if(binTestTmpDir.exists()){
                tcl.addJar(binTestTmpDir.toURI().toURL());
            }
            if(srcResourcesDir.exists()){
                tcl.addJar(srcResourcesDir.toURI().toURL());
            }
            if(testResourcesDir.exists()){
                tcl.addJar(testResourcesDir.toURI().toURL());
            }

            for (File fentry : Utils.getMatchingFilesRecursively(binTmpDir, ".*class$")) {
                assert fentry.isFile();                
                String absolutePath = fentry.getCanonicalPath();
                //Cut from the entry path the the aprt related to the bin tmp dir
                String relativePath = absolutePath.substring(binTmpDirCanonicalPathLength + 1);
                
                String className = getClassName(relativePath);
                if (tcl.isTestClass(className)) {
                    target = testsjar;
                    targetdir = version.getBinTestDir();
                    for (String testcase : tcl.getAllTestcases(className)) {
                        testslist.write(testcase + "\n");
                    }
                } else {
                    target = codejar;
                    targetdir = version.getBinDir();
                }
                Utils.addToJar(target, relativePath, fentry);                
                File destDir = new File(targetdir, relativePath).getParentFile();
                FileUtils.copyFileToDirectory(fentry, destDir, true);
            }

            if(binTestTmpDir.exists()){
            	for (File fentry : Utils.getMatchingFilesRecursively(binTestTmpDir, ".*class$")) {
                    assert fentry.isFile();

                    String absolutePath = fentry.getCanonicalPath();
                    //Cut from the entry path the the part related to the bin tmp dir
                    String relativePath = absolutePath.substring(binTestTmpDirCanonicalPathLength + 1);                    

                    String className = getClassName(relativePath);
                    if (tcl.isTestClass(className)) {
                        for (String testcase : tcl.getAllTestcases(className)) {
                            testslist.write(testcase + "\n");
                        }
                    }

                    Utils.addToJar(testsjar, relativePath, fentry);
                    File destDir = new File(version.getBinTestDir(), relativePath).getParentFile();
                    FileUtils.copyFileToDirectory(fentry, destDir, true);
                }
            }
            
            if(srcResourcesDir.exists()){
                for (File fentry : Utils.getMatchingFilesRecursively(srcResourcesDir, ".*$")) {
                    assert fentry.isFile();

                    String absolutePath = fentry.getCanonicalPath();
                    //Cut from the entry path the the aprt related to the resources dir
                    String relativePath = absolutePath.substring(srcResourcesDir.getCanonicalPath().length() + 1);
                    Utils.addToJar(codejar, relativePath, fentry);

                    File destDir = new File(version.getBinDir(), relativePath).getParentFile();
                    FileUtils.copyFileToDirectory(fentry, destDir, true);
                }            	
            }
            
            if(testResourcesDir.exists()){
                for (File fentry : Utils.getMatchingFilesRecursively(testResourcesDir, ".*$")) {
                    assert fentry.isFile();

                    String absolutePath = fentry.getCanonicalPath();
                    //Cut from the entry path the the aprt related to the resources dir
                    String relativePath = absolutePath.substring(testResourcesDir.getCanonicalPath().length() + 1);
                    Utils.addToJar(testsjar, relativePath, fentry);

                    File destDir = new File(version.getBinTestDir(), relativePath).getParentFile();
                    FileUtils.copyFileToDirectory(fentry, destDir, true);
                }            	
            }
            
            codejar.close();
            testsjar.close();
            testslist.close();
            
            Utils.sortTextFile(version.getTestslistFile());
        }
        return true;
    }
    
    private void compileSrc(Version version) throws IOException, InterruptedException{
    	String classpath = version.getClassPath();
    	compile(version, version.getSourceDir(), version.getBinTmpDir(), classpath);
    }
    
    private void compileTests(Version version) throws IOException, InterruptedException{
    	String classpath = version.getClassPath();
    	classpath += File.pathSeparatorChar + version.getBinTmpDir().getAbsolutePath();
    	compile(version, version.getTestsSourceDir(), version.getBinTestTmpDir(), classpath);
    }
    
    private void compile(Version version, File srcDir, File destination, String classpath) throws IOException, InterruptedException{
    	


        List<String> args = new ArrayList<String>();
        args.add("javac");
        
        StringWriter compileCommand = new StringWriter();
        compileCommand.append("-g -nowarn -d ");
        args.add("-g");
        args.add("-nowarn");
        args.add("-d");
        args.add(destination.getAbsolutePath());
        args.add("-cp");
        args.add(classpath);
        
        compileCommand.append(destination.getAbsolutePath());
        compileCommand.append(" -cp ").append(classpath).append(" ");            
               
        String javaVersion = version.getJavaVersion();
        if (!javaVersion.isEmpty()) {
        	args.add("-source");
        	args.add(javaVersion);
        	args.add("-target");
        	args.add(javaVersion);
        	
        }
        
        HashSet<File> filesList = Utils.getMatchingFilesRecursively(srcDir, ".*java$");
        for (File file : filesList) {
            args.add(file.getAbsolutePath());
        }
        
        Process process = new ProcessBuilder(args).start();
        process.waitFor();
        
        String line;        
        
        if(process.exitValue() != 0){
        	log.logError("Error while compiling version "+version.getName());
        	InputStreamReader isr2 = new InputStreamReader(process.getErrorStream());
            BufferedReader br2 = new BufferedReader(isr2);
            while ((line = br2.readLine()) != null) {
                log.logError(line);
            }
        	throw new RuntimeException("Error compiling "+version.getName());
        }
        else{
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
        	while ((line = br.readLine()) != null) {
            	log.log(line);
            }
        }
    }
    
    private String getClassName(String relativePath) throws IOException{
        String className = relativePath.substring(0, relativePath.lastIndexOf('.')).replace('/', '.');        
        return className;
    }
    
}
