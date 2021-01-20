# skribbless
This repo contains the game skribbless based on [scribbl.io](https://skribbl.io/). The game in is current state is only made to be run locally.

## Installation
to use the application, we first need to make sure all needed things are downloaded and installed.
First of this program makes use of JSpace, we have used the zip file for this project however this can also be imported through maven.
Now this project should already be packaged in such a way that jspace should be running on its own.

This is a java project which was develop using java 11 however it has also been tested on java 13. So, either of these versions are fine.

We do however also use javaFX, now in order to use this an SDK must be downloaded, for the project we used 11.0.2, so this is recommended, and can be found [here](https://gluonhq.com/products/javafx/).

## Usage
Now to use the application the Server must first be launched: Note that the Server can take an argument in the form of userinfo:port if no argument is given it defaults to localhost:9001.

After this as many ClientApps as desired. However, to launch the ClientApp we must first set the VM-options. In IntelliJ this is found under:
Run -> Edit Configurations... -> Configuration -> VM-options
In this field we put the following:
```bash
--module-path <PATH_TO_JAVAFX_HERE> --add-modules javafx.controls,javafx.fxml 
```
Now in the path it is important that it leads to the lib folder in the SDK. Meaning the path could look something like this:
```bash
--module-path C:/opt/javafx/javafx-sdk-11.0.2/lib --add-modules javafx.controls,javafx.fxml 
```
Remember if the path contains spaces use ${PATH_TO_JAVAFX_HERE} instead.

## Authors
This project is made by Malthe Ørberg Pedersen and Thor Eric Dueholm.
