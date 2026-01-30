package com.isa.jutjubic.security.utils;

public class TileUtils {

    public static TileBounds tileToBounds(String tileId) {
        // tileId format: "zoom_x_y_range"
        String[] parts = tileId.split("_");
        int zoom = Integer.parseInt(parts[0]);
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);

        double n = Math.pow(2, zoom);

        double lon_min = x / n * 360.0 - 180.0;
        double lon_max = (x + 1) / n * 360.0 - 180.0;

        double lat_min = tile2lat(y + 1, zoom);
        double lat_max = tile2lat(y, zoom);

        return new TileBounds(lat_min, lat_max, lon_min, lon_max);
    }

    private static double tile2lat(int y, int zoom) {
        double n = Math.PI - 2.0 * Math.PI * y / Math.pow(2.0, zoom);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }
}
