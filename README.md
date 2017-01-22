brainflex
=========

Communicate with MindFlex toy EEG via BrainLink

This works best with the custom BrainLink firmware:
https://github.com/arpruss/custom-brainlink-firmware

javac -cp ./'*' -d bin -sourcepath src src/mobi/omegacentauri/brainflex/BrainFlex.java
java -cp bin:./'*' mobi.omegacentauri.brainflex.BrainFlex
