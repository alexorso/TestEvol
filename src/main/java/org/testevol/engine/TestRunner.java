package org.testevol.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.AssertionFailedError;
import junit.framework.TestSuite;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Test.None;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.testevol.engine.TestResult.TestOutcome;
import org.testevol.engine.util.TrexClassLoader;
import org.testevol.engine.util.Utils;

public class TestRunner {
	
	private static Set<String> executedTests = null;
	private static Set<String> testsToIgnore = null;
	private static Set<String> onlyTests = null;
	
	private static final String SET_UP_METHOD = "setUp";
	private static final String TEAR_DOWN_METHOD = "tearDown";

	private static TrexClassLoader trexClsLoader;

	private static ExecutorService executor = Executors.newFixedThreadPool(1);

	public static void init(Set<String> ignoredTests, Set<String> onlyTests){
		executedTests = new HashSet<String>();
		TestRunner.testsToIgnore = ignoredTests;
		TestRunner.onlyTests = onlyTests;
	}
	
	public static boolean alreadyExecuted(String className){
		return executedTests.contains(className);
	}
	
	// Returns an empty map in case of a "compilation error"
	// Returns null in the case of abstract classes
	public static Map<String, TestResult> runTests(String className) {
		HashMap<String, TestResult> results = new HashMap<String, TestResult>();
		Class<?> klass = null;

		Object testObj = null;
		try {
			if (trexClsLoader != null) {
				TrexClassLoader localTrexClsLoader = new TrexClassLoader(
						trexClsLoader.getURLs());
				Thread.currentThread()
						.setContextClassLoader(localTrexClsLoader);
				klass = localTrexClsLoader.loadClass(className);
			} else {
				klass = Class.forName(className);
			}
			testObj = createInstance(klass);
			if (testObj == null) {
				return null;
			}
			klass.getDeclaredMethods();//force an exception
			klass.getMethods();//force an exception	
		} catch (ClassNotFoundException e) {
			// e.printStackTrace();
			// TODO: Check that it can only be a compilation error
			results = logError(className, e, klass, results);
			return results;
		} catch (IllegalAccessException e) {
			// TODO: Check that it can only be a compilation error
			// e.printStackTrace();
			results = logError(className, e, klass, results);
			return results;
		} catch (NoSuchMethodException e) {
			// e.printStackTrace();
			// TODO: Check that it can only be a compilation error
			results = logError(className, e, klass, results);
			return results;
		} catch (InstantiationException e) {
			// e.printStackTrace();
			// TODO: Check that it can only be a compilation error
			results = logError(className, e, klass, results);
			return results;
		} catch (InvocationTargetException e) {
			// e.printStackTrace();
			// TODO: Check that it can only be a compilation error
			results = logError(className, e, klass, results);
			return results;
		} catch (NoClassDefFoundError e) {
			// e.printStackTrace();
			// TODO: Check that it can only be a compilation error
			results = logError(className, e, klass, results);
			return results;
		} catch (Throwable e) {
			// e.printStackTrace();
			// TODO: Check that it can only be a compilation error
			results = logError(className, e, klass, results);
			return results;
		}

		Method beforeClassMethod = null;
		Method afterClassMethod = null;
		for (Method m : klass.getMethods()) {
			if (m.isAnnotationPresent(AfterClass.class)) {
				afterClassMethod = m;
			} else if (m.isAnnotationPresent(BeforeClass.class)) {
				beforeClassMethod = m;
			}
		}

		try {
			// invoke the setup method
			if (beforeClassMethod != null) {
				beforeClassMethod.setAccessible(true);
				beforeClassMethod.invoke(null);
			}

			List<Method> testMethods = getTestMethods(klass);
			if (!testMethods.isEmpty()) {
				for (Method method : testMethods) {
					TestResult res = execMethod(method, klass, createInstance(klass), results);
					if (res != null) {
						results.put(
								Utils.getCanonicalMethodSignature(method),
								res);
					}
				}
			}
			else{
				return null;
//				if(testObj instanceof TestCase){
//					try {
//						Method suite = testObj.getClass().getDeclaredMethod("suite");
//						junit.framework.Test test = (junit.framework.Test) suite.invoke(testObj);
//						if(test instanceof TestSuite){
//							execTestSuite((TestSuite)test, results);
//						}
//						if(results.isEmpty()){
//							return null;
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//						//IGNORE
//					}
//				}

			}
					
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			t.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			// invoke the setup method
			if (afterClassMethod != null) {
				afterClassMethod.setAccessible(true);
				try {
					afterClassMethod.invoke(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		executedTests.add(className);	
		return results;
	}

	public static void execTestSuite(TestSuite testSuite, HashMap<String, TestResult> results){
		Enumeration<junit.framework.Test> tests = testSuite.tests(); 
		while(tests.hasMoreElements()){
			junit.framework.Test localTest = tests.nextElement();
			if(localTest instanceof TestSuite){
				execTestSuite((TestSuite)localTest, results);
			}
			else{
				String className = localTest.getClass().getName();
				if(executedTests.contains(className)){
					continue;
				}
				List<Method> testMethods = getTestMethods(localTest.getClass());
				for(Method method:testMethods){
					TestResult res = execMethod(method, localTest.getClass(), localTest, results);
					if (res != null) {
						results.put(
								Utils.getCanonicalMethodSignature(method),
								res);
					}
				}
				executedTests.add(localTest.getClass().getName());
			}
		}
	}
	
	private static int column = 0;
	private static void feedback(Method m) {
		Utils.print(".");
		if(column == 80){
			Utils.println("");
			column = 0;
		}
		else{
				
			column++;
		}
	}
	
	
	public static TestResult execMethod(final Method method,final Class<?> clazz, final Object obj, HashMap<String, TestResult> results){		
		feedback(method);
		TestResult res= null;
		try {
			if (isTimeoutEnabled()) {
				FutureTask<TestResult> execMethodTask = new FutureTask<TestResult>(
						new Callable<TestResult>() {
							@Override
							public TestResult call()
									throws Exception {
								TestResult res = runTest(clazz,method,obj);
								return res;
							}
						});
				executor.submit(execMethodTask);
				res = execMethodTask.get(30, TimeUnit.SECONDS);
			} else {
				res = runTest(clazz, method, obj);
			}
		} catch (InterruptedException e) {
			res = new TestResult(TestOutcome.RUNTIME_ERROR, e);
		} catch (ExecutionException e) {
			res = new TestResult(TestOutcome.RUNTIME_ERROR, e);
		} catch (TimeoutException e) {
			res = new TestResult(TestOutcome.RUNTIME_ERROR, e);
		} catch (Exception ex) {
			// This two exception may be throw by the call method
			// inside Callable from FutureTask
			// COMPILATION ERROR
			if (ex instanceof IllegalAccessException
					|| ex instanceof InvocationTargetException) {
				if(results != null){
					logError(method, ex, results);					
				}
			}
		}
		
		return res;
	}
	
	public static List<Method> getTestMethods(Class klass) {
		List<Method> methods = new ArrayList<Method>();
		for (Method method : klass.getMethods()) {
			String fullyQualifidName = klass.getName() + "." + method.getName();
			if (Utils.isIgnoredMethod(method)) {
				continue;
			}
			if (!Utils.isTestMethod(method)) {
				continue;
			}
			
			if(onlyTests != null && !onlyTests.isEmpty() &&
					!onlyTests.contains(fullyQualifidName)){
				continue;
			}
			if(testsToIgnore != null && !testsToIgnore.isEmpty() &&
					testsToIgnore.contains(fullyQualifidName)){
				continue;
			}
			methods.add(method);
		}
		return methods;
	}

	public static HashMap<String, TestResult> logError(String className,
			Throwable e, Class<?> klass, HashMap<String, TestResult> results) {
		System.err.println("====== test class <" + className + " beg> ======");
		System.err.println("");
		System.err.println("[treated as a compilation error]");
		System.err.println("");
		e.printStackTrace();
		if (klass != null) {
			try {
				for (Method method : klass.getDeclaredMethods()) {
					if (Utils.isIgnoredMethod(method)) {
						results.put(Utils.getCanonicalMethodSignature(method),
								new TestResult(TestOutcome.IGNORE, null));
					} else if (Utils.isTestMethod(method)) {
						results.put(
								Utils.getCanonicalMethodSignature(method),
								new TestResult(TestOutcome.COMPILATION_ERROR, e));
					}
				}
			} catch (NoClassDefFoundError ex) {
				System.err.println("======< additional error beg>======");
				ex.printStackTrace();
				System.err.println("======< additional error end>======");
			} catch (VerifyError ex) {
				System.err.println("======< additional error beg>======");
				ex.printStackTrace();
				System.err.println("======< additional error end>======");
			} catch (IncompatibleClassChangeError e2) {
				System.err.println("======< additional error beg>======");
				e2.printStackTrace();
				System.err.println("======< additional error end>======");
			}

			System.err.println("==================<" + className
					+ " end>======");
		}
		return results;
	}

	public static HashMap<String, TestResult> logError(Method method,
			Throwable e, HashMap<String, TestResult> results) {
		System.err.println("====== test case <" + method + " beg>======");
		System.err.println("");
		System.err.println("[treated as a compilation error]");
		System.err.println("");
		e.printStackTrace();
		System.err.println("==================<" + method + " end>======");
		results.put(Utils.getCanonicalMethodSignature(method), new TestResult(
				TestOutcome.COMPILATION_ERROR, e));
		return results;
	}

	// public static TestResult runTest(String className, String methodName)
	// throws ClassNotFoundException, IllegalAccessException,
	// InstantiationException, InvocationTargetException,
	// NoSuchMethodException {
	// Class<?> klass = Class.forName(className);
	// Object testObj = createInstance(klass);
	// if (testObj == null) {
	// return null;
	// }
	// for (Method method : klass.getDeclaredMethods()) {
	// if (method.getName().equals(methodName)) {
	// return runTest(klass, method, testObj);
	// }
	// }
	// throw new RuntimeException("Method " + methodName
	// + " not found in Class " + className);
	// }

	public static Object createInstance(Class<?> cls)
			throws ClassNotFoundException, IllegalAccessException,
			NoSuchMethodException, InstantiationException,
			InvocationTargetException {
		// check whether the test class is abstract
		if (Modifier.isAbstract(cls.getModifiers()) || !Modifier.isPublic(cls.getModifiers()) || isInnerClass(cls) || isIgnoredClass(cls)) {
			return null;
		}

		Object testObj = null;
		try {
			if(cls.isAnnotationPresent(RunWith.class)){
				RunWith runWith = cls.getAnnotation(RunWith.class);
				Class runner = runWith.value();
				if(Parameterized.class.isAssignableFrom(runner)){
					testObj = createFromParameterized(cls);
					if(testObj != null){
						return testObj;
					}
				}
			}

			testObj = cls.newInstance(); // try the nullary test case
											// constructor
		} catch (Exception ie) {
			// nullary constructor does not exist
			// try the constructor with the string parameter
			Class<?>[] strArgTypes = new Class[] { Class
					.forName("java.lang.String") };
			Constructor<?> strParamConstr;
			try {
				strParamConstr = cls.getConstructor(strArgTypes);
				Object[] strArgs = new Object[] { cls.getName() };
				testObj = strParamConstr.newInstance(strArgs);
			} catch (SecurityException e) {
				// Ignore...
			} catch (NoSuchMethodException e) {
				// Ignore...
			}

		}

		return testObj;
	}
	
	public static boolean isIgnoredClass(Class cls){
		return cls.isAnnotationPresent(Ignore.class);
	}
	
	private static Object createFromParameterized(Class<?> cls) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		List<Method> methods = getParameterizedMethods(cls);
		if(methods.isEmpty()){
			return null;
		}	
		
		Method getParameters = methods.get(0);
		Collection<Object[]> parametersList = (Collection<Object[]>) getParameters.invoke(null);
		if(!parametersList.isEmpty() && parametersList.iterator().hasNext()){
			Object[] parameters = parametersList.iterator().next();//Take the first one
			Class[] paramTypes = new Class[parameters.length];
			for (int i = 0; i < parameters.length; i++) {
				paramTypes[i] = parameters[i].getClass();
			}
			try {
				Constructor constructor = getConstructor(cls, paramTypes);
				if(constructor != null){
					return constructor.newInstance(parameters);					
				}

			} catch (Exception e) {e.printStackTrace();}
		}
		return null;
		
	}
	
	private static Constructor getConstructor(Class cls, Class[] types){
		for(Constructor c:cls.getConstructors()){
			if(c.getParameterTypes().length == types.length){
				for (int i = 0; i < types.length; i++) {
					if(!c.getParameterTypes()[i].isAssignableFrom(types[i])){
						break;
					}
				}
				return c;
			}
		}
		return null;
	}

	private static boolean isInnerClass(Class clazz) {
		return clazz.getEnclosingClass() != null;
	}

	/**
	 * Initializes the class loader to be used for loading test and application
	 * classes
	 * 
	 * @param urls
	 */
	public static void initClassLoader(URL[] urls) {
		trexClsLoader = new TrexClassLoader(urls);
	}

	/**
	 * Adds a jar to the current class loader
	 * 
	 * @param url
	 * @throws Exception
	 */
	public static void addJarToClassLoader(URL url) throws Exception {
		trexClsLoader.addJar(url);
	}

	/**
	 * Creates new (test) class loader that is a child of the current thread's
	 * context class loader. Should be called before starting a test run for a
	 * version pair. DOES NOT WORK
	 * 
	 * @param jarURLs
	 */
	/*
	 * public static void createClassLoader( URL[] jarURLs ) { ClassLoader
	 * currThreadClsLoader = Thread.currentThread().getContextClassLoader();
	 * URLClassLoader urlClsLoader = new URLClassLoader( jarURLs,
	 * currThreadClsLoader ); Thread.currentThread().setContextClassLoader(
	 * urlClsLoader ); }
	 */
	/**
	 * Resets the current thread's context class loader to the parent of that
	 * loader. This should be called after a test run for a version pair is
	 * over, so that for the next test run, the created test class loader would
	 * be a child of the original class loader and no the previously created
	 * test class loader. DOES NOT WORK
	 */
	/*
	 * public static void resetClassLoader() { ClassLoader
	 * currThreadParentClsLoader =
	 * Thread.currentThread().getContextClassLoader(); if (
	 * currThreadParentClsLoader != null ) {
	 * Thread.currentThread().setContextClassLoader(
	 * currThreadParentClsLoader.getParent() ); } }
	 */
	public static TestResult runTest(Class<?> klass, Method method, Object testObj)
			throws IllegalAccessException, InvocationTargetException {
		// check whether setup and teardown methods exist

		List<Method> setUpMethods = getSetUpMethods(klass);
		List<Method> tearDownMethods = getTearDownMethods(klass);
		try {
			Class expectedException = null;
			try {
				// invoke the setup method
				if (!setUpMethods.isEmpty()) {
					for (Method setUpMethod : setUpMethods) {
						setUpMethod.setAccessible(true);
						setUpMethod.invoke(testObj);
					}
				}
				if (method.isAnnotationPresent(Test.class)) {
					Test testAnnotation = method.getAnnotation(Test.class);
					expectedException = testAnnotation.expected();
					if (expectedException.equals(None.class)) {
						expectedException = null;
					}
				}

				method.setAccessible(true);
				method.invoke(testObj);

			} catch (InvocationTargetException invTgtExcp) {
				boolean rethrowException = true;
				Throwable cause = invTgtExcp.getCause();
				// Expected exception
				if (expectedException != null
						&& expectedException.isAssignableFrom(cause.getClass())) {
					rethrowException = false;
				}
				if (rethrowException) {
					throw cause;
				}
			}
		} catch (AssertionFailedError afe) {
			afe.printStackTrace();
			return new TestResult(TestOutcome.ASSERT_FAILURE, afe);
		} catch (NoSuchMethodError nme) {
			nme.printStackTrace();
			return new TestResult(TestOutcome.COMPILATION_ERROR, nme);
		}catch (NoSuchFieldError nfe) {
			nfe.printStackTrace();
			return new TestResult(TestOutcome.COMPILATION_ERROR, nfe);
		} catch (NoClassDefFoundError ncde) {
			ncde.printStackTrace();
			return new TestResult(TestOutcome.COMPILATION_ERROR, ncde);
		} catch (IllegalAccessError illegalAccessException) {
			illegalAccessException.printStackTrace();
			return new TestResult(TestOutcome.COMPILATION_ERROR, illegalAccessException);
		}
		catch (Throwable t) {
			t.printStackTrace();
			return new TestResult(TestOutcome.RUNTIME_ERROR, t);
		} finally {
			// invoke the teardown method
			try {
				if (testObj != null && tearDownMethods != null) {
					// invoke the setup method
					if (!tearDownMethods.isEmpty()) {
						for (int i = tearDownMethods.size() - 1; i >= 0; i--) {
							tearDownMethods.get(i).setAccessible(true);
							tearDownMethods.get(i).invoke(testObj);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return new TestResult(TestOutcome.PASS, null);
	}
	
	public static List<Method> getParameterizedMethods(Class clazz) {
		List<Method> methods = new ArrayList<Method>();
		getMethods(clazz, null, Parameters.class, methods);
		return methods;
	}

	public static List<Method> getSetUpMethods(Class clazz) {
		List<Method> methods = new ArrayList<Method>();
		getMethods(clazz, SET_UP_METHOD, Before.class, methods);
		return methods;
	}

	public static List<Method> getTearDownMethods(Class clazz) {
		List<Method> methods = new ArrayList<Method>();
		getMethods(clazz, TEAR_DOWN_METHOD, After.class, methods);
		return methods;
	}

	private static void getMethods(Class clazz, String methodName,
			Class<? extends java.lang.annotation.Annotation> annotation,
			List<Method> methods) {
		if (clazz.getSuperclass() != null
				&& !clazz.getSuperclass().equals(Object.class)) {
			getMethods(clazz.getSuperclass(), methodName, annotation, methods);
		}

		try {
			if(methodName != null){
				Method method = clazz.getDeclaredMethod(methodName, new Class[0]);
				if(!contains(methods,method)){
					methods.add(method);	
				}				
			}
		} catch (Exception e1) {
			// does not exists wit
		} catch (Throwable e1) {
			//
		}
		
		try {
			for (Method m : clazz.getMethods()) {
				if (m.isAnnotationPresent(annotation) && !contains(methods,m)) {
					methods.add(m);
				}
			}
		} catch (Exception e1) {
			// do nothing
		} catch (Throwable e1) {
			// do nothing
		}

	}
	
	private static boolean contains(List<Method> methods, Method method){
		for(Method m:methods){
			if(m.getName().equals(method.getName())){
				return true;
			}
		}
		return false;
	}

	// private static List<Method> getMethods(Class clazz, String methodName,
	// Class<? extends java.lang.annotation.Annotation> annotation){
	// if(clazz == null || clazz.equals(Object.class)){
	// return new ArrayList<Method>();
	// }
	// List<Method> methods=new ArrayList<Method>();
	// try {
	// methods.add(clazz.getDeclaredMethod(methodName, new Class[0]));
	// return methods;
	// } catch (Exception e1) {
	// //does not exists with setUpName
	// }
	// for (Method m : clazz.getMethods()) {
	// if (m.isAnnotationPresent(annotation)) {
	// methods.add(m);
	// }
	// }
	// if(methods.isEmpty()) {
	// methods = getMethods(clazz.getSuperclass(), methodName, annotation);
	// }
	// return methods;
	// }

	public static boolean isTimeoutEnabled() {
		String timeout = System.getProperty("timeout");
		return timeout != null && Boolean.valueOf(timeout);
	}

}
