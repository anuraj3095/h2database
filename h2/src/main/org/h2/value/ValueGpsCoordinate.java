package org.h2.value;
import org.h2.engine.CastDataProvider;
import org.h2.util.StringUtils;

public class ValueGpsCoordinate extends Value {

    private String value;

    ValueGpsCoordinate (String s) {
        this.value = s;
    }

    @Override
    public TypeInfo getType() {
        return TypeInfo.TYPE_GPS_COORDINATE;
    }

    @Override
    public int getValueType() {
        return Value.GPS_COORDINATE;
    }

    @Override
    public String getString() {
        return value;
    }

    @Override
    public int getMemory() {
        return value.length() * 2 + 94;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /*
     * Similar to hashCode()
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof ValueGpsCoordinate &&
                this.value.equals(((ValueGpsCoordinate) other).value);
    }

    public static Value get(String s) {
        return new ValueGpsCoordinate(s);
    }
    
    public static boolean validate(String s) {
      
      String[] splitCoord = s.split(",");
      
      System.out.println(splitCoord.length);
      
      if (splitCoord.length != 2) {
        return false;
      }
      
        try {
        double latitude = Double.valueOf(splitCoord[0]);
        double longitude = Double.valueOf(splitCoord[1]);
        
        if (Math.abs(latitude) > 90 || Math.abs(longitude) > 180) {
          //System.out.println("> 90 || 180");
          return false;
        }
        
        } catch (Exception e) {
          return false;
        }

      return true;
  }


    @Override
    public StringBuilder getSQL(StringBuilder builder, int sqlFlags) {
       return StringUtils.quoteStringSQL(builder, value).append("::GPS_COORDINATE");
    }

    @Override
    public int compareTypeSafe(Value v, CompareMode mode, CastDataProvider provider) {
      String other = ((ValueGpsCoordinate) v).value;
      return mode.compareString(value, other, false);
    }

}
