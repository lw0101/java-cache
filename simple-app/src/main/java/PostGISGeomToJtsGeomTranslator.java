import org.postgis.PGgeometry;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class PostGISGeomToJtsGeomTranslator {

	public Geometry translatePostgis2Jts(PGgeometry pgGeometry) throws ParseException {
		WKTReader jtsWktReader = new WKTReader();
		StringBuffer sb = new StringBuffer();
		pgGeometry.getGeometry().outerWKT(sb);
		return jtsWktReader.read(sb.toString());
	}

}