Paper Crawler:
Program that allows us to research a depth of references paper or a set of papers
there are multiple configuration options:
1. 3 input types: 
   - pdf(you select pdf file and program will parse it and extract references for further processing)
   - doi(you pass a doi of a paper you want to research)
   - doi file input(you pass a file that contain separate doi on each line and it will sequentially process them)
2. 2 search modes
   - fast(searching for specific keywords in title + abstract)
   - slow(searching in title + abstract + fulltext) its slow because we will need to download every single pdf during processing
3. key words field, here you can type any logical expression with symbols {}, !, &, |. 
   For instance: {Friedel&sem}|{Friedel&stm}|{Friedel&{111|100|001}}|{oscillation&sem}|{oscillation&stm}|{oscillation&{111|100|001}}
4. Pdf source(site from which pdf will get downloaded), i`ts an optional field and can be ignored, by default we search on all sci-hub mirrors(.se, .st, .ru, etc...)
5. you can adjust pdfs limit(general limit for all pdfs downloaded per single input(in doi sile input counts separately for each doi))
6. as well as output limit its the number of output entities you will see in output area
   (note, pdf limit will always be cut down to output limit, this means that pdf limit always <= output limit)
7. search depth:
   be careful with this one, as on average each paper has about 30 references, so, with each layer number of requests will multiply by 30(on average) 
   which may cause program to run for too long
8. Next and the last setting is dictionary file, you can apply it if you want to define some aliases for particular words that will be used for key word filtering
   you simply write a rule for single word in separate lines each, rule example: Pd(111)==Pd111, you can have more than 2 aliases in single line, but note that 
   program only considers aliases a words from the same line, so, another rule should be on another line
9. Delete failed pdfs button is now inactive because of redundance, previously it was used to delete all failed pdfs after processing, 
   but after adding multiple dois feature it became unusable and due to low priority of making it working it was abandoned for now

Progress field:
 Here you can see the state of programs work: Depth, Pdf downloading abd other stages of processing, each state is followed by number of current item processing, 
 total number of items on this stage and estimated time counter that will show you an approximate time of execution of single stage

Instruction on how to run app:
1. make sure you have java 17+ on your machine installed, you can check it by typing "java -version" in console
you will see current version of java(if you have one installed) on your computer
2. download PaperCrawler.7z archive and open it, run corresponding runner file (for windows runWindows.bat) and thats it