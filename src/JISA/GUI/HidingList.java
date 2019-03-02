package JISA.GUI;

import JISA.Control.Returnable;
import com.sun.javafx.collections.NonIterableChange;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class HidingList<E extends XYChart.Data<Double, Double>> implements ObservableList<E> {

    private List<E>                             backingList     = new LinkedList<>();
    private List<E>                             shownKey        = new LinkedList<>();
    private List<E>                             shown           = new ArrayList<>();
    private List<ListChangeListener<? super E>> changeListeners = new LinkedList<>();
    private Predicate<E>                        condition       = (e) -> true;

    public HidingList() {
    }

    public List<E> fullList() {
        return backingList;
    }

    @Override
    public synchronized void addListener(ListChangeListener<? super E> listChangeListener) {
        changeListeners.add(listChangeListener);
    }

    @Override
    public synchronized void removeListener(ListChangeListener<? super E> listChangeListener) {
        changeListeners.remove(listChangeListener);
    }

    @Override
    public synchronized boolean addAll(E... es) {
        return addAll(Arrays.asList(es));
    }

    @Override
    public synchronized boolean setAll(E... es) {
        return setAll(Arrays.asList(es));
    }

    @Override
    public synchronized boolean setAll(Collection<? extends E> collection) {
        backingList.clear();
        boolean r = backingList.addAll(collection);
        updateShown();
        return r;
    }

    @Override
    public synchronized boolean removeAll(E... es) {
        return removeAll(Arrays.asList(es));
    }

    @Override
    public synchronized boolean retainAll(E... es) {
        return retainAll(Arrays.asList(es));
    }

    @Override
    public synchronized void remove(int i, int i1) {
        shown.subList(i, i1).clear();
        updateShown();
    }

    @Override
    public synchronized int size() {
        return shown.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return backingList.isEmpty();
    }

    @Override
    public synchronized boolean contains(Object o) {
        return backingList.contains(o);
    }

    @Override
    public synchronized Iterator<E> iterator() {
        return shown.iterator();
    }

    @Override
    public synchronized Object[] toArray() {
        return shown.toArray();
    }

    @Override
    public synchronized <T> T[] toArray(T[] a) {
        return shown.toArray(a);
    }

    @Override
    public synchronized boolean add(E e) {
        boolean r = backingList.add(e);
        updateShown();
        return r;
    }

    @Override
    public synchronized boolean remove(Object o) {
        boolean r = backingList.remove(o);
        updateShown();
        return r;
    }

    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        return backingList.containsAll(c);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends E> c) {
        boolean r = backingList.addAll(c);
        updateShown();
        return r;
    }

    @Override
    public synchronized boolean addAll(int index, Collection<? extends E> c) {
        boolean r = backingList.addAll(index, c);
        updateShown();
        return r;
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        boolean r = backingList.removeAll(c);
        updateShown();
        return r;
    }

    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        boolean r = backingList.retainAll(c);
        updateShown();
        return r;
    }

    @Override
    public synchronized void clear() {
        backingList.clear();
        updateShown();
    }

    @Override
    public synchronized E get(int index) {
        return shown.get(index);
    }

    @Override
    public synchronized E set(int index, E element) {
        E e = backingList.set(index, element);
        updateShown();
        return e;
    }

    @Override
    public synchronized void add(int index, E element) {
        backingList.add(index, element);
        updateShown();
    }

    @Override
    public synchronized E remove(int index) {
        E e = shown.remove(index);
        updateShown();
        return e;
    }

    @Override
    public synchronized int indexOf(Object o) {
        return shown.indexOf(o);
    }

    @Override
    public synchronized int lastIndexOf(Object o) {
        return shown.lastIndexOf(o);
    }

    @Override
    public synchronized ListIterator<E> listIterator() {
        return shown.listIterator();
    }

    @Override
    public synchronized ListIterator<E> listIterator(int index) {
        return shown.listIterator(index);
    }

    @Override
    public synchronized List<E> subList(int fromIndex, int toIndex) {
        return shown.subList(fromIndex, toIndex);
    }

    public synchronized void setShowCondition(Predicate<E> condition) {
        this.condition = condition;
        updateShown();
    }

    public synchronized void updateShown() {

        // Remove those that should no-longer be there
        List<E>       toRemove    = new LinkedList<>();
        List<E>       toRemoveKey = new LinkedList<>();
        List<Integer> keys        = new LinkedList<>();
        for (int i = 0; i < shown.size(); i++) {

            E e = shownKey.get(i);
            if (!condition.test(e) || !backingList.contains(e)) {
                toRemove.add(shown.get(i));
                toRemoveKey.add(shownKey.get(i));
                keys.add(i);
            }

        }

        shown.removeAll(toRemove);
        shownKey.removeAll(toRemoveKey);
        for (int i = 0; i < keys.size(); i++) {
            triggerUpdate(new NonIterableChange.SimpleRemovedChange<>(keys.get(i), keys.get(i), toRemove.get(i), this));
        }


        // Add any new ones to show
        for (E e : backingList) {

            if (condition.test(e) && !shownKey.contains(e)) {
                shownKey.add(e);
                shown.add((E) new XYChart.Data<>(e.getXValue(), e.getYValue()));
                int index = shown.size() - 1;
                triggerUpdate(new NonIterableChange.SimpleAddChange(index, index + 1, this));
            }

        }


    }

    private void triggerUpdate(ListChangeListener.Change<E> change) {
        for (ListChangeListener<? super E> listener : changeListeners) {
            listener.onChanged(change);
        }
    }

    @Override
    public synchronized void addListener(InvalidationListener invalidationListener) {

    }

    @Override
    public synchronized void removeListener(InvalidationListener invalidationListener) {

    }

    public interface Filer<E> {
        Fileable<E> convert(E o);

        default Collection<Fileable<E>> convertAll(Collection<E> collection) {

            List<Fileable<E>> list = new LinkedList<>();

            for (E e : collection) {
                list.add(convert(e));
            }

            return list;

        }

    }

    public interface Fileable<E> extends Serializable, Comparable<Fileable<E>> {
        E clone();
    }

}
