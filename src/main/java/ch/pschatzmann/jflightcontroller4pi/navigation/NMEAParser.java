package ch.pschatzmann.jflightcontroller4pi.navigation;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for NMEA GPS messages
 * see https://gist.github.com/javisantana/1326141/30d6b5b603fa113d7a17bfcc0a8aaa25a107d581
 * @author pschatzmann
 *
 */
public class NMEAParser {
	private static Logger log = LoggerFactory.getLogger(NMEAParser.class);
	private static final Map<String, INMEASentenceParser> NMEASentenceParsers = new HashMap<String, INMEASentenceParser>();
	private GPSPosition position;
	
    public NMEAParser(GPSPosition value) {
		this.position = value;
		
    	NMEASentenceParsers.put("GPGGA", new GPGGA());
    	NMEASentenceParsers.put("GPGGL", new GPGGL());
    	NMEASentenceParsers.put("GPRMC", new GPRMC());
    	NMEASentenceParsers.put("GPRMZ", new GPRMZ());
    	NMEASentenceParsers.put("GPVTG", new GPVTG());
    }
    
    
	public GPSPosition parse(String line) {
		if(line.startsWith("$")) {
			String nmea = line.substring(1);
			String[] tokens = nmea.split(",");
			String type = tokens[0];
			if(NMEASentenceParsers.containsKey(type)) {
				log.info("parsing {}", type);
				NMEASentenceParsers.get(type).parse(tokens, position);
			}
		}
		return position;
	}
	
	// utils
	static double Latitude2Decimal(String lat, String NS) {
		double med = Double.parseDouble(lat.substring(2))/60.0f;
		med +=  Double.parseDouble(lat.substring(0, 2));
		if(NS.startsWith("S")) {
			med = -med;
		}
		return med;
	}

	static double Longitude2Decimal(String lon, String WE) {
		double med = Double.parseDouble(lon.substring(3))/60.0f;
		med +=  Double.parseDouble(lon.substring(0, 3));
		if(WE.startsWith("W")) {
			med = -med;
		}
		return med;
	}

	// parsers 
	class GPGGA implements INMEASentenceParser {
		@Override
		public boolean parse(String [] tokens, GPSPosition position) {
			position.setTime( Double.parseDouble(tokens[1]));
			position.setLatitude(Latitude2Decimal(tokens[2], tokens[3]));
			position.setLongitude(Longitude2Decimal(tokens[4], tokens[5]));
			position.setQuality(Integer.parseInt(tokens[6]));
			position.setAltitude(Double.parseDouble(tokens[9]));
			return true;
		}
	}
	
	class GPGGL implements INMEASentenceParser {
		@Override
		public boolean parse(String [] tokens, GPSPosition position) {
			position.setLatitude(Latitude2Decimal(tokens[1], tokens[2]));
			position.setLongitude(Longitude2Decimal(tokens[3], tokens[4]));
			position.setTime(Double.parseDouble(tokens[5]));
			return true;
		}
	}
	
	class GPRMC implements INMEASentenceParser {
		@Override
		public boolean parse(String [] tokens, GPSPosition position) {
			position.setTime(Double.parseDouble(tokens[1]));
			position.setLatitude(Latitude2Decimal(tokens[3], tokens[4]));
			position.setLongitude( Longitude2Decimal(tokens[5], tokens[6]));
			position.setSpeed(Double.parseDouble(tokens[7]));
			//position.dir = Double.parseDouble(tokens[8]);
			return true;
		}
	}
		
	class GPRMZ implements INMEASentenceParser {
		@Override
		public boolean parse(String [] tokens, GPSPosition position) {
			position.setAltitude( Double.parseDouble(tokens[1]));
			return true;
		}
	}

	class GPVTG implements INMEASentenceParser {
		@Override
		public boolean parse(String [] tokens, GPSPosition position) {
			position.setSpeed( Double.parseDouble(tokens[7]));
			return true;
		}
	}

}
