package jisa.experiment.queue;

import javafx.scene.image.Image;
import jisa.gui.GUI;
import jisa.gui.queue.ActionDisplay;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface Action<T> extends Serializable {

    /**
     * Returns the name of the action.
     *
     * @return Name of the action
     */
    String getName();

    /**
     * Changes the name of the action.
     *
     * @param name The new name
     */
    void setName(String name);

    /**
     * Adds a listener to this action that is triggered any time its name is changed.
     *
     * @param listener Listener to add
     *
     * @return The added listener
     */
    Listener<Action<T>> addNameListener(Listener<Action<T>> listener);

    /**
     * Returns the value of the specified attribute for this action. Returns null if it doesn't exist.
     *
     * @param key Attribute key
     *
     * @return Attribute value
     */
    String getAttribute(String key);

    /**
     * Sets the value of the specified attribute for this action.
     *
     * @param key   Attribute key
     * @param value Attribute value
     */
    void setAttribute(String key, String value);

    /**
     * Returns whether this action has an attribute with the given name.
     *
     * @param key Attribute key to check
     *
     * @return Does it exist?
     */
    boolean hasAttribute(String key);

    /**
     * Removes the attribute with the given name from this action - if it exists.
     *
     * @param key Key of attribute to remove
     */
    void removeAttribute(String key);

    /**
     * Returns an unmodifiable mapping of all attributes.
     *
     * @return Attribtues
     */
    Map<String, String> getAttributes();

    default String getAttributeString(String delimiter, String assignment) {

        return getAttributes()
            .entrySet()
            .stream()
            .map(e -> String.format("%s%s%s", e.getKey(), assignment, e.getValue()))
            .collect(Collectors.joining(delimiter));

    }

    default String getAttributeString() {
        return getAttributeString(", ", "=");
    }

    /**
     * Adds a listener that is triggered any time the attributes of the action are updated.
     *
     * @param listener Listener to add
     *
     * @return The added listener
     */
    Listener<Action<T>> addAttributeListener(Listener<Action<T>> listener);

    void addTag(String tag);

    void removeTag(String tag);

    void clearTags();

    List<String> getTags();

    /**
     * Resets the action to the state it was in before being run.
     */
    void reset();

    /**
     * Runs the action. Should only return once the action is complete/interrupted/failed.
     */
    void start();

    void setOnStart(Listener<Action<T>> onStart);

    void setOnFinish(Listener<Action<T>> onFinish);

    /**
     * Resumes the action from where it was last interrupted
     */
    default void resume() {
        start();
    }

    /**
     * Interrupts and stops the action.
     */
    void stop();

    /**
     * Returns the current status of this action.
     *
     * @return Status of action
     */
    Status getStatus();

    /**
     * Returns the exception that caused the the action to result in failure.
     *
     * @return Exception
     */
    Exception getError();

    /**
     * Sets the current status of this action.
     *
     * @param status The status to set
     */
    void setStatus(Status status);

    /**
     * Adds a listener that is triggered any time the status of the action is changed.
     *
     * @param listener Listener to add
     *
     * @return The added listener
     */
    Listener<Action<T>> addStatusListener(Listener<Action<T>> listener);

    /**
     * Adds a listener that is triggered any time the name, status or attributes of the action are changed.
     *
     * @param listener Listener to add
     *
     * @return The added listener
     */
    default Listener<Action<T>> addListener(Listener<Action<T>> listener) {
        addNameListener(listener);
        addAttributeListener(listener);
        addStatusListener(listener);
        addChildrenListener(listener);
        return listener;
    }

    /**
     * Removes the specified listener for the action - if it has it.
     *
     * @param listener The listener to remove
     */
    void removeListener(Listener<Action<T>> listener);

    /**
     * Returns whether the action is currently running or not.
     *
     * @return Is it running?
     */
    boolean isRunning();

    /**
     * Returns whether this action is critical - critical actions require the entire action queue to abort if they fail.
     *
     * @return Is it critical?
     */
    boolean isCritical();

    /**
     * Sets whether this action is critical - critical actions require the entire action queue to abort if they fail.
     *
     * @param critical Should it be critical
     */
    void setCritical(boolean critical);

    /**
     * Returns whatever data this action generates.
     *
     * @return Generated data
     */
    T getData();

    /**
     * Returns an unmodifiable list of sub-actions that this action contains. The exact meaning of a sub-action and how
     * it relates to the running of this action depends on the type of action this is.
     *
     * @return List of sub-actions
     */
    List<Action> getChildren();

    /**
     * Adds a listener that is triggered any time child actions are added/removed.
     *
     * @param listener Listener to add
     *
     * @return The added listener
     */
    Listener<Action<T>> addChildrenListener(Listener<Action<T>> listener);

    /**
     * Performs whatever routine is necessary to allow the user to edit this action.
     */
    void userEdit();

    /**
     * Returns the JavaFX node used to display this action in an ActionQueueDisplay object.
     *
     * @return Node
     */
    ActionDisplay<Action<T>> getDisplay();

    /**
     * Creates a deep copy of this action.
     *
     * @return Deep copy of this action
     */
    Action<T> copy();

    enum Status {

        NOT_STARTED("Not Started", "queued"),
        RUNNING("Running", "progress"),
        RETRY("Running (Retry)", "progress"),
        STOPPING("Stopping", "progress"),
        INTERRUPTED("Interrupted", "cancelled"),
        COMPLETED("Completed", "complete"),
        ERROR("Error Encountered", "error");

        private final Image  image;
        private final String text;

        Status(String text, String imageName) {
            this.text = text;
            image     = new Image(GUI.class.getResourceAsStream(String.format("images/%s.png", imageName)));
        }

        public Image getImage() {
            return image;
        }

        public String getText() {
            return text;
        }

    }

}
