brainflex
=========

Communicate with MindFlex toy EEG via BrainLink

This works best with the custom BrainLink firmware:
https://github.com/arpruss/custom-brainlink-firmware

javac -cp pi-rgbled-1.2-SNAPSHOT.jar:pi4j-core.jar:jssc.jar:commons-math3-3.3.jar -d bin -sourcepath src src/mobi/omegacentauri/brainflex/BrainFlex.java
java -cp bin:pi-rgbled-1.2-SNAPSHOT.jar:pi4j-core.jar:jssc.jar:commons-math3-3.3.jar mobi.omegacentauri.brainflex.BrainFlex
