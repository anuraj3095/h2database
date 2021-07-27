package org.h2.expression.aggregate;

import org.h2.engine.SessionLocal;
import org.h2.value.Value;
import org.h2.value.ValueGpsCoordinate;
import org.h2.value.ValueNull;

/**
 * Data stored while calculating a GPS_CENTROID aggregate.
 */
final class AggregateDataGPSCentroid extends AggregateData {

  private long centroidX;
  private long centroidY;
  private long count;
  
  AggregateDataGPSCentroid() {
}
  
  @Override
  void add(SessionLocal session, Value v) {
    
    if (v == ValueNull.INSTANCE) {
      return;
    }
    count++;
    String x = v.getString();
    String[] splitCoord = x.split(",");

    double latitude = Double.valueOf(splitCoord[0]);
    double longitude = Double.valueOf(splitCoord[1]);
    
    centroidX += latitude;
    centroidY += longitude;
  }

  @Override
  Value getValue(SessionLocal session) {
    
    Double xc = (centroidX / (double)count);
    Double xy = (centroidY / (double)count);
    String gps = Double.toString(xc) + "," + Double.toString(xy);
    return ValueGpsCoordinate.get(gps);
    
  }

}