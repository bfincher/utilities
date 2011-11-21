package com.fincher;

import java.util.Iterator;

import junit.framework.Assert;

import org.junit.Test;

public class NaturalOrderSetTest {
	
	private NaturalOrderSet<Integer> buildSet() {
		NaturalOrderSet<Integer> set = new NaturalOrderSet<Integer>();
		for (int i = 0; i < 100; i++) {
			set.add(i);
		}
		
		return set;
	}
	
	@Test
	public void testAdd() {
		NaturalOrderSet<Integer> set = buildSet();				
		
		Iterator<Integer> iterator = set.iterator();
		for (int i = 0; i < 100; i++) {
			Assert.assertEquals(new Integer(i), iterator.next());
		}
	}
	
	@Test
	public void testRemove() {
		NaturalOrderSet<Integer> set = buildSet();
		
		for (int i = 0; i < 100; i+=2) {
			set.remove(i);
		}
		
		Assert.assertEquals(50, set.size());
		
		for (int i = 1; i < 100; i+=2) {
			Assert.assertTrue(set.contains(i));
		}
	}

}
