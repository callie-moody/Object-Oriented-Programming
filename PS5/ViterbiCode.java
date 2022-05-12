import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import static java.util.Collections.max;

public class ViterbiCode {
    //Current state to next state to transition value
    Map<String, Map<String, Double>> transitions = new HashMap<String, Map<String, Double>>();
    //observations to states to contained scores
    Map<String, Map<String, Double>> observations = new HashMap<String, Map<String, Double>>();
    //list in order of rounds- Current state to previous state to score
    List<Map<String, Map<String, Double>>> bestpaths = new ArrayList<Map<String, Map<String, Double>>>();
    Double notseen = -100.0; //unseen penalty


    public ViterbiCode(){}

    //creates the transition and observation tables
    public void viterbitrainer() {
        List<String[]> tags = new ArrayList<String[]>();       //contains the tags in a training file
        List<String[]> sentences = new ArrayList<String[]>();   //contains the words in a training file

        try {//reads in training files
            BufferedReader states = new BufferedReader(new FileReader("inputs/brown-train-tags.txt"));
            BufferedReader observations = new BufferedReader(new FileReader("inputs/brown-train-sentences.txt"));
            String line;

            //store tag by tag in the tags array
            while ((line = states.readLine()) != null) {
                tags.add(line.split(" "));
            }
            //store word by word in the sentences array
            while ((line = observations.readLine()) != null) {
                line = line.toLowerCase();
                sentences.add(line.split(" "));
            }

        } catch (Exception e) {
            System.err.println("Could not load file");
        }


        //sets transition probabilities
        //for each sentence in the tags array
        for (int l = 0; l < tags.size(); l++) {
            //begins in a start state
            String current = "#";
            //for each state in this sentence
            for (int w = 0; w < tags.get(l).length; w++) {

                //if the state has not already been found, add it to the map
                if (!transitions.containsKey(current)) {
                    transitions.put(current, new HashMap<String, Double>());
                }
                //if this specific transition has already been found, increment the count
                if (transitions.get(current).containsKey(tags.get(l)[w])) {
                    transitions.get(current).put(tags.get(l)[w], transitions.get(current).get(tags.get(l)[w]) + 1.0);
                }
                //else, add this transition to the map with a count of one
                else {
                    transitions.get(current).put(tags.get(l)[w], 1.0);
                }
                //set the next state to be the new current state, and repeat
                current = tags.get(l)[w];
            }
        }

        //convert the raw count to the logarithm probability format
        for (String t : transitions.keySet()) {

            Double total = 0.0;
            //finds total occurences of this state
            for (String s : transitions.get(t).keySet()) {
                total += transitions.get(t).get(s); }
            //divide count of each transition by state total, and do log10
            for (String s : transitions.get(t).keySet()) {
                transitions.get(t).put(s, Math.log10(transitions.get(t).get(s) / total));
            }
        }


        //set observation probabilities

        //for each sentence
        for (int l = 0; l < tags.size(); l++) {//for each word
            //for each word
            for (int w = 0; w < tags.get(l).length; w++) {

                //if this word has not yet been observed, add to the map
                if (!observations.containsKey(sentences.get(l)[w])) {
                    observations.put(sentences.get(l)[w], new HashMap<String, Double>());
                }

                //if the observation has not been recorded in this state already, add the state to the
                // corresponding map and set count to 1
                if (!observations.get(sentences.get(l)[w]).containsKey(tags.get(l)[w])) {
                    observations.get(sentences.get(l)[w]).put(tags.get(l)[w], 1.0);
                }
                //otherwise, simply increment the count for this state corresponding to this observation
                else {
                    observations.get(sentences.get(l)[w])
                            .put(tags.get(l)[w], 1.0 + observations.get(sentences.get(l)[w]).get(tags.get(l)[w]));
                }
            }
        }

        //converts observation counts to logarithmic probability format
        //for each state
        for (String s : transitions.keySet()) {
            Double total = 0.0;
            //finds total occurrences of each state in any observation and adds their counts
            for (String w : observations.keySet()) {
                if (observations.get(w).containsKey(s)) {
                    total += observations.get(w).get(s);
                }
            }
            //divide each count by the total occurences and calculate log10
            for (String w : observations.keySet()) {
                if (observations.get(w).containsKey(s)) {
                    observations.get(w).put(s, Math.log10(observations.get(w).get(s) / total));
                }
            }
        }
    }

    // fills in the probabilities of each path in the map
    public void calcprobmap(String observation) {
        //Stores current state
        String current;

        //stores most likely states for this round: next state, previous state, score
        Map<String, Map<String, Double>> newstates = new HashMap<String, Map<String, Double>>();

        //if there have been previous rounds
        if (bestpaths.size() > 0) {

            //for each possible current state stored from the previous round
            for (String s : bestpaths.get(bestpaths.size() - 1).keySet()) {
                current = s;

                if (transitions.get(s) != null) {

                    //find all possible transitions to another state
                    for (String next : transitions.get(s).keySet()) {
                        double obsscore;


                        //check if observed is in the state, save observation score based on this
                        if (observations.containsKey(observation) && observations.get(observation).containsKey(next)) {
                            obsscore = observations.get(observation).get(next);
                        } else {
                            obsscore = notseen;
                        }

                        Map<String, Double> prevandscore = new HashMap<String, Double>();

                        //current state score, plus new score increments (transition and observation)
                        Double newscore = max(bestpaths.get(bestpaths.size() - 1).get(s).values()) + transitions.get(current).get(next) + obsscore;

                        //if there already is a path to this state, only add if the new score is higher
                        if (newstates.containsKey(next)) {

                            boolean bigger = true;

                            for (Double d : newstates.get(next).values()) {
                                if (newscore < d) {
                                    bigger = false;
                                }
                            }

                            //if it is bigger, add to the map, replacing a lees likely path
                            if (bigger) {
                                prevandscore.put(current, newscore);
                                //map from new state
                                newstates.put(next, prevandscore);
                            }
                        }

                        //if this is the first path to this state, add it to the map
                        else {
                            prevandscore.put(current, newscore);
                            //map from new state
                            newstates.put(next, prevandscore);
                        }
                    }
                }
            }
        }
        //if this is the first round, set current state to start state
        else {
            current = "#";
            for (String next : transitions.get("#").keySet()) {
                double obsscore;

                //check if observed, adjust score appropriately
                if (observations.containsKey(observation) && observations.get(observation).containsKey(next)) {
                    obsscore = observations.get(observation).get(next);
                } else {
                    obsscore = notseen;
                }

                //map that stores current state and score
                Map<String, Double> prevandscore = new HashMap<String, Double>();
                prevandscore.put(current, transitions.get(current).get(next) + obsscore);
                //add to the map, corresponding to the next state
                newstates.put(next, prevandscore);
            }
        }
        //add the states for this round to the overall list
        bestpaths.add(newstates);
    }


    //uses existing best paths map to find the most likely path
    public List<String> backtrace() {
        Double max = -100000.0;
        String currentstate = null;
        List<String> path = new ArrayList<>();

        //Starting at the most recent round, find the state that you are most likely in and set to current
        for (String s : bestpaths.get(bestpaths.size() - 1).keySet()) {
            for (String p : bestpaths.get(bestpaths.size() - 1).get(s).keySet()) {
                Double d = bestpaths.get(bestpaths.size() - 1).get(s).get(p);
                if (d > max) {
                    max = d;
                    currentstate = s;
                }
            }
        }
        //add this state to the path list
        path.add(currentstate);

        //for each round, find the most likely route to the current round(backtrace)
        for (int i = bestpaths.size() - 1; i >= 0; i--) {
            for (String s : bestpaths.get(i).get(currentstate).keySet()) {
                currentstate = s;
            }
            //add to the path(reversing order)
            path.add(0, currentstate);
        }
        return path;
    }

    //returns most likely path for a test file
    public List<List<String>> viterbitestfile() {
        try {
            //read in test file
            BufferedReader testfile = new BufferedReader(new FileReader("inputs/brown-test-sentences.txt"));
            String line = "";

            //store an array, one sentence path at each index
            List<List<String>> bestpathset = new ArrayList<List<String>>();

            //read in line by line
            while ((line = testfile.readLine()) != null) {
                line = line.toLowerCase();
                bestpaths = new ArrayList<Map<String, Map<String, Double>>>();
                //split each line into individual words
                String[] words = line.split(" ");

                //for each word, record the observation and alter the round probabilities accordingly
                for (int i = 0; i < words.length; i++) {
                    calcprobmap(words[i]);
                }

                //add the sentence's path to the list
                bestpathset.add(backtrace());

            }
            return bestpathset;

        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not read in test file");
            return null;
        }

    }


    //user interface
    public void menu() {
        //Initialize user input scanner
        Scanner input = new Scanner(System.in);
        String cont = "";

        //continue until asked to quit
        while (!cont.equals("q")) {

            System.out.println("Would you like to: \n a. read in from file \n b. write " +
                    "your own sentence \n c. read from a file and check accuracy based on a key \n q. quit ");

            cont = input.nextLine();

            //calls read in file method
            if (cont.equals("a")) {
                System.out.println(viterbitestfile());
            }

            //allows user to input their own sentence
            else if (cont.equals("b")) {
                bestpaths = new ArrayList<Map<String, Map<String, Double>>>();
                String line = input.nextLine();
                line = line.toLowerCase();
                String[] words = line.split(" ");
                for (int i = 0; i < words.length; i++) {
                    calcprobmap(words[i]); }
                System.out.println(backtrace());
            }

            //find accuracy of trainint using a test file and tag key
            else if (cont.equals("c")) {
                try {
                    //read in the tag key file and find the correct path
                    BufferedReader keyfile = new BufferedReader(new FileReader("inputs/brown-test-tags.txt"));
                    String line = "";
                    List<String[]> keypathset = new ArrayList<String[]>();
                    while ((line = keyfile.readLine()) != null) {
                        String[] tags = line.split(" ");
                        keypathset.add(tags); }

                    //call method to return the path the program predicts
                    List<List<String>> foundpath = viterbitestfile();
                    int numwrong = 0;
                    int totaltags = 0;

                    //compare word by word, incrementing total word count and wrong prediction count appropriately
                    for (int i = 0; i < keypathset.size(); i++) {
                        for (int j = 0; j < keypathset.get(i).length; j++) {
                            totaltags++;
                            if (!keypathset.get(i)[j].equals(foundpath.get(i).get(j + 1))) {
                                numwrong++;
                            }
                        }
                    }

                    System.out.println(numwrong + " wrong tags");
                    System.out.println(numwrong * 100 / totaltags + " percent wrong tags");
                    System.out.println(totaltags - numwrong + " correct tags");

                } catch (Exception e) {
                    System.err.println("error");
                }
            }
        }
    }


    public static void main(String[] args) {
        ViterbiCode code = new ViterbiCode();
        code.viterbitrainer();
        code.menu();
    }
}