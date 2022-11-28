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

### Output
NuclQ

### Settings
The following settings dialog pops up when launching the plugin - here is an explanation of individual settings:
![NuclQ settings](https://user-images.githubusercontent.com/27991883/204276811-651ac146-01f6-41fa-8e77-09b77a5b5d69.png)


### Example run


