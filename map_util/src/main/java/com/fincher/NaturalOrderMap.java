package com.fincher;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class NaturalOrderMap <K, V> extends AbstractMap<K, V> {
	
	private final Set<Entry<K,V>> entrySet;
	
	public NaturalOrderMap() {
		entrySet = new NaturalOrderSet<Entry<K,V>>();
	}
	
	public NaturalOrderMap(int initialCapacity) {
		entrySet = new NaturalOrderSet<Entry<K,V>>(initialCapacity);
	}
	
	public NaturalOrderMap(Map<? extends K, ? extends V> m) {
		entrySet = new NaturalOrderSet<Entry<K,V>>(m.size());
		putAll(m);
	}
	
	@Override
	public Set<Entry<K,V>> entrySet() {
		return entrySet;
	}
	
	@Override
	public V put(K key, V value) {
		V oldValue = get(key);
		if (oldValue == null) {
			MapEntry entry = new MapEntry(key, value);
			entrySet.add(entry);
		} else {
			Entry<K,V> entry = null;
			for (Entry<K,V> e: entrySet) {
				if (e.getKey().equals(key)) {
					entry = e;
					break;
				}
			}
			
			entry.setValue(value);
		}
		
		return oldValue;
	}
	
	private class MapEntry implements  Entry<K,V> {
		private final K key;
		private V value;
		
		public MapEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}
		
		public K getKey() {
			return key;
		}
		
		public V getValue() {
			return value;
		}
		
		public V setValue(V value) {
			V oldValue = this.value;
			this.value = value;
			return oldValue;
		}
		
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			
			Entry<?,?> entry = (Entry<?,?>)o;
			return key.equals(entry.getKey()) 
			&& value.equals(entry.getValue());
		}
		
	}
		
}
