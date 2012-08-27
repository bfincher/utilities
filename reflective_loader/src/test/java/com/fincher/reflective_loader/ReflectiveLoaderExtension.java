package com.fincher.reflective_loader;

public class ReflectiveLoaderExtension extends ReflectiveLoader {
	
	private final String classToLoad;
	public Class<?> loadedClass;
	
	public ReflectiveLoaderExtension(Class<?> representativeClass) throws ReflectionException {
		super(representativeClass);
		this.classToLoad = representativeClass.getName();
		load();		
	}
	
	protected void loadClass(String className) throws ReflectionException {
		if (className.equals(classToLoad)) {
			try {
				loadedClass = Class.forName(className);
			} catch (ClassNotFoundException cnfe) {
				throw new ReflectionException(cnfe);
			}
		}
	}

}
