package hangman;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class EvilHangman {

    public static void main(String[] args) throws IOException, EmptyDictionaryException  {

        //Dictionary path to textfile WordLength >=2 guesses >=1

            String dictionaryFileName = args[0];
            String wordLengthString = args[1];
            String numOfGuessesString = args[2];
            // convert strings in text field to int
            int wordLength = Integer.parseInt(wordLengthString);
            int numOfGuesses = Integer.parseInt(numOfGuessesString);
            // convert filename to File Object
            File srcFile = new File(dictionaryFileName);

            EvilHangmanGame game = new EvilHangmanGame();

            game.startGame(srcFile,wordLength);

            gameLoop(numOfGuesses, wordLength, game); //is this legal?


    }

    public static void gameLoop(int numOfGuesses, int wordLength, EvilHangmanGame game) {
        char INPUT_ERROR = '0';
        Set<String> returnedStringSet = new HashSet<>();
        ArrayList<Integer> indexArray = new ArrayList<Integer>();
        String currentUserProgress = "";
        String answer = null;
        Boolean win = false;

        //initialize currentUserProgress
        for (int i = 0; i < wordLength; i++) {
            currentUserProgress = currentUserProgress + "-";
        }
        
        while (numOfGuesses != 0) {
            char guess = '0';
            boolean guessFound = false;
            int foundAtIndex = 0;
            int numberOfGuessInWord = 0;
            indexArray = new ArrayList<Integer>(); // reset indexArray

            System.out.println("You have " + numOfGuesses + " guesses left");
            SortedSet<Character> inputHistory = game.getGuessedLetters();
            //Print input history
            System.out.print("Used letters: ");
            Iterator<Character> it = inputHistory.iterator();
            while (it.hasNext()) {
                System.out.print(it.next() + " ");
            }
            System.out.println();
            System.out.println("Word: " + currentUserProgress);


            do {
                try {
                    guess = getGuess(inputHistory);
                } catch (IOException e) {
                    System.out.println("Invalid input");
                }
            } while (guess == INPUT_ERROR);

            // Valid input, make guess
            try {
                returnedStringSet = game.makeGuess(guess);

                //generate a sample word
                Iterator<String> itr = returnedStringSet.iterator();
                answer = itr.next();

                //use word to determine indices with the guess
                for (int i = 0; i < answer.length(); i++) {
                    if (answer.charAt(i) == guess) {
                        foundAtIndex = answer.charAt(i);
                        indexArray.add(i);
                    }
                }
                if (!indexArray.isEmpty()) {
                    guessFound = true;
                }
                numberOfGuessInWord = indexArray.size();

                if (guessFound == false) {
                    numOfGuesses--;
                    System.out.println("Sorry, there are no " + guess + "'s");
                }
                else {

                    if (guessFound) {
                        // Replace HYPHEN with GUESS with ARRAY of INDICES
                        StringBuilder sb = new StringBuilder(currentUserProgress);

                        for (int i = 0; i < indexArray.size(); i++) {
                            sb.setCharAt(indexArray.get(i), guess);
                        }
                        currentUserProgress = sb.toString();

                        System.out.println("Yes, there is " + numberOfGuessInWord + " " + guess);

                        win = true;
                        for (int i = 0; i < wordLength; i++) {
                            if (currentUserProgress.charAt(i) == '-') {
                                win = false;
                            }
                        }
                        if (win) {
                            break;
                        }
                    }
                }
            } catch (GuessAlreadyMadeException e) {
                System.out.println("You've already made that guess.");
            }
        }
        if (win) {
            System.out.println("You win!");
        }
        else {
            System.out.println("You lose!");
        }

        System.out.println("The word was: " + answer);
    }

    public static char getGuess(SortedSet<Character> inputHistory) throws IOException {
        char guess;
        int PROPER_INPUT_LENGTH = 1;
        int FIRST_CHAR = 0;
        char INPUT_ERROR = '0';

        // Get guess
        System.out.println("Enter a guess: ");
        //Create scanner object to read in system.input and save as string
        Scanner scanGuess = new Scanner(System.in);
        String guessString = scanGuess.nextLine();
        // Make sure the guess is only one character long
        if (guessString.length() == PROPER_INPUT_LENGTH) {

            //Convert to char and make lowercase
            guess = guessString.charAt(FIRST_CHAR);
            if (Character.isLetter(guess)) {
                guess = Character.toLowerCase(guess);
                //Check if in inputHistory
                Iterator<Character> it = inputHistory.iterator();
                while (it.hasNext()) {
                    if (guess == it.next()) {
                        System.out.println("You already used that letter");
                        return INPUT_ERROR;
                    }
                }
                return guess;
            }
            else {
                System.out.println("Invalid input");
                return INPUT_ERROR;
            }

        }
        else throw new IOException();
    }
}

