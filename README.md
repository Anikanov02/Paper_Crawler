I added some plugins to be able to run multimodule projects using a single jar.
Also i fixed issue with api service and now it correctly retrieves and parses response and prints it
to CONSOLE(!) not on ui, so make sure to also check console output while running application

Also. Program runs quite slow because of all the api calls, so it may take some time to process
even a single paper.
but then i added filtrage of response parameters and it did help a bit

I added to response parameter "reference" because i believe it is what we are looking for

And now im fully in creating sciHub API to feed it dois from crossref

Instruction on how to run app:
1. install maven(not necessary when using some IDEs like IntelliJ as they have built in integration)
2. install java
3. clone repo from github
4. cd to root of a project in console
5. execute command "mvn clean install" and wait for it to process. It will download all necessary dependencies and generate jar files, 
   you gonna need ui-1.0-SNAPSHOT.jar
6. then cd ui/target
7. and then java -jar ui-1.0-SNAPSHOT.jar
   you will see some console output and then ui part appears

You may also want to run it wia IDE (maybe)
in case of IntelliJ:
clone repo->right click pom.xml in a root->reload maven project to download all dependencies->go to ui module and run UIMain class

but there may be some problems with defining project structure(no code changes, just ide configuration) if IDE would not fetch it automatically