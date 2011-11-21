package com.fincher.htpasswd;

import java.util.Iterator;
import java.util.List;

public class Entries {
	
	private final Iterator<String> entries;
	
	public Entries(List<String> list) {
		entries = list.iterator();
	}
	
	public boolean hasNext() {
		return entries.hasNext();
	}
	
	public String next() {
		return entries.next();
	}

}
