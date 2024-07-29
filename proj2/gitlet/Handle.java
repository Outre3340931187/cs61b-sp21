package gitlet;

import java.util.HashSet;
import java.util.Objects;

public class Handle {
    public static void handleInit(String[] args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        if (Repository.GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        Repository.init();
    }

    public static void handleAdd(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        String filename = args[1];
        if (!Utils.join(Repository.CWD, filename).exists()) {
            System.out.println("File does not exist.");
            return;
        }
        Repository.add(filename);
    }

    public static void handleCommit(String[] args) {
        if (args.length == 1) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        boolean addEmpty = Objects.requireNonNull(Dir.add().list()).length == 0;
        boolean removeEmpty = Utils.readObject(Dir.remove(), HashSet.class).isEmpty();
        if (addEmpty && removeEmpty) {
            System.out.println("No changes added to the commit.");
            return;
        }
        String message = args[1];
        Repository.commit(message);
    }

    public static void handleRm(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        String filename = args[1];
        boolean staged = Utils.join(Dir.add(), filename).exists();
        boolean tracked = Tools.getHeadCommit().getBlobHashCodes().containsKey(filename);
        if (!staged && !tracked) {
            System.out.println("No reason to remove the file.");
            return;
        }
        Repository.rm(filename);
    }

    public static void handleLog(String[] args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        Repository.log();
    }

    public static void handleGlobalLog(String[] args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        Repository.globalLog();
    }

    public static void handleFind(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        String message = args[1];
        Repository.find(message);
    }

    public static void handleCheckout(String[] args) {
        if (args.length == 2) {

        } else if (args.length == 3) {
            String filename = args[2];
            Commit commit = Tools.getHeadCommit();
            if (!commit.getBlobHashCodes().containsKey(filename)) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            Repository.checkout(filename);
        } else if (args.length == 4) {
            String commitHashCode = args[1];
            String filename = args[3];
            if (!Commit.contains(commitHashCode)) {
                System.out.println("No commit with that id exists.");
                return;
            }
            Commit commit = Tools.getCommit(commitHashCode);
            if (!commit.getBlobHashCodes().containsKey(filename)) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            Repository.checkout(commitHashCode, filename);
        } else {
            System.out.println("Incorrect operands.");
        }
    }
}
