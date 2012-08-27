package com.fincher.htpasswd;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

public abstract class AbstractHtpasswdTest {
	
	public void test(AbstractHtpasswd htpasswd) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("bfincher", htpasswd.cryptPassword("bfincher", "passwd1"));
		map.put("otherUser", htpasswd.cryptPassword("otherUser", "passwd2"));
		
		File tempFile = new File("tempFile");
		tempFile.deleteOnExit();
		
		htpasswd.writePasswordFile(tempFile, map);
		map = htpasswd.readPasswordFile(tempFile);
		
		Assert.assertEquals(2, map.size());
		Assert.assertTrue(map.containsKey("bfincher"));
		Assert.assertTrue(map.containsKey("otherUser"));
		Assert.assertTrue(htpasswd.verifyPassword("bfincher", "passwd1", map.get("bfincher")));
		Assert.assertTrue(htpasswd.verifyPassword("otherUser", "passwd2", map.get("otherUser")));
	}

}
