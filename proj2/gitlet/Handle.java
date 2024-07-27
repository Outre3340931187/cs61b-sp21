package gitlet;

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
        if (Objects.requireNonNull(Dir.add().list()).length == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        String message = args[1];
        Repository.commit(message);
    }

    public static void handleCheckout(String[] args) {
        if (args.length == 2) {
            if (args[1].startsWith("--")) {
                String filename = args[1].substring(2);
                Commit commit = Tools.getHeadCommit();
                if (!commit.getBlobHashCodes().containsKey(filename)) {
                    System.out.println("File does not exist in that commit.");
                    return;
                }
                Repository.checkout(filename);
            } else {

            }
        } else if (args.length == 3) {
            String commitHashCode = args[1];
            String filename = args[2].substring(2);
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

    public static void handleLog(String[] args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
        }
        Repository.log();
    }
}
