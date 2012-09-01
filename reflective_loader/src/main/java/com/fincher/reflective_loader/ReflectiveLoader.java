package com.fincher.reflective_loader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

/** Use Java Reflection to load classes from the classpath
 * 
 * @author Brian Fincher
 *
 */
public abstract class ReflectiveLoader {	
	
	private static final Logger LOGGER = Logger.getLogger(ReflectiveLoader.class);

	private final String baseJavaPackage;	

	private final ClassLoader classloader;	
	
	private final Class<?> representativeClass;

	/** Construct a new ReflectiveLoader
	 * 
	 * @param baseJavaPackage The base directory (in dot notation) from which classes should be loaded
	 * @param classloader The classloader used to find resources from the classpath
	 */
	public ReflectiveLoader(Class<?> representativeClass) {
		this.baseJavaPackage = representativeClass.getPackage().getName();
		this.classloader = representativeClass.getClassLoader();
		this.representativeClass = representativeClass;
	}

	/** Load classes
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	protected void load() throws ReflectionException {		
		try {
			String repClassAsPath = representativeClass.getName().replace('.', '/') + ".class";
			URI uri = classloader.getResource(repClassAsPath).toURI();
//			URI uri = classloader.getResource(baseJavaPackage.replace('.', File.separatorChar)).toURI();
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("uri = " + uri);
			}

			int idx = uri.toString().indexOf(".jar!");
			if (idx != -1) {
				JarClassLoader.load(this, uri, baseJavaPackage);					
			}
			else {
				FileClassLoader.load(this, uri, baseJavaPackage);
			}
		} catch (Exception e) {
			throw new ReflectionException(e);
		}
	}			

	/** Notify the implementor that a class has been found
	 * 
	 * @param className The name of the class
	 */
	protected abstract void loadClass(String className)
			throws ReflectionException;

}
