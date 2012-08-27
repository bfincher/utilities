package com.fincher.htpasswd;

import java.util.StringTokenizer;

public class HtpasswdMd5 extends AbstractHtpasswd {
	
	private final String realm;
	
	public HtpasswdMd5(String realm) {
		super(AlgorithmEnum.MD5);
		this.realm = realm;
	}
	
	@Override
	public String cryptPassword(String userName, String password) {
		byte[] bytes = getMessageDigest().digest(new String(userName + ":" + realm + ":" + password).getBytes());
		
		StringBuilder encryptedPasswd = new StringBuilder();
		
		for (byte b: bytes) {
    		String hex = Integer.toHexString(b);
    		if (hex.length() > 2) {
    			hex = hex.substring(hex.length() - 2);
    		}
    		encryptedPasswd.append(hex);
    	}
		
		return encryptedPasswd.toString();
	}
	
	@Override
	protected String parsePasswordFileEntryPassword(String passwordFileEntry) {
		StringTokenizer st = new StringTokenizer(passwordFileEntry, ":");
		st.nextToken(); // skip userName
		st.nextToken();// skip realm
		return st.nextToken();
	}
	
	@Override
	protected String buildPasswordFileEntry(String userName, String password) {
		return userName + ":" + realm + ":" + password;
	}

}
