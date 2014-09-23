package com.codexperiments.rx;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import android.database.Cursor;
import rx.functions.Func1;

public class CursorList<TElement> implements List<TElement>, Closeable {
    private Cursor mCursor;
    private int mSize;
    private Func1<Cursor, TElement> mGetElement;

    public CursorList(Cursor pCursor) {
        super();
        mCursor = pCursor;
        mSize = pCursor.getCount();
        if (mSize < 0) mSize = 0;
    }

    @Override
    public void close() throws IOException {
        mCursor.close();
    }

    @Override
    public boolean add(TElement pElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int pLocation, TElement pElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends TElement> pCollection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int pIndex, Collection<? extends TElement> pCollection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object pObject) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> pCollection) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public TElement get(int pLocation) {
        if ((pLocation < 0) || (pLocation > mSize)) throw new IndexOutOfBoundsException();
        mCursor.move(pLocation);
        return mGetElement.call(mCursor);
    }

    @Override
    public int indexOf(Object pObject) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Iterator<TElement> iterator() {
        return new IteratorImpl(0);
    }

    @Override
    public int lastIndexOf(Object pObject) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<TElement> listIterator() {
        return new IteratorImpl(0);
    }

    @Override
    public ListIterator<TElement> listIterator(int pLocation) {
        return new IteratorImpl(pLocation);
    }

    @Override
    public TElement remove(int pLocation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object pObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> pArg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> pArg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TElement set(int pLocation, TElement pObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return mSize;
    }

    @Override
    public List<TElement> subList(int pStart, int pEnd) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] pArray) {
        // TODO
        throw new UnsupportedOperationException();
    }

    private class IteratorImpl implements ListIterator<TElement> {
        private int mLocation;

        public IteratorImpl(int pLocation) {
            mLocation = pLocation;
        }

        @Override
        public void add(TElement pElement) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            return mCursor.isAfterLast();
        }

        @Override
        public boolean hasPrevious() {
            return mLocation == 0;
        }

        @Override
        public TElement next() {
            if (mLocation < mSize) return get(mSize++);
            else throw new NoSuchElementException();
        }

        @Override
        public int nextIndex() {
            return mLocation + 1;
        }

        @Override
        public TElement previous() {
            if (mLocation > 0) return get(mSize--);
            else throw new NoSuchElementException();
        }

        @Override
        public int previousIndex() {
            return mLocation - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(TElement pElement) {
            throw new UnsupportedOperationException();
        }
    }
}
