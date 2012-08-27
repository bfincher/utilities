package com.fincher.htpasswd;

import java.io.IOException;

import org.junit.Test;

public class HtpasswdShaTest extends AbstractHtpasswdTest {
	
	@Test
	public void test() throws IOException {
		HtpasswdSha htpasswd = new HtpasswdSha();
		test(htpasswd);
	}

}
