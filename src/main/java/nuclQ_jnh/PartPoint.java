package nuclQ_jnh;

import ij.ImagePlus;

class PartPoint{
	int x = 0; 
	int y = 0; 
	int surface = 0;
	double intensity = 0.0;
		
	public PartPoint(PartPoint p){
		x = p.x;
		y = p.y;
		surface = p.surface;
		intensity = p.intensity;		
	}
	
	public PartPoint(int px, int py, ImagePlus imp, int channel){
		intensity = imp.getStack().getVoxel(px, py, imp.getStackIndex(channel, 1, 1)-1);
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
	}
}

