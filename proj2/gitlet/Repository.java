package gitlet;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Represents a gitlet repository.
 *
 * @author TODO
 */
public class Repository {
    /*
      List all instance variables of the Repository class here with a useful
      comment above them describing what that variable represents and how that
      variable is used. We've provided two examples for you.
     */

    public static final File CWD = new File(System.getProperty("user.dir"));

    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");

    public static final String DEFAULT_BRANCH_NAME = "master";

    public static final String DOT_COMMIT = ".commit";

    public static final String DOT_BLOB = ".blob";

    public static final String DOT_HEAD = ".head";

    public static boolean initialized() {
        return Repository.GITLET_DIR.exists();
    }

    public static void init() {
        try {
            // objects
            File objects = Utils.join(GITLET_DIR, "objects");
            File commits = Utils.join(objects, "commits");
            File blobs = Utils.join(objects, "blobs");
            // refs
            File refs = Utils.join(GITLET_DIR, "refs");
            File heads = Utils.join(refs, "heads");
            // HEAD
            File head = Utils.join(GITLET_DIR, "HEAD");
            // staging
            File staging = Utils.join(GITLET_DIR, "staging");
            File add = Utils.join(staging, "add");
            File remove = Utils.join(staging, "remove");

            boolean success = true;
            success &= GITLET_DIR.mkdir();
            success &= objects.mkdir();
            success &= commits.mkdir();
            success &= blobs.mkdir();
            success &= refs.mkdir();
            success &= heads.mkdir();
            success &= head.createNewFile();
            success &= staging.mkdir();
            success &= add.mkdir();
            success &= remove.createNewFile();

            ZonedDateTime epoch = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault());
            Commit initialCommit = new Commit("initial commit", epoch, null, null);
            String hashCode = initialCommit.getHashCode();
            File initialCommitFile = Utils.join(commits, hashCode + DOT_COMMIT);
            File initialBranchHead = Utils.join(heads, DEFAULT_BRANCH_NAME + DOT_HEAD);
            success &= initialCommitFile.createNewFile();
            success &= initialBranchHead.createNewFile();
            Utils.writeObject(initialCommitFile, initialCommit);
            Utils.writeContents(initialBranchHead, hashCode);
            Utils.writeObject(remove, new HashSet<String>());
            Utils.writeContents(head, DEFAULT_BRANCH_NAME);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void add(String filename) {
        try {
            // remove it from the remove stage if it is there.
            HashSet<String> removedFilenames = Tools.getRemovedFilenames();
            removedFilenames.remove(filename);
            Utils.writeObject(Dir.remove(), removedFilenames);

            Commit currentCommit = Tools.getHeadCommit();
            File thisFile = Utils.join(CWD, filename);
            byte[] thisFileBytes = Tools.readAllBytes(thisFile);

            if (currentCommit.getBlobHashCodes().containsKey(filename)) {
                String hashCode = currentCommit.getBlobHashCodes().get(filename);
                byte[] commitedFileContents = Tools.getBlob(hashCode).getContents();
                if (Arrays.equals(thisFileBytes, commitedFileContents)) {
                    // remove it from the add stage if it is there.
                    boolean success = Utils.join(Dir.add(), filename).delete();
                    return;
                }
            }
            Blob blob = new Blob(filename, thisFileBytes);
            File addedFile = Utils.join(Dir.add(), filename);
            boolean success = addedFile.createNewFile();
            Utils.writeObject(addedFile, blob);
        } catch (IOException e) {
            throw new GitletException();
        }
    }

    public static void commit(String message, String anotherParentHashCode) {
        try {
            String parentHashCode = Tools.getHeadCommitHashCode();
            Map<String, String> parentBlobs = Tools.getCommit(parentHashCode).getBlobHashCodes();

            File[] addedFiles = Utils.join(Dir.add()).listFiles();
            if (addedFiles != null) {
                for (File f : addedFiles) {
                    String filename = f.getName();
                    String hashCode = Tools.addStagingToBlobs(f);
                    parentBlobs.put(filename, hashCode);
                }
            }
            HashSet<String> removedFilenames = Tools.getRemovedFilenames();
            for (String filename : removedFilenames) {
                parentBlobs.remove(filename);
            }
            Tools.clearStaging();

            List<String> parents = new ArrayList<>();
            parents.add(parentHashCode);
            if (anotherParentHashCode != null) {
                parents.add(anotherParentHashCode);
            }
            Commit newCommit = new Commit(message, null, parents, parentBlobs);
            String newCommitHashCode = newCommit.getHashCode();
            File newCommitFile = Utils.join(Dir.commits(), newCommitHashCode + DOT_COMMIT);
            boolean success = newCommitFile.createNewFile();
            Utils.writeObject(newCommitFile, newCommit);
            Utils.writeContents(Tools.getHeadFile(), newCommitHashCode);
        } catch (IOException e) {
            throw new GitletException("Failed to commit.");
        }
    }

    public static void rm(String filename) {
        File stagedFile = Utils.join(Dir.add(), filename);
        if (stagedFile.exists()) {
            boolean success = stagedFile.delete();
        }
        if (Tools.getHeadCommit().getBlobHashCodes().containsKey(filename)) {
            HashSet<String> removedFilenames = Tools.getRemovedFilenames();
            removedFilenames.add(filename);
            boolean success = Utils.join(CWD, filename).delete();
            Utils.writeObject(Dir.remove(), removedFilenames);
        }
    }

    public static void log() {
        Commit commit = Tools.getHeadCommit();
        while (true) {
            commit.dump();
            if (commit.getParentHashCodes() == null) {
                break;
            }
            commit = Tools.getCommit(commit.getParentHashCodes().get(0));
        }
    }

    public static void globalLog() {
        List<String> commitFilenames = Utils.plainFilenamesIn(Dir.commits());
        assert commitFilenames != null;
        for (String filename : commitFilenames) {
            File commitFile = Utils.join(Dir.commits(), filename);
            Utils.readObject(commitFile, Commit.class).dump();
        }
    }

    public static void find(String message) {
        List<String> commitFilenames = Utils.plainFilenamesIn(Dir.commits());
        assert commitFilenames != null;
        boolean found = false;
        for (String filename : commitFilenames) {
            File commitFile = Utils.join(Dir.commits(), filename);
            Commit commit = Utils.readObject(commitFile, Commit.class);
            if (message.equals(commit.getMessage())) {
                System.out.println(commit.getHashCode());
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status() {
        System.out.println("=== Branches ===");
        String currentBranchName = Tools.getCurrentBranchName();
        for (String filename : Objects.requireNonNull(Utils.plainFilenamesIn(Dir.heads()))) {
            String branchName = filename.substring(0, filename.lastIndexOf('.'));
            if (branchName.equals(currentBranchName)) {
                System.out.print("*");
            }
            System.out.println(branchName);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        for (String filename : Objects.requireNonNull(Utils.plainFilenamesIn(Dir.add()))) {
            System.out.println(filename);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        TreeSet<String> removedFilenames = new TreeSet<>(Tools.getRemovedFilenames());
        for (String filename : removedFilenames) {
            System.out.println(filename);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        TreeMap<String, String> modifiedNotStagedFiles = Tools.getModifiedNotStagedFiles();
        modifiedNotStagedFiles.forEach((file, s) -> System.out.printf("%s (%s)\n", file, s));
        System.out.println();

        System.out.println("=== Untracked Files ===");
        Tools.getUntrackedFiles().forEach(System.out::println);
        System.out.println();
    }

    public static void checkout(String filename) {
        checkout(Tools.getHeadCommitHashCode(), filename);
    }

    public static void checkout(String commitHashCode, String filename) {
        try {
            Commit commit = Tools.getCommit(commitHashCode);
            String blobHashCode = commit.getBlobHashCodes().get(filename);
            byte[] contents = Tools.getBlob(blobHashCode).getContents();
            File workspaceFile = Utils.join(CWD, filename);
            if (!workspaceFile.exists()) {
                boolean success = workspaceFile.createNewFile();
            }
            Utils.writeContents(workspaceFile, contents);
        } catch (IOException e) {
            throw new GitletException();
        }
    }

    private static void changeWorkspace(Commit commit) {
        try {
            Map<String, String> branchBlobHashCodes = commit.getBlobHashCodes();
            Map<String, String> currentBlobHashCodes = Tools.getHeadCommit().getBlobHashCodes();
            for (String filename : currentBlobHashCodes.keySet()) {
                if (!branchBlobHashCodes.containsKey(filename)) {
                    boolean success = Utils.join(CWD, filename).delete();
                }
            }
            for (String filename : branchBlobHashCodes.keySet()) {
                File file = Utils.join(CWD, filename);
                if (!file.exists()) {
                    boolean success = file.createNewFile();
                }
                byte[] contents = Tools.getBlob(branchBlobHashCodes.get(filename)).getContents();
                Utils.writeContents(file, contents);
            }
            Tools.clearStaging();
        } catch (IOException e) {
            throw new GitletException();
        }
    }

    public static void checkoutBranch(String branchName) {
        changeWorkspace(Tools.getHeadCommit(branchName));
        Utils.writeContents(Dir.head(), branchName);
    }

    public static void branch(String branchName) {
        try {
            String currentCommitHashCode = Tools.getHeadCommitHashCode();
            File branchFile = Utils.join(Dir.heads(), branchName + DOT_HEAD);
            boolean success = branchFile.createNewFile();
            Utils.writeContents(branchFile, currentCommitHashCode);
        } catch (IOException e) {
            throw new GitletException();
        }
    }

    public static void rmBranch(String branchName) {
        boolean success = Utils.join(Dir.heads(), branchName + DOT_HEAD).delete();
    }

    public static void reset(String commitHashCode) {
        changeWorkspace(Tools.getCommit(commitHashCode));
        File headFile = Utils.join(Dir.heads(), Tools.getCurrentBranchName() + DOT_HEAD);
        Utils.writeContents(headFile, commitHashCode);
    }

    public static void merge(String branchName) {
        // TODO: checkUntrackedFiles(Commit);
        try {
            String splitCommitHash = Tools.getSplitCommitHashCode(branchName);
            if (Tools.getHeadCommitHashCode(branchName).equals(splitCommitHash)) {
                System.out.println("Given branch is an ancestor of the current branch.");
                return;
            }
            if (Tools.getHeadCommitHashCode().equals(splitCommitHash)) {
                changeWorkspace(Tools.getHeadCommit(branchName));
                System.out.println("Current branch fast-forwarded.");
                return;
            }
            boolean conflict = false;
            String branchHeadHash = Tools.getHeadCommitHashCode(branchName);
            Map<String, String> currentBlobs = Tools.getHeadCommit().getBlobHashCodes();
            Map<String, String> branchBlobs = Tools.getCommit(branchHeadHash).getBlobHashCodes();
            Map<String, String> splitBlobs = Tools.getCommit(splitCommitHash).getBlobHashCodes();
            for (String file : splitBlobs.keySet()) {
                boolean currentExists = currentBlobs.containsKey(file);
                boolean branchExists = branchBlobs.containsKey(file);
                if (currentExists && branchExists) {
                    // both exists
                    byte[] contents = Tools.getBlob(splitBlobs.get(file)).getContents();
                    byte[] currentContents = Tools.getBlob(currentBlobs.get(file)).getContents();
                    byte[] branchContents = Tools.getBlob(branchBlobs.get(file)).getContents();
                    boolean currentModified = !Arrays.equals(contents, currentContents);
                    boolean branchModified = !Arrays.equals(contents, branchContents);
                    if (!currentModified && branchModified) {
                        File workspaceFile = Utils.join(CWD, file);
                        if (!workspaceFile.exists()) {
                            boolean success = workspaceFile.createNewFile();
                        }
                        Utils.writeContents(Utils.join(CWD, file), branchContents);
                        add(file);
                    } else if (currentModified && branchModified) {
                        if (!Arrays.equals(currentContents, branchContents)) {
                            conflict = true;
                            String newCon = Tools.mergeContents(currentContents, branchContents);
                            Utils.writeContents(Utils.join(CWD, file), newCon);
                        }
                    }
                } else if (currentExists && !branchExists) {
                    // only exists in current branch
                    byte[] contents = Tools.getBlob(splitBlobs.get(file)).getContents();
                    byte[] currentContents = Tools.getBlob(currentBlobs.get(file)).getContents();
                    boolean currentModified = !Arrays.equals(contents, currentContents);
                    if (!currentModified) {
                        boolean success = Utils.join(CWD, file).delete();
                        currentBlobs.remove(file);
                    } else {
                        conflict = true;
                        String newContents = Tools.mergeContents(currentContents, null);
                        Utils.writeContents(Utils.join(CWD, file), newContents);
                    }
                } else if (!currentExists && branchExists) {
                    // only exists in merged branch
                    byte[] contents = Tools.getBlob(splitBlobs.get(file)).getContents();
                    byte[] branchContents = Tools.getBlob(branchBlobs.get(file)).getContents();
                    boolean branchModified = !Arrays.equals(contents, branchContents);
                    if (branchModified) {
                        conflict = true;
                        String newContents = Tools.mergeContents(null, branchContents);
                        Utils.writeContents(Utils.join(CWD, file), newContents);
                    }
                }
            }
            Set<String> currentDifference = new HashSet<>(currentBlobs.keySet());
            currentDifference.removeAll(splitBlobs.keySet());
            Set<String> branchDifference = new HashSet<>(branchBlobs.keySet());
            branchDifference.removeAll(splitBlobs.keySet());
            for (String file : branchDifference) {
                if (currentDifference.contains(file)) {
                    byte[] currentContents = Tools.getBlob(currentBlobs.get(file)).getContents();
                    byte[] branchContents = Tools.getBlob(branchBlobs.get(file)).getContents();
                    if (!Arrays.equals(currentContents, branchContents)) {
                        conflict = true;
                        String newContents = Tools.mergeContents(currentContents, branchContents);
                        Utils.writeContents(Utils.join(CWD, file), newContents);
                    }
                } else {
                    checkout(branchHeadHash, file);
                    add(file);
                }
            }
            String message = "Merged " + branchName + " into " + Tools.getCurrentBranchName() + ".";
            if (conflict) {
                System.out.println("Encountered a merge conflict.");
            }
            commit(message, branchHeadHash);
        } catch (IOException e) {
            throw new GitletException();
        }
    }
}
