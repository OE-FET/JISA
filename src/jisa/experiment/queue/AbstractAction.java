package jisa.experiment.queue;

import java.util.*;

public abstract class AbstractAction<T> implements Action<T> {

    private       String                    name;
    private       boolean                   isCritical         = false;
    private       Status                    status             = Status.NOT_STARTED;
    private       Listener<Action<T>>       onEdit             = a -> {};
    private       Listener<Action<T>>       onStart            = action -> {};
    private       Listener<Action<T>>       onFinish           = action -> {};
    private final List<String>              tags               = new LinkedList<>();
    private final Map<String, String>       attributes         = new LinkedHashMap<>();
    private final List<Listener<Action<T>>> nameListeners      = new LinkedList<>();
    private final List<Listener<Action<T>>> attributeListeners = new LinkedList<>();
    private final List<Listener<Action<T>>> statusListeners    = new LinkedList<>();
    private final List<Listener<Action<T>>> childListeners     = new LinkedList<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        nameListeners.forEach(l -> l.updateRegardless(this));
    }

    @Override
    public Listener<Action<T>> addNameListener(Listener<Action<T>> listener) {
        nameListeners.add(listener);
        return listener;
    }

    @Override
    public String getAttribute(String key) {
        return attributes.getOrDefault(key, null);
    }

    @Override
    public void setAttribute(String key, String value) {
        attributes.put(key, value);
        attributeListeners.forEach(l -> l.updateRegardless(this));
    }

    @Override
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    @Override
    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    @Override
    public Map<String, String> getAttributes() {
        return Map.copyOf(attributes);
    }

    @Override
    public Listener<Action<T>> addAttributeListener(Listener<Action<T>> listener) {
        attributeListeners.add(listener);
        return listener;
    }

    @Override
    public void addTag(String tag) {
        tags.add(tag);
        attributeListeners.forEach(l -> l.updateRegardless(this));
    }

    @Override
    public void removeTag(String tag) {
        tags.remove(tag);
        attributeListeners.forEach(l -> l.updateRegardless(this));
    }

    public void clearTags() {
        tags.clear();
        attributeListeners.forEach(l -> l.updateRegardless(this));
    }

    @Override
    public List<String> getTags() {
        return List.copyOf(tags);
    }

    @Override
    public abstract void reset();

    public void setOnStart(Listener<Action<T>> onStart) {
        this.onStart = onStart;
    }

    public void setOnFinish(Listener<Action<T>> onFinish) {
        this.onFinish = onFinish;
    }

    protected void onStart() {
        onStart.updateRegardless(this);
    }

    protected void onFinish() {
        onFinish.updateRegardless(this);
    }

    @Override
    public abstract void start();

    @Override
    public abstract void stop();

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {

        this.status = status;

        for (Listener<Action<T>> l : statusListeners) {
            l.updateRegardless(this);
        }

    }

    @Override
    public Listener<Action<T>> addStatusListener(Listener<Action<T>> listener) {
        statusListeners.add(listener);
        return listener;
    }

    @Override
    public void removeListener(Listener<Action<T>> listener) {
        nameListeners.remove(listener);
        attributeListeners.remove(listener);
        statusListeners.remove(listener);
        childListeners.remove(listener);
    }

    @Override
    public List<Listener<Action<T>>> getStatusListeners () {
        return statusListeners;
    }

    @Override
    public abstract boolean isRunning();

    @Override
    public boolean isCritical() {
        return isCritical;
    }

    @Override
    public void setCritical(boolean critical) {
        this.isCritical = critical;
    }

    @Override
    public abstract T getData();

    @Override
    public abstract List<Action> getChildren();

    @Override
    public Listener<Action<T>> addChildrenListener(Listener<Action<T>> listener) {
        childListeners.add(listener);
        return listener;
    }

    @Override
    public void userEdit() {
        onEdit.updateRegardless(this);
    }

    public void setOnEdit(Listener<Action<T>> onEdit) {
        this.onEdit = onEdit;
    }

    protected void childrenChanged() {
        childListeners.forEach(l -> l.updateRegardless(this));
    }

    protected void copyBasicParametersTo(AbstractAction<T> copy) {
        getAttributes().forEach(copy::setAttribute);
        copy.setOnStart(onStart);
        copy.setOnFinish(onFinish);
        copy.setOnEdit(onEdit);
    }

}
