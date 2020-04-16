package com.company;

/*
CSC 436 - Program Assignment #1
Made by Jack Botelho and *INSERT GROUP MEMBERS HERE*
In April 2020
 */

//File reading packages
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
//Importing arraylist for variable sizes
import java.sql.Array;
import java.util.ArrayList;

public class Main {

    //The args parameter will hold two files, one with the list of relations (args[0])
    //The other with the list of dependencies(args[1])
    public static void main(String[] args) throws IOException {
        //Creates an instance of the buffered reader to read from a file input
        BufferedReader reader = null;
        //Creates an instance of a BufferedReader for the second document
        BufferedReader readTwo = null;
        if (args.length != 2) {
            System.out.println("Error, only accepts two files!");
            return;
        }
        //Now to get an array of the attributes
        //Makes sure the file can open correctly
        try {
            reader = new BufferedReader(new FileReader(args[0]));//For the first file
            readTwo = new BufferedReader(new FileReader(args[1]));//For the second file
        }
        //Catches file errors
        catch (IOException e) {
            e.printStackTrace();
            return;//Ends the program if there is a file error
        }
        //Make an arraylist to hold the attributes
        ArrayList attr = new ArrayList<String>();
        //Reads the first line of the attribute document
        String line = reader.readLine();
        //Loops through the document while there is still stuff to read
        while (line != null) {
            attr.add(line);
            line = reader.readLine();
        }

        //Make an arrayList to hold the dependencies
        ArrayList dep = new ArrayList<String>();
        line = readTwo.readLine();
        while (line != null) {
            dep.add(line);
            line = readTwo.readLine();
        }

        //Turning the dependencies ArrayList into a 2D array with column 1 being the left side
        //and column 2 being the right side of the dependency
        String[][] depend = new String[dep.size()][2];
        //Populating the 2D array
        for (int i = 0; i < dep.size(); i++) {
            //Puts the dependency into a temp string
            String s = (String) dep.get(i);
            //This loop will divide the string up into the left and right sides of the dependency
            String left = "";//Holds the left side
            String right = "";//Holds the right side
            Boolean onLeft = true; //True if we are on the left side of the dependency
            for (int j = 0; j < s.length(); j++) {
                char q = s.charAt(j);
                if (q != '>') {
                    if (onLeft) {
                        left += q;
                    } else {
                        right += q;
                    }
                } else {
                    onLeft = false;
                }
            }
            //Trim any white space
            left = left.trim();
            right = right.trim();

            //Put the strings into depend
            depend[i][0] = left;
            depend[i][1] = right;
        }

        //Spliting the attributes into three categories
        //Left side: attributes that only appear on the left side of the dependencies
        //Middle: attributes that are on both the left and right side
        //Right: Attributes that only appear on the right side
        String[] split = new String[3];
        split[0] = "";//Left
        split[1] = "";//Middle (both)
        split[2] = "";//Right
        //This loop will do through the entire dependency array and populate the split array
        //Note: this will only populate the left and right halves, the middle will the next
        for(int i = 0; i < depend.length; i++){
            //This loop will check the left side of the dep array
            for(int j = 0; j < depend[i][0].length(); j++){
                char ch = depend[i][0].charAt(j);
                //If the character doesnt appear in the left or right arrays
                if(!nuContain(split[0], ch)) {
                    split[0] += ch;
                }
            }
            //Now it will check the right hand part of the depend array
            for(int j = 0; j < depend[i][1].length(); j++){
                char ch = depend[i][1].charAt(j);
                //If the character doesnt appear in the left or right arrays
                if(!nuContain(split[2], ch)) {
                    split[2] += ch;
                }
            }
        }

        //Now to fill the middle portion with character in both the left and right strings
        String left = ""; //This will hold the left attributes that are not in the right side
        for(int i = 0; i < split[0].length(); i++){
            char ch = split[0].charAt(i);
            //IF that char is in the right side string
            if(nuContain(split[2], ch)){
                split[1] += ch; //Add char to the middle string
                //Remove char from right string
                int ri = split[2].indexOf(ch);
                if(ri < split[2].length() - 1) {
                    String nuRight = split[2].substring(0, ri) + split[2].substring(ri + 1);
                    split[2] = nuRight;
                }
                else{
                    String nuRight = split[2].substring(0, ri);
                    split[2] = nuRight;
                }
            }
            else{
                left += ch;//If it isn't on the right side add it to the new left side
            }
        }
        split[0] = left;//Change the split to the new left side

        //Now we need to find the cloture of attributes from the left string
        //First we find the cloture from just the left side
        int alen = attr.size();//Number of attributes
        ArrayList cand = new ArrayList<String>();
        //We first check the attributes that only appear on the left if left is not blank
        if(split[0] != "") {
            String test = split[0];//Get a string with the attributes tested
            String output = closure(test, depend, alen); //The output to see if the one attribute is a candidate key
            if (output.length() == alen) {//if it is
                cand.add(test);
            }
            //If the left side does not give us a candidate key we try adding attributes from the middle
            else{
                for(int i = 0; i < split[1].length(); i++) {
                    String nuTest = test + split[1].charAt(i);
                    output = closure(nuTest, depend, alen);
                    if (output.length() == alen) {//if it is
                        cand.add(nuTest);
                    }
                }
            }
        }
        //Going down the list of dependencies (left side)
        for(int i = 0; i < depend.length; i++){
            String test = depend[i][0];
            String output = closure(test, depend, alen);
            if (output.length() == alen) {//if it is
                cand.add(test);
            }
            else{//try adding an attribute to it
                for(int j = 0; j < alen; j++){//go down the list of attributes
                    String c = (String) attr.get(j);
                    //Making sure the attribute we are adding is not already either on the right side
                    //of the dependency, the same letter or already in the array list
                    if(c.compareTo(test) != 0 && c.compareTo(depend[i][1]) != 0 && !cand.contains((String) c)){
                        String nuTest = test + c; //making a new test
                        nuTest = removeDup(nuTest); //removing duplicates
                        output = closure(nuTest, depend, alen); //output of colsure
                        if (output.length() == alen) {//if it is a key
                            cand.add(nuTest);//add to master key list
                        }
                    }
                }
            }
        }

        //NOTE: Need to find a way to trim keys that can be trimmed. E.g. BE when E is already in the list
        //Trimming keys that can be trimmed
        for(int i = 0; i < cand.size(); i++){
            String c = (String) cand.get(i);
            //Subloop to check with other elements
            for(int j = (i + 1) % cand.size(); j != i; j = (j + 1) % cand.size()){
                String test = (String) cand.get(j);
                char[] ch = test.toCharArray();
                //Looping through the char array
                boolean isIn = true; //True if the second sting is inside the first string
                for(int k = 0; k < ch.length; k++){
                    //If the string contains those chars
                    if(!nuContain(c, ch[k])){
                        isIn = false;
                    }
                }
                if(isIn){
                    cand.remove(i);//remove the redundent string
                    break;//end the loop
                }
            }

        }

        if(cand.size() > 0){
            System.out.println("Candidate keys are: ");
            for(int i = 0; i <  cand.size(); i++){
                System.out.println(cand.get(i));
            }
        }
        else{
            System.out.println("No candidate keys found!");
        }
    }

    //This method returns true if that char exists in the string, false otherwise
    public static boolean nuContain(String s, char t) {
        for (int i = 0; i < s.length(); i++){
           if(s.charAt(i) == t){
               return true;
           }
        }
        return false;
    }

    //Removes duplicate letters from the string
    public static String removeDup(String s){
        String result = "";
        char[] let = s.toCharArray();

        for(int i = 0; i < let.length; i++){
            if(!nuContain(result, let[i])){
                result += let[i];
            }
        }
        return result;
    }

        //This method calcualtes the closure of a string, based on the dependencies and the maximum number of attributes
    public static String closure(String st, String[][] dep, int max){
        String result = st; //intial attributes given

        boolean isChange = true;//True if the string was changed at all during the loop
        while(isChange){
            int strlen = result.length();//length of the string at the start
            for(int i = 0; i < dep.length; i++){
                char[] atr = dep[i][0].toCharArray();//turn the left side of the dep. into a char array
                boolean isIn = true;
                for(int j = 0; j < atr.length; j++){
                    if (!nuContain(result, atr[j])){//is that character already in the result string
                        isIn = false;
                    }
                }
                if(isIn && result.length() < max){//if it is in and we are not at the maximum amount of attributes  (might need to make < into <=)
                    result += dep[i][1];//Add the attributes into result
                    result = removeDup(result);//remove duplicates
                }
            }
            if(result.length() == strlen){//if there was no change to the string during the loop
                isChange = false;
            }
        }
        return result;
    }
}