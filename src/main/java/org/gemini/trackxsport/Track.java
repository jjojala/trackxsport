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

import java.util.Iterator;
import java.util.NoSuchElementException;
import jssc.SerialPort;
import jssc.SerialPortException;

/*
 *
 * <h1>GD-003 Protocol</h1>
 *
 * <p>The content of each request:
 * <pre><tt>
 *  | offset | type   | description
 *  |--------|--------|------------------------
 *  | 0x00   | byte   | fixed 'H' (0x48)
 *  | 0x01   | byte   | fixed 'Y' (0x59)
 *  | 0x02   | byte   | fixed 0x03 ("request family")
 *  | 0x03   | byte   | fixed 0x02 ("get track")
 *  | 0x04   | uint16 | fixed 0x01 (message length, always the same)
 *  | 0x06   | byte   | track id to be fetched, but this is ignored by the
 *  |                   device. Instead, regardless of this, the most recent
 *  |                   track is always returned. This is likely a bug in
 *  |                   GD-003.
 *  | 0x07   | byte   | fixed 0x07 (first checksum byte, algorithm unknown,
 *  |                   but this works for this message).
 *  | 0x08   | byte   | fixed 0x1b (second checksum byte, works for this)
 * </tt></pre>
 *
 * <p>The reply is one, or typically several track segment packages,
 * described further in {@link TrackSegment}. Note, that even though
 * the request supports giving the desired track identifier as a parameter,
 * the device seems to reply with the most latest track it has recorded, i.e.
 * the given parameter is not honored. This seems to an obvious bug in the
 * device firmware (and may be fixed in some forthcoming versions).
 */
public final class Track {
    
    private static final byte[] REQUEST_MESSAGE = {
        0x48, 0x59, 0x03, 0x02, 0x01, 0x00, 0x01, 0x07, 0x1b
    };

    private byte[] data;

    public static Track getTrack(final SerialPort port, final long waitTime)
            throws SerialPortException, InterruptedException {
        if (!port.writeBytes(REQUEST_MESSAGE))
            throw new SerialPortException(port.getPortName(),
                    "Track.requestTrack", "Writing request failed");
        
        return new Track(DataUtil.readBytes(port, waitTime));
    }

    private Track(final byte[] data) {        
        this.data = data;
    }
    
    public int getTrackId() {
        return data[TrackSegment.TRACK_ID];
    }
    
    public Iterator<TrackSegment> segments() {
        
        return new Iterator<TrackSegment>() {
            
            private int offset = 0;
            
            @Override
            public boolean hasNext() {
                return (offset < data.length)
                        && (data[offset+2] == 0x03)
                        && (data[offset+3] == 0x02);
            }

            @Override
            public TrackSegment next() {
                if (!hasNext())
                    throw new NoSuchElementException();

                final int length = DataUtil.readUInt16(data,
                            offset + DataUtil.MESSAGE_SIZE_OFFS)
                        + DataUtil.MESSAGE_SIZE_PADDING;
                final TrackSegment block = new TrackSegment(data, offset, length);
                offset += length;

                return block;
            }
        };
    }

}

