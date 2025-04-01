package jisa.devices.camera.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;
import jisa.results.Column;
import jisa.results.ResultList;
import jisa.results.ResultTable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface MultiTrack extends Feature {

    /**
     * Sets whether the camera is in multi-track mode.
     *
     * @param enabled Enable multi-track mode?
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    void setMultiTrackEnabled(boolean enabled) throws IOException, DeviceException;

    /**
     * Returns whether the camera is currently in multi-track mode.
     *
     * @return Is it in multi-track mode?
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    boolean isMultiTrackEnabled() throws IOException, DeviceException;

    /**
     * Sets the tracks for the camera to use when in mult-track mode.
     *
     * @param tracks Tracks to use.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    void setMultiTracks(Collection<Track> tracks) throws IOException, DeviceException;

    /**
     * Returns the tracks set to be used by the camera when in multi-track mode.
     *
     * @return Tracks to use.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    List<Track> getMultiTracks() throws IOException, DeviceException;

    /**
     * Sets the tracks for the camera to use when in mult-track mode.
     *
     * @param tracks Tracks to use.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    default void setMultiTracks(Track... tracks) throws IOException, DeviceException {
        setMultiTracks(Arrays.asList(tracks));
    }

    /**
     * Adds a single track to the end of the list of tracks to use when the camera is in multi-track mode.
     *
     * @param startRow Index of the first row in the track.
     * @param endRow   Index of the last row in the track.
     * @param isBinned Whether the camera should bin the contained rows together to effectively make a single row.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    default void addMultiTrack(int startRow, int endRow, boolean isBinned) throws IOException, DeviceException {

        List<Track> tracks = getMultiTracks();
        tracks.add(new Track(startRow, endRow, isBinned));
        setMultiTracks(tracks);

    }

    /**
     * Adds a single track at the specified index in the list of tracks to use when the camera is in multi-track mode.
     *
     * @param startRow Index of the first row in the track.
     * @param endRow   Index of the last row in the track.
     * @param isBinned Whether the camera should bin the contained rows together to effectively make a single row.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    default void addMultiTrack(int index, int startRow, int endRow, boolean isBinned) throws IOException, DeviceException {

        List<Track> tracks = getMultiTracks();
        tracks.add(index, new Track(startRow, endRow, isBinned));
        setMultiTracks(tracks);

    }

    /**
     * Removes a single track, from the list of tracks to use when the camera is in multi-track mode, at the given index.
     *
     * @param index Index of track to remove.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    default void removeMultiTrack(int index) throws IOException, DeviceException {

        List<Track> tracks = getMultiTracks();
        tracks.remove(index);
        setMultiTracks(tracks);

    }

    /**
     * Class to represent individual tracks in a multi-track camera configuration.
     */
    class Track {

        private final int     startRow;
        private final int     endRow;
        private final boolean binned;

        public Track(int startRow, int endRow, boolean binned) {
            this.startRow = startRow;
            this.endRow   = endRow;
            this.binned   = binned;
        }

        public int getStartRow() {
            return startRow;
        }

        public int getEndRow() {
            return endRow;
        }

        public boolean isBinned() {
            return binned;
        }

    }

    static void addParameters(MultiTrack inst, Class<?> target, ParameterList parameters) {

        Column<Integer> start  = Column.ofIntegers("Start Row");
        Column<Integer> end    = Column.ofIntegers("End Row");
        Column<Boolean> binned = Column.ofBooleans("Binned");
        ResultTable     table  = new ResultList(start, end, binned);

        parameters.addOptional("Multi-Track", inst::isMultiTrackEnabled, false, () -> {

            ResultTable tab  = new ResultList(start, end, binned);

            for (Track track : inst.getMultiTracks()) {
                tab.addData(track.getStartRow(), track.getEndRow(), track.isBinned());
            }

            return tab;

        }, table, t -> inst.setMultiTrackEnabled(false), t -> {

            inst.setMultiTracks(t.stream().map(r -> new Track(r.get(start), r.get(end), r.get(binned))).collect(Collectors.toList()));
            inst.setMultiTrackEnabled(true);

        });

    }

}
