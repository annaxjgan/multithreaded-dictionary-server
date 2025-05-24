## A multithreaded dictionary server 

To run:
1. Git clone this repository
   
2. Start the server by running the DictionaryServer.jar file your local machine, providing a port size, work size and dict file
`java -jar DictionaryServer.jar 1000 3 src/resource/dict.json`

3. Run the client program on the same port
`java -jar DictionaryClient.jar localhost 1000`
