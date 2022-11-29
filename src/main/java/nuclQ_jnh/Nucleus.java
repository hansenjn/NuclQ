package nuclQ_jnh;

import java.util.ArrayList;
import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;

class Nucleus{
	double centerX = 0; 
	double centerY = 0;
	int xMin = Integer.MAX_VALUE, xMax = 0, yMin = Integer.MAX_VALUE, yMax = 0;
	ArrayList<PartPoint> insidePoints;
	ArrayList<MembranePoint> surfacePoints;
	ArrayList<MembranePoint> maxRangePointsC1 = null, maxRangePointsC2 = null, maxRangePointsCOI = null;
	double overlapC1 = 0.0;
	double overlapC2 = 0.0;
		
	public Nucleus(ArrayList<PartPoint> ps, ImagePlus impC1, ImagePlus impC2, double maximumAllowedDist, double stepSize, int nrOfChannels){
		surfacePoints = new ArrayList<MembranePoint>(ps.size());
		insidePoints = new ArrayList<PartPoint>(ps.size());
		for(int i = 0; i < ps.size(); i++){
			centerX += ps.get(i).x;
			centerY += ps.get(i).y;		
			
			if(ps.get(i).x > xMax)	xMax = ps.get(i).x;
			if(ps.get(i).x < xMin)	xMin = ps.get(i).x;
			if(ps.get(i).y > yMax)	yMax = ps.get(i).y;
			if(ps.get(i).y < yMin)	yMin = ps.get(i).y;
			
			if(impC1.getStack().getVoxel(ps.get(i).x, ps.get(i).y, 0) != 0.0){
				overlapC1 ++;
			}
			if(impC2.getStack().getVoxel(ps.get(i).x, ps.get(i).y, 0) != 0.0){
				overlapC2 ++;
			}
			
			if(ps.get(i).surface>0){
				surfacePoints.add(new MembranePoint(ps.get(i), maximumAllowedDist, stepSize, nrOfChannels));
			}else{
				insidePoints.add(new PartPoint(ps.get(i)));
			}
			surfacePoints.trimToSize();
			insidePoints.trimToSize();
		}
		centerX /= ps.size();
		centerY /= ps.size();
		overlapC1 /= ps.size();
		overlapC2 /= ps.size();
		
		orderSurfacePoints();
	}
	
	public static double [][][] getAvgAndSDOfProfiles (ArrayList<MembranePoint> points){
		double avgProfiles [][][] = new double [2][3][points.get(0).intensityProfile(1).length]; // [avg, sd][channel1, channel2]
		double profileCt [][] = new double [3][points.get(0).intensityProfile(1).length];	//[channel1, channel2]
		for(int c = 0; c < 3; c++){
				Arrays.fill(avgProfiles [0][c], 0.0);
				Arrays.fill(avgProfiles [1][c], 0.0);
				Arrays.fill(profileCt [c], 0.0);
		}
		
		//Determine AVG
		double profile [];
		for(int i = 0; i < points.size(); i++){
			profile = points.get(i).intensityProfile(1);
			for(int p = 0; p < profile.length; p++){
				if(!Double.isNaN(profile[p])){
					avgProfiles[0][0][p] += profile [p];
					profileCt[0][p] += 1.0;
				}					
			}	
			
			profile = points.get(i).intensityProfile(2);
			for(int p = 0; p < profile.length; p++){
				if(!Double.isNaN(profile[p])){
					avgProfiles[0][1][p] += profile [p];
					profileCt[1][p] += 1.0;
				}					
			}
			
			profile = points.get(i).intensityProfile(3);
			for(int p = 0; p < profile.length; p++){
				if(!Double.isNaN(profile[p])){
					avgProfiles[0][2][p] += profile [p];
					profileCt[2][p] += 1.0;
				}					
			}
		}
		
		for(int c = 0; c < 3; c++){
			for(int p = 0; p < avgProfiles[0][0].length; p++){
				if(profileCt [c][p] > 0.0){
					avgProfiles [0][c][p] /= profileCt [c][p];
				}else{
					avgProfiles [0][c][p] = Double.NaN;
				}
				
			}
		}
		
		System.gc();
		
		//Determine SD
		for(int i = 0; i < points.size(); i++){
			profile = points.get(i).intensityProfile(1);
			for(int p = 0; p < profile.length; p++){
				if(!Double.isNaN(profile[p])){
					avgProfiles[1][0][p] += Math.pow(profile [p]-avgProfiles[0][0][p], 2.0);
				}
			}
			
			profile = points.get(i).intensityProfile(2);
			for(int p = 0; p < profile.length; p++){
				if(!Double.isNaN(profile[p])){
					avgProfiles[1][1][p] += Math.pow(profile [p]-avgProfiles[0][1][p], 2.0);
				}
			}
			
			profile = points.get(i).intensityProfile(3);
			for(int p = 0; p < profile.length; p++){
				if(!Double.isNaN(profile[p])){
					avgProfiles[1][2][p] += Math.pow(profile [p]-avgProfiles[0][2][p], 2.0);
				}
			}
		}
		for(int c = 0; c < 3; c++){
			for(int p = 0; p < avgProfiles[0][0].length; p++){
				if(profileCt [c][p] > 0.0){
					avgProfiles [1][c][p] /= profileCt [c][p] - 1.0;
					avgProfiles [1][c][p] = Math.sqrt(avgProfiles [1][c][p]);
				}else{
					avgProfiles [1][c][p] = Double.NaN;
				}
				
			}
		}		
		return avgProfiles;
	}
	
	public static double [][] getAvgAndSDOfProfilesIndividChannel (ArrayList<MembranePoint> points, int channel){
		double avgProfiles [][] = new double [2][points.get(0).intensityProfile(1).length]; // [avg, sd][channel1, channel2]
		double profileCt [] = new double [points.get(0).intensityProfile(1).length];	//[channel1, channel2]
		
		Arrays.fill(avgProfiles [0], 0.0);
		Arrays.fill(avgProfiles [1], 0.0);
		Arrays.fill(profileCt, 0.0);
		
		//Determine AVG
		double profile [];
		for(int i = 0; i < points.size(); i++){
			profile = points.get(i).intensityProfile(channel);
			for(int p = 0; p < profile.length; p++){
				if(!Double.isNaN(profile[p])){
					avgProfiles[0][p] += profile [p];
					profileCt[p] += 1.0;
				}					
			}				
		}
		
		for(int p = 0; p < avgProfiles[0].length; p++){
			if(profileCt[p] > 0.0){
				avgProfiles [0][p] /= profileCt [p];
			}else{
				avgProfiles [0][p] = Double.NaN;
			}
		}
		
		System.gc();
		
		//Determine SD
		for(int i = 0; i < points.size(); i++){
			profile = points.get(i).intensityProfile(channel);
			for(int p = 0; p < profile.length; p++){
				if(!Double.isNaN(profile[p])){
					avgProfiles[1][p] += Math.pow(profile [p]-avgProfiles[0][p], 2.0);
				}
			}			
		}
		for(int p = 0; p < avgProfiles[0].length; p++){
			if(profileCt [p] > 0.0){
				avgProfiles [1][p] /= profileCt [p] - 1.0;
				avgProfiles [1][p] = Math.sqrt(avgProfiles [1][p]);
			}else{
				avgProfiles [1][p] = Double.NaN;
			}
			
		}	
		return avgProfiles;
	}
	
	private void orderSurfacePoints(){
//		IJ.log("sps-" + surfacePoints.size());
		ArrayList<MembranePoint> newPoints = new ArrayList<MembranePoint> (surfacePoints.size());
		MembranePoint p = surfacePoints.get(0);
		surfacePoints.remove(0);
		surfacePoints.trimToSize();
		newPoints.add(p);
		double distance;		
		int index = -1;
		while(surfacePoints.size()>0){
			distance = Double.POSITIVE_INFINITY;
			scanning: for(int j = surfacePoints.size()-1; j >= 0; j--){				
				if(getDistance(p,surfacePoints.get(j)) < distance){
					index = j;
					distance = getDistance(p,surfacePoints.get(j));
					if(distance == 1){
						break scanning;
					}
				}
			}
			p = surfacePoints.get(index);
			newPoints.add(p);	
			surfacePoints.remove(index);
			surfacePoints.trimToSize();			
		}
		surfacePoints = null;
		surfacePoints = newPoints;
//		IJ.log("sps2-" + surfacePoints.size());
		System.gc();
	}

	public void selectQuarterPoints(int part){
		maxRangePointsC1 = getPartofPointsWithMaximumIntensity(part, 1);
		maxRangePointsC2 = getPartofPointsWithMaximumIntensity(part, 2);
		maxRangePointsCOI = getPartofPointsWithMaximumIntensity(part, 3);
	}

	private ArrayList<MembranePoint> getPartofPointsWithMaximumIntensity(int part, int channel){
		int nPoints = (int)Math.round(surfacePoints.size()/part);
		ArrayList<MembranePoint> selPoints = new ArrayList<MembranePoint> (nPoints);
		int index = 0, startIndex = 0;
		double profileSum;
		double maxSum = 0.0;
		double [] profile;
		
		//TODO: skip if part = 1!
		for(int i = 0; i < surfacePoints.size(); i++){
			profileSum = 0;
			for(int j = 0; j < nPoints; j++){
				index = i + j;
				if(index>=surfacePoints.size()){
					index -= surfacePoints.size();
				}
				profile = surfacePoints.get(index).intensityProfile(channel);
				for(int k = 0; k < profile.length; k++){
					if(!Double.isNaN(profile[k])){
						profileSum += profile [k];						
					}
				}
			}
			if(profileSum > maxSum){
				maxSum = profileSum;
				startIndex = i;
			}
		}
		for(int j = 0; j < nPoints; j++){
			index = startIndex + j;
			if(index>=surfacePoints.size()){
				index -= surfacePoints.size();
			}
			selPoints.add(new MembranePoint(surfacePoints.get(index)));
		}
//		IJ.log("maxSum: " + maxSum);
		selPoints.trimToSize();
//		for(int j = 0; j < selPoints.size(); j++){
//			IJ.log("cP|" + selPoints.get(j).intensityProfile(channel)[selPoints.get(j).profileCentre]);
//		}
		return selPoints;
	}
	
	
	private static double getDistance(MembranePoint p, MembranePoint q){
		return Math.sqrt(Math.pow(p.x-q.x, 2.0)+Math.pow(p.y-q.y, 2.0));
	}
}

