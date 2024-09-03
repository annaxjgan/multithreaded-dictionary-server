/**
 * Name: Anna Gan, Student ID: 1579818
 * The DictionaryHandler class manages a dictionary of words and their meanings, stored as a JSON file. 
 * It provides thread-safe methods to add, remove, update, and retrieve meanings, ensuring data persistence by loading from and saving to a JSON file.
 */

package server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DictionaryHandler{
	
    public static HashMap<String, List<String>> dict = new HashMap<>();
    public static String fileName;
    public static JSONParser parser = new JSONParser();

    /**
     * Constructor for DictionaryHandler.
     * @param fileName The name of the file from which to load the dictionary data.
     */
    public DictionaryHandler (String fileName) {
        this.fileName = fileName;
    	loadDataFromFile();
    }
    
    /**
     * Loads the dictionary data from the specified JSON file.
     * Data is read and parsed into the in-memory dictionary.
     */
    private void loadDataFromFile() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            // Parse the JSON file into a JSONObject
            JSONObject jsonObject = (JSONObject) parser.parse(bufferedReader);

            // Convert JSONObject to HashMap
            for (Object key : jsonObject.keySet()) {
                String keyStr = (String) key;
                Object value = jsonObject.get(keyStr);
                JSONArray jsonArray = (JSONArray) value;
                List<String> list = new ArrayList<>();

                for (Object obj : jsonArray) {
                    list.add(obj.toString().trim()); // Ensure that the elements are converted to strings with whitespaces trimmed
                }
                dict.put(keyStr, list);
            }

        } catch (FileNotFoundException e) {
            System.err.println("Resource/File not found: " + e.getMessage());
            System.exit(1);
		} catch (IOException | ParseException e) {
			System.err.println("Error: Unable to read content of file." + "\""+ fileName +"\"");
            System.exit(1);
		} 
    }
    
    /**
     * Saves the current dictionary data to the specified JSON file.
     * Converts the HashMap to a JSONObject and writes it to the file.
     */
    static void saveDataToFile() {
        JSONObject jsonObject = new JSONObject();
        
        for (Map.Entry<String, List<String>> entry : dict.entrySet()) {
            String key = entry.getKey();
            List<String> list = entry.getValue();
            
            JSONArray jsonArray = new JSONArray();
            for (String item : list) {
                jsonArray.add(item); 
            }
            
            jsonObject.put(key, jsonArray);
        }
        
        // Write the JSONObject to a file
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(jsonObject.toString()); 
        } catch (IOException e) {
           System.out.println("Unable to save changes to file");
        }
    }

    
    /**
     * Retrieves the meaning(s) of a given word from the dictionary.
     * @param command The command JSON object containing the word to look up.
     * @return A string containing the meaning(s) or an error message.
     */
    synchronized String getMeaning(JSONObject command) {
        String clientQuery = (String) command.get("word");
        if (clientQuery.isEmpty()) {
        	return  "ERROR: No word entered! Please enter a word to update meaning.";
        } 
        else {
            if (dict.get(clientQuery) != null) {
		        List<String> queryMeanings = dict.get(clientQuery);
            	StringBuilder result = new StringBuilder("Meaning(s):\n");
            	int i = 1;
            	for (Object meaning : queryMeanings) {
            		result.append(i).append(". ").append(meaning.toString().trim()).append("\n");
            		i++;   
            	}
            	return result.toString();
            }
            else {
            	return "ERROR: Word not found. The word "  + "\""+ clientQuery +"\"" + " does not exists/has been removed from the dictionary";
            }
        }
    }
    
    /**
     * Adds a new word and its meaning(s) to the dictionary.
     * @param command The command JSON object containing the word and meaning(s) to add.
     * @return A success or error message.
     */
    synchronized String addNewWord(JSONObject command) {
    	String newWord = (String) command.get("word");
		String meaningString = (String) command.get("meaning");

        if (newWord.isEmpty() || meaningString.isEmpty()) {
        	return "ERROR: Missing word or meaning input(s) ! Separate multiple meanings using commas for example 'meaning_1, meaning_2' ";
        } 
        else {
        	if (dict.get(newWord) == null) {
                String[] meanings = meaningString.split(",");
        		List<String> meaningList = new ArrayList<>();
        		for (Object element : meanings) {
        			if (element instanceof String) {
        				meaningList.add((String) element);
        			}
        		}
        		dict.put(newWord, meaningList);

        		return "SUCCESS: New word has been added. Query word to view meanings."; 
        		 
        	}
        	else {
            	return "ERROR: The word "  + "\""+ newWord +"\"" + " already exists in the dictionary";
        	}
        }
    	
    }

    /**
     * Removes a word from the dictionary.
     * @param command The command JSON object containing the word to remove.
     * @return A success or error message.
     */
    synchronized String removeWord(JSONObject command) {
        	String word = (String) command.get("word");
            
            if (word.isEmpty()) {
                return "ERROR: No word entered!  Please enter a word to remove.";
            } else {
                if (dict.get(word) != null) {
	                dict.remove(word);
	                return "SUCCESS: \"" + word + "\"" + " has been removed from the dictionary.";
                } else {
                    return "ERROR: Word not found. The word "  + "\""+ word +"\"" + " does not exists/has been removed from in the dictionary";
                }
            }
     
    }
    
    /**
     * Adds a new meaning to an existing word in the dictionary.
     * @param command The command JSON object containing the word and new meaning to add.
     * @return A success or error message.
     */
    synchronized String addNewMeaning(JSONObject command) {
            String existingWord = (String) command.get("word");
            String newMeaning = (String) command.get("newMeaning");
            
            if (existingWord.isEmpty()||newMeaning.isEmpty()) {
                return "ERROR: Missing word or meaning !";
            }

            else {
                if (dict.get(existingWord) != null) {
                	if (newMeaning.isEmpty()){
	                    return "ERROR: No new meaning(s) entered! Please add meaning(s) for the word " + "\""+ existingWord +"\"";
                	}
                	else {
                		//Iterate through the list of existing meaning to see if there is duplicate meaning
		                List<String> existingMeaning = dict.get(existingWord);
		                boolean matching = false;
		                for (String eachMeaning : existingMeaning) {
		                    if (eachMeaning.toLowerCase().equals(newMeaning.toLowerCase())) {
		                    	matching = true;
		                    }
		                }
		                if (!matching) {
		                	existingMeaning.add(newMeaning);                                
			                return "SUCCESS: New meaning has been added for the word " + "\"" + existingWord + "\"";

		                }
		                else {
			                return "ERROR: New meaning \"" + newMeaning + "\" already exists for word " + "\"" + existingWord + "\"";
		                }
		              
                	}
                } else {
                    return "ERROR: The word "  + "\""+ existingWord +"\"" + " does not exists/has been removed from the dictionary. Choose \"Add new word\" to add word to dictionary.";
                }
            }
    	

    }

    /**
     * Updates an existing meaning of a word in the dictionary.
     * @param command The command JSON object containing the word, existing meaning, and new meaning.
     * @return A success or error message.
     */
    String updateMeaning(JSONObject command) {
            String wordToUpdate = (String) command.get("word");
            String existingMeaning = (String) command.get("existingMeaning");
            String updateMeaning = (String) command.get("newMeaning");
            int existingMeaningIndex = 0;
            boolean existingMeaningMatch = false;
            boolean uniqueNewMeaning = true;
            
            if (wordToUpdate.isEmpty() || existingMeaning.isEmpty() || updateMeaning.isEmpty()) {
            	return  "ERROR: Missing word / existing meaning / new meaning input(s)!";
            }

            else if (dict.get(wordToUpdate) == null) {
            	return "ERROR: Word not found. The word "  + "\""+ wordToUpdate +"\"" + " does not exists/has been removed from the dictionary";
            }

            else {
                			
            	List<String> meaningList = dict.get(wordToUpdate);
            	
            	//Check if new meaning is duplicated in existing meaning
            	for (String meaning:meaningList) {
            		if (updateMeaning.toLowerCase().equals(meaning.toLowerCase())) {
            			uniqueNewMeaning = false;
            		}
            	}

            	// Iterate through the list and update the meaning if an existing meaning match is found
            	for (int i = 0; i < meaningList.size(); i++) {
            		if (existingMeaning.toLowerCase().trim().equals(meaningList.get(i).toLowerCase())) {
            			existingMeaningMatch = true;
            			existingMeaningIndex = i;
            			break; // Exit the loop once the update is done
            		}
            	}
            	if (!existingMeaningMatch){
            		return  "ERROR: Existing meaning \""+ existingMeaning + "\" not found for the word " + "\"" + wordToUpdate + "\"";

            	}
            	else if (!uniqueNewMeaning) {
            		return "ERROR: New meaning \"" + updateMeaning + "\" entered already exists for the word \"" + wordToUpdate + "\"";

            	}
            	else {
            		meaningList.set(existingMeaningIndex, updateMeaning);
            		return  "SUCCESS: Meaning has been updated for word " + "\""+ wordToUpdate+"\"";
            	}

		    }

    }
    
}