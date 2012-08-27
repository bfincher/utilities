package com.fincher;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;

public class NaturalOrderMapTest {

	private Integer key1 = 1;
	private Integer key2 = 2;
	private Integer key3 = 3;
	private Integer key4 = 4;
	private Integer key5 = 5;
//	private Integer key6 = 6;
//	private Integer key7 = 7;
	private Integer key8 = 8;

	private String value1 = "1";
	private String value2 = "2";
	private String value3 = "3";
	private String value4 = "4";
	private String value5 = "5";
//	private String value6 = "6";
//	private String value7 = "7";
	private String value8 = "8";

	@Test
	public void testInsertion() {
		NaturalOrderMap<Integer, String> map = new NaturalOrderMap<Integer, String>();

		map.put( key2, value2 );
		map.put( key8, value8 );
		map.put( key3, value3 );
		map.put( key5, value5 );
		map.put( key4, value4 );
		map.put( key1, value1 );

		ArrayList<String> list = new ArrayList<String>(map.values());
		Assert.assertEquals( list.get( 0 ), value2 );
		Assert.assertEquals( list.get( 1 ), value8 );
		Assert.assertEquals( list.get( 2 ), value3 );
		Assert.assertEquals( list.get( 3 ), value5 );
		Assert.assertEquals( list.get( 4 ), value4 );
		Assert.assertEquals( list.get( 5 ), value1 );        
	}

	@Test
	public void testInsertionAndRemove() {
		NaturalOrderMap<Integer, String> map = new NaturalOrderMap<Integer, String>();
		map.put( key2, value2 );
		map.put( key8, value8 );
		map.put( key3, value3 );
		map.put( key5, value5 );
		map.put( key4, value4 );
		map.put( key1, value1 );

		Assert.assertEquals( map.remove( key8 ), value8 );

		ArrayList<String> list = new ArrayList<String>(map.values());
		Assert.assertEquals( list.get( 0 ), value2 );
		Assert.assertEquals( list.get( 1 ), value3 );
		Assert.assertEquals( list.get( 2 ), value5 );
		Assert.assertEquals( list.get( 3 ), value4 );
		Assert.assertEquals( list.get( 4 ), value1 );        
		Assert.assertEquals( map.size(), 5 );        

		Assert.assertEquals( map.remove( key2 ), value2 );

		list = new ArrayList<String>( map.values() );
		Assert.assertEquals( list.get( 0 ), value3 );
		Assert.assertEquals( list.get( 1 ), value5 );
		Assert.assertEquals( list.get( 2 ), value4 );
		Assert.assertEquals( list.get( 3 ), value1 );        
		Assert.assertEquals( map.size(), 4 );        

		Assert.assertEquals( map.remove( key1 ), value1 );

		list = new ArrayList<String>( map.values() );
		Assert.assertEquals( list.get( 0 ), value3 );
		Assert.assertEquals( list.get( 1 ), value5 );
		Assert.assertEquals( list.get( 2 ), value4 );
		Assert.assertEquals( map.size(), 3 );        

		Assert.assertEquals( map.remove( key5 ), value5 );

		list = new ArrayList<String>( map.values() );
		Assert.assertEquals( list.get( 0 ), value3 );
		Assert.assertEquals( list.get( 1 ), value4 );
		Assert.assertEquals( map.size(), 2 );        

		map.put( key1, value1 );

		list = new ArrayList<String>( map.values() );
		Assert.assertEquals( list.get( 0 ), value3 );
		Assert.assertEquals( list.get( 1 ), value4 );
		Assert.assertEquals( list.get( 2 ), value1 );
		Assert.assertEquals( map.size(), 3 );        

		map.put( key1, value1 );

		list = new ArrayList<String>( map.values() );
		Assert.assertEquals( list.get( 0 ), value3 );
		Assert.assertEquals( list.get( 1 ), value4 );
		Assert.assertEquals( list.get( 2 ), value1 );
		Assert.assertEquals( map.size(), 3 );        
	}

}
