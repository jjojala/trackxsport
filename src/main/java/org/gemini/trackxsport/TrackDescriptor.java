/*
 * Copyright (c) 2016 Jari Ojala (jari.ojala@iki.fi)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
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
 * A more detailed investigation revealed, that the request follows the generic
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

    public static final int
            DESC_BEGIN = 0x06,
            DESC_LEN = 0x20;
    
    /** Field offsets */
    public static final int 
            DESC_TRACK_OFFS = 0x00,     /* byte */
            DESC_WPCNT_OFFS = 0x04,     /* uint16, waypoint count */
            DESC_YEAR_OFFS = 0x08,      /* byte, years after 2000 */
            DESC_MONTH_OFFS = 0x09,     /* byte, 0..11 */
            DESC_MDAY_OFFS = 0x0a,      /* byte, 0..30 */
            DESC_HOUR_OFFS = 0x0b,      /* byte, 0..23 (GMT) */
            DESC_MINUTE_OFFS = 0x0c,    /* byte, 0..59 */
            DESC_SECS_OFFS = 0x0d,      /* byte, 0..59 */
            DESC_CALS_OFFS = 0x1e;      /* uint16, kcal/10 */


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
        for (int offset = DESC_BEGIN; (offset + DESC_LEN) < data.length;   ) {
            final TrackDescriptor descriptor =
                    new TrackDescriptor(data, offset);
            descriptors.put(descriptor.getTrackId(), descriptor);
            offset += DESC_LEN;
        }

        return descriptors;
    }

    private TrackDescriptor(final byte[] data, final int offset) { 
        this.data = data;
        this.offset = offset;
        System.out.format("Found descriptor for track %02d\n", getTrackId());
    }

    public final int getTrackId() {
        return data[offset + DESC_TRACK_OFFS];
    }

    public final int getWaypointCount() {
        return DataUtil.readUInt16(
                data, offset + DESC_WPCNT_OFFS);
    }

    public final GregorianCalendar getTrackBeginTime() {
        return new GregorianCalendar(
                data[offset + DESC_YEAR_OFFS] + 2000,
                data[offset + DESC_MONTH_OFFS] - 1,
                data[offset + DESC_MDAY_OFFS],
                data[offset + DESC_HOUR_OFFS],
                data[offset + DESC_MINUTE_OFFS],
                data[offset + DESC_SECS_OFFS]);
    }

    public final int getCalories() {
        return DataUtil.readUInt16(data, offset + DESC_CALS_OFFS) * 10;
    }
}
