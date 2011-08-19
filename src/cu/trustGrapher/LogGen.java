////////////////////////////////LogGen//////////////////////////////////
package cu.trustGrapher;

import cu.trustGrapher.eventplayer.TrustLogEvent;
import java.io.File;
import java.util.List;
import java.util.Random;
import aohara.utilities.BitStylus;
import aohara.utilities.ChatterBox;

/**
 * Generates a random Trust Event Log file for use with the TrustGrapher simulator
 * @author Andrew O'Hara
 */
public class LogGen {

////////////////////////////////Static Methods//////////////////////////////////
    private static StringBuffer getHeader(File file) {
        return new StringBuffer("@relation " + file.getName() + "\n\n@attribute assessorID string\n@attribute assesseeID string\n"
                + "@attribute feedbackValue string\n\n@data\n");
    }

    private static File getSaveLocation() {
        File file = BitStylus.chooseSaveLocation("Where would you like to save the log?", new File("/home/zalpha314/Programming/Work/TrustGrapher2/test"), new String[]{"arff"});
        if (!file.getAbsolutePath().endsWith(".arff")) {
            file = new File(file.getPath() + ".arff");
        }
        return file;
    }

    private static String generateArffLog(int length, int peers, File file) {
        Random r = new Random();
        StringBuffer s = getHeader(file);
        int peer1 = -1, peer2 = -1;
        int rating;
        String feedback;
        for (int i = 0; i < length; i++) {
            if (peer2 < peers) {
                peer1 = peer2 + 1;
            } else {
                peer1 = r.nextInt(peers);
            }

            if (peer1 < peers) {
                peer2 = peer1 + 1;
            } else {
                peer2 = peer1;
                while (peer1 == peer2) {
                    peer2 = r.nextInt(peers);
                }
            }

            rating = r.nextInt(11);
            if (rating == 10) {
                feedback = "1.0";
            } else {
                feedback = "0." + rating;
            }
            s.append(peer1).append(",").append(peer2).append(",").append(feedback).append("\n");
        }
        return s.toString();
    }

    public static void saveEventLog(List<TrustLogEvent> events) {
        File saveLocation = getSaveLocation();
        StringBuffer log = getHeader(saveLocation);
        for (int i = 0 ; i < events.size() - 1 ; i++) {
            if (events.get(i) != null){
                log.append(events.get(i).toString()).append("\n");
            }
        }
        log.append(events.get(events.size() - 1));
        BitStylus.saveTextToFile(saveLocation, log.toString());
        if (saveLocation.exists()){
            ChatterBox.alert("File succesfully saved to:\n" + saveLocation.getPath());
        }else{
            ChatterBox.alert("The file was not succesfully created.\n" + saveLocation.getPath());
        }
    }

    public static void main(String[] args) {
        int peers = 1;
        int length = 0;
        while (peers > length) {
            length = Integer.parseInt(ChatterBox.userInput("What length would you like the log to be?"));
            peers = Integer.parseInt(ChatterBox.userInput("How many peers would you like?"));
            if (peers > length) {
                ChatterBox.alert("Error: The number of peers cannot be greater than the size of the log.");
            }
        }
        File saveLocation = getSaveLocation();
        String log = generateArffLog(length, peers, saveLocation);
        BitStylus.saveTextToFile(saveLocation, log);
    }
}
////////////////////////////////////////////////////////////////////////////////
