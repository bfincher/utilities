package com.fincher.htpasswd;

import java.util.StringTokenizer;


public class HtpasswdSha extends AbstractHtpasswd {
	
	public HtpasswdSha() {
		super(AlgorithmEnum.SHA1);
	}
	
	@Override
	public String cryptPassword(String username, String password) {
		return "{SHA}" + new sun.misc.BASE64Encoder().encode(getMessageDigest().digest(password.getBytes()));		
	}		
	
	@Override
	protected String parsePasswordFileEntryPassword(String passwordFileEntry) {
		StringTokenizer st = new StringTokenizer(passwordFileEntry, ":");
		st.nextToken(); // skip userName
		return st.nextToken();
	}
	
	@Override
	protected String buildPasswordFileEntry(String userName, String password) {
		return userName + ":" + password;
	}
}
