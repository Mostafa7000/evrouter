package com.ev.evrouter.util;

import java.util.List;

public class GeometryUtil {

    private static final int EARTH_RADIUS_KM = 6371;

    // Haversine formula for distance between two points
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    // Check if a point is within 'maxDistanceKm' of ANY segment in the polyline
    public static boolean isNearRoute(double stationLat, double stationLon, List<double[]> polyline, double maxDistanceKm) {
        // Simple optimization: check distance to points first (faster than segments)
        // For high precision, you strictly need point-to-segment distance,
        // but checking against polyline vertices is usually sufficient for EV routing
        // if the polyline resolution is high.

        for (double[] point : polyline) {
            // polyline format from your code is [lon, lat]
            double routeLon = point[0];
            double routeLat = point[1];

            if (haversine(stationLat, stationLon, routeLat, routeLon) <= maxDistanceKm) {
                return true;
            }
        }
        return false;
    }
}