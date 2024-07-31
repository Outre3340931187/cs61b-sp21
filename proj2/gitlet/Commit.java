package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Represents a gitlet commit object.
 *
 * @author Outre
 */
public class Commit implements Serializable, Dumpable {
    /*
      List all instance variables of the Commit class here with a useful
      comment above them describing what that variable represents and how that
      variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private final DateTimeFormatter formatter
            = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss yyyy xx", Locale.US);

    private final String message;
    private final ZonedDateTime commitTime;
    private final List<String> parentHashCodes;
    private final Map<String, String> blobHashCodes;
    private final String hashCode;


    public Commit(String msg, ZonedDateTime time, List<String> parents, Map<String, String> blobs) {
        this.message = msg;
        this.commitTime = time == null ? ZonedDateTime.now() : time;
        this.parentHashCodes = parents;
        this.blobHashCodes = blobs == null ? new HashMap<>() : blobs;
        this.hashCode = Utils.sha1(Utils.serialize(this));
    }

    public String getMessage() {
        return message;
    }

    public ZonedDateTime getCommitTime() {
        return commitTime;
    }

    public List<String> getParentHashCodes() {
        return parentHashCodes;
    }

    public Map<String, String> getBlobHashCodes() {
        return blobHashCodes;
    }

    public String getHashCode() {
        return hashCode;
    }

    @Override
    public void dump() {
        System.out.println("===");
        System.out.println("commit " + hashCode);
        if (parentHashCodes != null && parentHashCodes.size() > 1) {
            String firstParentHashCode = parentHashCodes.get(0).substring(0, 7);
            String secondParentHashCode = parentHashCodes.get(1).substring(0, 7);
            System.out.println("Merge: " + firstParentHashCode + " " + secondParentHashCode);
        }
        System.out.println("Date: " + commitTime.format(formatter));
        System.out.println(message);
        System.out.println();
    }

    public static boolean contains(String commitHashCode) {
        if (commitHashCode == null) {
            return false;
        }
        File[] commitFiles = Dir.commits().listFiles();
        if (commitFiles == null) {
            return false;
        }
        commitHashCode += Repository.DOT_COMMIT;
        for (File commitFile : commitFiles) {
            if (commitFile.getName().equals(commitHashCode)) {
                return true;
            }
        }
        return false;
    }
}
