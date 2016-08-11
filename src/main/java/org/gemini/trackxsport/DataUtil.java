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

import java.util.ArrayList;
import java.util.List;
import jssc.SerialPort;
import jssc.SerialPortException;

public final class DataUtil {
    
    public static final long DEFAULT_WAIT_TIME = 500;
    
    public static final int
            MESSAGE_SIZE_PADDING = 0x08, /* size+padding = total length */
            MESSAGE_SIZE_OFFS = 0x04;   /* uint16 */

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

    public static int readUInt16(final byte[] data, int offset) {
        return ((data[offset+1] & 0xff) << 8) | (data[offset] & 0xff);
    }

    public static int readInt32(final byte[] b, int offset) {
        return ((b[offset + 3] & 0xff) << 24) | ((b[offset + 2] & 0xff) << 16)
                | ((b[offset + 1] & 0xff) << 8) | (b[offset] & 0xff);
    }

    public static float readFloat(final byte[] b, int offset) {
        return Float.intBitsToFloat(readInt32(b, offset));
    }
    
    public static StringBuilder dump(final StringBuilder buffer,
            final byte[] data, final int offset, final int length) {

        for (int i = 0; i < length; i++) {
            if (i % 16 == 0)
                buffer.append("\n");
            buffer.append(String.format("%02x "));
        }

        return buffer;
    }

    private DataUtil() {
        throw new AssertionError("Oops, not to be instantiated!");
    }
}
