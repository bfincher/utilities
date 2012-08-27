package com.fincher;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class NaturalOrderSet <E> extends AbstractSet<E> {
	
	private int modCount = 0;
	private final ArrayList<E> list;
	
	public NaturalOrderSet() {
		list = new ArrayList<E>();
	}
	
	public NaturalOrderSet(int initialSize) {
		list = new ArrayList<E>(initialSize);
	}
	
	public NaturalOrderSet(Collection<? extends E> c) {
		list = new ArrayList<E>(c);
	}
	
	@Override
	public boolean add(E e) {
		if (list.contains(e)) {
			return false;
		} else {
			list.add(e);
			modCount++;
			return true;
		}
	}
	
	@Override
	public int size() {
		return list.size();
	}
	
	@Override
	public Iterator<E> iterator() {
		return new Itr();
	}
	
	private class Itr implements Iterator<E> {
		private int expectedModCount = modCount;
		private int nextIdx = 0;
		private boolean removeAllowed = false;
		
		@Override
		public boolean hasNext() {
			return nextIdx < list.size();
		}
		
		@Override
		public E next() {			
			if (hasNext()) {
				if (modCount == expectedModCount) {
					removeAllowed = true;
					return list.get(nextIdx++);
				} else {
					throw new ConcurrentModificationException();
				}
			} else {
				throw new NoSuchElementException();
			}
		}
		
		@Override
		public void remove() {
			
			if (modCount == expectedModCount) {			
				if (removeAllowed) {
					modCount++;
					expectedModCount++;
					removeAllowed = false;
					
					list.remove(nextIdx - 1);
					nextIdx--;
				} else {
					throw new IllegalStateException();
				}
			} else {
				throw new ConcurrentModificationException();
			}
		}
	}

}
