package nuclQ_jnh;
/** ===============================================================================
* NuclQ Version 0.0.2
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation (http://www.gnu.org/licenses/gpl.txt )
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*  
* See the GNU General Public License for more details.
*  
* Copyright (C) Jan Niklas Hansen
* Date: April 16, 2020 (This Version: May 06, 2020)
*   
* For any questions please feel free to contact me (jan.hansen@uni-bonn.de).
* =============================================================================== */

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.text.*;

import javax.swing.UIManager;

import ij.*;
import ij.gui.*;
import ij.io.*;
import ij.measure.*;
import ij.plugin.*;
import ij.plugin.filter.Binary;
import ij.process.ByteProcessor;
import ij.process.FloodFiller;
import ij.process.ImageProcessor;
import ij.process.LUT;
import ij.process.AutoThresholder.Method;
import ij.text.*;

public class NuclQMain implements PlugIn, Measurements {
	// Name variables
	static final String PLUGINNAME = "NuclQ";
	static final String PLUGINVERSION = "0.0.2";

	// Fix fonts
	static final Font SuperHeadingFont = new Font("Sansserif", Font.BOLD, 16);
	static final Font HeadingFont = new Font("Sansserif", Font.BOLD, 14);
	static final Font SubHeadingFont = new Font("Sansserif", Font.BOLD, 12);
	static final Font TextFont = new Font("Sansserif", Font.PLAIN, 12);
	static final Font InstructionsFont = new Font("Sansserif", 2, 12);
	static final Font RoiFont = new Font("Sansserif", Font.PLAIN, 12);
	Font roiFont = new Font("Sansserif", Font.PLAIN, 20);

	// Fix formats
	DecimalFormat df6 = new DecimalFormat("#0.000000");
	DecimalFormat df3 = new DecimalFormat("#0.000");
	DecimalFormat df0 = new DecimalFormat("#0");
	DecimalFormat dfDialog = new DecimalFormat("#0.000000");

	static final String[] nrFormats = { "US (0.00...)", "Germany (0,00...)" };

	static SimpleDateFormat NameDateFormatter = new SimpleDateFormat("yyMMdd_HHmmss");
	static SimpleDateFormat FullDateFormatter = new SimpleDateFormat("yyyy-MM-dd	HH:mm:ss");
	static SimpleDateFormat FullDateFormatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	// Progress Dialog
	ProgressDialog progress;
	boolean processingDone = false;
	boolean continueProcessing = true;

	// -----------------define params for Dialog-----------------
	static final String[] taskVariant = { "active image in FIJI", "multiple images (open multi-task manager)",
			"all images open in FIJI" };
	String selectedTaskVariant = taskVariant[1];
	int tasks = 1;
	
	boolean recalibrate = false;
	double calibration = 0.21;
	String calibrationUnit = "µm";

	boolean gb1 = true;
	double gaussianSigma1 = 2.0;
	int CDapi = 1, C1 = 2, C2 = 4, COI = 3;
	double foldSD = 4.0;
	double profileStepSize;
	
	double minFractionOverlapC1 = 0.5, minFractionOverlapC2 = 0.5;
	
	int partForSelectedRegion = 4;
	
	double maxDistUnits = 5.0;
	int minSize = 200;
	double maxDistPx;
	String[] thresholdMethods = { "Default", "IJ_IsoData", "Huang", "Intermodes", "IsoData", "Li", "MaxEntropy", "Mean",
			"MinError", "Minimum", "Moments", "Otsu", "Percentile", "RenyiEntropy", "Shanbhag", "Triangle", "Yen" };
	String selectedThresholdMethod = "Triangle";

	// static final String[] bioFormats = {".tif" , "raw microscopy file (e.g.
	// OIB-file)"};
	// String bioFormat = bioFormats [0];
	//
	boolean saveDate = false;
	// -----------------define params for Dialog-----------------

	// Variables for processing of an individual task
	// enum channelType {PLAQUE,CELL,NEURITE};

public void run(String arg) {
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
//-------------------------GenericDialog--------------------------------------
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
	
	GenericDialog gd = new GenericDialog(PLUGINNAME + " - set parameters");	
	//show Dialog-----------------------------------------------------------------
	//.setInsets(top, left, bottom)
	gd.setInsets(0,0,0);	gd.addMessage(PLUGINNAME + ", Version " + PLUGINVERSION + ", \u00a9 2020 JN Hansen (jan.hansen@uni-bonn.de)", SuperHeadingFont);	
	gd.setInsets(5,0,0);	gd.addChoice("process ", taskVariant, selectedTaskVariant);
	
	gd.setInsets(10,0,0);	gd.addMessage("Calibration", HeadingFont);
	gd.setInsets(0,0,0);	gd.addCheckbox("manually calibrate image", recalibrate);
	gd.setInsets(0,0,0);	gd.addNumericField("    -> calibration [unit/px]: ", calibration, 4);
	gd.setInsets(0,0,0);	gd.addStringField("    -> calibration unit: ", calibrationUnit);
	
	gd.setInsets(10,0,0);	gd.addMessage("Preprocessing", HeadingFont);
	gd.setInsets(0,0,0);	gd.addCheckbox("Perform Gaussian blur on all channels before binarizing", gb1);
	gd.setInsets(0,0,0);	gd.addNumericField("Sigma of Gaussian blur", gaussianSigma1, 2);
	
	gd.setInsets(10,0,0);	gd.addMessage("Channel selection", HeadingFont);
	gd.setInsets(0,0,0);	gd.addNumericField("DAPI channel", CDapi, 0);
	gd.setInsets(0,0,0);	gd.addNumericField("Channel 1", C1, 0);
	gd.setInsets(0,0,0);	gd.addNumericField("Channel 2", C2, 0);
	gd.setInsets(0,0,0);	gd.addNumericField("Channel of interest", COI, 0);
	
	gd.setInsets(10,0,0);	gd.addMessage("Thresholding methods", HeadingFont);	
	gd.setInsets(0,0,0);	gd.addNumericField("Fold SD for threshold in Channel 1 and 2", foldSD, 2);
	gd.setInsets(0,0,0);	gd.addChoice("Thresholding Method for DAPI", thresholdMethods, selectedThresholdMethod);
	
	gd.setInsets(10,0,0);	gd.addMessage("Filtering and Analyzing", HeadingFont);
	gd.setInsets(0, 0, 0);	gd.addNumericField("Mininimum size of accepted nuclei (px)", minSize, 0);
	gd.setInsets(0, 0, 0);	gd.addNumericField("Mininimum overlap of nuclei with positive signal in Channel 1", minFractionOverlapC1*100.0, 2);
	gd.setInsets(0, 0, 0);	gd.addNumericField("Mininimum overlap of nuclei with positive signal in Channel 2", minFractionOverlapC2*100.0, 2);
	gd.setInsets(0, 0, 0);	gd.addNumericField("Maximum distance to the nuclear membrane (calibration units)", maxDistUnits, 2);
	gd.setInsets(0, 0, 0);	gd.addNumericField("Divisioning of membrane for obtaining the highest intensity region", partForSelectedRegion, 0);
		
	gd.setInsets(10,0,0);	gd.addMessage("Output settings", SubHeadingFont);
	gd.setInsets(0,0,0);	gd.addCheckbox("save date in output file names", saveDate);
	
	gd.showDialog();
	//show Dialog-----------------------------------------------------------------

	//read and process variables--------------------------------------------------	
	selectedTaskVariant = gd.getNextChoice();
	
	recalibrate = gd.getNextBoolean();
	calibration = (double) gd.getNextNumber();
	calibrationUnit = gd.getNextString();
	
	gb1 = gd.getNextBoolean();
	gaussianSigma1 = gd.getNextNumber();
	
	CDapi = (int)gd.getNextNumber();
	C1 = (int)gd.getNextNumber();
	C2 = (int)gd.getNextNumber();
	COI = (int)gd.getNextNumber();
	
	foldSD = gd.getNextNumber();
	
	selectedThresholdMethod = gd.getNextChoice();
		
	minSize = (int) gd.getNextNumber();
	minFractionOverlapC1 = gd.getNextNumber()/100.0;
	minFractionOverlapC2 = gd.getNextNumber()/100.0;
	maxDistUnits = gd.getNextNumber();
	partForSelectedRegion = (int) gd.getNextNumber();
	
	saveDate = gd.getNextBoolean();
	df6.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
	df3.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
	df0.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
	dfDialog.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
	//read and process variables--------------------------------------------------
	if (gd.wasCanceled()) return;
	
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
//---------------------end-GenericDialog-end----------------------------------
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

	String name [] = {"",""};
	String dir [] = {"",""};
	ImagePlus allImps [] = new ImagePlus [2];
	{
		//Improved file selector
		try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception e){}
		if(selectedTaskVariant.equals(taskVariant[1])){
			OpenFilesDialog od = new OpenFilesDialog ();
			od.setLocation(0,0);
			od.setVisible(true);
			
			od.addWindowListener(new java.awt.event.WindowAdapter() {
		        public void windowClosing(WindowEvent winEvt) {
		        	return;
		        }
		    });
		
			//Waiting for od to be done
			while(od.done==false){
				try{
					Thread.currentThread().sleep(50);
			    }catch(Exception e){
			    }
			}
			
			tasks = od.filesToOpen.size();
			name = new String [tasks];
			dir = new String [tasks];
			for(int task = 0; task < tasks; task++){
				name[task] = od.filesToOpen.get(task).getName();
				dir[task] = od.filesToOpen.get(task).getParent() + System.getProperty("file.separator");
			}		
		}else if(selectedTaskVariant.equals(taskVariant[0])){
			if(WindowManager.getIDList()==null){
				new WaitForUserDialog("Plugin canceled - no image open in FIJI!").show();
				return;
			}
			FileInfo info = WindowManager.getCurrentImage().getOriginalFileInfo();
			name [0] = info.fileName;	//get name
			dir [0] = info.directory;	//get directory
			tasks = 1;
		}else if(selectedTaskVariant.equals(taskVariant[2])){	// all open images
			if(WindowManager.getIDList()==null){
				new WaitForUserDialog("Plugin canceled - no image open in FIJI!").show();
				return;
			}
			int IDlist [] = WindowManager.getIDList();
			tasks = IDlist.length;	
			if(tasks == 1){
				selectedTaskVariant=taskVariant[0];
				FileInfo info = WindowManager.getCurrentImage().getOriginalFileInfo();
				name [0] = info.fileName;	//get name
				dir [0] = info.directory;	//get directory
			}else{
				name = new String [tasks];
				dir = new String [tasks];
				allImps = new ImagePlus [tasks];
				for(int i = 0; i < tasks; i++){
					allImps[i] = WindowManager.getImage(IDlist[i]); 
					FileInfo info = allImps[i].getOriginalFileInfo();
					name [i] = info.fileName;	//get name
					dir [i] = info.directory;	//get directory
				}		
			}
					
		}
	}
	 	
	//add progressDialog
	progress = new ProgressDialog(name, tasks);
	progress.setLocation(0,0);
	progress.setVisible(true);
	progress.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(WindowEvent winEvt) {
        	if(processingDone==false){
        		IJ.error("Script stopped...");
        	}
        	continueProcessing = false;	        	
        	return;
        }
	});
		
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
//------------------------------CELL MEASUREMENT------------------------------
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

	//set ROIs
	Roi [] selections = new Roi [tasks];
   	ImagePlus imp;
   	//set Rois
   	{
		IJ.setTool("polygon");
		{
			for(int task = 0; task < tasks; task++){
				if(selectedTaskVariant.equals(taskVariant[1])){
					if(name[task].substring(name[task].lastIndexOf(".")).equals(".tif")||
		   					name[task].substring(name[task].lastIndexOf(".")).equals(".tiff")||
		   					name[task].substring(name[task].lastIndexOf(".")).equals(".TIF")||
		   					name[task].substring(name[task].lastIndexOf(".")).equals(".TIFF")){
						imp = IJ.openVirtual(dir [task] + name [task]);
					}else{
						IJ.run("Bio-Formats", "open=[" +dir[task] + name[task]
		   						+ "] autoscale color_mode=Default rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT");
		   				imp = WindowManager.getCurrentImage();
					}
				}else if(selectedTaskVariant.equals(taskVariant[0])){
		   			imp = WindowManager.getCurrentImage().duplicate();
		   			imp.deleteRoi();
		   		}else{
		   			imp = allImps[task].duplicate();
		   			imp.deleteRoi();
		   		}				
								
				while(true){
					imp.show();
					imp.setDisplayMode(IJ.COMPOSITE);
					progress.replaceBarText("user interaction required... [task " + (task+1) + "/" + tasks + "]");
					new WaitForUserDialog("Set a Roi touching some nuclei of exclusively those cells with no signal in any channel [task " + (task+1) + "/" + tasks + "]").show();
					if(imp.getRoi()!=null) break;
				}		
				selections [task] = imp.getRoi();
				
				imp.changes = false;
				imp.close();
				System.gc();
			}
		}
		System.gc();
	}
	
   	RoiEncoder re;
   	for(int task = 0; task < tasks; task++){
	running: while(continueProcessing){
		Date startDate = new Date();
		progress.updateBarText("in progress...");
		//Check for problems
				if(name[task].substring(name[task].lastIndexOf("."),name[task].length()).equals(".txt")){
					progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": A file is no image! Could not be processed!", ProgressDialog.ERROR);
					progress.moveTask(task);	
					break running;
				}
				if(name[task].substring(name[task].lastIndexOf("."),name[task].length()).equals(".zip")){	
					progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": A file is no image! Could not be processed!", ProgressDialog.ERROR);
					progress.moveTask(task);	
					break running;
				}		
		//Check for problems
				
		//open Image
		   	try{
		   		if(selectedTaskVariant.equals(taskVariant[1])){
		   			if(name[task].substring(name[task].lastIndexOf(".")).equals(".tif")||
		   					name[task].substring(name[task].lastIndexOf(".")).equals(".tiff")||
		   					name[task].substring(name[task].lastIndexOf(".")).equals(".TIF")||
		   					name[task].substring(name[task].lastIndexOf(".")).equals(".TIFF")){
		   				//TIFF file
		   				imp = IJ.openImage(""+dir[task]+name[task]+"");		
		   			}else{
		   				//bio format reader
		   				IJ.run("Bio-Formats", "open=[" +dir[task] + name[task]
		   						+ "] autoscale color_mode=Default rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT");
		   				imp = WindowManager.getCurrentImage();		   				
		   			}		   			
		   		}else if(selectedTaskVariant.equals(taskVariant[0])){
		   			imp = WindowManager.getCurrentImage().duplicate();
		   			imp.deleteRoi();
		   		}else{
		   			imp = allImps[task].duplicate();
		   			imp.deleteRoi();
		   		}
		   	}catch (Exception e) {
		   		progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": file is no image - could not be processed!", ProgressDialog.ERROR);
				progress.moveTask(task);	
				break running;
			}
		   	imp.hide();
			imp.deleteRoi();
		   	imp.lock();
			
			/******************************************************************
			*** 						CALIBRATION 						***	
			*******************************************************************/
		   	{
		   		double pixelWidth = imp.getCalibration().pixelWidth;
				double pixelHeight = imp.getCalibration().pixelHeight; 
				String unit = imp.getCalibration().getUnit();
				if(recalibrate){
					pixelWidth = calibration;		imp.getCalibration().pixelWidth = pixelWidth;
					pixelHeight = calibration;		imp.getCalibration().pixelHeight = pixelHeight;
					unit = calibrationUnit;	imp.getCalibration().setUnit(unit);
				}else{				
					calibration = pixelWidth;
					calibrationUnit = ""+unit;
					if(pixelWidth!=pixelHeight){
						progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": x and y calibration in metadata differ - used only x metadata calibration for both, x and y!", ProgressDialog.LOG);
					}					
				}				
				/** determine max Distance in pixels*/
			   	maxDistPx = maxDistUnits / ((pixelWidth+pixelHeight)/2.0);		   	
		   	}
		   	profileStepSize = calibration;
				
		   	
	   	/******************************************************************
		*** 						PROCESSING							***	
		*******************************************************************/
			
		   	imp.deleteRoi();
	   		imp.unlock();
	   		
	   		ImagePlus impCopy = imp.duplicate();
	   		
	   		
	   		/**
	   		 * INITIAL GAUSSIAN BLUR
	   		 * */
	   		if(gb1){
			   	for(int c = 0; c < imp.getNChannels(); c++){
			   		imp.setC(c+1);
			   		imp.getProcessor().blurGaussian(gaussianSigma1);
			   	}		   		
		   	}
	   		
	   		/**
	   		 * SEGMENT DAPI CHANNEL AND RETRIEVE NUCLEI
	   		 * */
	   		double thresholdDAPI = thresholdImage(imp, selectedThresholdMethod, imp.getStackIndex(CDapi, 1, 1));
		   	ImagePlus impCDapi = IJ.createHyperStack(imp.getTitle()+" CDapi", imp.getWidth(), imp.getHeight(),
		   			1, 1, 1, 8);
		   	
		   	for(int x = 0; x < imp.getWidth(); x++){
	   			for(int y = 0; y < imp.getHeight(); y++){
	   				if(imp.getStack().getVoxel(x, y, imp.getStackIndex(CDapi, 1, 1)-1)>0.0){
	   					impCDapi.getStack().setVoxel(x, y, impCDapi.getStackIndex(1, 1, 1)-1,Math.pow(2.0, impCDapi.getBitDepth())-1);
	   				}else{
	   					impCDapi.getStack().setVoxel(x, y, impCDapi.getStackIndex(1, 1, 1)-1,0.0);
	   				}
	   				
	   			}
		   	}		   	
		   	
		   	// watershed
		   	Prefs.blackBackground = true;
		   	IJ.run(impCDapi, "Watershed", "");
		   	
//		   	impC2.show();
//		   	impC1.show();
//		   	impCDapi.show();
//		   	imp.show();
//		   	
//		   	new WaitForUserDialog("wait").show();
//		   	
//		   	impC2.hide();
//		   	impC1.hide();
//		   	impCDapi.hide();
//		   	imp.hide();
		   			   	
		   	// filter particles and return
		   	ArrayList<ArrayList<PartPoint>> nucleiParts = filterChannel2DAndReturnParticles(impCDapi, 1, "nuclei", minSize);
		   	progress.notifyMessage("Found nuclei parts: " + nucleiParts.size(), ProgressDialog.LOG);
	   		
	   		/**
	   		 * ESTIMATE BACKGROUND SIGNAL
	   		 * */		   	
		   	double meanC1 = 0.0, meanC2 = 0.0, sdC1 = 0.0, sdC2 = 0.0;
	   		int number = 0;
	   		int particleNr = 0;
	   		ImagePlus impBG = IJ.createHyperStack(imp.getTitle()+" BG", imp.getWidth(), imp.getHeight(),
		   			1, 1, 1, 8);
	   		
//	   		Rectangle r = selections[task].getBounds();
//	   		for(int x = r.x; x <= r.x+r.width && x < imp.getWidth(); x++){
//   			if(x < 0) x = 0;
//   			for(int y = r.y; y <= r.y+r.height && y < imp.getHeight(); y++){
//   				if(y < 0) y = 0;
//   				if(selections[task].contains(x, y)){
//   					meanC1 += imp.getStack().getVoxel(x, y, imp.getStackIndex(C1, 1, 1)-1);
//   					meanC2 += imp.getStack().getVoxel(x, y, imp.getStackIndex(C2, 1, 1)-1);
//   					number++;
//   				}	
//   			}
//   		}
	   		
	   		
	   		
	   		for(int i = 0; i < nucleiParts.size(); i++){
	   			estimatingParticle: for(int j = 0; j < nucleiParts.get(i).size(); j++){
	   				if(selections[task].contains(nucleiParts.get(i).get(j).x, nucleiParts.get(i).get(j).y)){
	   					for(int k = 0; k < nucleiParts.get(i).size(); k++){
	   						meanC1 += imp.getStack().getVoxel(nucleiParts.get(i).get(k).x, 
	   								nucleiParts.get(i).get(k).y, imp.getStackIndex(C1, 1, 1)-1);
		   					meanC2 += imp.getStack().getVoxel(nucleiParts.get(i).get(k).x, 
		   							nucleiParts.get(i).get(k).y, imp.getStackIndex(C2, 1, 1)-1);
		   					number++;
		   					impBG.getStack().setVoxel(nucleiParts.get(i).get(k).x, 
			   						nucleiParts.get(i).get(k).y, 0, 255.0);
	   					}	   	
	   					particleNr++;
	   					break estimatingParticle;
	   				}
			   	}		   		
		   	}
	   		
	   		{
//		   		progress.notifyMessage("Sum C1 " + meanC1 + " | Sum C2" + meanC2 + "| n " + number, ProgressDialog.LOG);
		   		meanC1 /= (double) number;
		   		meanC2 /= (double) number;
		   		
//		   		for(int x = r.x; x <= r.x+r.width && x < imp.getWidth(); x++){
//		   			if(x < 0) x = 0;
//		   			for(int y = r.y; y <= r.y+r.height && y < imp.getHeight(); y++){
//		   				if(y < 0) y = 0;
//		   				if(selections[task].contains(x, y)){
//		   					sdC1 += Math.pow(imp.getStack().getVoxel(x, y, imp.getStackIndex(C1, 1, 1)-1)
//		   							- meanC1, 2.0);
//		   					sdC2 += Math.pow(imp.getStack().getVoxel(x, y, imp.getStackIndex(C2, 1, 1)-1)
//		   							- meanC2, 2.0);
//		   				}	
//		   			}
//		   		}
		   		
		   		for(int i = 0; i < nucleiParts.size(); i++){
		   			estimatingParticle: for(int j = 0; j < nucleiParts.get(i).size(); j++){
		   				if(selections[task].contains(nucleiParts.get(i).get(j).x, nucleiParts.get(i).get(j).y)){
		   					for(int k = 0; k < nucleiParts.get(i).size(); k++){
		   						sdC1 += Math.pow(imp.getStack().getVoxel(nucleiParts.get(i).get(k).x, 
			   							nucleiParts.get(i).get(k).y, imp.getStackIndex(C1, 1, 1)-1)
			   							- meanC1, 2.0);
			   					sdC2 += Math.pow(imp.getStack().getVoxel(nucleiParts.get(i).get(k).x, 
			   							nucleiParts.get(i).get(k).y, imp.getStackIndex(C2, 1, 1)-1)
			   							- meanC2, 2.0);
		   					}	   					
		   					break estimatingParticle;
		   				}
				   	}		   		
			   	}
		   		
		   		sdC1 /= number-1.0;
		   		sdC2 /= number-1.0;
				sdC1 = Math.sqrt(sdC1);
				sdC2 = Math.sqrt(sdC2);		   		
		   	}
	   		progress.notifyMessage(particleNr + " particles with in total " 
	   				+ number + " pixels used for estimating background", ProgressDialog.LOG);
		   	
	   		/**
	   		 * SEGMENT OTHER CHANNELS
	   		 * */
		   	double thresholdC1 = meanC1 + foldSD * sdC1;
//		   	progress.notifyMessage("Threshold C1: " + thresholdC1 + "(mean " + meanC1 + " sd " + sdC1 + ")", ProgressDialog.LOG);
		   	double thresholdC2 = meanC2 + foldSD * sdC2;
//		   	progress.notifyMessage("Threshold C2: " + thresholdC2 + "(mean " + meanC2 + " sd " + sdC2 + ")", ProgressDialog.LOG);
		   	
		   	ImagePlus impC1 = IJ.createHyperStack(imp.getTitle()+" C1", imp.getWidth(), imp.getHeight(),
		   			1, 1, 1, 8);
			ImagePlus impC2 = IJ.createHyperStack(imp.getTitle()+" C2", imp.getWidth(), imp.getHeight(),
		   			1, 1, 1, 8);
						
		   	for(int x = 0; x < imp.getWidth(); x++){
	   			for(int y = 0; y < imp.getHeight(); y++){
	   				if(imp.getStack().getVoxel(x, y, imp.getStackIndex(C1, 1, 1)-1) <= thresholdC1){
	   					impC1.getStack().setVoxel(x, y, impC1.getStackIndex(1, 1, 1)-1, 0.0);
	   				}else{
//	   					progressDialog.notifyMessage("C1 no "+(imp.getStack().getVoxel(x, y, imp.getStackIndex(C1, 1, 1)-1))
//	   							+" | " + thresholdC1, ProgressDialog.LOG);
	   					impC1.getStack().setVoxel(x, y, impC1.getStackIndex(1, 1, 1)-1, Math.pow(2, impC1.getBitDepth())-1);
	   				}
	   				if(imp.getStack().getVoxel(x, y, imp.getStackIndex(C2, 1, 1)-1) <= thresholdC2){
	   					impC2.getStack().setVoxel(x, y, impC2.getStackIndex(1, 1, 1)-1, 0.0);
	   				}else{
	   					impC2.getStack().setVoxel(x, y, impC2.getStackIndex(1, 1, 1)-1, Math.pow(2, impC2.getBitDepth())-1);
	   				}
	   			}
		   	}
			
//		   	impC2.show();
//		   	impC1.show();
//		   	
//		   	new WaitForUserDialog("wait").show();
//		   	
//		   	impC2.hide();
//		   	impC1.hide();
		   			   	
		   	// maximum filter, Fill Holes
		   	{
			   	((ByteProcessor)impC2.getProcessor()).dilate(1, 0);
			   	((ByteProcessor)impC2.getProcessor()).dilate(1, 0);
		   			   		
//		   		impC1.show();
//			   	new WaitForUserDialog("wait").show();
//			   	impC1.hide();
		   		
			   	fill(impC1.getProcessor(), (int)Math.pow(2.0,(double)imp.getBitDepth())-1, 0);
			   	
//			   	impC1.show();
//			   	new WaitForUserDialog("wait").show();
//			   	impC1.hide();
		   		                       
			   	((ByteProcessor)impC1.getProcessor()).erode(1, 0);
			   	((ByteProcessor)impC1.getProcessor()).erode(1, 0);
		   		
//			   	impC2.show();
//			   	new WaitForUserDialog("wait1").show();
//			   	impC2.hide();
			   	
			   	((ByteProcessor)impC2.getProcessor()).dilate(1, 0);
			   	((ByteProcessor)impC2.getProcessor()).dilate(1, 0);
//		   		
//		   		impC2.show();
//			   	new WaitForUserDialog("wait").show();
//			   	impC2.hide();
//		   			   		
		   		fill(impC2.getProcessor(), (int)Math.pow(2.0,(double)imp.getBitDepth())-1, 0);
//		   		
//		   		impC2.show();
//			   	new WaitForUserDialog("wait").show();
//			   	impC2.hide();
	
			   	((ByteProcessor)impC2.getProcessor()).erode(1, 0);
			   	((ByteProcessor)impC2.getProcessor()).erode(1, 0);
			   	
//			   	impC2.show();
//			   	new WaitForUserDialog("wait").show();
//			   	impC2.hide();
		   	}
		   	
		   	//
		   	ArrayList<Nucleus> nuclei = new ArrayList<Nucleus>(nucleiParts.size());
		   	for(int i = 0; i < nucleiParts.size(); i++){
		   		nuclei.add(new Nucleus(nucleiParts.get(i), impC1, impC2, maxDistUnits, profileStepSize, 3));		   		
		   	}
		   	nuclei.trimToSize();
		   	progress.notifyMessage("Found nuclei " + nuclei.size(), ProgressDialog.LOG);
		   	
		   	//remove nonoverlapping nuclei
		   	ImagePlus impCDapiFiltered = impCDapi.duplicate();
		   	ImagePlus impCDapiSegmented = impCDapi.duplicate();
		 	for(int i = nuclei.size()-1; i >= 0; i--){
		 		if(nuclei.get(i).overlapC1 < minFractionOverlapC1 || nuclei.get(i).overlapC2 < minFractionOverlapC2){
//		 			progress.notifyMessage("Remove nucleus " + i + ": C1=" + nuclei.get(i).overlapC1 + "|C2=" + nuclei.get(i).overlapC2, ProgressDialog.LOG);
		   			for(int j = 0; j < nuclei.get(i).surfacePoints.size(); j++){
		   				impCDapiFiltered.getStack().setVoxel(nuclei.get(i).surfacePoints.get(j).x, 
		   						nuclei.get(i).surfacePoints.get(j).y, 0, 0.0);
		   			}
		   			for(int j = 0; j < nuclei.get(i).insidePoints.size(); j++){
		   				impCDapiFiltered.getStack().setVoxel(nuclei.get(i).insidePoints.get(j).x, 
		   						nuclei.get(i).insidePoints.get(j).y, 0, 0.0);
		   				impCDapiSegmented.getStack().setVoxel(nuclei.get(i).insidePoints.get(j).x, 
		   						nuclei.get(i).insidePoints.get(j).y, 0, 0.0);
		   			}
		 			nuclei.remove(i);
		   		}
		 	}
		   	nuclei.trimToSize();
		   	progress.notifyMessage("Selected nuclei " + nuclei.size(), ProgressDialog.LOG);
		   	
//		   	new WaitForUserDialog("wait").show();
		   	
		   	ImagePlus mapImp = associateToMembranes(nuclei, impCopy, impCDapiFiltered);
		   			   	
//		   	mapImp.show();
//		   	new WaitForUserDialog("wait").show();
//		   	mapImp.hide();
		   	
		   	TextRoi txtID;
		   	mapImp.setOverlay(new Overlay());
		   	mapImp.setHideOverlay(false);
		   	for(int i = 0; i < nuclei.size(); i++){
		   		txtID = new TextRoi(nuclei.get(i).centerX,nuclei.get(i).centerY, df0.format(i+1), RoiFont);
				txtID.setStrokeColor(Color.WHITE);
				mapImp.getOverlay().add(txtID);
		   	}		   			   	
		   	
		/******************************************************************
		*** 						OUPUT OPTIONS						***	
		*******************************************************************/
		//Define Output File Names
			Date currentDate = new Date();
			
			String filePrefix;
			if(name[task].contains(".")){
				filePrefix = name[task].substring(0,name[task].lastIndexOf(".")) + "_NuQ_" + partForSelectedRegion;
			}else{
				filePrefix = name[task] + "_NuQ_" + partForSelectedRegion;
			}
			
			if(saveDate){
				filePrefix += "_" + NameDateFormatter.format(currentDate);
			}
						
			filePrefix = dir[task] + filePrefix;
		//Define Output File Names
			
		//Output Datafiles
			re = new RoiEncoder(filePrefix + "_roi");
			try {
				re.write(selections [task]);
			} catch (IOException e) {
				progress.notifyMessage("ROI Manager: "  + e.getMessage(), ProgressDialog.ERROR);
			}
			
			//output background image
			{
				IJ.saveAsTiff(impBG, filePrefix + "_bg.tif");
			}
						
			ImagePlus impOut;
			//Output outline
			{
				impOut = IJ.createHyperStack(imp.getTitle() + " Surface", imp.getWidth(), imp.getHeight(), 
						1, 1, 1, 8);
				for(int i = 0; i < nuclei.size(); i++){
					for(int j = 0; j < nuclei.get(i).surfacePoints.size(); j++){
						impOut.getStack().setVoxel(nuclei.get(i).surfacePoints.get(j).x, nuclei.get(i).surfacePoints.get(j).y, 
								impOut.getStackIndex(1, 1, 1)-1,
								255.0);
					}
				}
				impOut.setOverlay(mapImp.getOverlay().duplicate());
				impOut.setHideOverlay(false);
				impOut.setCalibration(imp.getCalibration());
				impOut.setC(1);
			 	IJ.run(impOut, "Grays", "");
				impOut.setDisplayRange(0, 255);		
				
				IJ.saveAsTiff(impOut, filePrefix + "_ol.tif");				
				for(int i = 0; i < nuclei.size(); i++){
			   		txtID = new TextRoi(nuclei.get(i).centerX,nuclei.get(i).centerY, df0.format(i+1), RoiFont);
					txtID.setStrokeColor(Color.WHITE);
					for(int ci = 0; ci < impOut.getNChannels(); ci++){
						txtID.drawPixels(impOut.getStack().getProcessor(impOut.getStackIndex(ci+1, 1, 1)));		
					}
			   	}
				IJ.saveAs(impOut, "PNG", filePrefix + "_ol.png");
				
			}			
			
			//Output masks
			{
				impOut = IJ.createHyperStack(imp.getTitle() + " Surface", imp.getWidth(), imp.getHeight(), 
						3, 1, 1, 8);
			 	for(int x = 0; x < imp.getWidth(); x++){
		   			for(int y = 0; y < imp.getHeight(); y++){
		   				impOut.getStack().setVoxel(x, y, impOut.getStackIndex(1, 1, 1)-1,
								impCDapiSegmented.getStack().getVoxel(x, y, impCDapiSegmented.getStackIndex(1, 1, 1)-1));
		   				impOut.getStack().setVoxel(x, y, impOut.getStackIndex(2, 1, 1)-1,
								impC1.getStack().getVoxel(x, y, impC1.getStackIndex(1, 1, 1)-1));
		   				impOut.getStack().setVoxel(x, y, impOut.getStackIndex(3, 1, 1)-1,
								impC2.getStack().getVoxel(x, y, impC2.getStackIndex(1, 1, 1)-1));
		   			}
			 	}
			 	impOut.setC(1);
			 	IJ.run(impOut, "Blue", "");
				impOut.setDisplayRange(0, 255);
				impOut.setC(2);
			 	IJ.run(impOut, "Red", "");
				impOut.setDisplayRange(0, 255);
				impOut.setC(3);
			 	IJ.run(impOut, "Green", "");
				impOut.setDisplayRange(0, 255);
				impOut.setDisplayMode(IJ.COMPOSITE);		
				impOut.setOverlay(new Overlay());
				impOut.setOverlay(mapImp.getOverlay().duplicate());
				impOut.setHideOverlay(false);
				impOut.setCalibration(imp.getCalibration());
				IJ.saveAsTiff(impOut, filePrefix + "_msk.tif");
				

				
				for(int i = 0; i < nuclei.size(); i++){
			   		txtID = new TextRoi(nuclei.get(i).centerX,nuclei.get(i).centerY, df0.format(i+1), RoiFont);
					txtID.setStrokeColor(Color.WHITE);
					for(int ci = 0; ci < impOut.getNChannels(); ci++){
						txtID.drawPixels(impOut.getStack().getProcessor(impOut.getStackIndex(ci+1, 1, 1)));	
					}
			   	}		
				IJ.saveAs(impOut, "PNG", filePrefix + "_msk.png");
			}			
			
			//Get Profiles, save Profiles, and make profile plots
			double xValues [] = new double [1+nuclei.get(0).surfacePoints.get(0).profileCentre*2];
			double yValuesAvgC1 [][] = new double [nuclei.size()][1+nuclei.get(0).surfacePoints.get(0).profileCentre*2];
			double yValuesAvgC2 [][] = new double [nuclei.size()][1+nuclei.get(0).surfacePoints.get(0).profileCentre*2];
			double yValuesAvgCOI [][] = new double [nuclei.size()][1+nuclei.get(0).surfacePoints.get(0).profileCentre*2];
			double yValuesSDC1 [][] = new double [nuclei.size()][1+nuclei.get(0).surfacePoints.get(0).profileCentre*2];
			double yValuesSDC2 [][] = new double [nuclei.size()][1+nuclei.get(0).surfacePoints.get(0).profileCentre*2];
			double yValuesSDCOI [][] = new double [nuclei.size()][1+nuclei.get(0).surfacePoints.get(0).profileCentre*2];
			
			double yValuesTopAvgC1 [][] = new double [nuclei.size()][1+nuclei.get(0).surfacePoints.get(0).profileCentre*2];
			double yValuesTopAvgC2 [][] = new double [nuclei.size()][1+nuclei.get(0).surfacePoints.get(0).profileCentre*2];
			double yValuesTopAvgCOI [][] = new double [nuclei.size()][1+nuclei.get(0).surfacePoints.get(0).profileCentre*2];
			double yValuesTopSDC1 [][] = new double [nuclei.size()][1+nuclei.get(0).surfacePoints.get(0).profileCentre*2];
			double yValuesTopSDC2 [][] = new double [nuclei.size()][1+nuclei.get(0).surfacePoints.get(0).profileCentre*2];
			double yValuesTopSDCOI [][] = new double [nuclei.size()][1+nuclei.get(0).surfacePoints.get(0).profileCentre*2];
			
			String legends [] = new String [nuclei.size()];
			double yMaxC1 = 0.0, yMaxC2 = 0.0, yMaxCOI = 0.0;
			{
				for(int p = 0; p < xValues.length; p++){
					xValues [p] = profileStepSize*((double) p - (double)((int)(xValues.length/2.0)));
				}
				
				double avgProfile [][][];
				for(int i = 0; i < nuclei.size(); i++){
					legends [i] = "N" + (i+1);
					avgProfile = Nucleus.getAvgAndSDOfProfiles(nuclei.get(i).surfacePoints);
					for(int p = 0; p < avgProfile[0][0].length; p++){
//						progress.notifyMessage(legends[i] + "|C1|" + "p" + p + ":" + avgProfile [0][0][p], ProgressDialog.LOG);
						yValuesAvgC1[i][p] = avgProfile [0][0][p];
						yValuesAvgC2[i][p] = avgProfile [0][1][p];
						yValuesAvgCOI[i][p] = avgProfile [0][2][p];
						yValuesSDC1[i][p] = avgProfile [1][0][p];
						yValuesSDC2[i][p] = avgProfile [1][1][p];
						yValuesSDCOI[i][p] = avgProfile [1][2][p];
						
						if(yMaxC1 < yValuesAvgC1[i][p]) yMaxC1 = yValuesAvgC1[i][p];
						if(yMaxC2 < yValuesAvgC2[i][p]) yMaxC2 = yValuesAvgC2[i][p];
						if(yMaxCOI < yValuesAvgCOI[i][p]) yMaxCOI = yValuesAvgCOI[i][p];
						if(yMaxC1 < yValuesSDC1[i][p]) yMaxC1 = yValuesSDC1[i][p];
						if(yMaxC2 < yValuesSDC2[i][p]) yMaxC2 = yValuesSDC2[i][p];
						if(yMaxCOI < yValuesSDCOI[i][p]) yMaxCOI = yValuesSDCOI[i][p];
					}
					
					avgProfile = Nucleus.getAvgAndSDOfProfiles(nuclei.get(i).maxRangePointsC1);
					for(int p = 0; p < avgProfile[0][0].length; p++){
						yValuesTopAvgC1[i][p] = avgProfile [0][0][p];
						yValuesTopSDC1[i][p] = avgProfile [1][0][p];						
						if(yMaxC1 < yValuesTopAvgC1[i][p]) yMaxC1 = yValuesTopAvgC1[i][p];
						if(yMaxC1 < yValuesTopSDC1[i][p]) yMaxC1 = yValuesTopSDC1[i][p];						
					}
					
					avgProfile = Nucleus.getAvgAndSDOfProfiles(nuclei.get(i).maxRangePointsC2);
					for(int p = 0; p < avgProfile[0][0].length; p++){
						yValuesTopAvgC2[i][p] = avgProfile [0][1][p];
						yValuesTopSDC2[i][p] = avgProfile [1][1][p];
						if(yMaxC2 < yValuesTopAvgC2[i][p]) yMaxC2 = yValuesTopAvgC2[i][p];
						if(yMaxC2 < yValuesTopSDC2[i][p]) yMaxC2 = yValuesTopSDC2[i][p];
					}
					
					avgProfile = Nucleus.getAvgAndSDOfProfiles(nuclei.get(i).maxRangePointsCOI);
					for(int p = 0; p < avgProfile[0][0].length; p++){
						yValuesTopAvgCOI[i][p] = avgProfile [0][2][p];
						yValuesTopSDCOI[i][p] = avgProfile [1][2][p];						
						if(yMaxCOI < yValuesTopAvgCOI[i][p]) yMaxCOI = yValuesTopAvgCOI[i][p];
						if(yMaxCOI < yValuesTopSDCOI[i][p]) yMaxCOI = yValuesTopSDCOI[i][p];
					}
					
					
				}
				yMaxC1 *= 1.1;
				yMaxC2 *= 1.1;
				yMaxCOI *= 1.1;
				
				impOut = plot2DArray(xValues, yValuesAvgC1, "Channel 1 - Avg",
						"distance to membrane (" + calibrationUnit + "), inside (-), outside (+)", 
						"average intensity in channel 1 (a.u.)", false, legends, yMaxC1);								
				IJ.saveAs(impOut, "PNG", filePrefix + "_C1_AVG.png");
				
				impOut = plot2DArray(xValues, yValuesAvgC2, "Channel 2 - Avg",
						"distance to membrane (" + calibrationUnit + "), inside (-), outside (+)",
						"average intensity in channel 2 (a.u.)", false, legends, yMaxC2);								
				IJ.saveAs(impOut, "PNG", filePrefix + "_C2_AVG.png");
				
				impOut = plot2DArray(xValues, yValuesAvgCOI, "Channel of interest - Avg",
						"distance to membrane (" + calibrationUnit + "), inside (-), outside (+)",
						"average intensity in channel of interest (a.u.)", false, legends, yMaxCOI);								
				IJ.saveAs(impOut, "PNG", filePrefix + "_COI_AVG.png");
								
				impOut = plot2DArray(xValues, yValuesSDC1, "Channel 1 - SD",
						"distance to membrane (" + calibrationUnit + "), inside (-), outside (+)", 
						"standard deviation of intensity in channel 1 (a.u.)", false, legends, yMaxC1);								
				IJ.saveAs(impOut, "PNG", filePrefix + "_C1_SD.png");
				
				impOut = plot2DArray(xValues, yValuesSDC2, "Channel 2 - SD",
						"distance to membrane (" + calibrationUnit + "), inside (-), outside (+)", 
						"standard deviation of intensity in channel 2 (a.u.)", false, legends, yMaxC2);								
				IJ.saveAs(impOut, "PNG", filePrefix + "_C2_SD.png");
				
				impOut = plot2DArray(xValues, yValuesSDCOI, "Channel of interest - SD",
						"distance to membrane (" + calibrationUnit + "), inside (-), outside (+)", 
						"standard deviation of intensity in channel of interest (a.u.)", false, legends, yMaxCOI);								
				IJ.saveAs(impOut, "PNG", filePrefix + "_COI_SD.png");
				
				/*
				 * Top Averages for a part
				 * */

				impOut = plot2DArray(xValues, yValuesTopAvgC1, "Channel 1 - Avg",
						"distance to membrane (" + calibrationUnit + "), inside (-), outside (+)", 
						"average intensity in channel 1 of max (Div " + partForSelectedRegion + ") (a.u.)", false, legends, yMaxC1);								
				IJ.saveAs(impOut, "PNG", filePrefix + "_C1_QAVG.png");
				
				impOut = plot2DArray(xValues, yValuesTopAvgC2, "Channel 2 - Avg",
						"distance to membrane (" + calibrationUnit + "), inside (-), outside (+)",
						"average intensity in channel 2 of max (Div " + partForSelectedRegion + ") (a.u.)", false, legends, yMaxC2);								
				IJ.saveAs(impOut, "PNG", filePrefix + "_C2_QAVG.png");
				
				impOut = plot2DArray(xValues, yValuesTopAvgCOI, "Channel of interest - Avg",
						"distance to membrane (" + calibrationUnit + "), inside (-), outside (+)",
						"average intensity in channel of interest of max (Div " + partForSelectedRegion + ") (a.u.)", false, legends, yMaxCOI);								
				IJ.saveAs(impOut, "PNG", filePrefix + "_COI_QAVG.png");
								
				impOut = plot2DArray(xValues, yValuesTopSDC1, "Channel 1 - SD",
						"distance to membrane (" + calibrationUnit + "), inside (-), outside (+)", 
						"standard deviation of intensity in channel 1 of max (Div " + partForSelectedRegion + ") (a.u.)", false, legends, yMaxC1);								
				IJ.saveAs(impOut, "PNG", filePrefix + "_C1_QSD.png");
				
				impOut = plot2DArray(xValues, yValuesTopSDC2, "Channel 2 - SD",
						"distance to membrane (" + calibrationUnit + "), inside (-), outside (+)", 
						"standard deviation of intensity in channel 2 of max (Div " + partForSelectedRegion + ") (a.u.)", false, legends, yMaxC2);								
				IJ.saveAs(impOut, "PNG", filePrefix + "_C2_QSD.png");
				
				impOut = plot2DArray(xValues, yValuesTopSDCOI, "Channel of interest - SD",
						"distance to membrane (" + calibrationUnit + "), inside (-), outside (+)", 
						"standard deviation of intensity in channel of interest of max (Div " + partForSelectedRegion + ") (a.u.)", false, legends, yMaxCOI);								
				IJ.saveAs(impOut, "PNG", filePrefix + "_COI_QSD.png");
				
			}
						
			impOut.changes = false;
			impOut.close();
			
			//save mapImp
			IJ.saveAsTiff(mapImp, filePrefix + "_mp.tif");
			mapImp.changes = false;
			mapImp.close();		
			
			imp.changes = false;
			imp.close();
			impC1.changes = false;
			impC1.close();
			impC2.changes = false;
			impC2.close();
			impCDapi.changes = false;
			impCDapi.close();
			impCDapiFiltered.changes = false;
			impCDapiFiltered.close();
			impCDapiSegmented.changes = false;
			impCDapiSegmented.close();
			
			//start metadata file
			TextPanel tp1 =new TextPanel("results");
			
			tp1.append("Saving date:	" + FullDateFormatter.format(currentDate)
						+ "	Starting date:	" + FullDateFormatter.format(startDate));
			tp1.append("Image name:	" + name[task]);
			tp1.append("");
			tp1.append("Settings:");
			tp1.append("	Calibration");
			tp1.append("		manually calibrate image	" + recalibrate);
			tp1.append("		calibration [unit/px]: 	" +  df6.format(calibration));
			tp1.append("		calibration unit: 	" +  calibrationUnit);
			
			tp1.append("	Preprocessing");
			tp1.append("		Perform Gaussian blur on all channels before binarizing	" + gb1);
			tp1.append("		Sigma of Gaussian blur	" +  df6.format(gaussianSigma1));
			
			tp1.append("	Channel selection");
			tp1.append("		DAPI channel	" +  df0.format(CDapi));
			tp1.append("		Channel 1	" +  df0.format(C1));
			tp1.append("		Channel 2	" +  df0.format(C2));
			tp1.append("		Channel of interest	" +  df0.format(COI));
			
			tp1.append("	Thresholding methods");	
			tp1.append("		Fold SD for threshold in Channel 1 and 2	" +  df6.format(foldSD));
			tp1.append("		Thresholding Method for DAPI	" +  selectedThresholdMethod);
			
			tp1.append("	Filtering and Analyzing");
			tp1.append("		Mininimum size of accepted nuclei (px)	" +  df0.format(minSize));
			tp1.append("		Mininimum overlap of nuclei with positive signal in Channel 1	" +  df6.format(minFractionOverlapC1*100.0));
			tp1.append("		Mininimum overlap of nuclei with positive signal in Channel 2	" +  df6.format(minFractionOverlapC2*100.0));
			tp1.append("		Maximum distance to the nuclear membrane (calibration units)	" +  df6.format(maxDistUnits));
			tp1.append("		Divisioning of membrane for obtaining the highest intensity region	" +  df0.format(partForSelectedRegion));
			

			tp1.append("	Estimated parameters");
			tp1.append("		Threshold DAPI channel	" + df6.format(thresholdDAPI));
			tp1.append("		Number of particles included in background estimation	" + df0.format(particleNr));
			tp1.append("		Number of pixels included in background estimation	" + df0.format(number));
			tp1.append("		Background Mean Channel 1	" + df6.format(meanC1));
			tp1.append("		Background Mean Channel 2	" + df6.format(meanC2));
			tp1.append("		Background SD Channel 1	" + df6.format(sdC1));
			tp1.append("		Background SD Channel 2	" + df6.format(sdC2));
			tp1.append("		Threshold Channel 1	" + df6.format(thresholdC1));
			tp1.append("		Threshold Channel 2	" + df6.format(thresholdC2));
			
			tp1.append("Results:");
			tp1.append("	Average Profiles - Channel 1:");
			tp1.append("			Distance to membrane (µm), inside (-), outside (+):");
			String appText = "	Nucleus ID	Overlap with channel";
			for(int p = 0; p < xValues.length; p++){
				appText += "	" + xValues[p];
			}
			tp1.append(appText);
			for(int i = 0; i < yValuesAvgC1.length; i++){
				appText = "	" + df0.format(i+1) + "	" + df6.format(nuclei.get(i).overlapC1);
				for(int p = 0; p < xValues.length; p++){
					appText += "	" + df6.format(yValuesAvgC1[i][p]);
				}
				tp1.append(appText);
			}
			
			tp1.append("");
			tp1.append("	Average Profiles - Channel 2:");
			tp1.append("			Distance to membrane (µm), inside (-), outside (+):");
			appText = "	Nucleus ID	Overlap with channel";
			for(int p = 0; p < xValues.length; p++){
				appText += "	" + xValues[p];
			}
			tp1.append(appText);
			for(int i = 0; i < yValuesAvgC2.length; i++){
				appText = "	" + df0.format(i+1) + "	" + df6.format(nuclei.get(i).overlapC2);
				for(int p = 0; p < xValues.length; p++){
					appText += "	" + df6.format(yValuesAvgC2[i][p]);
				}
				tp1.append(appText);
			}

			tp1.append("");
			tp1.append("	Average Profiles - Channel of interest:");
			tp1.append("			Distance to membrane (µm), inside (-), outside (+):");
			appText = "	Nucleus ID	";
			for(int p = 0; p < xValues.length; p++){
				appText += "	" + xValues[p];
			}
			tp1.append(appText);
			for(int i = 0; i < yValuesAvgCOI.length; i++){
				appText = "	" + df0.format(i+1) + "	";
				for(int p = 0; p < xValues.length; p++){
					appText += "	" + df6.format(yValuesAvgCOI[i][p]);
				}
				tp1.append(appText);
			}
			
			tp1.append("");
			tp1.append("	Standard Deviation Profiles - Channel 1:");
			tp1.append("			Distance to membrane (µm), inside (-), outside (+):");
			appText = "	Nucleus ID	Overlap with channel";
			for(int p = 0; p < xValues.length; p++){
				appText += "	" + xValues[p];
			}
			tp1.append(appText);
			for(int i = 0; i < yValuesSDC1.length; i++){
				appText = "	" + df0.format(i+1) + "	" + df6.format(nuclei.get(i).overlapC1);
				for(int p = 0; p < xValues.length; p++){
					appText += "	" + df6.format(yValuesSDC1[i][p]);
				}
				tp1.append(appText);
			}
			
			tp1.append("");
			tp1.append("	Standard Deviation Profiles - Channel 2:");
			tp1.append("			Distance to membrane (µm), inside (-), outside (+):");
			appText = "	Nucleus ID	Overlap with channel";
			for(int p = 0; p < xValues.length; p++){
				appText += "	" + xValues[p];
			}
			tp1.append(appText);
			for(int i = 0; i < yValuesSDC2.length; i++){
				appText = "	" + df0.format(i+1) + "	" + df6.format(nuclei.get(i).overlapC2);
				for(int p = 0; p < xValues.length; p++){
					appText += "	" + df6.format(yValuesSDC2[i][p]);
				}
				tp1.append(appText);
			}	
			

			tp1.append("");
			tp1.append("	Standard Deviation Profiles - Channel of interest:");
			tp1.append("			Distance to membrane (µm), inside (-), outside (+):");
			appText = "	Nucleus ID	";
			for(int p = 0; p < xValues.length; p++){
				appText += "	" + xValues[p];
			}
			tp1.append(appText);
			for(int i = 0; i < yValuesSDCOI.length; i++){
				appText = "	" + df0.format(i+1) + "	";
				for(int p = 0; p < xValues.length; p++){
					appText += "	" + df6.format(yValuesSDCOI[i][p]);
				}
				tp1.append(appText);
			}
			
			/*
			 * Save part intensities
			 * */
			tp1.append("Results:");
			tp1.append("	Average Profiles of Max (Div " + partForSelectedRegion + ") - Channel 1:");
			tp1.append("			Distance to membrane (µm), inside (-), outside (+):");
			appText = "	Nucleus ID	Overlap with channel";
			for(int p = 0; p < xValues.length; p++){
				appText += "	" + xValues[p];
			}
			tp1.append(appText);
			for(int i = 0; i < yValuesTopAvgC1.length; i++){
				appText = "	" + df0.format(i+1) + "	" + df6.format(nuclei.get(i).overlapC1);
				for(int p = 0; p < xValues.length; p++){
					appText += "	" + df6.format(yValuesTopAvgC1[i][p]);
				}
				tp1.append(appText);
			}
			
			tp1.append("");
			tp1.append("	Average Profiles of Max (Div " + partForSelectedRegion + ") - Channel 2:");
			tp1.append("			Distance to membrane (µm), inside (-), outside (+):");
			appText = "	Nucleus ID	Overlap with channel";
			for(int p = 0; p < xValues.length; p++){
				appText += "	" + xValues[p];
			}
			tp1.append(appText);
			for(int i = 0; i < yValuesTopAvgC2.length; i++){
				appText = "	" + df0.format(i+1) + "	" + df6.format(nuclei.get(i).overlapC2);
				for(int p = 0; p < xValues.length; p++){
					appText += "	" + df6.format(yValuesTopAvgC2[i][p]);
				}
				tp1.append(appText);
			}

			tp1.append("");
			tp1.append("	Average Profiles of Max (Div " + partForSelectedRegion + ") - Channel of interest:");
			tp1.append("			Distance to membrane (µm), inside (-), outside (+):");
			appText = "	Nucleus ID	";
			for(int p = 0; p < xValues.length; p++){
				appText += "	" + xValues[p];
			}
			tp1.append(appText);
			for(int i = 0; i < yValuesTopAvgCOI.length; i++){
				appText = "	" + df0.format(i+1) + "	";
				for(int p = 0; p < xValues.length; p++){
					appText += "	" + df6.format(yValuesTopAvgCOI[i][p]);
				}
				tp1.append(appText);
			}
			
			tp1.append("");
			tp1.append("	Standard Deviation Profiles of Max (Div " + partForSelectedRegion + ") - Channel 1:");
			tp1.append("			Distance to membrane (µm), inside (-), outside (+):");
			appText = "	Nucleus ID	Overlap with channel";
			for(int p = 0; p < xValues.length; p++){
				appText += "	" + xValues[p];
			}
			tp1.append(appText);
			for(int i = 0; i < yValuesTopSDC1.length; i++){
				appText = "	" + df0.format(i+1) + "	" + df6.format(nuclei.get(i).overlapC1);
				for(int p = 0; p < xValues.length; p++){
					appText += "	" + df6.format(yValuesTopSDC1[i][p]);
				}
				tp1.append(appText);
			}
			
			tp1.append("");
			tp1.append("	Standard Deviation Profiles of Max (Div " + partForSelectedRegion + ") - Channel 2:");
			tp1.append("			Distance to membrane (µm), inside (-), outside (+):");
			appText = "	Nucleus ID	Overlap with channel";
			for(int p = 0; p < xValues.length; p++){
				appText += "	" + xValues[p];
			}
			tp1.append(appText);
			for(int i = 0; i < yValuesTopSDC2.length; i++){
				appText = "	" + df0.format(i+1) + "	" + df6.format(nuclei.get(i).overlapC2);
				for(int p = 0; p < xValues.length; p++){
					appText += "	" + df6.format(yValuesTopSDC2[i][p]);
				}
				tp1.append(appText);
			}	
			

			tp1.append("");
			tp1.append("	Standard Deviation Profiles of Max (Div " + partForSelectedRegion + ") - Channel of interest:");
			tp1.append("			Distance to membrane (µm), inside (-), outside (+):");
			appText = "	Nucleus ID	";
			for(int p = 0; p < xValues.length; p++){
				appText += "	" + xValues[p];
			}
			tp1.append(appText);
			for(int i = 0; i < yValuesTopSDCOI.length; i++){
				appText = "	" + df0.format(i+1) + "	";
				for(int p = 0; p < xValues.length; p++){
					appText += "	" + df6.format(yValuesTopSDCOI[i][p]);
				}
				tp1.append(appText);
			}
			
			
			addFooter(tp1, currentDate);				
			tp1.saveAs(filePrefix + "m.txt");
			
		//Output Datafile
		processingDone = true;
		break running;
	}	
	progress.updateBarText("finished!");
	progress.setBar(1.0);
	progress.moveTask(task);
}
}

	/**
	* 1st dimension > different graphs
	* 2nd dimension > y points
	* */
	private static ImagePlus plot2DArray(double xValues [], double [][] array, String label, String xLabel, String yLabel,
			boolean logarithmic, String legends [], double yMax){
		Color c;
		Plot p;
		ImagePlus pImp;
		String legend = "";
		PlotWindow.noGridLines = true;
		
		p = new Plot(label, xLabel, yLabel);
		p.setAxisYLog(logarithmic);
		p.updateImage();
		p.setSize(600, 400);
		p.setLimits(xValues[0]-1.0, xValues[xValues.length-1]+1.0, 0.0, yMax+5.0);
		p.updateImage();
		for(int i = 0; i < array.length; i++){
			c = new Color(54+(int)(i/(double)array.length*200.0), 54+(int)(i/(double)array.length*200.0), 54+(int)(i/(double)array.length*200.0));
			p.setColor(c);
			p.addPoints(xValues,array[i],PlotWindow.LINE);
			legend += "" + legends [i];
			legend += "\n";
		}
		p.addLegend(legend);
		p.setLimits(xValues[0]-1.0, xValues[xValues.length-1]+1.0, 0.0, yMax+5.0);
		p.updateImage();
	//	p.setLimitsToFit(true);
		pImp = p.makeHighResolution("plot",1,true,false);
		return pImp;
	}

	public static double getMedian(double [] values){
		double [] medians = new double [values.length];
		for(int i = 0; i < values.length; i++){
			medians [i] = values [i];
		}
		
		Arrays.sort(medians);
		
		if(medians.length%2==0){
			return (medians[(int)((double)(medians.length)/2.0)-1]+medians[(int)((double)(medians.length)/2.0)])/2.0;
		}else{
			return medians[(int)((double)(medians.length)/2.0)];
		}		
	}

	private void addFooter(TextPanel tp, Date currentDate) {
		tp.append("");
		tp.append("Datafile was generated on " + FullDateFormatter2.format(currentDate) + " by '" + PLUGINNAME
				+ "', an ImageJ plug-in by Jan Niklas Hansen (jan.hansen@uni-bonn.de).");
		tp.append("The plug-in '" + PLUGINNAME + "' is distributed in the hope that it will be useful,"
				+ " but WITHOUT ANY WARRANTY; without even the implied warranty of"
				+ " MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.");
		tp.append("Plug-in version:	V" + PLUGINVERSION);

	}

	private static ImagePlus minIntensityProjection(ImagePlus imp) {
		ImagePlus impMin = IJ.createImage("minimum projection", imp.getWidth(), imp.getHeight(), 1, imp.getBitDepth());

		int maxValue = (int) (Math.pow(2.0, imp.getBitDepth()) - 1);

		for (int x = 0; x < imp.getWidth(); x++) {
			for (int y = 0; y < imp.getHeight(); y++) {
				impMin.getStack().setVoxel(x, y, 0, maxValue);
				for (int s = 0; s < imp.getStackSize(); s++) {
					if (imp.getStack().getVoxel(x, y, s) < impMin.getStack().getVoxel(x, y, 0)) {
						impMin.getStack().setVoxel(x, y, 0, imp.getStack().getVoxel(x, y, s));
					}
				}
			}
		}

		impMin.setCalibration(imp.getCalibration());
		return impMin;
	}

	private static ImagePlus medianIntensityProjection(ImagePlus imp) {
		ImagePlus impMedi = IJ.createImage("median projection", imp.getWidth(), imp.getHeight(), 1, imp.getBitDepth());

		int maxValue = (int) (Math.pow(2.0, imp.getBitDepth()) - 1);

		double medians[] = new double[imp.getStackSize()];
		double median;
		for (int x = 0; x < imp.getWidth(); x++) {
			for (int y = 0; y < imp.getHeight(); y++) {
				// Arrays.fill(medians, Double.NaN);
				for (int s = 0; s < imp.getStackSize(); s++) {
					medians[s] = imp.getStack().getVoxel(x, y, s);
				}
				Arrays.sort(medians);

				if (medians.length % 2 == 0) {
					median = (medians[(int) ((double) (medians.length) / 2.0) - 1]
							+ medians[(int) ((double) (medians.length) / 2.0)]) / 2.0;
				} else {
					median = medians[(int) ((double) (medians.length) / 2.0)];
				}
				impMedi.getStack().setVoxel(x, y, 0, median);
			}
		}

		impMedi.setCalibration(imp.getCalibration());
		return impMedi;
	}

	/**
	 * @param slice:
	 *            1 <= stackImage <= stacksize
	 */
	private static double thresholdImage(ImagePlus imp, String algorithm, int stackImage) {
		// threshold image
		imp.setSlice(stackImage);
		imp.getProcessor().setSliceNumber(stackImage);
		imp.getProcessor().setAutoThreshold(Method.valueOf(Method.class,algorithm), true);
//		IJ.setAutoThreshold(imp, (algorithm + " dark"));
		double minThreshold = imp.getProcessor().getMinThreshold();
//		IJ.log("min" + minThreshold);
		double imageMax = Math.pow(2.0, imp.getBitDepth()) - 1.0;

		for (int x = 0; x < imp.getWidth(); x++) {
			for (int y = 0; y < imp.getHeight(); y++) {
				if (imp.getStack().getVoxel(x, y, stackImage - 1) >= minThreshold) {
					imp.getStack().setVoxel(x, y, stackImage - 1, imageMax);
				} else {
					imp.getStack().setVoxel(x, y, stackImage - 1, 0.0);
				}
			}
		}
		// IJ.log("bin ");userCheck(impMax);
		return minThreshold;
	}

	//Flood filling option - fill holes from IJ.plugin.filter.Binary
	// Binary fill by Gabriel Landini, G.Landini at bham.ac.uk
    // 21/May/2008
    void fill(ImageProcessor ip, int foreground, int background) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        FloodFiller ff = new FloodFiller(ip);
        ip.setColor(127);
        for (int y=0; y<height; y++) {
            if (ip.getPixel(0,y)==background) ff.fill(0, y);
            if (ip.getPixel(width-1,y)==background) ff.fill(width-1, y);
        }
        for (int x=0; x<width; x++){
            if (ip.getPixel(x,0)==background) ff.fill(x, 0);
            if (ip.getPixel(x,height-1)==background) ff.fill(x, height-1);
        }
        byte[] pixels = (byte[])ip.getPixels();
        int n = width*height;
        for (int i=0; i<n; i++) {
        if (pixels[i]==127)
            pixels[i] = (byte)background;
        else
            pixels[i] = (byte)foreground;
        }
    }
    
	/**
	* Method adapted from Cilia v0.0.9, on 16.04.2020, https://github.com/hansenjn/CiliaQ
	* Filter the indicated channel of the image and remove particles below the @param minSize threshold.
	* @param imp: Hyperstack image where one channel represents the )recording of the volume of interest
	* @param c: defines the channel of the Hyperstack image imp, in which the information for the volume of interest is stored 1 < c < number of channels
	* @param particleLabel: the label for the volume of interest which is displayed in the progress dialog while obtaining object information
	* @param increaseRange: defines whether also diagonal pixels should be allowed while Flood Filling
	* */
    ArrayList<ArrayList<PartPoint>> filterChannel2DAndReturnParticles(ImagePlus imp, int c, String particleLabel,int minSize){	
		ImagePlus refImp = imp.duplicate();		
		int nrOfPoints = 0;
		for(int x = 0; x < imp.getWidth(); x++){
			for(int y = 0; y < imp.getHeight(); y++){	
				if(imp.getStack().getVoxel(x, y, imp.getStackIndex(c, 1, 1)-1) > 0.0){
					nrOfPoints++;
				}
			}
		}
			
		ArrayList<ArrayList<PartPoint>> particles = new ArrayList<ArrayList<PartPoint>>((int)Math.round((double)nrOfPoints/(double)minSize));
		
		int pc100 = nrOfPoints/100; if (pc100==0){pc100 = 1;}
		int pc1000 = nrOfPoints/1000; if (pc1000==0){pc1000 = 1;}
		int floodFilledPc = 0, floodFilledPcOld = 0;
		int[][] floodNodes = new int[nrOfPoints][2];
		int floodNodeX, floodNodeY, index = 0;
		ArrayList<PartPoint> preliminaryParticle;
		
		searchCells:for(int x = 0; x < imp.getWidth(); x++){
			for(int y = 0; y < imp.getHeight(); y++){		
				if(imp.getStack().getVoxel(x, y, imp.getStackIndex(c, 1, 1)-1) > 0.0){
					preliminaryParticle = new ArrayList<PartPoint>(nrOfPoints-floodFilledPc);		
					System.gc();
					preliminaryParticle.add(new PartPoint(x, y, refImp, c));
					
					imp.getStack().setVoxel(x, y, imp.getStackIndex(c, 1, 1)-1, 0.0);
					floodFilledPc++;
					
					//Floodfiller					
					floodNodeX = x;
					floodNodeY = y;
					 
					index = 0;
					 
					floodNodes[0][0] = floodNodeX;
					floodNodes[0][1] = floodNodeY;

					while (index >= 0){
						floodNodeX = floodNodes[index][0];
						floodNodeY = floodNodes[index][1];						
						index--;            						
						if ((floodNodeX > 0) 
								&& imp.getStack().getVoxel(floodNodeX-1, floodNodeY, imp.getStackIndex(c,1,1)-1) > 0.0){
							
							preliminaryParticle.add(new PartPoint(floodNodeX-1,floodNodeY, refImp, c));
							imp.getStack().setVoxel(floodNodeX-1, floodNodeY, imp.getStackIndex(c,1,1)-1, 0.0);
							
							index++;
							floodFilledPc++;
							
							floodNodes[index][0] = floodNodeX-1;
							floodNodes[index][1] = floodNodeY;
						}
						if ((floodNodeX < (imp.getWidth()-1)) 
								&& imp.getStack().getVoxel(floodNodeX+1, floodNodeY, imp.getStackIndex(c,1,1)-1) > 0.0){
							
							preliminaryParticle.add(new PartPoint(floodNodeX+1,floodNodeY, refImp, c));
							imp.getStack().setVoxel(floodNodeX+1, floodNodeY, imp.getStackIndex(c, 1, 1)-1, 0.0);
							
							index++;
							floodFilledPc++;
							
							floodNodes[index][0] = floodNodeX+1;
							floodNodes[index][1] = floodNodeY;
						}
						if ((floodNodeY > 0) 
								&& imp.getStack().getVoxel(floodNodeX, floodNodeY-1, imp.getStackIndex(c,1,1)-1) > 0.0){
							
							preliminaryParticle.add(new PartPoint(floodNodeX,floodNodeY-1, refImp, c));
							imp.getStack().setVoxel(floodNodeX, floodNodeY-1, imp.getStackIndex(c, 1, 1)-1, 0.0);
							
							index++;
							floodFilledPc++;
							
							floodNodes[index][0] = floodNodeX;
							floodNodes[index][1] = floodNodeY-1;
						}                
						if ((floodNodeY < (imp.getHeight()-1)) 
								&& imp.getStack().getVoxel(floodNodeX, floodNodeY+1, imp.getStackIndex(c,1,1)-1) > 0.0){
							
							preliminaryParticle.add(new PartPoint(floodNodeX,floodNodeY+1, refImp, c));
							imp.getStack().setVoxel(floodNodeX, floodNodeY+1, imp.getStackIndex(c, 1, 1)-1, 0.0);
							
							index++;
							floodFilledPc++;
							
							floodNodes[index][0] = floodNodeX;
							floodNodes[index][1] = floodNodeY+1;
						}
					}					
					//Floodfiller	
					preliminaryParticle.trimToSize();
					if(preliminaryParticle.size() >= minSize){
						particles.add(preliminaryParticle);		
//						progress.notifyMessage("Particle accepted: " + preliminaryParticle.size(), ProgressDialog.LOG); 
					}else{
//						progress.notifyMessage("Particle excluded: " + preliminaryParticle.size(), ProgressDialog.LOG); 
						preliminaryParticle.clear();
						preliminaryParticle.trimToSize();
					}
					
					if(floodFilledPc%(pc100)<pc1000){						
						progress.updateBarText("Connecting " + particleLabel + " complete: " + df3.format(((double)(floodFilledPc)/(double)(nrOfPoints))*100) + "%");
						progress.addToBar(0.2*((double)(floodFilledPc-floodFilledPcOld)/(double)(nrOfPoints)));
						floodFilledPcOld = floodFilledPc;
					}	
				}
				if(floodFilledPc==nrOfPoints){					
					break searchCells;
				}
			}
		}
		progress.updateBarText("Connecting " + particleLabel + " complete: " + df3.format(((double)(floodFilledPc)/(double)(nrOfPoints))*100) + "%");
		progress.addToBar(0.2*((double)(floodFilledPc-floodFilledPcOld)/(double)(nrOfPoints)));	
		particles.trimToSize();
		
		refImp.changes = false;
		refImp.close();
		System.gc();
		
		//write back to image
		{
			for(int j = 0; j < particles.size();j++){
				for(int i = 0; i < particles.get(j).size();i++){
					imp.getStack().setVoxel(particles.get(j).get(i).x,
							particles.get(j).get(i).y, 
							imp.getStackIndex(c, 1, 1)-1, 
							particles.get(j).get(i).intensity);
				}
			}
		}		
		return particles;
   }
   
    private ImagePlus associateToMembranes(ArrayList<Nucleus> nuclei, ImagePlus imp, ImagePlus impDapi){
    	ImagePlus mapImp = IJ.createHyperStack("map image", imp.getWidth(), imp.getHeight(), 1, imp.getNSlices(), imp.getNFrames(), 16);
    	int total = imp.getNSlices()*imp.getNFrames()*imp.getWidth()*imp.getHeight();
    	int counter = 0;
    	double step = (double) total / (double) imp.getNSlices() / (double) imp.getWidth() * 0.1;
    	
		for(int x = 0; x < imp.getWidth(); x++){
			for(int y = 0; y < imp.getHeight(); y++){
					associateToMembrane(nuclei, x, y, imp, impDapi, mapImp);
					counter ++;
				progress.updateBarText("Associate pixels to membrane (" 
						+ df3.format((double)(counter)/total*100.0) + "%)");
				progress.addToBar(step);
			}			
		}
		
		step = 0.1 / (double) nuclei.size();
		for(int n = 0; n < nuclei.size(); n++){
	   		nuclei.get(n).selectQuarterPoints(partForSelectedRegion);
	   		progress.updateBarText("Select top-intensity (Div " + partForSelectedRegion + ") of membrane (" 
					+ df3.format((double)(n+1)/(double)nuclei.size()*100.0) + "%)");
			progress.addToBar(step);
	   	}
		
    	return mapImp;
    }
    
    private void associateToMembrane(ArrayList<Nucleus> nuclei, int x, int y, ImagePlus imp, 
    		ImagePlus impDapi, ImagePlus mapImp){
    	int index, indexReconstruction;
    	double dist = 0.0, minDist = Double.POSITIVE_INFINITY;
    	MembranePoint p = null;
    	for(int i = 0; i < nuclei.size(); i++){
    		for(int j = 0; j < nuclei.get(i).surfacePoints.size(); j++){
    			dist = Math.sqrt(Math.pow(x-nuclei.get(i).surfacePoints.get(j).x, 2.0)
    					+ Math.pow(y-nuclei.get(i).surfacePoints.get(j).y, 2.0)) * calibration;
    			if (dist <= maxDistUnits && dist < minDist){
    				p = nuclei.get(i).surfacePoints.get(j);
    				minDist = dist;
    				
    			}
    		}
    	}
    	
    	if(p != null){
    		index = imp.getStackIndex(C1, 1, 1)-1;
    		indexReconstruction = impDapi.getStackIndex(1, 1, 1)-1;
    		if(!p.addToProfile(minDist, 
    				imp.getStack().getVoxel(x, y, index),
    				1,
    				impDapi.getStack().getVoxel(x, y, indexReconstruction)==0.0)){
    			IJ.log("Problem");
//    		}else{
//    			IJ.log(minDist + "|C1|" + imp.getStack().getVoxel(x, y, index));
    		}
    		
    		index = imp.getStackIndex(C2, 1, 1)-1;
    		indexReconstruction = impDapi.getStackIndex(1, 1, 1)-1;
    		if(!p.addToProfile(minDist, 
    				imp.getStack().getVoxel(x, y, index),
    				2,
    				impDapi.getStack().getVoxel(x, y, indexReconstruction)==0.0)){
    			IJ.log("Problem");
//    		}else{
//    			IJ.log(minDist + "|C2|" + imp.getStack().getVoxel(x, y, index));
    		}
    		
    		index = imp.getStackIndex(COI, 1, 1)-1;
    		indexReconstruction = impDapi.getStackIndex(1, 1, 1)-1;
    		if(!p.addToProfile(minDist, 
    				imp.getStack().getVoxel(x, y, index),
    				3,
    				impDapi.getStack().getVoxel(x, y, indexReconstruction)==0.0)){
    			IJ.log("Problem");
//    		}else{
//    			IJ.log(minDist + "|C2|" + imp.getStack().getVoxel(x, y, index));
    		}
    		
    		mapImp.getStack().setVoxel(x, y, mapImp.getStackIndex(1, 1, 1)-1,
    				convertDistanceToIntensity(minDist,maxDistUnits,impDapi.getStack().getVoxel(x, y, indexReconstruction)==0.0));
    	}	
    }
    
    private static double convertDistanceToIntensity(double dist, double maxDist, boolean outside){
    	double converted = (65535.0/2.0)/maxDist*dist;
    	if(outside)	converted *= -1.0;
    	converted += (65535.0/2.0) + converted;	
    	return converted;	
    }
   
}// end main class