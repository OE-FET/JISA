package JISA.Collections;

import java.io.Serializable;
import java.util.*;

public class FileList<E extends Serializable & Comparable<E>> extends FileBasedCollection<E> implements List<E> {
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return false;
    }

    @Override
    public E get(int index) {

        int i = 0;

        for (E element : this) {

            if (i == index) {
                return element;
            }

            i++;

        }

        throw new IndexOutOfBoundsException();

    }

    @Override
    public E set(int index, E element) {

        E old = get(index);
        List<E> before = subList(0, index-1);
        List<E> after  = subList(index+1, size()-1);
        clear();
        addAll(before);
        add(element);
        addAll(after);

        return old;

    }

    @Override
    public void add(int index, E element) {
        List<E> before = subList(0, index-1);
        List<E> after  = subList(index, size()-1);
        clear();
        addAll(before);
        add(element);
        addAll(after);
    }

    @Override
    public E remove(int index) {

        E object = get(index);
        remove(object);
        return object;

    }

    @Override
    public int indexOf(Object o) {

        int i = 0;

        if (o == null) {
            for (E element : this) {

                if (element == null) {
                    return i;
                }
                i++;
            }
        } else {
            for (E element : this) {
                if (element == o) {
                    return i;
                }
                i++;
            }
        }

        return -1;

    }

    @Override
    public int lastIndexOf(Object o) {

        int i = 0;
        int last = -1;

        if (o == null) {
            for (E element : this) {

                if (element == null) {
                    last = i;
                }
                i++;
            }
        } else {
            for (E element : this) {
                if (element == o) {
                    last = i;
                }
                i++;
            }
        }

        return last;

    }

    @Override
    public ListIterator<E> listIterator() {

        Iterator<E> itr = iterator();

        return new ListIterator<E>() {

            int index = 0;

            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public E next() {
                index++;
                return itr.next();
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public E previous() {
                return null;
            }

            @Override
            public int nextIndex() {
                return index;
            }

            @Override
            public int previousIndex() {
                return index-1;
            }

            @Override
            public void remove() {
                itr.remove();
            }

            @Override
            public void set(E e) {

            }

            @Override
            public void add(E e) {

            }
        };
    }

    @Override
    public ListIterator<E> listIterator(int indx) {

        Iterator<E> itr = iterator();

        ListIterator<E> i = new ListIterator<E>() {

            int index = 0;

            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public E next() {
                index++;
                return itr.next();
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public E previous() {
                return null;
            }

            @Override
            public int nextIndex() {
                return index;
            }

            @Override
            public int previousIndex() {
                return index-1;
            }

            @Override
            public void remove() {
                itr.remove();
            }

            @Override
            public void set(E e) {

            }

            @Override
            public void add(E e) {

            }
        };

        for (int j = 0; j < indx; j ++) {
            i.next();
        }

        return i;

    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {

        int i = 0;
        ArrayList<E> sub = new ArrayList<>();
        for (E element : this) {
            if (i >= fromIndex && i <= toIndex) {
                sub.add(element);
            }
            i++;
        }

        return sub;

    }
}
