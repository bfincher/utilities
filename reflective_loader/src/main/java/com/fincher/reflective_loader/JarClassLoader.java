package com.fincher.reflective_loader;

import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Loads classes from a jar file
 * 
 * @author Brian Fincher
 *
 */
class JarClassLoader {

	/** Loads classes from the jar file
	 * 
	 * @param parent The parent loader
	 * @param uri The URI of the jar file
	 * @param baseJavaPackage The base directory (in dot notation) from which classes should be loaded
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException,
	 * @throws IllegalAccessException
	 */
	public static void load(ReflectiveLoader parent, URI uri, String baseJavaPackage) 
			throws ReflectionException {

		try {
			baseJavaPackage = baseJavaPackage.replace('.', '/');
			int idx = uri.toString().indexOf(".jar!");
			String jarFileName = uri.toString().substring(9, idx + 4);
			jarFileName = jarFileName.replace("%20"," ");

			JarFile jarFile = null;
			try {
				jarFile = new JarFile(jarFileName);

				Enumeration<JarEntry> files = jarFile.entries();

				while (files.hasMoreElements()) {
					JarEntry entry = files.nextElement();
					if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
//						String className = entry.getName().replace(File.separatorChar, '.');
						String className = entry.getName();
						className = className.substring(0, className.length() - 6);
						if (className.startsWith(baseJavaPackage)) {
							className = className.replace('/', '.');
							parent.loadClass(className);
						}
					}
				}
			} finally {
				if (jarFile != null) {
					jarFile.close();
				}
			}
		} catch (IOException ioe) {
			throw new ReflectionException(ioe);
		}
	}

}
