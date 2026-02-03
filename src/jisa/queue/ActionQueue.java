package jisa.queue;

import java.util.LinkedList;
import java.util.List;

public class ActionQueue {

    private final List<Action>         actions   = new LinkedList<>();
    private final List<ActionListener> listeners = new LinkedList<>();

    public Action.Status run() {

        actions.forEach(Action::reset);

        for (Action action : actions) {

            Action.Status status = action.run();

            switch (status) {

                case ERROR:


            }

        }

        return null;

    }

    public interface ActionListener {
        void changed(List<Action> actions);
    }

}
