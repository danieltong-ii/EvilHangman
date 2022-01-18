package hangman;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EvilHangmanGame implements IEvilHangmanGame{
    Set<String> initialDictionarySet = new HashSet<>();
    Set<String> dictionarySubset = new HashSet<>();
    SortedSet<Character> inputHistory = new TreeSet<>();
    Map<String, Set<String>> partition = new HashMap<>();

    @Override
    public void startGame(File dictionary, int wordLength) throws IOException, EmptyDictionaryException {
        initialDictionarySet = new HashSet<>();
        dictionarySubset = new HashSet<>();
        inputHistory = new TreeSet<>();

        if (dictionary.length() == 0) {
            throw new EmptyDictionaryException();
        }
        else {
            Scanner scanner = new Scanner(dictionary);
            while (scanner.hasNext()) {
                initialDictionarySet.add(scanner.next());
            }
        }
        if (wordLength < 2) {
            throw new EmptyDictionaryException();
        }
        if (!(isValidWordlength(wordLength))) {
            throw new EmptyDictionaryException();
        }

        //Create subset with user selected wordlength
        createSubset(wordLength);
    }

    public boolean isValidWordlength(int wordLength) {
        boolean valid = false;
        for (String word : initialDictionarySet) {

            if (word.length() >= wordLength) {
                valid = true;
            }
        }
        return valid;
    }
    // Just with proper wordlength
    public void createSubset(int wordLength) {
        for (String word : initialDictionarySet) {
            if (word.length() == wordLength) {
                dictionarySubset.add(word);
            }
        }
    }

    public void createAllSubsets(char guess){
        for (String word : dictionarySubset) {
            String currentSubsetKey = getSubsetKey(word, guess);
            Set<String>existingKeySets = partition.keySet();

            if ((existingKeySets.contains(currentSubsetKey)) == false) {
                Set<String> newSubsetKeyNewSet = new HashSet<>();
                newSubsetKeyNewSet.add(word);
                partition.put(currentSubsetKey,newSubsetKeyNewSet);
            }
            else {
                partition.get(currentSubsetKey).add(word);
            }
        }
    }


    @Override
    public Set<String> makeGuess(char guess) throws GuessAlreadyMadeException {
        if (inputHistory.contains(Character.toLowerCase(guess))) {
            throw new GuessAlreadyMadeException();
        }
        if (inputHistory.contains(Character.toUpperCase(guess))) {
            throw new GuessAlreadyMadeException();
        }
        if (inputHistory.contains(guess)) {
            throw new GuessAlreadyMadeException();
        }
        else {
            inputHistory.add(guess);
            partition = new HashMap<>();
        }
        //1. Create the subsets based on the guess and store in MAP PARTITION
        createAllSubsets(guess);
        //2. Determine the largest subset
        return determineLargestSubset(guess);
    }

    public Set<String> determineLargestSubset(char guess) {
        Map<String, Set<String>> subMap = new HashMap<>();
        Set<String> LargestSubset = new HashSet<>();
        int LARGEST_SET_SIZE = 0;

        for (Map.Entry<String, Set<String>> entry : partition.entrySet()) {
            if (entry.getValue().size() > LARGEST_SET_SIZE) {
                LARGEST_SET_SIZE = entry.getValue().size();
            }
        }
        for (Map.Entry<String, Set<String>> entry : partition.entrySet()) {
            if (entry.getValue().size() == LARGEST_SET_SIZE) {
                subMap.put(entry.getKey(),entry.getValue());
                LargestSubset = entry.getValue();
            }
        }
        // Decide whether multiple subsets are the same size
        if (subMap.size() == 1) {
            dictionarySubset = LargestSubset;
            return dictionarySubset;
        }
        else {
            return resolveSubsetPriority(subMap, guess); // SHOULD RETURN dictionarySubset
        }
    }


    public Set<String> resolveSubsetPriority(Map<String,Set<String>> subMap, char guess) {
        int ONLY_ONE = 1;

        //1. LETTER DOESNT APPEAR AT ALL
        for (Map.Entry<String, Set<String>> entry: subMap.entrySet()) {
            if(getSubsetKeyGuessFreq(entry.getKey()) == 0) {
                dictionarySubset = entry.getValue(); //Set the next dictionaryset equal to the partition with words that don't have guessed character
                return dictionarySubset;
            }
        }
        //2. FEWEST LETTERS;  //If Counter is over 1, that means multiple sets had the same number of guess char in their subset key [MULTIPLE LOWEST]
        subMap = getSubsetKeyWithFewestLetters(subMap, guess);
        if (( subMap.size() == ONLY_ONE )) {
            for (Map.Entry<String, Set<String>> entry: subMap.entrySet()) {
                dictionarySubset = entry.getValue();
                return dictionarySubset;
            }
        }
        // 3. RIGHTMOST

        do {
            subMap = getRightmostSubsetKey(subMap, guess);
            if ((subMap.size() == ONLY_ONE)) {
                for (Map.Entry<String, Set<String>> entry: subMap.entrySet()) {
                    dictionarySubset = entry.getValue();
                    return dictionarySubset;
                }
            }
        } while (subMap.size() > 1);

        return null;
    }


    public Map<String,Set<String>> getRightmostSubsetKey(Map<String,Set<String>> subMap, char guess) {
        int RIGHTMOST_SUM = 0;
        Map<String, Set<String>> tempSubMap = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : subMap.entrySet()) {
            int SUBSETKEY_SUM = 0;
            for (int i = 0; i < entry.getKey().length(); i++) {
                if (entry.getKey().charAt(i) == guess) {
                    SUBSETKEY_SUM = SUBSETKEY_SUM + i;
                }
            }
            if (SUBSETKEY_SUM > RIGHTMOST_SUM) {
                RIGHTMOST_SUM = SUBSETKEY_SUM;
            }
        }
        for (Map.Entry<String, Set<String>> entry : subMap.entrySet()) {
            int SUBSETKEY_SUM = 0;
            for (int i = 0; i < entry.getKey().length(); i++) {
                if (entry.getKey().charAt(i) == guess) {
                    SUBSETKEY_SUM = SUBSETKEY_SUM + i;
                }
            }
            if (SUBSETKEY_SUM == RIGHTMOST_SUM) {
                tempSubMap.put(entry.getKey(),entry.getValue());
            }
        }
        return tempSubMap;
    }

    public Map<String,Set<String>> getSubsetKeyWithFewestLetters(Map<String,Set<String>> subMap, char guess) {
        int FEWEST_LETTERS = 10;
        int COUNTER = 0;
        Map<String, Set<String>> tempSubMap = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry: subMap.entrySet()) {
            if (getSubsetKeyGuessFreq(entry.getKey()) < FEWEST_LETTERS) {
                FEWEST_LETTERS = getSubsetKeyGuessFreq(entry.getKey());
            }
        }
        for (Map.Entry<String, Set<String>> entry: subMap.entrySet()) {
            if (getSubsetKeyGuessFreq(entry.getKey()) == FEWEST_LETTERS) {
                tempSubMap.put(entry.getKey(), entry.getValue());
                COUNTER++;
            }
        }
        //overwrite subMap so that it has the partitions with the highest priority

        return tempSubMap;
    }

    public String getSubsetKey(String word, char guess) {
        String subsetKey = "";
        String HYPHEN = "-";

        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == guess) {
                subsetKey = subsetKey + guess;
            }
            else {
                subsetKey = subsetKey + HYPHEN;
            }
        }
        return subsetKey;
    }

    public int getSubsetKeyGuessFreq(String subsetKey) {
        int FREQUENCY = 0;
        for (int i = 0; i < subsetKey.length(); i++) {
            if (subsetKey.charAt(i) != '-') {
                FREQUENCY++;
            }
        }
        return FREQUENCY;
    }

    @Override
    public SortedSet<Character> getGuessedLetters() {
        return inputHistory;
    }
}
