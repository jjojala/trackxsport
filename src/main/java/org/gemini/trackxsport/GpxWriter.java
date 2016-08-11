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

import java.io.PrintWriter;
import java.util.GregorianCalendar;
import java.util.Stack;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

public class GpxWriter {

    private static final DatatypeFactory xmlTypes;
    
    static {
        try {
            xmlTypes = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException ex) {
            throw new InstantiationError();
        }
    }
    
    private final PrintWriter writer;
    private final GregorianCalendar beginTime;

    public GpxWriter(final PrintWriter writer, final GregorianCalendar beginTime) {
        this.writer = writer;
        this.beginTime = beginTime;

        this.writer.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\"\n" +
                "    xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    creator=\"http://github.com/jjojala/trackxsport\"\n" +
                "    version=\"1.1\"\n" +
                "    xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1\n" +
                "        http://www.topografix.com/GPX/1/1/gpx.xsd\n" +
                "        http://www.garmin.com/xmlschemas/TrackPointExtension/v1\n" +
                "        http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\">\n" +
                "  <metadata>\n" +
                "    <time>%s</time>\n" +
                "  </metadata>\n\n", xmlTypes.newXMLGregorianCalendar(this.beginTime));
    }
    
    public GpxWriter beginTrack(final int trackId) {
        writer.format("  <trk>\n");
        writer.println("    <src>GD-003 Sports Watch /w GPS and BT heart rate monitor, rev E3.628</src>");
        writer.format("    <number>%d</number>\n", trackId);
        
        return this;
    }
    
    public GpxWriter endTrack() {
        writer.format("  </trk>\n");
        
        return this;
    }
    
    public GpxWriter beginTrackSegment() {
        writer.println("      <trkseg>");
        return this;
    }
    
    public GpxWriter endTrackSegment() {
        writer.println("      </trkseg>");
        return this;
    }
    
    public GpxWriter writeWaypoint(final Waypoint waypoint) {
        beginTime.add(GregorianCalendar.SECOND, waypoint.getDelay());
        writer.format("        <trkpt lat=\"%f\" lon=\"%f\">\n",
                waypoint.getLatitude(), waypoint.getLongitude());
        writer.format("          <ele>%d</ele>\n", waypoint.getAltitude());
        writer.format("          <time>%s</time>\n",
                xmlTypes.newXMLGregorianCalendar(beginTime));
        writer.println("          <extensions>");
        writer.format("            <gpxtpx:hr>%d</gpxtpx:hr>\n", waypoint.getHeartRate());
        writer.println("          </extensions>");
        writer.println("        </trkpt>");

        return this;
    }
    
    public void close() {
        writer.append("</gpx>\n");
    }
}
