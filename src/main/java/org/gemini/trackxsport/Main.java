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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import jssc.SerialPort;
import jssc.SerialPortException;

public class Main {        
    
    public static void main(final String[] args) {
        int status = 0;
        final SerialPort port = new SerialPort("COM4");
        try {
            port.openPort();
            port.setParams(57600, 8, 1, 0);
/*
            { // version
                port.writeBytes(new byte[] {
                    0x48, 0x59, 0x02, 0x01, 0x00, 0x00, 0x03, 0x0b });  

                final byte[] response = port.readBytes(15);
                for (byte b: response)
                    System.out.format("%02X ", b);
                System.out.println();
            }
*/            

            final SimpleDateFormat formatter =
                    new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.000'Z'");

            final Map<Integer, TrackDescriptor> tracks =
                    TrackDescriptor.getTrackDescriptors(port, DataUtil.DEFAULT_WAIT_TIME);
            
            {
                final Track track = Track.getTrack(port, DataUtil.DEFAULT_WAIT_TIME);
                System.out.format("Found data for track %02d\n", track.getTrackId());

                final TrackDescriptor desc = tracks.get(track.getTrackId());
                final GregorianCalendar trackTime = desc.getTrackBeginTime();
                
                final File gpx = new File(
                    new SimpleDateFormat(String.format(
                            "YYYY-MM-dd-\'%02d.gpx\'", track.getTrackId()))
                            .format(desc.getTrackBeginTime().getTime()));

                System.out.format("Creating %s...\n", gpx.getAbsoluteFile());
                
                final GpxWriter out = new GpxWriter(new PrintStream(
                        new FileOutputStream(gpx)), trackTime);
                                
                out.beginTrack(track.getTrackId());

                int blockCnt = 0, wpTotal = 0;
                final Iterator<TrackSegment> segments = track.segments();
                while (segments.hasNext()) {
                    out.beginTrackSegment();
                    ++blockCnt;
                    final TrackSegment segment = segments.next();
                    
                    int wpCount = 0;
                    final Iterator<Waypoint> waypoints = segment.waypoints();
                    while (waypoints.hasNext()) {
                        ++wpCount;
                        out.writeWaypoint(waypoints.next());
                    }
                    
                    System.out.format("Block %d - %d waypoints\n", blockCnt, wpCount);
                    wpTotal += wpCount;
                    out.endTrackSegment();
                }
                
                System.out.format("Processed %d waypoints\n", wpTotal);
                
                out.endTrack();
                out.close();
            }
        }
        
        catch (final Exception ex) {
            ex.printStackTrace(System.err);
            status = 1;
        }
        
        finally {
            try { port.closePort(); }
            catch (final SerialPortException ex) {
                ex.printStackTrace(System.err);
            }
        }
        
        System.exit(status);        
    }
}