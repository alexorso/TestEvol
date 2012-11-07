package org.testevol.engine.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.testevol.engine.TestRunner;

public class Utils {
	private static boolean diagnosticOn = false;

	private static PrintStream out = System.out;

	private static PrintStream err = System.err;

	public static void println(String string) {
		if (diagnosticOn) {
			out.println(string);
		}
	}

	public static void print(String string) {
		if (diagnosticOn) {
			out.print(string);
		}
	}

	public static HashSet<File> getMatchingFiles(String dir, String filter) {
		return getMatchingFiles(new File(dir), filter);
	}

	public static HashSet<File> getMatchingFiles(File dir, String filter) {
		HashSet<File> filesList = new HashSet<File>();
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("No such directory " + dir);
		}
		for (File file : dir.listFiles()) {
			if (file.getName().matches(filter)) {
				filesList.add(file);
			}
		}
		return filesList;
	}

	public static HashSet<File> getMatchingFilesRecursively(String dir,
			String filter) {
		return getMatchingFilesRecursively(new File(dir), filter);
	}

	public static HashSet<File> getMatchingFilesRecursively(File dir,
			String filter) {
		HashSet<File> filesList = new HashSet<File>();
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("No such directory " + dir);
		}
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				filesList.addAll(getMatchingFilesRecursively(file, filter));
			} else {
				if (file.getName().matches(filter)) {
					filesList.add(file);
				}
			}
		}
		return filesList;
	}

	public static String getFileContentAsString(File cpfile) {
		FileReader in = null;
		try {
			in = new FileReader(cpfile);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		char[] buffer = new char[128];
		int len;
		String content = "";
		try {
			while ((len = in.read(buffer)) != -1) {
				content += new String(buffer, 0, len);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content.trim();
	}

	public static void removeAndCreate(File file) {
		if (file.exists()) {
			removeFilesRecursively(file);
		}
		assert !file.exists();
		file.mkdir();
	}

	public static void removeFilesRecursively(String file) {
		removeFilesRecursively(new File(file));
	}

	public static void removeFilesRecursively(File file) {
		if (!file.exists()) {
			return;
		}
		if (!file.isFile() && file.listFiles().length != 0) {
			for (File del : file.listFiles()) {
				removeFilesRecursively(del);
			}
		}
		file.delete();
	}

	public static String makePathsAbsolute(String string, String absDir) {
		StringTokenizer st = new StringTokenizer(string, File.pathSeparator);
		if (st.countTokens() == 0) {
			return null;
		}
		String result = null;
		try {
			result = new File(absDir + File.separator + st.nextToken())
					.getCanonicalPath();
			while (st.hasMoreTokens()) {
				result = result
						+ File.pathSeparator
						+ new File(absDir + File.separator + st.nextToken())
								.getCanonicalPath();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	private static HashMap<String, String> primitiveNameMap;
	static {
		primitiveNameMap = new HashMap<String, String>();
		primitiveNameMap.put("boolean", "Z");
		primitiveNameMap.put("byte", "B");
		primitiveNameMap.put("char", "C");
		primitiveNameMap.put("double", "D");
		primitiveNameMap.put("float", "F");
		primitiveNameMap.put("int", "I");
		primitiveNameMap.put("long", "J");
		primitiveNameMap.put("short", "S");
		primitiveNameMap.put("void", "V");
	}

	public static String getCanonicalTypeName(Class<?> cls) {
		if (primitiveNameMap.containsKey(cls.getName())) {
			return primitiveNameMap.get(cls.getName());
		}
		String name = "L" + cls.getCanonicalName() + ";";
		return name.replaceAll("\\.", "/");
	}

	public static String getCanonicalMethodSignature(Method method) {
		StringBuffer buf = new StringBuffer();
		buf.append(method.getDeclaringClass().getName());
		buf.append(".");
		buf.append(method.getName());
		buf.append("(");
		for (Class<?> cls : method.getParameterTypes()) {
			buf.append(getCanonicalTypeName(cls));
		}
		buf.append(")");
		buf.append(getCanonicalTypeName(method.getReturnType()));
		return buf.toString();
	}

	public static void sortTextFile(File file) {
		LineNumberReader in;
		FileWriter out;
		try {
			in = new LineNumberReader(new FileReader(file));
			String line;
			HashSet<String> lines = new HashSet<String>();
			while ((line = in.readLine()) != null) {
				lines.add(line);
				// Utils.println(line);
			}
			in.close();
			Utils.removeFilesRecursively(file);
			out = new FileWriter(file);
			String[] strLines = lines.toArray(new String[0]);
			Arrays.sort(strLines);
			for (String sortedLine : strLines) {
				out.write(sortedLine + "\n");
			}
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean isTestMethod(Method method, TrexClassLoader classLoader) {
		
		// Ignore the method that receive parameters
		if(method.getParameterTypes().length > 0){
			return false;
		}
		return classLoader.isTestMethod(method);
	}

	public static boolean isIgnoredMethod(Method method, TrexClassLoader classLoader) {
		return classLoader.isAnnotationPresent(method, TestRunner.IgnoreAnnotation);
	}

	public static HashSet<URL> getClassPathFromString(String classpath) {
		HashSet<URL> cpurls = new HashSet<URL>();
		// Create a set of classpath URLs to be used later
		StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
		while (st.hasMoreTokens()) {
			try {
				cpurls.add(new File(st.nextToken()).toURI().toURL());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
		}
		return cpurls;
	}

	public static void addToJar(JarOutputStream target, String pathInsideJar,
			File fentry) throws IOException {
		JarEntry entry = new JarEntry(pathInsideJar);
		entry.setTime(fentry.lastModified());
		target.putNextEntry(entry);

		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				fentry));
		byte[] buffer = new byte[1024];
		while (true) {
			int count = in.read(buffer);
			if (count == -1) {
				break;
			}
			target.write(buffer, 0, count);
		}
		target.closeEntry();
		in.close();
	}

	public static void unzip(File destDir, InputStream is) {
		final int BUFFER = 2048;
		try {
			BufferedOutputStream dest = null;
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				//System.out.println("Extracting: " + entry);

				File destinationFilePath = new File(destDir, entry.getName());
				// create directories if required.
				destinationFilePath.getParentFile().mkdirs();
				
				if (!entry.isDirectory()) {
					int count;
					byte data[] = new byte[BUFFER];
					// write the files to the disk
					FileOutputStream fos = new FileOutputStream(
							destinationFilePath);
					dest = new BufferedOutputStream(fos, BUFFER);
					while ((count = zis.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();
				}
			}
			zis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static File getTempDir(){
		return getTempDir(null);
	}
	
	public static File getTempDir(File baseDir){
		File dir = null;
		while(dir == null){
			if(baseDir != null){
				dir = new File(baseDir, UUID.randomUUID().toString());	
			}
			else{
				dir = new File(UUID.randomUUID().toString());				
			}
			if(dir.exists()){
				dir = null;
			}			
		}
		return dir;
	}
}
