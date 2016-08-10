/*
 *
 */
package org.gemini.trackxsport;

/**
 *
 */
public class ProtocolUtil {

    public static final int
            MESSAGE_SIZE_PADDING = 0x08, /* size+padding = total length */
            MESSAGE_SIZE_OFFS = 0x04;   /* uint16 */

    public static final int 
            DESC_BEGIN = 0x06,
            DESC_LEN = 0x20,
            DESC_TRACK_OFFS = 0x00,     /* byte */
            DESC_WPCNT_OFFS = 0x04,     /* uint16, waypoint count */
            DESC_YEAR_OFFS = 0x08,      /* byte, years after 2000 */
            DESC_MONTH_OFFS = 0x09,     /* byte, 0..11 */
            DESC_MDAY_OFFS = 0x0a,      /* byte, 0..30 */
            DESC_HOUR_OFFS = 0x0b,      /* byte, 0..23 (GMT) */
            DESC_MINUTE_OFFS = 0x0c,    /* byte, 0..59 */
            DESC_SECS_OFFS = 0x0d,      /* byte, 0..59 */
            DESC_CALS_OFFS = 0x1e;      /* uint16, kcal/10 */
    
    public static final int
            TRACK_WP_COUNT = 0x0a,      /* uint16 */
            TRACK_ID_OFFS = 0x06;       /* byte */
    
    public static final int
            WP_BEGIN = 0x26,
            WP_LEN = 0x10,
            WP_LON = 0x00,              /* float32, longitude [degrees] */
            WP_LAT = 0x04,              /* float32, latitude [degrees] */
            WP_SPEED = 0x08,            /* uint16, speed [km/h] */
            WP_ALTITUDE = 0x0a,         /* uint16 */
            WP_DELAY = 0x0c,            /* uint16, seoonds since last wp */
            WP_HEARTRATE = 0x0e;        /* uint16, heartbeat rate [1/min] */

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

    private ProtocolUtil() {
        throw new AssertionError("Oops. Not to be instantiated.");
    }
}
