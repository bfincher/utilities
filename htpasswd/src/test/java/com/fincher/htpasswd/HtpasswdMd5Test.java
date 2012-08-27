package com.fincher.htpasswd;

import java.io.IOException;

import org.junit.Test;

public class HtpasswdMd5Test extends AbstractHtpasswdTest {
	
	@Test
	public void test() throws IOException {
		HtpasswdMd5 htpasswd = new HtpasswdMd5("realm");
		test(htpasswd);
	}

}
