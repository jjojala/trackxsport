/*
 * 
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
                final PrintStream out = new PrintStream(new FileOutputStream(gpx));
                System.out.format("Creating %s...\n", gpx.getAbsoluteFile());
                                
                out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                out.println("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creator=\"TrackSport\" version=\"1.1\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\">");
                out.println("  <metadata>");
                out.format("     <name>%s</name>\n", formatter.format(trackTime.getTime()));
                out.println("  </metadata>");

                out.println("  <trk>");
                out.println("    <trkseg>");

                int blockCnt = 0, wpTotal = 0;
                final Iterator<TrackSegment> segments = track.segments();
                while (segments.hasNext()) {
                    ++blockCnt;
                    final TrackSegment segment = segments.next();
                    
                    int wpCount = 0;
                    final Iterator<Waypoint> waypoints = segment.waypoints();
                    while (waypoints.hasNext()) {
                        ++wpCount;
                        final Waypoint waypoint = waypoints.next();
                        trackTime.add(GregorianCalendar.SECOND, waypoint.getDelay());

                        out.format( "      <trkpt lat=\"%f\" lon=\"%f\">\n",
                                waypoint.getLatitude(), waypoint.getLongitude());
                        out.format( "        <ele>%d</ele>\n", waypoint.getAltitude());
                        out.format( "        <time>%s</time>\n", formatter.format(trackTime.getTime()));
                        out.println("        <extensions>");
                        out.format( "          <gpxtpx:hr>%d</gpxtpx:hr>\n", waypoint.getHeartRate());
                        out.println("        </extensions>");
                        out.println("      </trkpt>");
                    }
                    
                    System.out.format("Block %d - %d waypoints\n", blockCnt, wpCount);
                    wpTotal += wpCount;
                }
                
                System.out.format("Processed %d waypoints\n", wpTotal);
                
                out.println("    </trkseg>");
                out.println("  </trk>");
                out.println("</gpx>");
                out.flush();
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