package jisa.gui.plotting;

import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.axes.AxisMode;
import de.gsi.chart.plugins.ChartPlugin;
import de.gsi.chart.plugins.MouseEventsHelper;
import de.gsi.chart.ui.ObservableDeque;
import de.gsi.chart.ui.geometry.Side;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.controlsfx.control.RangeSlider;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class JISAZoomer extends ChartPlugin {

    public static final  String                                ZOOMER_OMIT_AXIS            = "OmitAxisZoom";
    public static final  String                                STYLE_CLASS_ZOOM_RECT       = "chart-zoom-rect";
    private static final int                                   ZOOM_RECT_MIN_SIZE          = 5;
    private static final Duration                              DEFAULT_ZOOM_DURATION       = Duration.millis(500.0);
    private static final int                                   DEFAULT_AUTO_ZOOM_THRESHOLD = 15;
    private static final int                                   DEFAULT_FLICKER_THRESHOLD   = 3;
    private static final int                                   FONT_SIZE                   = 22;
    public static final  Predicate<MouseEvent>                 DEFAULT_MOUSE_FILTER        = MouseEventsHelper::isOnlyMiddleButtonDown;
    private              double                                panShiftX;
    private              double                                panShiftY;
    private              Point2D                               previousMouseLocation;
    private final        BooleanProperty                       enablePanner;
    private final        BooleanProperty                       autoZoomEnable;
    private final        IntegerProperty                       autoZoomThreshold;
    private final        EventHandler<MouseEvent>              panStartHandler;
    private final        EventHandler<MouseEvent>              panDragHandler;
    private final        EventHandler<MouseEvent>              panEndHandler;
    public               Predicate<MouseEvent>                 panFilter                   = e -> MouseEventsHelper.isOnlyMiddleButtonDown(e) || (MouseEventsHelper.isOnlyPrimaryButtonDown(e) && e.isControlDown());
    public final         Predicate<MouseEvent>                 defaultZoomInMouseFilter;
    public final         Predicate<MouseEvent>                 defaultZoomOutMouseFilter;
    public final         Predicate<MouseEvent>                 defaultZoomOriginFilter;
    public final         Predicate<ScrollEvent>                defaultScrollFilter;
    private              Predicate<MouseEvent>                 zoomInMouseFilter;
    private              Predicate<MouseEvent>                 zoomOutMouseFilter;
    private              Predicate<MouseEvent>                 zoomOriginMouseFilter;
    private              Predicate<ScrollEvent>                zoomScrollFilter;
    private final        Rectangle                             zoomRectangle;
    private              Point2D                               zoomStartPoint;
    private              Point2D                               zoomEndPoint;
    private final        ObservableDeque<Map<Axis, ZoomState>> zoomStacks;
    private final        HBox                                  zoomButtons;
    private              ZoomRangeSlider                       xRangeSlider;
    private              boolean                               xRangeSliderInit;
    private final        ObservableList<Axis>                  omitAxisZoom;
    private final        ObjectProperty<AxisMode>              axisMode;
    private              Cursor                                originalCursor;
    private final        ObjectProperty<Cursor>                dragCursor;
    private final        ObjectProperty<Cursor>                zoomCursor;
    private final        BooleanProperty                       animated;
    private final        ObjectProperty<Duration>              zoomDuration;
    private final        BooleanProperty                       updateTickUnit;
    private final        BooleanProperty                       sliderVisible;
    private final        EventHandler<MouseEvent>              zoomInStartHandler;
    private final        EventHandler<MouseEvent>              zoomInDragHandler;
    private final        EventHandler<MouseEvent>              zoomInEndHandler;
    private final        EventHandler<ScrollEvent>             zoomScrollHandler;
    private final        EventHandler<MouseEvent>              zoomOutHandler;
    private final        EventHandler<MouseEvent>              zoomOriginHandler;

    public JISAZoomer() {
        this(AxisMode.XY);
    }

    public JISAZoomer(AxisMode zoomMode) {
        this(zoomMode, false);
    }

    public JISAZoomer(AxisMode zoomMode, boolean animated) {
        this.enablePanner              = new SimpleBooleanProperty(this, "enablePanner", true);
        this.autoZoomEnable            = new SimpleBooleanProperty(this, "enableAutoZoom", false);
        this.autoZoomThreshold         = new SimpleIntegerProperty(this, "autoZoomThreshold", 15);
        this.panStartHandler           = (event) -> {
            if (this.isPannerEnabled() && panFilter.test(event)) {
                this.panStarted(event);
                event.consume();
            }

        };
        this.panDragHandler            = (event) -> {
            if (this.panOngoing()) {
                this.panDragged(event);
                event.consume();
            }

        };
        this.panEndHandler             = (event) -> {
            if (this.panOngoing()) {
                this.panEnded();
                event.consume();
            }

        };
        this.defaultZoomInMouseFilter  = (event) -> MouseEventsHelper.isOnlySecondaryButtonDown(event) && MouseEventsHelper.modifierKeysUp(event) && this.isMouseEventWithinCanvas(event);
        this.defaultZoomOutMouseFilter = (event) -> MouseEventsHelper.isOnlySecondaryButtonDown(event) && event.getClickCount() >= 2 && this.isMouseEventWithinCanvas(event);
        this.defaultZoomOriginFilter   = (event) -> MouseEventsHelper.isOnlyPrimaryButtonDown(event) && event.getClickCount() >= 2 && this.isMouseEventWithinCanvas(event);
        this.defaultScrollFilter       = (event) -> event.isControlDown() && this.isMouseEventWithinCanvas(event);
        this.zoomInMouseFilter         = this.defaultZoomInMouseFilter;
        this.zoomOutMouseFilter        = this.defaultZoomOutMouseFilter;
        this.zoomOriginMouseFilter     = this.defaultZoomOriginFilter;
        this.zoomScrollFilter          = this.defaultScrollFilter;
        this.zoomRectangle             = new Rectangle();
        this.zoomStacks                = new ObservableDeque(new ArrayDeque());
        this.zoomButtons               = this.getZoomInteractorBar();
        this.omitAxisZoom              = FXCollections.observableArrayList();
        this.axisMode                  = new SimpleObjectProperty<AxisMode>(this, "axisMode", AxisMode.XY) {
            protected void invalidated() {
                Objects.requireNonNull((AxisMode) this.get(), "The " + this.getName() + " must not be null");
            }
        };
        this.dragCursor                = new SimpleObjectProperty(this, "dragCursor");
        this.zoomCursor                = new SimpleObjectProperty(this, "zoomCursor");
        this.animated                  = new SimpleBooleanProperty(this, "animated", false);
        this.zoomDuration              = new SimpleObjectProperty<Duration>(this, "zoomDuration", DEFAULT_ZOOM_DURATION) {
            protected void invalidated() {
                Objects.requireNonNull((Duration) this.get(), "The " + this.getName() + " must not be null");
            }
        };
        this.updateTickUnit            = new SimpleBooleanProperty(this, "updateTickUnit", true);
        this.sliderVisible             = new SimpleBooleanProperty(this, "sliderVisible", true);
        this.zoomInStartHandler        = (event) -> {
            if (this.getZoomInMouseFilter() == null || this.getZoomInMouseFilter().test(event)) {
                this.zoomInStarted(event);
                event.consume();
            }

        };
        this.zoomInDragHandler         = (event) -> {
            if (this.zoomOngoing()) {
                this.zoomInDragged(event);
                event.consume();
            }

        };
        this.zoomInEndHandler          = (event) -> {
            if (this.zoomOngoing()) {
                this.zoomInEnded();
                event.consume();
            }

        };
        this.zoomScrollHandler         = (event) -> {
            if (this.getZoomScrollFilter() == null || this.getZoomScrollFilter().test(event)) {
                AxisMode mode = this.getAxisMode();
                if (this.zoomStacks.isEmpty()) {
                    this.makeSnapshotOfView();
                }

                Iterator var3 = this.getChart().getAxes().iterator();

                while (true) {
                    Axis axis;
                    while (true) {
                        do {
                            if (!var3.hasNext()) {
                                event.consume();
                                return;
                            }

                            axis = (Axis) var3.next();
                        } while (axis.getSide() == null);

                        if (axis.getSide().isHorizontal()) {
                            if (mode.allowsX()) {
                                break;
                            }
                        } else if (mode.allowsY()) {
                            break;
                        }
                    }

                    if (!this.isOmitZoomInternal(axis)) {
                        zoomOnAxis(axis, event);
                    }
                }
            }
        };
        this.zoomOutHandler            = (event) -> {
            if (this.getZoomOutMouseFilter() == null || this.getZoomOutMouseFilter().test(event)) {
                boolean zoomOutPerformed = this.zoomOut();
                if (zoomOutPerformed) {
                    event.consume();
                }
            }

        };
        this.zoomOriginHandler         = (event) -> {
            if (this.getZoomOriginMouseFilter() == null || this.getZoomOriginMouseFilter().test(event)) {
                boolean zoomOutPerformed = this.zoomOrigin();
                if (zoomOutPerformed) {
                    event.consume();
                }
            }

        };
        this.setAxisMode(zoomMode);
        this.setAnimated(animated);
        this.setZoomCursor(Cursor.CROSSHAIR);
        this.setDragCursor(Cursor.CLOSED_HAND);
        this.zoomRectangle.setManaged(false);
        this.zoomRectangle.getStyleClass().add("chart-zoom-rect");
        this.getChartChildren().add(this.zoomRectangle);
        this.registerMouseHandlers();
        this.chartProperty().addListener((change, o, n) -> {
            if (o != null) {
                o.getToolBar().getChildren().remove(this.zoomButtons);
                o.getPlotArea().setBottom((Node) null);
                this.xRangeSlider.prefWidthProperty().unbind();
            }

            if (n != null) {
                if (this.isAddButtonsToToolBar()) {
                    n.getToolBar().getChildren().add(this.zoomButtons);
                }

                ZoomRangeSlider slider = new ZoomRangeSlider(n);
                if (this.isSliderVisible()) {
                    n.getPlotArea().setBottom(slider);
                    this.xRangeSlider.prefWidthProperty().bind(n.getCanvasForeground().widthProperty());
                }
            }

        });
    }

    public JISAZoomer(boolean animated) {
        this(AxisMode.XY, animated);
    }

    public final BooleanProperty animatedProperty() {
        return this.animated;
    }

    public final BooleanProperty autoZoomEnabledProperty() {
        return this.autoZoomEnable;
    }

    public IntegerProperty autoZoomThresholdProperty() {
        return this.autoZoomThreshold;
    }

    public final ObjectProperty<AxisMode> axisModeProperty() {
        return this.axisMode;
    }

    public void clear() {
        this.zoomStacks.clear();
    }

    public void clear(Axis axis) {
        Iterator var2 = this.zoomStacks.iterator();

        while (var2.hasNext()) {
            Map<Axis, ZoomState> stackStage = (Map) var2.next();
            stackStage.remove(axis);
        }

    }

    public final ObjectProperty<Cursor> dragCursorProperty() {
        return this.dragCursor;
    }

    public int getAutoZoomThreshold() {
        return this.autoZoomThresholdProperty().get();
    }

    public final AxisMode getAxisMode() {
        return (AxisMode) this.axisModeProperty().get();
    }

    public final Cursor getDragCursor() {
        return (Cursor) this.dragCursorProperty().get();
    }

    public RangeSlider getRangeSlider() {
        return this.xRangeSlider;
    }

    public final Cursor getZoomCursor() {
        return (Cursor) this.zoomCursorProperty().get();
    }

    public final Duration getZoomDuration() {
        return (Duration) this.zoomDurationProperty().get();
    }

    public Predicate<MouseEvent> getZoomInMouseFilter() {
        return this.zoomInMouseFilter;
    }

    public HBox getZoomInteractorBar() {

        HBox buttonBar = new HBox();
        buttonBar.setPadding(new Insets(1.0, 1.0, 1.0, 1.0));

        Button zoomOut = new Button("\uD83D\uDDD6");

        zoomOut.setMinWidth(25);
        zoomOut.setPadding(new Insets(3.0, 3.0, 3.0, 3.0));
        zoomOut.setTooltip(new Tooltip("zooms to origin and enables auto-ranging"));

        Button zoomModeXY = new Button("⬌⬍");
        zoomModeXY.setMinWidth(25);
        zoomModeXY.setPadding(new Insets(3.0, 3.0, 3.0, 3.0));
        zoomModeXY.setTooltip(new Tooltip("set zoom-mode to X & Y range (N.B. disables auto-ranging)"));

        Button zoomModeX = new Button("⬌");
        zoomModeX.setMinWidth(25);
        zoomModeX.setPadding(new Insets(3.0, 3.0, 3.0, 3.0));
        zoomModeX.setTooltip(new Tooltip("set zoom-mode to X range (N.B. disables auto-ranging)"));

        Button zoomModeY = new Button("⬍");
        zoomModeY.setMinWidth(25);
        zoomModeY.setPadding(new Insets(3.0, 3.0, 3.0, 3.0));
        zoomModeY.setTooltip(new Tooltip("set zoom-mode to Y range (N.B. disables auto-ranging)"));

        zoomOut.setOnAction((evt) -> {
            this.zoomOrigin();
            this.getChart().getAxes().forEach((axis) -> {
                axis.setAutoRanging(true);
            });
        });

        zoomModeXY.setOnAction((evt) -> {
            this.setAxisMode(AxisMode.XY);
        });

        zoomModeX.setOnAction((evt) -> {
            this.setAxisMode(AxisMode.X);
        });

        zoomModeY.setOnAction((evt) -> {
            this.setAxisMode(AxisMode.Y);
        });

        buttonBar.getChildren().addAll(zoomOut, new Separator(Orientation.VERTICAL), zoomModeXY, zoomModeX, zoomModeY);

        return buttonBar;

    }

    public Predicate<MouseEvent> getZoomOriginMouseFilter() {
        return this.zoomOriginMouseFilter;
    }

    public Predicate<MouseEvent> getZoomOutMouseFilter() {
        return this.zoomOutMouseFilter;
    }

    public Predicate<ScrollEvent> getZoomScrollFilter() {
        return this.zoomScrollFilter;
    }

    public final boolean isAnimated() {
        return this.animatedProperty().get();
    }

    public final boolean isAutoZoomEnabled() {
        return this.autoZoomEnabledProperty().get();
    }

    public final boolean isPannerEnabled() {
        return this.pannerEnabledProperty().get();
    }

    public final boolean isSliderVisible() {
        return this.sliderVisibleProperty().get();
    }

    public final boolean isUpdateTickUnit() {
        return this.updateTickUnitProperty().get();
    }

    public final ObservableList<Axis> omitAxisZoomList() {
        return this.omitAxisZoom;
    }

    public final BooleanProperty pannerEnabledProperty() {
        return this.enablePanner;
    }

    public final void setAnimated(boolean value) {
        this.animatedProperty().set(value);
    }

    public final void setAutoZoomEnabled(boolean state) {
        this.autoZoomEnabledProperty().set(state);
    }

    public void setAutoZoomThreshold(int value) {
        this.autoZoomThresholdProperty().set(value);
    }

    public final void setAxisMode(AxisMode mode) {
        this.axisModeProperty().set(mode);
    }

    public final void setDragCursor(Cursor cursor) {
        this.dragCursorProperty().set(cursor);
    }

    public final void setPannerEnabled(boolean state) {
        this.pannerEnabledProperty().set(state);
    }

    public final void setSliderVisible(boolean state) {
        this.sliderVisibleProperty().set(state);
    }

    public final void setUpdateTickUnit(boolean value) {
        this.updateTickUnitProperty().set(value);
    }

    public final void setZoomCursor(Cursor cursor) {
        this.zoomCursorProperty().set(cursor);
    }

    public final void setZoomDuration(Duration duration) {
        this.zoomDurationProperty().set(duration);
    }

    public void setZoomInMouseFilter(Predicate<MouseEvent> zoomInMouseFilter) {
        this.zoomInMouseFilter = zoomInMouseFilter;
    }

    public void setZoomOriginMouseFilter(Predicate<MouseEvent> zoomOriginMouseFilter) {
        this.zoomOriginMouseFilter = zoomOriginMouseFilter;
    }

    public void setZoomOutMouseFilter(Predicate<MouseEvent> zoomOutMouseFilter) {
        this.zoomOutMouseFilter = zoomOutMouseFilter;
    }

    public void setZoomScrollFilter(Predicate<ScrollEvent> zoomScrollFilter) {
        this.zoomScrollFilter = zoomScrollFilter;
    }

    public final BooleanProperty sliderVisibleProperty() {
        return this.sliderVisible;
    }

    public final BooleanProperty updateTickUnitProperty() {
        return this.updateTickUnit;
    }

    public final ObjectProperty<Cursor> zoomCursorProperty() {
        return this.zoomCursor;
    }

    public final ObjectProperty<Duration> zoomDurationProperty() {
        return this.zoomDuration;
    }

    public boolean zoomOrigin() {
        this.clearZoomStackIfAxisAutoRangingIsEnabled();
        Map<Axis, ZoomState> zoomWindows = (Map) this.zoomStacks.peekLast();
        if (zoomWindows != null && !zoomWindows.isEmpty()) {
            this.clear();
            this.performZoom(zoomWindows, false);
            if (this.xRangeSlider != null) {
                this.xRangeSlider.reset();
            }

            Iterator var2 = this.getChart().getAxes().iterator();

            while (var2.hasNext()) {
                Axis axis = (Axis) var2.next();
                axis.forceRedraw();
            }

            return true;
        } else {
            return false;
        }
    }

    public ObservableDeque<Map<Axis, ZoomState>> zoomStackDeque() {
        return this.zoomStacks;
    }

    private void clearZoomStackIfAxisAutoRangingIsEnabled() {
        Chart chart = this.getChart();
        if (chart != null) {
            Iterator var2 = this.getChart().getAxes().iterator();

            while (true) {
                Axis axis;
                do {
                    label35:
                    do {
                        while (var2.hasNext()) {
                            axis = (Axis) var2.next();
                            if (axis.getSide().isHorizontal()) {
                                continue label35;
                            }

                            if (this.getAxisMode().allowsY() && (axis.isAutoRanging() || axis.isAutoGrowRanging())) {
                                this.clear(axis);
                            }
                        }

                        return;
                    } while (!this.getAxisMode().allowsX());
                } while (!axis.isAutoRanging() && !axis.isAutoGrowRanging());

                this.clear(axis);
            }
        }
    }

    private Map<Axis, ZoomState> getZoomDataWindows() {
        ConcurrentHashMap<Axis, ZoomState> axisStateMap = new ConcurrentHashMap();
        if (this.getChart() == null) {
            return axisStateMap;
        } else {
            double   minX              = this.zoomRectangle.getX();
            double   minY              = this.zoomRectangle.getY() + this.zoomRectangle.getHeight();
            double   maxX              = this.zoomRectangle.getX() + this.zoomRectangle.getWidth();
            double   maxY              = this.zoomRectangle.getY();
            Point2D  minPlotCoordinate = this.getChart().toPlotArea(minX, minY);
            Point2D  maxPlotCoordinate = this.getChart().toPlotArea(maxX, maxY);
            Iterator var12             = this.getChart().getAxes().iterator();

            while (var12.hasNext()) {
                Axis   axis = (Axis) var12.next();
                double dataMax;
                double dataMin;
                if (axis.getSide().isVertical()) {
                    dataMin = axis.getValueForDisplay(minPlotCoordinate.getY());
                    dataMax = axis.getValueForDisplay(maxPlotCoordinate.getY());
                } else {
                    dataMin = axis.getValueForDisplay(minPlotCoordinate.getX());
                    dataMax = axis.getValueForDisplay(maxPlotCoordinate.getX());
                }

                switch (this.getAxisMode()) {
                    case X:
                        if (axis.getSide().isHorizontal()) {
                            axisStateMap.put(axis, new ZoomState(dataMin, dataMax, axis.isAutoRanging(), axis.isAutoGrowRanging()));
                        }
                        break;
                    case Y:
                        if (axis.getSide().isVertical()) {
                            axisStateMap.put(axis, new ZoomState(dataMin, dataMax, axis.isAutoRanging(), axis.isAutoGrowRanging()));
                        }
                        break;
                    case XY:
                    default:
                        axisStateMap.put(axis, new ZoomState(dataMin, dataMax, axis.isAutoRanging(), axis.isAutoGrowRanging()));
                }
            }

            return axisStateMap;
        }
    }

    private void installDragCursor() {
        Region chart = this.getChart();
        this.originalCursor = chart.getCursor();
        if (this.getDragCursor() != null) {
            chart.setCursor(this.getDragCursor());
        }

    }

    private void installZoomCursor() {
        Region chart = this.getChart();
        this.originalCursor = chart.getCursor();
        if (this.getDragCursor() != null) {
            chart.setCursor(this.getZoomCursor());
        }

    }

    private boolean isOmitZoomInternal(Axis axis) {
        boolean propertyState = isOmitZoom(axis);
        return propertyState || this.omitAxisZoomList().contains(axis);
    }

    private void makeSnapshotOfView() {
        Bounds bounds = this.getChart().getBoundsInLocal();
        double minX   = bounds.getMinX();
        double minY   = bounds.getMinY();
        double maxX   = bounds.getMaxX();
        double maxY   = bounds.getMaxY();
        this.zoomRectangle.setX(bounds.getMinX());
        this.zoomRectangle.setY(bounds.getMinY());
        this.zoomRectangle.setWidth(maxX - minX);
        this.zoomRectangle.setHeight(maxY - minY);
        this.pushCurrentZoomWindows();
        this.performZoom(this.getZoomDataWindows(), true);
        this.zoomRectangle.setVisible(false);
    }

    private void panChart(Chart chart, Point2D mouseLocation) {
        if (chart instanceof XYChart) {
            double oldMouseX = this.previousMouseLocation.getX();
            double oldMouseY = this.previousMouseLocation.getY();
            double newMouseX = mouseLocation.getX();
            double newMouseY = mouseLocation.getY();
            this.panShiftX += oldMouseX - newMouseX;
            this.panShiftY += oldMouseY - newMouseY;
            Iterator var11 = chart.getAxes().iterator();

            while (var11.hasNext()) {
                Axis axis = (Axis) var11.next();
                if (axis.getSide() != null && !this.isOmitZoomInternal(axis)) {
                    Side    side        = axis.getSide();
                    double  prevData    = axis.getValueForDisplay(side.isHorizontal() ? oldMouseX : oldMouseY);
                    double  newData     = axis.getValueForDisplay(side.isHorizontal() ? newMouseX : newMouseY);
                    double  offset      = prevData - newData;
                    boolean allowsShift = side.isHorizontal() ? this.getAxisMode().allowsX() : this.getAxisMode().allowsY();
                    if (!hasBoundedRange(axis) && allowsShift) {
                        axis.setAutoRanging(false);
                        axis.set(axis.getMin() + offset, axis.getMax() + offset);
                    }
                }
            }

            this.previousMouseLocation = mouseLocation;
        }
    }

    private void panDragged(MouseEvent event) {
        Point2D mouseLocation = this.getLocationInPlotArea(event);
        this.panChart(this.getChart(), mouseLocation);
        this.previousMouseLocation = mouseLocation;
    }

    private void panEnded() {
        Chart chart = this.getChart();
        if (chart != null && this.panShiftX != 0.0 && this.panShiftY != 0.0 && this.previousMouseLocation != null) {
            Iterator var2 = chart.getAxes().iterator();

            while (var2.hasNext()) {
                Axis axis = (Axis) var2.next();
                if (axis.getSide() != null && !this.isOmitZoomInternal(axis)) {
                    Side    side        = axis.getSide();
                    boolean allowsShift = side.isHorizontal() ? this.getAxisMode().allowsX() : this.getAxisMode().allowsY();
                    if (!hasBoundedRange(axis) && allowsShift) {
                        axis.setAutoRanging(false);
                    }
                }
            }

            this.panShiftX             = 0.0;
            this.panShiftY             = 0.0;
            this.previousMouseLocation = null;
            this.uninstallCursor();
        }
    }

    protected static boolean hasBoundedRange(Axis axis) {
        return axis.minProperty().isBound() || axis.maxProperty().isBound();
    }

    private boolean panOngoing() {
        return this.previousMouseLocation != null;
    }

    private void panStarted(MouseEvent event) {
        this.previousMouseLocation = this.getLocationInPlotArea(event);
        this.panShiftX             = 0.0;
        this.panShiftY             = 0.0;
        this.installDragCursor();
        this.clearZoomStackIfAxisAutoRangingIsEnabled();
        this.pushCurrentZoomWindows();
    }

    private void performZoom(Map.Entry<Axis, ZoomState> zoomStateEntry, boolean isZoomIn) {
        ZoomState zoomState = (ZoomState) zoomStateEntry.getValue();
        if (zoomState.zoomRangeMax - zoomState.zoomRangeMin == 0.0) {

        } else {
            Axis axis = (Axis) zoomStateEntry.getKey();
            if (isZoomIn && (axis.getSide().isHorizontal() && this.getAxisMode().allowsX() || axis.getSide().isVertical() && this.getAxisMode().allowsY())) {
                axis.setAutoRanging(false);
            }

            if (this.isAnimated()) {
                if (!hasBoundedRange(axis)) {
                    Timeline xZoomAnimation = new Timeline();
                    xZoomAnimation.getKeyFrames().setAll(new KeyFrame[]{new KeyFrame(Duration.ZERO, new KeyValue[]{new KeyValue(axis.minProperty(), axis.getMin()), new KeyValue(axis.maxProperty(), axis.getMax())}), new KeyFrame(this.getZoomDuration(), new KeyValue[]{new KeyValue(axis.minProperty(), zoomState.zoomRangeMin), new KeyValue(axis.maxProperty(), zoomState.zoomRangeMax)})});
                    xZoomAnimation.play();
                }
            } else if (!hasBoundedRange(axis)) {
                axis.set(zoomState.zoomRangeMin, zoomState.zoomRangeMax);
            }

            if (!isZoomIn) {
                axis.setAutoRanging(zoomState.wasAutoRanging);
                axis.setAutoGrowRanging(zoomState.wasAutoGrowRanging);
            }

        }
    }

    private void performZoom(Map<Axis, ZoomState> zoomWindows, boolean isZoomIn) {
        Iterator var3 = zoomWindows.entrySet().iterator();

        while (var3.hasNext()) {
            Map.Entry<Axis, ZoomState> entry = (Map.Entry) var3.next();
            if (!this.isOmitZoomInternal((Axis) entry.getKey())) {
                this.performZoom(entry, isZoomIn);
            }
        }

        var3 = this.getChart().getAxes().iterator();

        while (var3.hasNext()) {
            Axis a = (Axis) var3.next();
            a.forceRedraw();
        }

    }

    private void performZoomIn() {
        this.clearZoomStackIfAxisAutoRangingIsEnabled();
        this.pushCurrentZoomWindows();
        this.performZoom(this.getZoomDataWindows(), true);
    }

    private void pushCurrentZoomWindows() {
        if (this.getChart() != null) {
            ConcurrentHashMap<Axis, ZoomState> axisStateMap = new ConcurrentHashMap();
            Iterator                           var2         = this.getChart().getAxes().iterator();

            while (var2.hasNext()) {
                Axis axis = (Axis) var2.next();
                switch (this.getAxisMode()) {
                    case X:
                        if (axis.getSide().isHorizontal()) {
                            axisStateMap.put(axis, new ZoomState(axis.getMin(), axis.getMax(), axis.isAutoRanging(), axis.isAutoGrowRanging()));
                        }
                        break;
                    case Y:
                        if (axis.getSide().isVertical()) {
                            axisStateMap.put(axis, new ZoomState(axis.getMin(), axis.getMax(), axis.isAutoRanging(), axis.isAutoGrowRanging()));
                        }
                        break;
                    case XY:
                    default:
                        axisStateMap.put(axis, new ZoomState(axis.getMin(), axis.getMax(), axis.isAutoRanging(), axis.isAutoGrowRanging()));
                }
            }

            if (!axisStateMap.keySet().isEmpty()) {
                this.zoomStacks.addFirst(axisStateMap);
            }

        }
    }

    private void registerMouseHandlers() {
        this.registerInputEventHandler(MouseEvent.MOUSE_PRESSED, this.zoomInStartHandler);
        this.registerInputEventHandler(MouseEvent.MOUSE_DRAGGED, this.zoomInDragHandler);
        this.registerInputEventHandler(MouseEvent.MOUSE_RELEASED, this.zoomInEndHandler);
        this.registerInputEventHandler(MouseEvent.MOUSE_CLICKED, this.zoomOutHandler);
        this.registerInputEventHandler(MouseEvent.MOUSE_CLICKED, this.zoomOriginHandler);
        this.registerInputEventHandler(ScrollEvent.SCROLL, this.zoomScrollHandler);
        this.registerInputEventHandler(MouseEvent.MOUSE_PRESSED, this.panStartHandler);
        this.registerInputEventHandler(MouseEvent.MOUSE_DRAGGED, this.panDragHandler);
        this.registerInputEventHandler(MouseEvent.MOUSE_RELEASED, this.panEndHandler);
    }

    private void uninstallCursor() {
        this.getChart().setCursor(this.originalCursor);
    }

    private void zoomInDragged(MouseEvent event) {
        Bounds plotAreaBounds = this.getChart().getPlotArea().getBoundsInLocal();
        this.zoomEndPoint = limitToPlotArea(event, plotAreaBounds);
        double zoomRectX      = plotAreaBounds.getMinX();
        double zoomRectY      = plotAreaBounds.getMinY();
        double zoomRectWidth  = plotAreaBounds.getWidth();
        double zoomRectHeight = plotAreaBounds.getHeight();
        if (this.isAutoZoomEnabled()) {
            double  diffX   = this.zoomEndPoint.getX() - this.zoomStartPoint.getX();
            double  diffY   = this.zoomEndPoint.getY() - this.zoomStartPoint.getY();
            int     limit   = Math.abs(this.getAutoZoomThreshold());
            boolean isZoomX = Math.abs(diffY) <= (double) limit && Math.abs(diffX) >= (double) limit && Math.abs(diffX / diffY) > 3.0;
            boolean isZoomY = Math.abs(diffX) <= (double) limit && Math.abs(diffY) >= (double) limit && Math.abs(diffY / diffX) > 3.0;
            if (isZoomX) {
                this.setAxisMode(AxisMode.X);
            } else if (isZoomY) {
                this.setAxisMode(AxisMode.Y);
            } else {
                this.setAxisMode(AxisMode.XY);
            }
        }

        if (this.getAxisMode().allowsX()) {
            zoomRectX     = Math.min(this.zoomStartPoint.getX(), this.zoomEndPoint.getX());
            zoomRectWidth = Math.abs(this.zoomEndPoint.getX() - this.zoomStartPoint.getX());
        }

        if (this.getAxisMode().allowsY()) {
            zoomRectY      = Math.min(this.zoomStartPoint.getY(), this.zoomEndPoint.getY());
            zoomRectHeight = Math.abs(this.zoomEndPoint.getY() - this.zoomStartPoint.getY());
        }

        this.zoomRectangle.setX(zoomRectX);
        this.zoomRectangle.setY(zoomRectY);
        this.zoomRectangle.setWidth(zoomRectWidth);
        this.zoomRectangle.setHeight(zoomRectHeight);
    }

    private void zoomInEnded() {
        this.zoomRectangle.setVisible(false);
        if (this.zoomRectangle.getWidth() > 5.0 && this.zoomRectangle.getHeight() > 5.0) {
            this.performZoomIn();
        }

        this.zoomStartPoint = this.zoomEndPoint = null;
        this.uninstallCursor();
    }

    private void zoomInStarted(MouseEvent event) {
        this.zoomStartPoint = new Point2D(event.getX(), event.getY());
        this.zoomRectangle.setX(this.zoomStartPoint.getX());
        this.zoomRectangle.setY(this.zoomStartPoint.getY());
        this.zoomRectangle.setWidth(0.0);
        this.zoomRectangle.setHeight(0.0);
        this.zoomRectangle.setVisible(true);
        this.installZoomCursor();
    }

    private boolean zoomOngoing() {
        return this.zoomStartPoint != null;
    }

    private boolean zoomOut() {
        this.clearZoomStackIfAxisAutoRangingIsEnabled();
        Map<Axis, ZoomState> zoomWindows = (Map) this.zoomStacks.pollFirst();
        if (zoomWindows != null && !zoomWindows.isEmpty()) {
            this.performZoom(zoomWindows, false);
            return true;
        } else {
            return this.zoomOrigin();
        }
    }

    public static boolean isOmitZoom(Axis axis) {
        return axis instanceof Node && ((Node) axis).getProperties().get("OmitAxisZoom") == Boolean.TRUE;
    }

    public static void setOmitZoom(Axis axis, boolean state) {
        if (axis instanceof Node) {
            if (state) {
                ((Node) axis).getProperties().put("OmitAxisZoom", true);
            } else {
                ((Node) axis).getProperties().remove("OmitAxisZoom");
            }

        }
    }

    private static Point2D limitToPlotArea(MouseEvent event, Bounds plotBounds) {
        double limitedX = Math.max(Math.min(event.getX() - plotBounds.getMinX(), plotBounds.getMaxX()), plotBounds.getMinX());
        double limitedY = Math.max(Math.min(event.getY() - plotBounds.getMinY(), plotBounds.getMaxY()), plotBounds.getMinY());
        return new Point2D(limitedX, limitedY);
    }

    private static void zoomOnAxis(Axis axis, ScrollEvent event) {
        if (!hasBoundedRange(axis) && event.getDeltaY() != 0.0) {
            boolean isZoomIn     = event.getDeltaY() > 0.0;
            boolean isHorizontal = axis.getSide().isHorizontal();
            double  mousePos     = isHorizontal ? event.getX() : event.getY();
            double  posOnAxis    = axis.getValueForDisplay(mousePos);
            double  max          = axis.getMax();
            double  min          = axis.getMin();
            double  scaling      = isZoomIn ? 0.9 : 1.1111111111111112;
            double  diffHalf1    = scaling * Math.abs(posOnAxis - min);
            double  diffHalf2    = scaling * Math.abs(max - posOnAxis);
            axis.set(posOnAxis - diffHalf1, posOnAxis + diffHalf2);
            axis.forceRedraw();
        }
    }

    private class ZoomRangeSlider extends RangeSlider {
        private final BooleanProperty                  invertedSlide       = new SimpleBooleanProperty(this, "invertedSlide", false);
        private       boolean                          isUpdating;
        private final ChangeListener<Boolean>          sliderResetHandler  = (ch, o, n) -> {
            this.resetSlider(n);
        };
        private final ChangeListener<Number>           rangeChangeListener = (ch, o, n) -> {
            if (!this.isUpdating) {
                this.isUpdating = true;
                Axis xAxis = JISAZoomer.this.getChart().getFirstAxis(Orientation.HORIZONTAL);
                xAxis.getMax();
                xAxis.getMin();
                double minBound = Math.min(xAxis.getMin(), this.getMin());
                double maxBound = Math.max(xAxis.getMax(), this.getMax());
                if (JISAZoomer.this.xRangeSliderInit) {
                    this.setMin(minBound);
                    this.setMax(maxBound);
                }

                this.isUpdating = false;
            }
        };
        private final ChangeListener<Number>           sliderValueChanged  = (ch, o, n) -> {
            if (JISAZoomer.this.isSliderVisible() && n != null && !this.isUpdating) {
                this.isUpdating = true;
                Axis xAxis = JISAZoomer.this.getChart().getFirstAxis(Orientation.HORIZONTAL);
                if (!xAxis.isAutoRanging() && !xAxis.isAutoGrowRanging()) {
                    this.isUpdating = false;
                } else {
                    this.setMin(xAxis.getMin());
                    this.setMax(xAxis.getMax());
                    this.isUpdating = false;
                }
            }
        };
        private final EventHandler<? super MouseEvent> mouseEventHandler   = (event) -> {
            if (JISAZoomer.this.zoomStacks.isEmpty()) {
                JISAZoomer.this.makeSnapshotOfView();
            }

            Axis xAxis = JISAZoomer.this.getChart().getFirstAxis(Orientation.HORIZONTAL);
            xAxis.setAutoRanging(false);
            xAxis.setAutoGrowRanging(false);
            xAxis.set(this.getLowValue(), this.getHighValue());
        };

        protected void resetSlider(Boolean n) {
            if (JISAZoomer.this.getChart() != null) {
                Axis axis = JISAZoomer.this.getChart().getFirstAxis(Orientation.HORIZONTAL);
                if (Boolean.TRUE.equals(n)) {
                    this.setMin(axis.getMin());
                    this.setMax(axis.getMax());
                }

            }
        }

        public ZoomRangeSlider(Chart chart) {
            Axis xAxis = chart.getFirstAxis(Orientation.HORIZONTAL);
            JISAZoomer.this.xRangeSlider = this;
            this.setPrefWidth(-1.0);
            this.setMaxWidth(Double.MAX_VALUE);
            xAxis.invertAxisProperty().bindBidirectional(this.invertedSlide);
            this.invertedSlide.addListener((ch, o, n) -> {
                this.setRotate(Boolean.TRUE.equals(n) ? 180.0 : 0.0);
            });
            xAxis.autoRangingProperty().addListener(this.sliderResetHandler);
            xAxis.autoGrowRangingProperty().addListener(this.sliderResetHandler);
            xAxis.minProperty().addListener(this.rangeChangeListener);
            xAxis.maxProperty().addListener(this.rangeChangeListener);
            this.lowValueProperty().addListener(this.sliderValueChanged);
            this.highValueProperty().addListener(this.sliderValueChanged);
            this.setOnMouseReleased(this.mouseEventHandler);
            this.lowValueProperty().bindBidirectional(xAxis.minProperty());
            this.highValueProperty().bindBidirectional(xAxis.maxProperty());
            JISAZoomer.this.sliderVisibleProperty().addListener((ch, o, n) -> {
                if (JISAZoomer.this.getChart() != null && !n.equals(o) && !this.isUpdating) {
                    this.isUpdating = true;
                    if (Boolean.TRUE.equals(n)) {
                        JISAZoomer.this.getChart().getPlotArea().setBottom(JISAZoomer.this.xRangeSlider);
                        this.prefWidthProperty().bind(JISAZoomer.this.getChart().getCanvasForeground().widthProperty());
                    } else {
                        JISAZoomer.this.getChart().getPlotArea().setBottom((Node) null);
                        this.prefWidthProperty().unbind();
                    }

                    this.isUpdating = false;
                }
            });
            JISAZoomer.this.addButtonsToToolBarProperty().addListener((ch, o, n) -> {
                Chart chartLocal = JISAZoomer.this.getChart();
                if (chartLocal != null && !n.equals(o)) {
                    if (Boolean.TRUE.equals(n)) {
                        chartLocal.getToolBar().getChildren().add(JISAZoomer.this.zoomButtons);
                    } else {
                        chartLocal.getToolBar().getChildren().remove(JISAZoomer.this.zoomButtons);
                    }

                }
            });
            JISAZoomer.this.xRangeSliderInit = true;
        }

        public void reset() {
            this.resetSlider(true);
        }
    }

    public static class ZoomState {
        protected double  zoomRangeMin;
        protected double  zoomRangeMax;
        protected boolean wasAutoRanging;
        protected boolean wasAutoGrowRanging;

        private ZoomState(double zoomRangeMin, double zoomRangeMax, boolean isAutoRanging, boolean isAutoGrowRanging) {
            this.zoomRangeMin       = Math.min(zoomRangeMin, zoomRangeMax);
            this.zoomRangeMax       = Math.max(zoomRangeMin, zoomRangeMax);
            this.wasAutoRanging     = isAutoRanging;
            this.wasAutoGrowRanging = isAutoGrowRanging;
        }

        public double getZoomRangeMax() {
            return this.zoomRangeMax;
        }

        public double getZoomRangeMin() {
            return this.zoomRangeMin;
        }

        public String toString() {
            return "ZoomState[zoomRangeMin= " + this.zoomRangeMin + ", zoomRangeMax= " + this.zoomRangeMax + ", wasAutoRanging= " + this.wasAutoRanging + ", wasAutoGrowRanging= " + this.wasAutoGrowRanging + "]";
        }

        public boolean wasAutoGrowRanging() {
            return this.wasAutoGrowRanging;
        }

        public boolean wasAutoRanging() {
            return this.wasAutoRanging;
        }
    }
}
