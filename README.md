brainflex
=========

Communicate with MindFlex toy EEG via BrainLink and use MindFLex data to control 5050 RGB dimmable LED strip, for Raspberry Pi.

This works best with the custom BrainLink firmware:
https://github.com/arpruss/custom-brainlink-firmware

javac -cp ./'*' -d bin -sourcepath src src/mobi/omegacentauri/brainflex/BrainFlex.java
java -cp bin:./'*' mobi.omegacentauri.brainflex.BrainFlex
=======


### Compiling and Running

1) Compile and run in terminal
> javac -cp pi-rgbled-1.2-SNAPSHOT.jar:pi4j-core.jar:jssc.jar:commons-math3-3.3.jar -d bin -sourcepath src src/mobi/omegacentauri/brainflex/BrainFlex.java
>java -cp bin:pi-rgbled-1.2-SNAPSHOT.jar:pi4j-core.jar:jssc.jar:commons-math3-3.3.jar mobi.omegacentauri.brainflex.BrainFlex

2) Run [pigpiod](http://abyz.co.uk/rpi/pigpio/pigpiod.html) in terminal:
> sudo pigpiod
// use "sudo pigpiog -p 8000" if it says the default port 8080 is busy

3) Turn on MindFlex and connect via bluetooth.

4) Set the baud rate to the rate you binded with the bluetooth deivce, press 'go' in Brainflex window.


### Contributors
This was apart of [Valley Hackthon](http://valleyhackathon.com/events/ValleyHack2017) team.

Samantha Reigelsberger<br />
Danielle Teatmeyer<br />
[Kevin He](https://github.com/hekevintran/brainflex)<br />
[Michael T. Andemeskel](https://github.com/mandemeskel)<br />

