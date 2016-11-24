package cz.vsb.gis.ruz76;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Point;
import org.geotools.data.DataUtilities;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ruz76 on 24.11.2016.
 */
public class Intersect {
	private final SimpleFeatureCollection zsj_fc;
    public static Point start;
    public Intersect() throws IOException {
        FileDataStore zsj_store = FileDataStoreFinder.getDataStore(new File("zsj.shp"));
        SimpleFeatureSource zsj_fs = zsj_store.getFeatureSource();
        zsj_fc = DataUtilities.collection(zsj_fs.getFeatures());
    }
	
	public void close() throws Exception {
		//zsj_store.dispose();
	}

    /*
    * Prints zsj that intersects with routes ordered based on route
    * This is the simplest and hopefully fastest way based only on distance from start point
    * */
    public String getRoute_Zsjs(LineString route, String routeid) throws Exception {
		String zsjs_string = "";
        start = route.getStartPoint();
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
        Filter filter = ff.intersects(ff.property("the_geom"), ff.literal(route));
        SimpleFeatureIterator zsj_fs_sfi = zsj_fc.subCollection(filter).features();
        //System.out.println("Route id: " + routeid);
        List<SimpleFeature> zsjs = new ArrayList();
        while (zsj_fs_sfi.hasNext()) {
            SimpleFeature zsj = zsj_fs_sfi.next();
            zsjs.add(zsj);
        }
        Collections.sort(zsjs, new CentroidDistanceComparator());
		for (int i = 0; i < zsjs.size(); i++) {
			SimpleFeature zsj = (SimpleFeature) zsjs.get(i);
			zsjs_string += "INSERT INTO zsjs (zsjid_from_to, zsjid, seq) VALUES ('" + routeid + "', '" + zsj.getAttribute("zsjid") + "', " + i + ");\n";
		}
		return zsjs_string;
        
    }

    /*
    * Prints zsj that intersects with routes ordered based on route
    * This is the more complicated way and slower way based on comparison of each segment in linestring
    * */
    public String getRoute_Zsjs_MorePrecise(LineString route, String routeid) throws Exception {
		String zsjs_string = "";
        //System.out.println("Route id: " + routeid);
        GeometryFactory gf = new GeometryFactory();
        Coordinate cs[] = new Coordinate[2];
        List<SimpleFeature> zsjs = new ArrayList();
        for (int i = 0; i < route.getNumPoints() - 1; i++) {
            cs[0] = route.getPointN(i).getCoordinate();
            cs[1] = route.getPointN(i+1).getCoordinate();
            LineString segment = gf.createLineString(cs);
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
            Filter filter = ff.intersects(ff.property("the_geom"), ff.literal(segment));
            SimpleFeatureIterator zsj_fs_sfi = zsj_fc.subCollection(filter).features();
            double segment_lenght = 0;
            String zsjid = "";
            while (zsj_fs_sfi.hasNext()) {
                SimpleFeature zsj = zsj_fs_sfi.next();
                MultiPolygon zsj_polygon = (MultiPolygon) zsj.getDefaultGeometry();
                double cur_segment_lenght = zsj_polygon.intersection(segment).getLength();
                if (cur_segment_lenght > segment_lenght) {
                    segment_lenght = cur_segment_lenght;
                    zsjid = zsj.getAttribute("zsjid").toString();
                }
            }
			zsjs_string += "INSERT INTO zsjs_moreprecise (zsjid_from_to, zsjid, seq) VALUES ('" + routeid + "', '" + zsjid + "', " + i + ");\n";
        }
		return zsjs_string;
    }
}

class CentroidDistanceComparator implements Comparator<SimpleFeature> {
    @Override
    public int compare(SimpleFeature a, SimpleFeature b) {
        MultiPolygon pa = (MultiPolygon) a.getDefaultGeometry();
        MultiPolygon pb = (MultiPolygon) b.getDefaultGeometry();
        return pa.distance(Intersect.start) < pb.distance(Intersect.start) ? -1 : pa.distance(Intersect.start) == pb.distance(Intersect.start) ? 0 : 1;
    }
}