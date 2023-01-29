# rip-maven-plugin

# TODO
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
