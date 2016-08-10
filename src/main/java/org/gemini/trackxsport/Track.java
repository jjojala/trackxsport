/*
 * 
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
 * described further in {@link TrackSegment}.
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
        
        return new Track(port, waitTime);
    }

    private Track(final SerialPort port, final long waitTime) 
            throws SerialPortException, InterruptedException {
        
        if (!port.writeBytes(REQUEST_MESSAGE))
            throw new SerialPortException(port.getPortName(),
                    "TrackDescriptorReader<init>", "Writing bytes failed");
        
        data = DataUtil.readBytes(port, waitTime);
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

                final int length = ProtocolUtil.readUInt16(data,
                            offset + ProtocolUtil.MESSAGE_SIZE_OFFS)
                        + ProtocolUtil.MESSAGE_SIZE_PADDING;
                final TrackSegment block = new TrackSegment(data, offset, length);
                offset += length;

                return block;
            }
        };
    }

}

