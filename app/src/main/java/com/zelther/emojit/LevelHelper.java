package com.zelther.emojit;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LevelHelper {

    int level;
    BufferedReader reader;
    String emoji,word;

    public LevelHelper(int level, InputStream in) {
        this.level = level;
        reader = new BufferedReader(new InputStreamReader(in));
        readFromFile();
    }

    /**
     * this method reads from the app's levels file and updates the word and the emoji
     * @return true if there is more to read, false otherwise
     */
    private boolean readFromFile() {
        try {
            String line;
            while(true) {
                line = reader.readLine();
                if(line==null) {
                    return false;
                }
                if(line.startsWith(String.valueOf(level)))
                    break;
            }
            String [] levelDetail = line.split("⊿");
            emoji = levelDetail[1];
            word = levelDetail[2].toUpperCase();
        } catch (IOException e) {}
        return true;
    }

    public int getLevel() {
        return level;
    }

    /**
     * this method updates the level helper to the next level
     * @return true if there is more levels, false otherwise
     */
    public boolean nextLevel() {
        level++;
        return readFromFile();
    }

    public String getEmoji() {
        return emoji;
    }

    public String getWord() {
        return word;
    }

    public int getWordsNum() {
        String[] words = word.split(" ");
        return words.length;
    }

    public int[] getWordsLengths() {
        String [] words = word.split(" ");
        int [] lengths = new int[words.length];
        for(int i=0; i<words.length; i++) {
            lengths[i] = words[i].length();
        }
        return lengths;
    }

    public char[] getLetters() {
        return word.replaceAll(" ","").toCharArray();
    }
}
