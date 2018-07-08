package com.zelther.emojit;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class ProjectUtiles {

    /** Gets an array of type int and returns the sum of all the numbers in it
     * @param array The int array which will be summed
     * @return The sum of the array*/
    public static int arraySum(int [] array) {
        int sum = 0;
        for(int i:array) {
            sum += i;
        }
        return sum;
    }

    /**Gets an int array and return the index in which if you cut the array in half, the two arrays will be almost equal
     * @param array The array to check
     * @return The index number*/
    public static int middleNumericArray(int[]array) {
        int currentMiddle = 0;
        int currentHighestNum = 100;
        for (int i = 0; i < array.length; i++) {
            int[] part1 = Arrays.copyOfRange(array, 0, i);
            int[] part2 = Arrays.copyOfRange(array, i, array.length);
            if (Math.max(ProjectUtiles.arraySum(part1), ProjectUtiles.arraySum(part2)) < currentHighestNum) {
                currentHighestNum = Math.max(ProjectUtiles.arraySum(part1), ProjectUtiles.arraySum(part2));
                currentMiddle = i;
            }
        }
        return currentMiddle;
    }

    /**
     * Gets an array and returns the first index in which there is null object.
     * @param array The array to check
     * @return the index. -1 if there isn't such.
     */
    public static int getFirstNullIndex(Object [] array) {
        for(int i=0; i<array.length; i++) {
            if(array[i]==null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets an array and an object. the returned will be the same array, with each elements equal to object set to null
     * @param array The array to change
     * @param t The object to check equality
     * @return The new array
     */
    public static <T> T[] removeToNull(T[]array, T t) {
        for(int i=0; i<array.length; i++) {
            if(array[i]==t) array[i] = null;
        }
        return array;
    }

    /**
     * Gets a list and checks if there are no null objects
     * @param array the array to check
     * @return true if there are no null object, false otherwise
     */
    public static boolean isListFull(Object[]array) {
        for(Object o: array) {
            if(o==null) return false;
        }
        return true;
    }

    /**
     * gets an array of type char and return it in array list format
     * @param array array of type char
     * @return array list of type character
     */
    public static ArrayList<Character> fromCharArrayToArrayList(char[] array) {
        ArrayList<Character> arrayList = new ArrayList<>();
        for(char c:array) {
            arrayList.add(new Character(c));
        }
        return arrayList;
    }

}
