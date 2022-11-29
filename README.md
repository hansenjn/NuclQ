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

### Settings
The following settings dialog pops up when launching the plugin - here is an explanation of individual settings, for more understanding how it affects the analysis workflow please see the section [Processing](https://github.com/hansenjn/NuclQ/edit/main/README.md#processing--analysis) below:

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/204276811-651ac146-01f6-41fa-8e77-09b77a5b5d69.png" width=500>
</p>

### Processing & Analysis
#### Initial selection of negative cells by the user
NuclQ first asks the user to draw a region of interest (ROI) in each image to be analyzed. This ROI needs to circumscribe / contain only objects that are "signal-negative" in the channels used for filtering. If you do not want to use the filtering for "signal-positive" objects you can just circumscibe any objects (nuclei) here - you however need to make sure that the ROI contains at least one object (or at least a part of it).

When all ROIs are set, all images are subjected to the analysis pipeline and all the following processing is fully-automated.

#### Automated processing / analysis
NuclQ uses the channel specified as the "DAPI channel" to determine and detect objects as follows:
1. If selected by the user, NuclQ blurs the channel with a Gaussian blur based on the user-defined sigma.
2. NuclQ segments the channel into for- and background by using the selected auto-threshold algorithm.
3. NuclQ applies a Watershed algorithm to split joint objects

NuclQ detects all the individual particles in the image as individual objects.

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

NuclQ determines the intensity histograms for output


### Output
NuclQ

### Example run


