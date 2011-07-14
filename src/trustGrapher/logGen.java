////////////////////////////////logGen//////////////////////////////////
package trustGrapher;

import java.io.File;
import java.util.Random;
import utilities.BitStylus;
import utilities.ChatterBox;

/**
 * Generates a random EventLog file
 * @author Andrew O'Hara
 */
public class logGen {

////////////////////////////////Static Methods//////////////////////////////////

    public static void generateTextLog(int length, int maxPeers, File file){
        Random r = new Random();
        String s = "" + length + "\n";
        int peer1, peer2;
        int rating;
        String feedback;
        int time = 0;
        for (int i=0 ; i<length ; i++){
            time = time + (50 * r.nextInt(7));
            peer1 = r.nextInt(maxPeers) + 1;
            peer2 = peer1;
            while (peer1 == peer2){
                peer2 = r.nextInt(maxPeers) + 1;
            }
            rating = r.nextInt(11);
            if (rating == 10){
                feedback = "1.0";
            }else{
                feedback = "0." + rating;
            }
            s = s + time+":"+peer1+":"+peer2+":"+feedback+"\n";
        }
        BitStylus.saveTextToFile(file, s);
    }

    public static void generateArffLog(int length, int maxPeers, File file){
        Random r = new Random();
        String s = "@relation " + file.getName() + "\n\n";
        s = s + "@attribute assessorID string\n";
        s = s + "@attribute assesseeID string\n";
        s = s + "@attribute feedbackValue string\n\n";
        s = s + "@data\n";
        int peer1, peer2;
        int rating;
        String feedback;
        for (int i=0 ; i<length ; i++){
            peer1 = r.nextInt(maxPeers);
            peer2 = peer1;
            while (peer1 == peer2){
                peer2 = r.nextInt(maxPeers);
            }
            rating = r.nextInt(11);
            if (rating == 10){
                feedback = "1.0";
            }else{
                feedback = "0." + rating;
            }
            s = s + peer1+","+peer2+","+feedback+"\n";
        }
        BitStylus.saveTextToFile(file, s);
    }

    public static void main(String[] args){
        String[] options = {"Text file", "Arff file"};
        int val = ChatterBox.questionPane("Question", "What file format would you like to save it to?", options);
        int length = Integer.parseInt(ChatterBox.userInput("What length would you like the log to be?"));
        int maxPeers = Integer.parseInt(ChatterBox.userInput("What is the maximum amount of peers that you would like?"));
        File file = BitStylus.chooseSaveLocation("Where would you like to save the log?", null);
        
        if (val == 0 && !file.getAbsolutePath().endsWith(".txt")){
            file = new File(file.getPath() + ".txt");
        }else if (val == 1 && !file.getAbsolutePath().endsWith(".arff")){
            file = new File(file.getPath() + ".arff");
        }
        
        switch(val){
            case 0: generateTextLog(length, maxPeers, file);
            case 1: generateArffLog(length, maxPeers, file);
        }
    }

}
////////////////////////////////////////////////////////////////////////////////