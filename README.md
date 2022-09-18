# PCBAnalyser
PCB Analyser using Disjoint Sets.

The app can modify an images pixels to change the tint of the image, aswell as make it grayscale.

It splits all the pixels into individual disjoint sets and uses a quick union-find algorithm to union disjoint sets within a specified colour group.

It can analyse components of a PCB by color of components, and label them automatically with preset values.

It can also look for individual components by clicking on one and matches with other components in the image. 
