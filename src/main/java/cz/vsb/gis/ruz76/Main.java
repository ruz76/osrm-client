package cz.vsb.gis.ruz76;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.WKBWriter;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Created by jencek on 12.11.16.
 */
public class Main {
    /*
    * Calculates routes between basic settlement units using OSRM.
    * Output is written to PostGIS dump for COPY.
    * */
    public static void main(String args[]) throws Exception {
        System.out.println("Test");
        ZsjDAO zsjdao = new ZsjDAO("jdbc:postgresql://localhost:5432/data", "user", "password");
        Map<Integer, Point> zsj = zsjdao.loadKeyValues();
        File file = new File("output.dump");
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        for (Map.Entry<Integer, Point> entry : zsj.entrySet()) {
            for (Map.Entry<Integer, Point> entry2 : zsj.entrySet()) {
                if (!entry.getKey().equals(entry2.getKey())) {
                    String route = Route.getRoute(entry.getValue(), entry2.getValue());
                    bw.write(entry.getKey() + "_" + entry2.getKey() + "\t" + PolylineDecoder.decodeToHex(route) + "\n");
                }
            }
        }
        bw.close();
    }
}