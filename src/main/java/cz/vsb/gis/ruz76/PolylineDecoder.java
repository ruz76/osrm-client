package cz.vsb.gis.ruz76;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.WKBWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Port to Java of Mark McClures Javascript PolylineEncoder :
 * http://facstaff.unca.edu/mcmcclur/GoogleMaps/EncodePolyline/decode.js
 * Source: https://github.com/scoutant/polyline-decoder
 */
public class PolylineDecoder {
    private static final double DEFAULT_PRECISION = 1E5;

    public static String decodeToHex(String encoded) {
        List<Point> pl = decode(encoded);
        Coordinate cor[] = new Coordinate[pl.size()];
        for (int i=0; i<pl.size(); i++) {
            cor[i] = new Coordinate(pl.get(i).getLng(), pl.get(i).getLat());
        }

        GeometryFactory gf = new GeometryFactory();
        LineString ls = gf.createLineString(cor);

        WKBWriter wkbw = new WKBWriter();
        byte[] b = wkbw.write(ls);

        return wkbw.bytesToHex(b);
    }

    public static List<Point> decode(String encoded) {
        return decode(encoded, DEFAULT_PRECISION);
    }

    /**
     * Precision should be something like 1E5 or 1E6. For OSRM routes found precision was 1E6, not the original default
     * 1E5.
     *
     * @param encoded
     * @param precision
     * @return
     */
    public static List<Point> decode(String encoded, double precision) {
        List<Point> track = new ArrayList<Point>();
        int index = 0;
        int lat = 0, lng = 0;

        while (index < encoded.length()) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            Point p = new Point((double) lat / precision, (double) lng / precision);
            track.add(p);
        }
        return track;
    }

}
