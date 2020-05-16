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

    ZsjDAO zsjdao = new ZsjDAO("jdbc:postgresql://localhost:5432/data", "user", "pwd");

    Map<Integer, Point> zsj = zsjdao.loadKeyValues();
    Intersect zsjintersect = new Intersect();

    File file = new File("output.dump");
    FileWriter fw = new FileWriter(file.getAbsoluteFile());
    BufferedWriter bw = new BufferedWriter(fw);

    File filezsjs = new File("zsjs.sql");
    FileWriter fwzsjs = new FileWriter(filezsjs.getAbsoluteFile());
    BufferedWriter bwzsjs = new BufferedWriter(fwzsjs);

    File filezsjs_moreprecise = new File("zsjs_moreprecise.sql");
    FileWriter fwzsjs_moreprecise = new FileWriter(filezsjs_moreprecise.getAbsoluteFile());
    BufferedWriter bwzsjs_moreprecise = new BufferedWriter(fwzsjs_moreprecise);

    for (Map.Entry<Integer, Point> entry : zsj.entrySet()) {
      for (Map.Entry<Integer, Point> entry2 : zsj.entrySet()) {
        if (!entry.getKey().equals(entry2.getKey())) {
          String route = Route.getRoute(entry.getValue(), entry2.getValue());
          bwzsjs.write(zsjintersect.getRoute_Zsjs(PolylineDecoder.decodeToLineString(route), entry.getKey() + "_" + entry2.getKey()) + "\n");
          bwzsjs_moreprecise.write(zsjintersect.getRoute_Zsjs_MorePrecise(PolylineDecoder.decodeToLineString(route), entry.getKey() + "_" + entry2.getKey()) + "\n");
          bw.write(entry.getKey() + "_" + entry2.getKey() + "\t" + PolylineDecoder.decodeToHex(route) + "\n");
        }
      }
    }
    bwzsjs_moreprecise.close();
    bwzsjs.close();
    bw.close();
    zsjintersect.close();
  }
}