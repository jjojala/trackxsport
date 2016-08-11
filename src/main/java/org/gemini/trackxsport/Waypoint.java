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

/**
 * {@code Waypoint} is fundamentally a point of interest along with some
 * interesting data collected from the device.
 * 
 * <h1>GD-003 Protocol</h1>
 * 
 * {@code Waypoint} packages are embedded into a {@link TrackSegment}.
 * 
 * <tt><pre>
 *   | offset | type    | description
 *   |--------|---------|-----------------
 *   | 0x00   | float32 | longitude in degrees
 *   | 0x04   | float32 | latitude in degrees
 *   | 0x08   | uint16  | speed, km/h
 *   | 0x0a   | uint16  | altitude in meters
 *   | 0x0c   | uint16  | time lapsed since last waypoint, in seconds
 *   | 0x0e   | uint16  | heartbeat, 1/min
 * </pre></tt>
 */
public final class Waypoint {

    private final byte[] data;
    private final int offset;

    /** Offsets */
    public final static int
            LONGITUDE = 0x00,
            LATITUDE = 0x04,
            SPEED = 0x08,
            ALTITUDE = 0x0a,
            ELAPSED = 0x0c,
            HEARTRATE = 0x0e;
    
    public final static int SIZE = 0x10;
    
    public Waypoint(final byte[] data, final int offset) {
        this.data = data;
        this.offset = offset;
    }

    public final float getLongitude() {
        return DataUtil.readFloat(data, offset + LONGITUDE);
    }

    public final float getLatitude() {
        return DataUtil.readFloat(data, offset + LATITUDE);
    }

    public final int getSpeed() {
        return DataUtil.readUInt16(data, offset + SPEED);
    }

    public final int getAltitude() {
        return DataUtil.readUInt16(data, offset + ALTITUDE);
    }

    public final int getDelay() {
        return DataUtil.readUInt16(data, offset + ELAPSED);
    }

    public final int getHeartRate() {
        return DataUtil.readUInt16(data, offset + HEARTRATE);
    }
}
