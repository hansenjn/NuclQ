# NuclQ
 NuclQ - an ImageJ plugin to quantify intensity levels by distance to the nuclear membrane.

## Copyright notice

Copyright (C) 2020-2023: Jan N. Hansen

## Contact
For questions, e.g., on using, copyright, and citing the plugin, please contact:

```jan.hansen (at) uni-bonn.de```

## Installing NuclQ
NuclQ is an ImageJ plugin that can be run in ImageJ or the extended ImageJ version FIJI. Thus to use NuclQ you need to first have an ImageJ or FIJI software downloaded to your computer. ImageJ/FIJI are open-source, freely available softwares that can be downloaded [here](https://imagej.net/downloads).

Donwload the NuclQ....jar file from the latest release at the [release page](https://github.com/hansenjn/NuclQ/releases), launch your ImageJ or FIJI and install the plugin by drag and drop of the downloaded NuclQ....jar file into ImageJs/FIJIs status bar (this is the red marked region in the following image).

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/201358020-c3685947-b5d8-4127-88ec-ce9b4ddf0e56.png" width=500>
</p>

Next, a dialog will pop up asking you to save the plugin. Just confirm the dialog. 

FIJI/ImageJ will ask you to restart the software. Close the software and open it again. 

Now, the installed plugin can be launched via the FIJI/ImageJ menu entry at Plugins > JNH > NuclQ ...

## Manual / Instructions on how to use NuclQ
### Scope of the plugin
NuclQ allows you to determine intensity levels in cells as a function of the distance to the nuclear border. It is thus useful for determining nuclear intensity level and also intensity levels in the cytoplasm / close to the nucleus. The same program may also be used to study intensity levels as a function of the distance to any other object(s) of interest as long as you can provide a channel whereh these objects are clearly distinguishable from the background.

NuclQ allows to prefilter the objects based on intensity levels in two more channels (e.g., including only objects that are considered "signal-positive" in another channel).

### Input
The input image needs to be a multi-channel image that features at least one channel reporting an object marker (e.g., for nuclei (DAPI, Hoechst)) and a channel of interest from which intensity levels shall be determined. From the object-marker channel, NuclQ will detect the objects, i.e., nuclei. 

Optionally, your image may contain two more channels that can be used to select NuclQ to quantify only nuclei that are considered "signal-positive" in these channels.

#### Note for segmented input images
You may also use an image in which the object channel is already segmented (= "binarize") - in this case, unselect blurring and select the "Default" auto-threshold algorithm in the settings dialog:

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/204472566-f6d0d7ed-dc02-4a18-9800-f06fc5f15c21.png" width=300>
   <br>
   <img src="https://user-images.githubusercontent.com/27991883/204472676-3c0d0841-3547-4331-906d-885036161142.png" width=200>
</p>

### Processing & Analysis
#### Launching NuclQ
Launch NuclQ via the menu entry Plugins > JNH > NuclQ...

#### Settings dialog
The following settings dialog pops up when launching the plugin - here is an explanation of a few general seetings, for more understanding how it affects the analysis workflow please see the sections below that will explain few settings in more detail and show how they affect the analysis workflow.

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/204276811-651ac146-01f6-41fa-8e77-09b77a5b5d69.png" width=500>
</p>

- The process option <img src="https://user-images.githubusercontent.com/27991883/204499384-cf646cc9-273d-4bf5-9b37-0b491494a8af.png" height=22> allows you to decide whether you want to process the active image or all images open in FIJI or use the multi-task manager to select a list of files to be analyzed that are not open in FIJI (see below). Note that any processed image needs to be saved somewhere on your file system for proper functioning and output of plots.
- Calibration Settings: if your image has no pixel calibration specified, you should check the box "manually calibrate image" and add the information here. To check whether your image is calibrated just look at the bar on top of the image window. If there is a calibration unit specified there is no reason for recalibrating:

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/204501844-cc6471d7-5908-419b-8e69-e74441798939.png" width=300>
</p>

- Output settings: if you add the processing date to the output file names you avoid that you will override an analysis run on the same image.

##### Note for customizing settings
You will in any case need to adapt the parameters ```Minimum size of accepted nuclei``` and the ```Maximum distance to the nuclear membrane``` to your study question. - ```Minimum size of accepted nuclei``` should be selected as small that still obejcts are excluded that are too small to be considered an object according to your study question.
- ```Maximum distance to the nuclear membrane``` defines the length of the intensity profiles in the output files (see section on intensity profile plots, x-axis of these plots). E.g., if you select "5.0" for this parameter, the profiles will only range from ```-5.0``` to ```+5.0```.

#### (Optionally) select images
If the option <img src="https://user-images.githubusercontent.com/27991883/204499384-cf646cc9-273d-4bf5-9b37-0b491494a8af.png" height=22> was selected, NuclQ allows you to compile a list of files to be analyzed. After generating the list, start the processing.

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/204499657-0d5d613a-942f-4f0a-92a6-215cbcd01f61.png" width=400>
</p>

#### Initial selection of negative cells by the user
NuclQ first asks the user to draw a region of interest (ROI) in each image to be analyzed. This ROI needs to circumscribe / contain only objects that are "signal-negative" in the channels used for filtering. If you do not want to use the filtering for "signal-positive" objects you can just circumscibe any objects (nuclei) here - you however need to make sure that the ROI contains at least one object (or at least a part of it).

When all ROIs are set, all images are subjected to the analysis pipeline and all the following processing is fully-automated. A "progress dialog" informs about analysis progress. When the analysis is done the bar will turn green (on windows computers - not so on Mac computers) and the status bar will say "analysis done!" (on any opereating system).

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/204496729-234f6688-bb33-4149-a3ec-e5d468c27e2e.png" width=450>
   <img src="https://user-images.githubusercontent.com/27991883/204497944-e7056fb8-50ed-4823-81c9-b9d669c7a381.png" width=450>
</p>

##### Example for initial selection of negative cells by the user
In the following image we want to detect the blue DAPI nuclei, we want to detect only the nuclei that have a strong green and magenta signal (= that are "signal-positive" in the green and magenta channel). Thus we draw a ROI around two negative nuclei that are signal negative. Note that to add multiple regions to one ROI you need to hold shift while encircling regions. Holding shift and encircling additional nuclei allows to add more regions to the same ROI.

<p align="center">
 <img src="https://user-images.githubusercontent.com/27991883/204496355-99d3f5c7-9968-4103-ac89-e02299da94d5.png" width=680>
 <br>
 <i>Image: (c) Jan N. Hansen.</i>
</p>

#### Automatic Processing and Quantification
##### Preprocessing
NuclQ uses the channel specified as the "DAPI channel" to determine and detect objects as follows:
1. If selected by the user, NuclQ blurs the channel with a Gaussian blur based on the user-defined sigma.
2. NuclQ segments the channel into for- and background by using the selected auto-threshold algorithm.
3. NuclQ applies a Watershed algorithm to split joint objects

##### Detection & Filtering
NuclQ detects all the individual particles in the image as individual objects. Particles are not included if they contain less pixels than what the user has specified at <img src="https://user-images.githubusercontent.com/27991883/204479856-b72c42dc-e845-465b-94d2-12d91a526723.png" height=18>.

NuclQ filters the detected objects based on intensity information in Channel 1 and Channel 2
1. NuclQ retrieves the pixel intensities in the additional channels "Channel 1" and "Channel 2" for those pixels that are positive in the segmented "DAPI channel" and that are inside the ROI selected by the user. 
2. NuclQ determines the mean and standard deviation of these pixel intensities for each channel ("Channel 1", and "Channel 2"). 
3. NuclQ determines a threshold for each channel as mean + "fold" * standard deviation, where "fold" is the value specified by the user in the settings dialog at <img src="https://user-images.githubusercontent.com/27991883/204474395-54ef148e-b828-4052-adaf-19645e65a8ad.png" height=22> .
4. NuclQ segments the "Channel 1" and "Channel 2" using the determined threshold: all pixel intensities < the threshold are set to zero, all pixel intensities >= the threshold are set to the maximum intensity value (e.g., 255 in an 8-bit image)
5. The created masks are improved (made more round and less noisy) by applying a series of dilations, erosions, and hole-filling onto the masks.
    - Dilation by 1 px
    - Dilation by 1 px
    - Fill holes
    - Erosion by 1 px
    - Erosion by 1 px
    - Dilation by 1 px
    - Dilation by 1 px
    - Fill holes
    - Erosion by 1 px
    - Erosion by 1 px
6. Now objects retrieved from the "DAPI channel" (i.e., nuclei) are removed from the list of objects further analyzed if they do not overlap with the channels "Channel 1" and "Channel 2" by the percentages selected by the user in the settings dialog:

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/204475756-fe7eb5f6-995e-4a2a-8d15-825ebad72cd9.png" height=44>
</p>

##### Quantification
NuclQ determines the intensity profiles that are output later:
- For each pixel in the image the distance to all outline points of all objects is computed, the closest outline point will be assigned the pixel.
- The pixel is however ignored and left unassigned, if the closest outline point is not within the acceptable range specified by the user: <img src="https://user-images.githubusercontent.com/27991883/204480950-f8155028-1b05-43cd-ae36-e1653a357d07.png" height=20>

- The pixel intensity is added to the individual intensity profile of the assigned outline point.
- For each object the intensity profiles of the surrounding points are analyzed.
- Depending on what the user has selected at <img src="https://user-images.githubusercontent.com/27991883/204479304-1b5acd69-fe26-4042-986f-af7ed0194f1e.png" height=22> the fraction of continuous outline points with the highest sum of intensity profile values is selected as the fraction of interest. E.g., if the user selected "4" the quarter of the outline points with the highest intensity sum is selected for further quantification.

In a final step, for each object, all intensity profiles from all surrounding points are collected and merged into the intensity profiles that are output later.

### Output
NuclQ outputs a lot of different files that allow you to inspect the success of your analysis:

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/204502656-e9b1fe97-e65d-4e02-8646-d8d700e86c3d.png" width=200
</p>
 
The naming of the file is based on the input file name (e.g., "ExampleFile.tif" above) and adding the label "_NuQ" for a NuclQ analysis, the number specified for fractioning at <img src="https://user-images.githubusercontent.com/27991883/204479304-1b5acd69-fe26-4042-986f-af7ed0194f1e.png" height=22> (e.g., "_4" above) and an outputfile specific ending. Some tif files (see e.g., ```..._mp.tif``` below) also contain the object IDs as overlays allowing you to find a certain object in the numeric output data.
 
Here follows a description of each file with a screenshot of an example file - for clarity, we also provide the original image and the ROI drawn by the user on the left of some output files by their specific endings.

#### Output maps
 
- ```..._bg.tif```: This file shows you which objects were used for calculating the threshold for "Channel 1" and "Channel 2"

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/204503915-2aaffa4a-7f51-4512-bbcc-d7daab61a861.png" width=400
</p>
 
- ```..._mp.tif```: This image provides a distance map to show as intensity how far a pixel was to an object. The mp.tif file can be best displayed by changing the LUT to "phase" in FIJI (Image > Look-Up-Table > Phase) - see bottom screenshot.
<p align="center">
 <img src="https://user-images.githubusercontent.com/27991883/204504477-65f568ee-a295-4861-b346-ea9b768778ae.png" width=600>
 <br>
 <img src="https://user-images.githubusercontent.com/27991883/204505093-a92c3a8d-5bd2-47df-860b-2d87f52277bb.png" width=600>
</p>

- ```..._msk.tif```: This image shows the detected objects from the DAPI channel (as blue outlines), the segmeneted Channel 1 in red (2nd channel in the file), and the segmeneted Channel 2 in green (3rd channel in the file). Note that the colors here are not corresponding to the colors of the input image! The same file is also available as a rendered ```..._msk.png``` file for direct display outside FIJI.

<p align="center">
 <img src="https://user-images.githubusercontent.com/27991883/204505795-270c8f1d-5f5f-478e-bd2c-f99517474ea0.png" width=600>
</p>

- ```ol.tif```: This image outlines the detected objects after filtering and provides also the IDs of the objects, allowing to find the results for a specific object in the image also in the output text files and plot. The same file is also available as a rendered ```..._ol.png``` file for direct display outside FIJI.

<p align="center">
 <img src="https://user-images.githubusercontent.com/27991883/204522951-4033723f-56f5-47ce-a127-4078efabd88b.png" width=600>
</p>

#### Intensity profile plots
The intensity profile plots are output for the different channels "Channel 1" (name contains ```..._C1_...```), "Channel 2" (name contains ```..._C2_...```), and "Channel of interest" (name contains ```..._COI_...```). For each channel there is the following ```.png``` files output:
- ```..._AVG.png```
- ```..._SD.png```
- ```..._QAVG.png```
- ```..._QSD.png```

<p align="center">
 <img src="https://user-images.githubusercontent.com/27991883/204526157-c7fc5270-7048-4f98-b857-2a0e2ea32078.png" width=200>
</p>


```..._AVG.png``` and ```..._SD.png``` show, for each individual object (labeled as "N1", "N2", ... in the plot), the mean and standard deviation, respectively, of the pixel intensities around the object, as a function of the distance to the object border (called "outline points" in the quantification section above). The outline object border is defined as the distance value of 0. Negative distance values refer to pixels inside the object, positive distance values refer to pixels outside the object.

<p align="center">
 <img src="https://user-images.githubusercontent.com/27991883/204525180-74db5849-9899-453c-8782-afb872abfb90.png" width=300>
 <img src="https://user-images.githubusercontent.com/27991883/204525209-0126dc02-643c-4e03-bc9c-0b9238a50c29.png" width=300>
</p>

```..._QAVG.png``` and ```..._QSD.png``` are computed as the ```..._AVG.png``` and ```..._SD.png``` profiles but including only the continuous stretch of outline points that yielded the largest sum of pixel intensities in the profile (the length of this stretch is defined by the parameter <img src="https://user-images.githubusercontent.com/27991883/204479304-1b5acd69-fe26-4042-986f-af7ed0194f1e.png" height=22>, see quantification section for more information).

<p align="center">
 <img src="https://user-images.githubusercontent.com/27991883/204525205-0e43b028-2a03-4938-9be0-93e6812fcd68.png" width=300>
 <img src="https://user-images.githubusercontent.com/27991883/204525185-cb631b52-2c23-4125-99a4-9666cfe207b3.png" width=300>
</p>

The raw values for each plot are provided in the text file (see below).

#### Text file with raw data
- ```...m.txt```: This is a tab-delimited text file that allows you to retrieve the settings applied in the analysis and the numeric values for all intensity profiles. For better seeing the table structure you may copy the contents into a table software (e.g., Excel or similar). The file starts with a ```Settings:```-Block in which the different settings selected by the user are logged. Later the exact numeric data for each Intensity profile plot follow, in the ```Results:```-Block. Each object is denoted as a line, the distance coordinates are denoted in columns. For each object, also the overlap with the analyzed channel is given for "Channel 1" and "Channel 2".

<p align="center">
 <img src="https://user-images.githubusercontent.com/27991883/204526996-ab458b1c-dec7-4ea3-bbcc-6195b416b144.png" width=400>
</p>

<p align="center">
 <img src="https://user-images.githubusercontent.com/27991883/204527352-7017e6e4-b458-407f-a025-16cda49e74e9.png" width=1000>
</p>

#### ROI file
- ```..._.roi```: This file stores the ROI that the user selected for the image. Open the image and drag and drop this .roi file into FIJI to display the ROI again in the image.

### Special notes for special use cases
#### Extra use case 1: Use binarized input image
You may also use an image in which the object channel is already segmented (= "binarize") - in this case, unselect blurring and select the "Default" auto-threshold algorithm in the settings dialog:

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/204472566-f6d0d7ed-dc02-4a18-9800-f06fc5f15c21.png" width=300>
   <br>
   <img src="https://user-images.githubusercontent.com/27991883/204472676-3c0d0841-3547-4331-906d-885036161142.png" width=200>
</p>

#### Extra use case 2: No filtering
You can also run this software without filtering the object channel - to do this set the following settings to the described values:

- Set the identifiers for "Channel 1" and "Channel 2" to the same number as what you select as the object channel ("DAPI channel"). E.g., if your object channel is channel 1, set:

<p align="center">
 <img src="https://user-images.githubusercontent.com/27991883/204528634-f5938f26-5f2b-4bef-9672-58f7c3c2459a.png" height=60>
</p>

- Set the fold SD threshold to 0.0:

<p align="center">
 <img src="https://user-images.githubusercontent.com/27991883/204528913-0261b2ad-2e8d-4a11-80d3-f0a2d662dd91.png" height=22>
</p>

- Set the minimum overlaps to 0:

<p align="center">
 <img src="https://user-images.githubusercontent.com/27991883/204529176-13cf1eb6-0647-4a9c-8dcf-1fdec257e7ad.png" height=45>
</p>

#### Extra use case 2: No "divisioning"
You can also run this software without revealing results for the highest-intensity region, which will speed up the analysis. In this case set the "Divisioning" to 1:

<p align="center">
 <img src="https://user-images.githubusercontent.com/27991883/204529475-f91088b7-4127-40e4-8277-8bae5ec9d81b.png" height=22>
</p>



