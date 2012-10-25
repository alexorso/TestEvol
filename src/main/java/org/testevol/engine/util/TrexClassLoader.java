package org.testevol.engine.util;


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.junit.Ignore;
import org.junit.Test;

public class TrexClassLoader extends URLClassLoader {

	private HashMap<String, Class<?>> loadedClasses;
	
	public TrexClassLoader( URL[] jarURLs ) {
		super( jarURLs );
		loadedClasses = new HashMap<String, Class<?>>();
	}

	public void addJar( URL url ) throws MalformedURLException {
		addURL( url );
	}

	public boolean isTestClass( String className ) throws ClassNotFoundException {

		Class<?> cls = findOrLoadClass( className );
		
		if(isTestClass("org.junit.runners.JUnit4", className)){
			return true;
		}
		if(isTestClass("org.junit.internal.runners.JUnit38ClassRunner", className)){
			return true;
		}
		return false;
	}

	private boolean isTestClass(String JUnitRunner, String className){
		try {
			Class<?> cls = findOrLoadClass( className );
			Class<?> clsJUnit = findOrLoadClass(JUnitRunner);
			Constructor c = clsJUnit.getConstructor(cls.getClass());
			Object o = c.newInstance(cls);
			int testCount = (Integer) o.getClass().getMethod("testCount").invoke(o);
			return testCount > 0;
		} catch (Exception e) {
			//e.printStackTrace();
			//It is not a test case!
		}
		return false;
	}
	
	public Collection<String> getAllTestcases( String className ) throws ClassNotFoundException {
		ArrayList<String> testMethods = new ArrayList<String>();
		Class<?> cls = findOrLoadClass( className );
		for ( Method method : cls.getMethods() ) {
			if ( ( method.getName().startsWith( "test" ) || method.isAnnotationPresent( Test.class ) ) &&
					!method.isAnnotationPresent( Ignore.class ) ) {
				testMethods.add( Utils.getCanonicalMethodSignature( method ) );
			}
		}
		return testMethods;
	}
	
	private Class<?> findOrLoadClass( String className ) throws ClassNotFoundException {
		Class<?> cls = loadedClasses.get( className );
		if ( cls == null ) {
			cls = loadClass( className );
			loadedClasses.put( className, cls );
		}
		return cls;
	}
	
	/**
	 * Helps to retrive all the class names
	 * @param url
	 * @return
	 */
	public Set<String> getAllClassNames( URL url ) {
		Set<String> classNamesSet = new HashSet<String>();
		
		try {
			//load URL
			addURL(url);		
			JarURLConnection conn = (JarURLConnection) url.openConnection();
			JarFile jarFile = conn.getJarFile();
			
			Enumeration<JarEntry> entries = jarFile.entries();
			while(entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String entryName = entry.getName();
				if(entryName.endsWith(".class")) {
					String className = entryName.substring(0, entryName.length()- 6).replace('/', '.');
					classNamesSet.add(className);
				}
			}		
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		return classNamesSet;
	}
}
