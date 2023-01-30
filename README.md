# rip-maven-plugin
Plugin to ease developing and documenting REST services

## Table of Contents
[Releases](#releases)
[Overview](#overview)
[Related projects](#related-projects)

## Releases
Not yet released to maven repo since this is currently a POC.

## Overview
The plugin provide the following maven goals:
* generateRest (bound to precompile phase as opposed to all the other goals that run after compilation)
* diff
* generateDoc
* generateUi
* generateClient
* rip: convience goal - calls diff, generateDoc, generateUi and generateClient

## Related projects
The rip-maven-plugin is a "meta-plugin", so to speak. It calls a lot of other plugins.


## TODO
Create a setup that deploys to github packages. Test that 'mvn clean deploy'
also deploys the generated clients. See https://stackoverflow.com/questions/59093341/how-do-you-upload-a-maven-artifact-to-github-packages-using-the-command-line

Test that one can use the rip-maven-plugin without the generateDoc and generateUi 
plugin. That is, make the generateClient step be able to 
A) use the swagger.json created from the generateDoc
B) use the swagger.json at some other path created by a plugin unrelated to rip
-maven-plugin
C) download the swagger.json from some specified URL.

Test if a pure rip-maven-plugin setup works with spring-security etc.

Change generateRestDoc - restAnnotationType should only relate to the annotations
The config of download of json vs generation of json should be a separate param.

breaking api changes check should be extracted to a mojo called verifyVersion
