package JISA.Collections;


import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

public class Fileable<E> implements Serializable, Comparable<Fileable<E>> {

    public static <E> Collection<Fileable<E>> asList(Collection<? extends E> collection) {

        LinkedList<Fileable<E>> list = new LinkedList<>();

        for (E e : collection) {
            list.add(new Fileable<>(e));
        }

        return list;

    }

    public E object;

    public Fileable(E o) {
        object = o;
    }


    @Override
    public int compareTo(Fileable<E> o) {
        return 0;
    }
}
