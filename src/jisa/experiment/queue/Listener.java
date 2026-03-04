package jisa.experiment.queue;

public interface Listener<T> {

    void update(T updated);

    default void updateRegardless(T updated) {

        try {
            update(updated);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

}
