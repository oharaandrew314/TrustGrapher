////////////////////////////////logGen//////////////////////////////////
package cu.trustGrapher;

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
    public static void generateArffLog(int length, int peers, File file){
        Random r = new Random();
        String s = "@relation " + file.getName() + "\n\n";
        s = s + "@attribute assessorID string\n";
        s = s + "@attribute assesseeID string\n";
        s = s + "@attribute feedbackValue string\n\n";
        s = s + "@data\n";
        int peer1 = -1, peer2 = -1;
        int rating;
        String feedback;
        for (int i=0 ; i<length ; i++){
            if (peer2 < peers){
                peer1 = peer2 + 1;
            }else{
                peer1 = r.nextInt(peers);
            }
            
            if (peer1 < peers){
                peer2 = peer1 + 1;
            }else{
                peer2 = peer1;
                while (peer1 == peer2){
                    peer2 = r.nextInt(peers);
                }
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
        int peers = 1;
        int length = 0;
        while (peers > length){
            length = Integer.parseInt(ChatterBox.userInput("What length would you like the log to be?"));
            peers = Integer.parseInt(ChatterBox.userInput("How many peers would you like?"));
            if (peers > length){
                ChatterBox.alert("Error: The number of peers cannot be greater than the size of the log.");
            }
        }

        File file = BitStylus.chooseSaveLocation("Where would you like to save the log?", new File("/home/zalpha314/Documents/Programming/Java/Work/TrustGrapher2/test"));
        if (!file.getAbsolutePath().endsWith(".arff")){
            file = new File(file.getPath() + ".arff");
        }
        generateArffLog(length, peers, file);
    }

}
////////////////////////////////////////////////////////////////////////////////