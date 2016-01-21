package com.chrisali.javaflightsim.aircraft;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import com.chrisali.javaflightsim.aero.StabilityDerivatives;
import com.chrisali.javaflightsim.aero.WingGeometry;
import com.chrisali.javaflightsim.enviroment.Environment;

public class Aircraft {
	protected Map<StabilityDerivatives, Object> stabDerivs;
	protected Map<WingGeometry, Double> 		wingGeometry;
	protected Map<MassProperties, Double> 		massProps;
	
	public static final String FILE_PATH = ".\\src\\com\\chrisali\\javaflightsim\\aircraft\\";
	
	// Default constructor to give default values for aircraft definition (Navion)
	public Aircraft() { 
		// Creates EnumMaps and populates them with: 
		// Stability derivative values (either Double or PiecewiseBicubicSplineInterpolatingFunction)
		// Wing geometry values (Double)
		// Mass properties		(Double)
		this.stabDerivs			= new EnumMap<StabilityDerivatives, Object>(StabilityDerivatives.class);
		this.wingGeometry		= new EnumMap<WingGeometry, Double>(WingGeometry.class);
		this.massProps			= new EnumMap<MassProperties, Double>(MassProperties.class);
		
		// =======================================
		// Default stability derivatives (Navion)
		// =======================================
		
		// Lift
		stabDerivs.put(StabilityDerivatives.CL_ALPHA,     new Double(4.44));
		stabDerivs.put(StabilityDerivatives.CL_0, 	      new Double(0.41));
		stabDerivs.put(StabilityDerivatives.CL_Q,         new Double(3.80));
		stabDerivs.put(StabilityDerivatives.CL_ALPHA_DOT, new Double(0.0));
		stabDerivs.put(StabilityDerivatives.CL_D_ELEV,    new Double(0.355));
		stabDerivs.put(StabilityDerivatives.CL_D_FLAP,    new Double(0.355));
		
		// Side Force
		stabDerivs.put(StabilityDerivatives.CY_BETA,      new Double(-0.564));
		stabDerivs.put(StabilityDerivatives.CY_D_RUD,     new Double(0.157));
		
		// Drag
		stabDerivs.put(StabilityDerivatives.CD_ALPHA,     new Double(0.33));
		stabDerivs.put(StabilityDerivatives.CD_0,         new Double(0.025));
		stabDerivs.put(StabilityDerivatives.CD_D_ELEV,    new Double(0.001));
		stabDerivs.put(StabilityDerivatives.CD_D_FLAP,    new Double(0.02));
		stabDerivs.put(StabilityDerivatives.CD_D_GEAR,    new Double(0.09));
		
		// Roll Moment
		stabDerivs.put(StabilityDerivatives.CROLL_BETA,   new Double(-0.074));
		stabDerivs.put(StabilityDerivatives.CROLL_P,      new Double(-0.410));
		stabDerivs.put(StabilityDerivatives.CROLL_R,      new Double(0.107));
		stabDerivs.put(StabilityDerivatives.CROLL_D_AIL,  new Double(-0.134));
		stabDerivs.put(StabilityDerivatives.CROLL_D_RUD,  new Double(0.107));
		
		// Pitch Moment
		stabDerivs.put(StabilityDerivatives.CM_ALPHA,     new Double(-0.683));
		stabDerivs.put(StabilityDerivatives.CM_0,         new Double(0.02));
		stabDerivs.put(StabilityDerivatives.CM_Q,         new Double(-9.96));
		stabDerivs.put(StabilityDerivatives.CM_ALPHA_DOT, new Double(-4.36));
		stabDerivs.put(StabilityDerivatives.CM_D_ELEV,    new Double(-0.923));
		stabDerivs.put(StabilityDerivatives.CM_D_FLAP,    new Double(-0.050));
		
		// Yaw Moment
		stabDerivs.put(StabilityDerivatives.CN_BETA,      new Double(0.071));
		stabDerivs.put(StabilityDerivatives.CN_P,      	  new Double(-0.0575));
		stabDerivs.put(StabilityDerivatives.CN_R,         new Double(-0.125));
		stabDerivs.put(StabilityDerivatives.CN_D_AIL,     new Double(-0.0035));
		stabDerivs.put(StabilityDerivatives.CN_D_RUD,     new Double(-0.072));
		
		// =======================================
		// Default wing geometry (Navion)
		// =======================================		
		
		// Aerodynamic center
		wingGeometry.put(WingGeometry.AC_X,   0.0);
		wingGeometry.put(WingGeometry.AC_Y,   0.0);
		wingGeometry.put(WingGeometry.AC_Z,   0.0);
		
		// Wing dimensions
		wingGeometry.put(WingGeometry.S_WING, 184.0);
		wingGeometry.put(WingGeometry.B_WING, 33.4);
		wingGeometry.put(WingGeometry.C_BAR,  5.7);
		
		// =======================================
		// Default mass properties (Navion)
		// =======================================
		
		// Center of Gravity
		massProps.put(MassProperties.CG_X, 			 0.0);
		massProps.put(MassProperties.CG_Y, 			 0.0);
		massProps.put(MassProperties.CG_Z, 			 0.0);
		
		// Moments of Inertia
		massProps.put(MassProperties.J_X,  			 1048.0);
		massProps.put(MassProperties.J_Y,    		 3000.0);
		massProps.put(MassProperties.J_Z,  			 3050.0);
		massProps.put(MassProperties.J_XZ, 			 0.0);
		
		// Weights and Mass (lbf/slug)
		massProps.put(MassProperties.WEIGHT_EMPTY,   1780.0);
		massProps.put(MassProperties.WEIGHT_FUEL,    360.0);
		massProps.put(MassProperties.WEIGHT_PAYLOAD, 610.0);
		massProps.put(MassProperties.TOTAL_MASS, (massProps.get(MassProperties.WEIGHT_EMPTY) + 
												  massProps.get(MassProperties.WEIGHT_FUEL)  +
												  massProps.get(MassProperties.WEIGHT_PAYLOAD))/Environment.getGravity()[2]);
	}
	
	// TODO Read a text file with aircraft attributes, and assign them to EnumMap	

	public Aircraft(String aircraftName){
		// Aerodynamics
		ArrayList<String[]> readAeroFile = readFileAndSplit(aircraftName, "Aero");
		
		for(int i = 0; i < readAeroFile.size(); i++) {
			for (String[] readLine : readAeroFile) {
				if (StabilityDerivatives.values()[i].equals(readLine[0]))
					this.stabDerivs.put(StabilityDerivatives.values()[i], Double.parseDouble(readLine[1]));
			}
		}
		
		// Mass Properties
		ArrayList<String[]> readMassPropFile = readFileAndSplit(aircraftName, "MassProperties");
		
		for(int i = 0; i < readMassPropFile.size(); i++) {
			for (String[] readLine : readMassPropFile) {
				if (StabilityDerivatives.values()[i].equals(readLine[0]))
					this.massProps.put(MassProperties.values()[i], Double.parseDouble(readLine[1]));
			}
		}
		
		// Wing Geometry
		ArrayList<String[]> readWingGeomFile = readFileAndSplit(aircraftName, "WingGeometry");
		
		for(int i = 0; i < readWingGeomFile.size(); i++) {
			for (String[] readLine : readWingGeomFile) {
				if (StabilityDerivatives.values()[i].equals(readLine[0]))
					this.wingGeometry.put(WingGeometry.values()[i], Double.parseDouble(readLine[1]));
			}
		}
	}
	
	public Double[] getCenterOfGravity() {return new Double[] {massProps.get(MassProperties.CG_X),
															   massProps.get(MassProperties.CG_Y),
															   massProps.get(MassProperties.CG_Z)};}

	public Double[] getAerodynamicCenter() {return new Double[] {wingGeometry.get(WingGeometry.AC_X),
																 wingGeometry.get(WingGeometry.AC_Y),
																 wingGeometry.get(WingGeometry.AC_Z)};}
	
	public Double[] getInertiaValues() {return new Double[] {massProps.get(MassProperties.J_X),
														     massProps.get(MassProperties.J_Y),
														     massProps.get(MassProperties.J_Z),
														     massProps.get(MassProperties.J_XZ)};}

	private static ArrayList<String[]> readFileAndSplit(String aircraftName, String fileContents) {
		StringBuilder sb = new StringBuilder();
		sb.append(FILE_PATH).append(aircraftName).append("\\").append(fileContents).append(".txt");
		ArrayList<String[]> readAndSplit = new ArrayList<>();
		String readLine = null;
		
		try (BufferedReader br = new BufferedReader(new FileReader(sb.toString()))) {
			while ((readLine = br.readLine()) != null)
				readAndSplit.add(readLine.split(" = "));
		} catch (FileNotFoundException e) {System.err.println("Could not find: " + aircraftName + ".txt!");}
		catch (IOException e) {System.err.println("Could not read: " + aircraftName + ".txt!");}
		catch (NullPointerException e) {System.err.println("Bad reference to: " + aircraftName + ".txt!");} 
		
		return readAndSplit;
	}
}
