# NuclQ
 NuclQ - an ImageJ plugin to quantify intensity levels by distance to the nuclear membrane.

## Copyright notice

Copyright (C) 2020-2022: Jan N. Hansen

## Contact

jan.hansen (at) uni-bonn.de

## Installing NuclQ
NuclQ is an ImageJ plugin that can be run in ImageJ or the extended ImageJ version FIJI. Thus to use MotiQ you need to first have an ImageJ or FIJI software downloaded to your computer. ImageJ/FIJI are open-source, freely available softwares that can be downloaded [here](https://imagej.net/downloads).

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

### (Optionally) select images
If the option <img src="https://user-images.githubusercontent.com/27991883/204499384-cf646cc9-273d-4bf5-9b37-0b491494a8af.png" height=22> was selected, NuclQ allows you to compile a list of files to be analyzed. After generating the list, start the processing.

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/204499657-0d5d613a-942f-4f0a-92a6-215cbcd01f61.png" width=400>
</p>


### Initial selection of negative cells by the user
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

### Automatic Processing and Quantification
#### Preprocessing
NuclQ uses the channel specified as the "DAPI channel" to determine and detect objects as follows:
1. If selected by the user, NuclQ blurs the channel with a Gaussian blur based on the user-defined sigma.
2. NuclQ segments the channel into for- and background by using the selected auto-threshold algorithm.
3. NuclQ applies a Watershed algorithm to split joint objects

#### Detection & Filtering
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

#### Quantification
NuclQ determines the intensity profiles that are output later:
- For each pixel in the image the distance to all outline points of all objects is computed, the closest outline point will be assigned the pixel.
- The pixel is however ignored and left unassigned, if the closest outline point is not within the acceptable range specified by the user: <img src="https://user-images.githubusercontent.com/27991883/204480950-f8155028-1b05-43cd-ae36-e1653a357d07.png" height=20>

- The pixel intensity is added to the individual intensity profile of the assigned outline point.
- For each object the intensity profiles of the surrounding points are analyzed.
- Depending on what the user has selected at <img src="https://user-images.githubusercontent.com/27991883/204479304-1b5acd69-fe26-4042-986f-af7ed0194f1e.png" height=22> the fraction of continuous outline points with the highest sum of intensity profile values is selected as the fraction of interest. E.g., if the user selected "4" the quarter of the outline points with the highest intensity sum is selected for further quantification.

In a final step, for each object, all intensity profiles from all surrounding points are collected and merged into the intensity profiles that are output later.

### Output
NuclQ

### Special use cases



