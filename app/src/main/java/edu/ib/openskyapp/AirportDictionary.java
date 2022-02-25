package edu.ib.openskyapp;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
* Klasa AirportDictionary służy do wczytywania pliku zawierającego kody icao lotnisk.
*/
public class AirportDictionary {

    private Dictionary<String, String> dictionary = new Hashtable<String, String>();
    private InputStream inputStream;

    public AirportDictionary(InputStream inputStream) {
        this.dictionary = dictionary;
        this.inputStream = inputStream;
    }

    /**
     * Returns słownik zawierający kody icao i lotniska.
     * @return dictionary
     * @throws RuntimeException
     */

    public Dictionary<String, String> fileToDictionary(){

        List resultList = new ArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(";");
                dictionary.put(row[0], row[1]);
                resultList.add(row[0]);
                resultList.add(row[1]);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Error in reading CSV file: " + e);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: " + e);
            }
        }

        for (int i = 0; i < resultList.size() - 1; i++){
            dictionary.put(resultList.get(i).toString(), resultList.get(i+1).toString());
        }

        return dictionary;

    }


    /**
     * Returns wartość odpowiadająca kluczowi w słowniku.
     * @param key
     * @return value
     */

    public String getValue(String key) {
        return dictionary.get(key);
    }


}