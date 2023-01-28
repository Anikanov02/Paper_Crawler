Here is a basic structure of a project.
There are 3 separate modules:
1. core: module that contains all the services, dtos and api integrations
2. ui: a module that builds an interface of an application
3. web: additional module that can be used later if needed to give an opportunity to trigger services from core through http

I had to start with ui part as there is no entry point in core module, although, I found the best option among all 
different kinds of libraries for parsing pdf in java - iText, and made service that would filter all th links from pdf.
The filtrage itself turned out to be more complex than expected and Im still working on a way to filter all the links
and not miss something but also not to get something unnecessary in the output of a method. Ui part is almost complete as well as 
LinkExtractorService, so i hope i will be able to figure out how to filter all the links by Monday as it seems to be the most difficult
part of a task