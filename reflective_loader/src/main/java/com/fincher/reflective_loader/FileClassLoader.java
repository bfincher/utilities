package com.fincher.reflective_loader;

import java.io.File;
import java.net.URI;

/** Loads classes from a file system
 * 
 * @author Brian Fincher
 *
 */
class FileClassLoader {
	
	private static FileFilter classFilter = new FileFilter();
	
	static {
		classFilter.addFileExtension("class");
		classFilter.setAcceptDirectories(true);
	}
	
	/** Loads classes from a file system
	 * 
	 * @param parent The parent loader
	 * @param uri The parent directory
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static void load(ReflectiveLoader parent, URI uri, String baseJavaPackage) 
	throws ReflectionException {
		
		baseJavaPackage = baseJavaPackage.replace('.', File.separatorChar);
		File baseDir = new File(uri).getParentFile();
		int idx = baseDir.getPath().indexOf(baseJavaPackage);
		String parentDir = baseDir.getPath().substring(0, idx- 1);
		
		loadFileEntries(parent, baseDir, parentDir);
	}
	
	/** Loads classes from a file system
	 * 
	 * @param parent The parent loader
	 * @param dir The directory from which to load files
	 * @param parentDir The parent directory name which will be stripped off to produce the class name
	 */
	private static void loadFileEntries(ReflectiveLoader parent, File dir, String parentDir) 
	throws ReflectionException {
		File[] files = dir.listFiles(classFilter);
		
		for (File file: files) {
			if (file.isDirectory())
				loadFileEntries(parent, file, parentDir);
			else {
				String fileName = file.getPath();
				int idx = fileName.indexOf(parentDir);
				String className = fileName.substring(idx + parentDir.length() + 1);
				className = className.replace(File.separatorChar, '.').replace(".class", "");
				
				parent.loadClass(className);				
			}
		}
	}
}
