package com.ev.evrouter.util;

import java.util.List;

public class PolylineEncoder {

    public static String encode(List<double[]> points) {
        StringBuilder result = new StringBuilder();
        long lastLat = 0;
        long lastLng = 0;

        for (double[] point : points) {
            // ORS GeoJSON is [lon, lat], we need [lat, lon] for encoding
            long lat = Math.round(point[1] * 1e5);
            long lng = Math.round(point[0] * 1e5);

            long dLat = lat - lastLat;
            long dLng = lng - lastLng;

            encodeValue(dLat, result);
            encodeValue(dLng, result);

            lastLat = lat;
            lastLng = lng;
        }
        return result.toString();
    }

    private static void encodeValue(long value, StringBuilder result) {
        value = value < 0 ? ~(value << 1) : (value << 1);
        while (value >= 0x20) {
            result.append(Character.toChars((int) ((0x20 | (value & 0x1f)) + 63)));
            value >>= 5;
        }
        result.append(Character.toChars((int) (value + 63)));
    }
}