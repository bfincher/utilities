package com.fincher.reflective_loader;

import junit.framework.Assert;

import org.junit.Test;

public class FileClassLoaderTest {
	
	@Test
	public void test() throws Exception {
		ReflectiveLoaderExtension instance = new ReflectiveLoaderExtension(FileFilter.class);
		Assert.assertEquals(FileFilter.class, instance.loadedClass);
	}

}
