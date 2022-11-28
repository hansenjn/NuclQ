package nuclQ_jnh;

import ij.ImagePlus;

class MembranePoint{
	int x = 0; 
	int y = 0;
	int surface = 0;
	
	double [][][] profile;	//[channel][0 value, 1 count][profile length, centre is the membranepoint]
	int profileCentre;
	double maxDist;
	double stepIncrement;
	
	public MembranePoint(MembranePoint p){
		x = p.x;
		y = p.y;
		surface = p.surface;
		profile = new double [p.profile.length][p.profile[0].length][p.profile[0][0].length];
		for(int i = 0; i < p.profile.length; i++){
			for(int j = 0; j < p.profile[0].length; j++){
				for(int k = 0; k < p.profile[0][0].length; k++){
					profile[i][j][k] = p.profile [i][j][k];
				}
			}
		}
		profileCentre = p.profileCentre;
		maxDist = p.maxDist;
		stepIncrement = p.stepIncrement;
	}
	
	public MembranePoint(PartPoint p, double maximumAllowedDist, double stepSize, int nrOfChannels){
		x = p.x;
		y = p.y;
		surface = p.surface;
		maxDist = maximumAllowedDist;
		profileCentre = (int)Math.round(maximumAllowedDist/stepSize);
		profile = new double [nrOfChannels][2][1+profileCentre*2];
		for(int c = 0; c < profile.length; c++){
			for(int i = 0; i < profile[0].length; i++){
				for (int l = 0; l < profile[0][0].length; l++){
					profile [c][i][l] = 0.0;
				}
			}
		}
		stepIncrement = stepSize;
	}
	
	public MembranePoint(int px, int py, ImagePlus imp, int channel, double maximumAllowedDist, double stepSize, int nrOfChannels){
		x = px;
		y = py;
		
		if(px > 0
				&& imp.getStack().getVoxel(px-1, py, imp.getStackIndex(channel, 1, 1)-1) == 0.0){	
			surface++;
		}

		if(px < imp.getWidth() - 1
				&& imp.getStack().getVoxel(px+1, py, imp.getStackIndex(channel, 1, 1)-1) == 0.0){	
			surface++;
		}

		if(py > 0
				&& imp.getStack().getVoxel(px, py-1, imp.getStackIndex(channel, 1, 1)-1) == 0.0){	
			surface++;
		}
		
		if(py < imp.getHeight() - 1
				&& imp.getStack().getVoxel(px, py+1, imp.getStackIndex(channel, 1, 1)-1) == 0.0){	
			surface++;
		}
		
		maxDist = maximumAllowedDist;
		stepIncrement = stepSize;
		profileCentre = (int)Math.round(maximumAllowedDist/stepIncrement);
		profile = new double [nrOfChannels][2][1+profileCentre*2];
		for(int c = 0; c < profile.length; c++){
			for(int i = 0; i < profile[0].length; i++){
				for (int l = 0; l < profile[0][0].length; l++){
					profile [c][i][l] = 0.0;
				}
			}
		}
		
	}
	
	/**
	 * @param channel: 1 <= channel <= max nr of channels
	 * @return true if was added to profile, false if could not be added
	 * */
	boolean addToProfile(double distance, double intensity, int channel, boolean outside){
		if(channel > profile.length)	return false;
		if(distance > maxDist) return false;
		int pos = (int)Math.round(distance / stepIncrement);
		if(outside){
			profile [channel-1][0][profileCentre+pos] += intensity;
			profile [channel-1][1][profileCentre+pos] += 1.0;
		}else{
			profile [channel-1][0][profileCentre-pos] += intensity;
			profile [channel-1][1][profileCentre-pos] += 1.0;
		}	
		return true;
	}
	
	/**
	 * @param channel: 1 <= channel <= max nr of channels
	 * */
	double [] intensityProfile (int channel){
		double [] outProfile = new double[profile[0][0].length];
		for(int i = 0; i < outProfile.length; i++){
			if(profile [channel-1][1][i] == 0.0){
				outProfile [i] = Double.NaN;
				continue;
			}
			outProfile [i] = profile [channel-1][0][i] / profile [channel-1][1][i]; 
		}
		return outProfile;
	}
}

