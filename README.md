# rip-maven-plugin
Plugin to ease developing and documenting REST services

## Table of Contents
[Status](#status)  
[Releases](#releases)  
[Requirements](#requirements)  
[Overview](#overview)  
[Related projects](#related-projects)  

## Status
Proof-of-concept.  
The project is not yet production ready since testing is still ongoing.

## Releases
Not yet released to maven repo since this is currently a POC.

## Requirements
TODO
The project has been compiled and tested with jdk18 and spring boot 3. 
See the pom.xml of the various modules in the project.  

I don't yet have an overview of the compatibilty. One could fx consider if it was possible
to set the target java version to something lower than java 18.
Guide on the matter: https://www.baeldung.com/java-source-target-options

## Overview
The plugin provide the following maven goals:
* generateRest (bound to precompile phase as opposed to all the other goals that run after compilation)
* diff
* generateDoc
* generateUi
* generateClient
* rip: meta goal - calls diff, generateDoc, generateUi and generateClient

## Related projects
The rip-maven-plugin is a "meta-plugin", so to speak. It calls a lot of other plugins.
TODO - list the projects being used

## TODO
Create a setup that deploys to github packages. Test that 'mvn clean deploy'
also deploys the generated clients. See https://stackoverflow.com/questions/59093341/how-do-you-upload-a-maven-artifact-to-github-packages-using-the-command-line

Test if a pure rip-maven-plugin setup works with spring-security etc.

Change generateRestDoc - restAnnotationType should only relate to the annotations
The config of download of json vs generation of json should be a separate param.

Code cleanup - search for "TODO"

Make thread-safe and consider if concurrent execution of some of the goals is possible
