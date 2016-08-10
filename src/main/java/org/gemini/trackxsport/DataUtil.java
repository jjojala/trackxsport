/*
 * 
 */
package org.gemini.trackxsport;

import java.util.ArrayList;
import java.util.List;
import jssc.SerialPort;
import jssc.SerialPortException;

public final class DataUtil {
    
    public static final long DEFAULT_WAIT_TIME = 500;
    
    public static byte[] concat(final byte[] result, final List<byte[]> buffers) {
        if (result == null || result.length == 0)
            return result;
        
        int pos = 0;
        for (final byte[] buffer: buffers) {
            System.arraycopy(buffer, 0, result, pos, buffer.length);
            pos += buffer.length;
        }
        
        return result;
    }

    public static byte[] readBytes(final SerialPort port, final long waitTime) 
            throws InterruptedException, SerialPortException {
        final List<byte[]> buffers = new ArrayList<>();        
        int length = 0;

        while (true) {
            if (port.getInputBufferBytesCount() <= 0)
                Thread.sleep(waitTime);

            final byte[] buffer = port.readBytes();
            if (buffer == null)
                break;
            
            buffers.add(buffer);
            length += buffer.length;
        }

        return concat(new byte[length], buffers);
    }
    
    public static String dump(final byte[] data, final int offset, final int count) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(String.format("data.length=%d, data[%d]={ ", data.length, offset));
        for (int i = 0; i < count; i++) {
            buffer.append(String.format("0x%02x ", data[offset+i]));
        }
        buffer.append(" ...");
        
        return buffer.toString();
    }

    private DataUtil() {
        throw new AssertionError("Oops, not to be instantiated!");
    }
}
