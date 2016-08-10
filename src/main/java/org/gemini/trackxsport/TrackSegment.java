/*
 *
 */
package org.gemini.trackxsport;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * {@code TrackSegment} represents an ordered list of logically connected
 * {@link Waypoint Waypoints}. For example, {@code TrackSegment} may be
 * a list of waypoints between start, stop or laps of an exercise. Other
 * possible use is a case where the GPS signal is lost for a while, in which
 * case the {@code Waypoints} may be ordered into two separate {@code
 * TrackSegments}.
 * 
 * <p>In case of GD-003, a {@code TrackSegment} represents a list of
 * waypoints embeded into a single block of data received from the
 * device. It's a bit unclear, if this behaviour in GD-003 is related to e.g.
 * loss of GPS-signal, or just a practicality of avoiding huge blocks of
 * data across serial line... (TODO: investigate different blcoks a bit...)
 * 
 * <p>An instance of this type is primarily instantiated by
 * {@link Track#segments()}, e.g:
 * 
 * <pre><tt> 
 *  final Track track = ...
 *  final Iterator<TrackSegment> segments = track.segments();}
 * </tt></pre>
 * 
 * <h1>GD-003 Protocol</h1>
 * 
 * When requesting a track (refer to {@link Track}), the device replies
 * at least one, but usually several data packages, each representing a 
 * {@code TrackSegment}. So far haven't found a way to know all packages
 * have been read, except waiting a bit before giving up. Reading is
 * done in {@link Track#getTrack(jssc.SerialPort, long)}.
 * 
 * <p>There's some evidence, that at least occasionally, the last message
 * sent by the device contains the following bytes or part of it:
 * 
 * <pre><tt>
 *   0x48 0x59 0x01 0x00 0x02 0x00 0x03 0x02 0x08 0x16
 * </pre></tt>
 * 
 * <p>The content of each segment package:
 * <pre><tt>
 *  | offset | type   | description
 *  |--------|--------|------------------------
 *  | 0x00   | byte   | fixed 'H' (0x48)
 *  | 0x01   | byte   | fixed 'Y' (0x59)
 *  | 0x02   | byte   | fixed 0x03 ("request family")
 *  | 0x03   | byte   | fixed 0x02 ("get track")
 *  | 0x04   | uint16 | data length (data begins at offset 0x06, checksum
 *  |                   bytes excluded.
 *  | 0x06   | byte   | track identifier
 *  | 0x08   | byte   | segment number
 *  | 0x0a   | uint16 | number of waypoints in this segment.
 *  | 0x26   |        | offset of first waypoint. See {@link Waypoint}.
 *  |        | byte   | checksum, unknown algorithm, currently not verified
 *  |        | byte   | checksum, unknown algorithm
 * </tt></pre>
 */
public final class TrackSegment {

    private final byte[] data;
    private final int offset;
    private final int length;
    
    /** Field offsets. */
    public static final int
            TRACK_ID = 0x06,
            SEGMENT_NO = 0x08,
            WAYPOINT_COUNT = 0x0a,
            WAYPOINTS = 0x26;
    
    /**
     * Create new {@code TrackSegment}. The invocation requires no I/O, but
     * expects that the valid {@code data} is available at given {@code offset}.
     * 
     * @param data reference to the array of bytes containing the segment data.
     * @param offset offset of this segment data in {@code data}.
     * @param length the number of bytes used for this segment.
     */
    public TrackSegment(final byte[] data, final int offset, final int length) {
        this.data = data;
        this.offset = offset;
        this.length = length;

        assert (data[offset] == 0x48 && data[offset + 1] == 0x59
                && data[offset + 2] == 0x03 && data[offset + 3] == 0x02);
    }

    /** Get the track identifier associated to this segment. */
    public final int getTrackId() {
        return data[offset + TRACK_ID];
    }
    
    /** Get the segment sequence number, 0...n */
    public final int getSegmentNo() {
        return data[offset + SEGMENT_NO];
    }
    
    /** Get number of {@link Waypoint Waypoints} included in this
     * {@code TrackSegment}. */
    public final int getWaypointCount() {
        return data[offset + WAYPOINT_COUNT];
    }

    /**
     * Get an iterator to the ordered {@link Waypoint Waypoints} held by this
     * segment.
     */
    public Iterator<Waypoint> waypoints() {
        return new Iterator<Waypoint>() {
            private int current = offset + WAYPOINTS;
            private final int last = offset + TrackSegment.this.length;

            @Override
            public boolean hasNext() {
                return (this.current + Waypoint.SIZE) < this.last;
            }

            @Override
            public Waypoint next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                
                final Waypoint next = new Waypoint(data, this.current);
                this.current += Waypoint.SIZE;
                return next;
            }
        };
    }
}
