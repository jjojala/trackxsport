/*
 * 
 */
package org.gemini.trackxsport;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * {@code TrackDescriptor} represents the meta-information of a {@link Track}.
 * 
 * <h1>GD-003 Protocol</h1>
 * 
 * In GD-003, this meta-information is retrieved with the following request:
 * <pre><tt>
 *   0x48 0x59 0x03 0x01 0x00 0x00 0x04 0x0f
 * </tt></pre>
 * 
 * A more detailed investigation reveals, that the request follows the generic
 * message pattern, described e.g. in {@link Track}. However, as the request
 * have no parameters, the data size (offset 0x04) is 0.
 * 
 * <p>The reply message contains usual header, plus zero or more blocks 
 * representing meta-data of {@link TrackSegment TrackSegments}:
 * 
 * <pre><tt>
 *   | offset | type   | description
 *   |--------|--------|-----------------------
 *   | 0x00   | byte   | fixed 0x48 ('H')
 *   | 0x01   | byte   | fixed 0x59 ('Y')
 *   | 0x02   | byte   | fixed 0x03 (family "tracks")
 *   | 0x03   | byte   | fixed 0x01 (command "get descriptors")
 *   | 0x04   | uint16 | data size, data begins at offset 0x06, but excludes
 *   |                   the tailing checksum bytes.
 *   | 0x06   |        | first descriptor begins... (see below)
 *   |        | byte   | unknown checksum, ignored
 *   |        | byte   | unknown checksum, ignored
 
 * </tt></pre>
 * 
 * Each descriptor follows the pattern below:
 * <pre><tt>
 *   | offset | type   | description
 *   |--------|--------|---------------------
 *   | 0x00   | uint16 | track identifier
 *   | 0x04   | uint16 | number of waypoints ... in first segment... ???
 *   | 0x08   | byte   | years after 2000
 *   | 0x09   | byte   | month 1...12
 *   | 0x0a   | byte   | day of month 1...31
 *   | 0x0b   | byte   | hours 0...23, in GMZ
 *   | 0x0c   | byte   | minutes, 0...59
 *   | 0x0d   | byte   | seconds, 0...59
 *   | 0x1e   | uint16 | total calories/10, i.e. 77 represnts 770cal 
 * </tt></pre>
 */
public class TrackDescriptor {
    
    private static final byte[] REQUEST_MESSAGE = {
        0x48, 0x59, 0x03, 0x01, 0x00, 0x00, 0x04, 0x0f
    };

    private final byte[] data;
    private final int offset;

    public static Map<Integer, TrackDescriptor> getTrackDescriptors(
                final SerialPort port, final long waitTime)
        throws SerialPortException, InterruptedException
    {
        if (!port.writeBytes(REQUEST_MESSAGE))
            throw new SerialPortException(port.getPortName(),
                "TrackDescriptor.request", "Sending request failed");

        final byte[] data = DataUtil.readBytes(port, waitTime);
        assert data != null && data.length > 10 && data[0] == 0x48
                && data[1] == 0x59 && data[2] == 0x03 && data[3] == 0x01;
                
        System.out.format("Read %d bytes for track descriptors\n", data.length);
        final Map<Integer, TrackDescriptor> descriptors = new HashMap<>();
        for (int offset = ProtocolUtil.DESC_BEGIN;
                (offset + ProtocolUtil.DESC_LEN) < data.length;   ) {
            final TrackDescriptor descriptor =
                    new TrackDescriptor(data, offset);
            descriptors.put(descriptor.getTrackId(), descriptor);
            offset += ProtocolUtil.DESC_LEN;
        }

        return descriptors;
    }

    private TrackDescriptor(final byte[] data, final int offset) { 
        this.data = data;
        this.offset = offset;
        System.out.format("Found descriptor for track %02d\n", getTrackId());
    }

    public final int getTrackId() {
        return data[offset + ProtocolUtil.DESC_TRACK_OFFS];
    }

    public final int getWaypointCount() {
        return ProtocolUtil.readUInt16(
                data, offset + ProtocolUtil.DESC_WPCNT_OFFS);
    }

    public final GregorianCalendar getTrackBeginTime() {
        return new GregorianCalendar(
                data[offset + ProtocolUtil.DESC_YEAR_OFFS] + 2000,
                data[offset + ProtocolUtil.DESC_MONTH_OFFS] - 1,
                data[offset + ProtocolUtil.DESC_MDAY_OFFS],
                data[offset + ProtocolUtil.DESC_HOUR_OFFS],
                data[offset + ProtocolUtil.DESC_MINUTE_OFFS],
                data[offset + ProtocolUtil.DESC_SECS_OFFS]);
    }

    public final int getCalories() {
        return ProtocolUtil.readUInt16(data, offset + ProtocolUtil.DESC_CALS_OFFS) * 10;
    }
}
