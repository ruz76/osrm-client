package cz.vsb.gis.ruz76;

import com.google.common.collect.Maps;
import org.postgresql.jdbc3.Jdbc3SimpleDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;


/**
 * Created by ruz76 on 26.10.2016.
 */
public class ZsjDAO {

    private final Jdbc3SimpleDataSource dataSource;

    public ZsjDAO(String jdbcConnection,
                  String jdbcName,
                  String jdbcPassword) throws ClassNotFoundException {
        this.dataSource = new Jdbc3SimpleDataSource();
        dataSource.setPassword(jdbcPassword);
        dataSource.setUser(jdbcName);
        dataSource.setUrl(jdbcConnection);
    }

    /**
     * Load all key values from database to memory
     *
     * @return map of key -> value
     */
    public Map<Integer, Point> loadKeyValues() throws Exception {
        Map<Integer, Point> map = Maps.newHashMap();
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement st = c.prepareStatement("SELECT zsjid, ST_X(ST_Transform(ST_Centroid(geom), 4326)) lon, ST_Y(ST_Transform(ST_Centroid(geom), 4326)) lat FROM osm.cd_zsj_selected_geom ORDER BY zsjid;")) {
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        Integer key = rs.getInt(1);
                        Double lon = rs.getDouble(2);
                        Double lat = rs.getDouble(3);
                        Point zsj = new Point(lat, lon);
                        map.put(key, zsj);
                    }
                }
            }
        }
        return map;
    }

}