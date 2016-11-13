package cz.vsb.gis.ruz76;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;

/**
 * Created by jencek on 12.11.16.
 */
public class Route {
    public static String getRoute(Point from, Point to) throws Exception {
        //String url = "http://192.168.100.3:5000/route/v1/driving/" + from.getLng() + "," + from.getLat() + ";" + to.getLng() + "," + to.getLat() + "?steps=false&alternatives=false";
        String url = "http://localhost:5000/route/v1/driving/" + from.getLng() + "," + from.getLat() + ";" + to.getLng() + "," + to.getLat() + "?steps=false&alternatives=false";
        JsonNode rootNode = new ObjectMapper().readTree(new URL(url));
        String geometry = rootNode.at("/routes/0/geometry").asText();
        return geometry;
    }
}
