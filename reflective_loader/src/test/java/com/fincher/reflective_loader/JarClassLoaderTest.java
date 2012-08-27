package com.fincher.reflective_loader;

import junit.framework.Assert;

import org.junit.Test;

public class JarClassLoaderTest {
	
	@Test
	public void test() throws Exception {
		ReflectiveLoaderExtension instance = new ReflectiveLoaderExtension(org.hamcrest.core.IsNull.class);
		Assert.assertEquals(org.hamcrest.core.IsNull.class, instance.loadedClass);
	}

}
