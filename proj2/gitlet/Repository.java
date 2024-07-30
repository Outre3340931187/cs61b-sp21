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
        Commit currentCommit = Tools.getHeadCommit();
        File thisFile = Utils.join(CWD, filename);
        byte[] thisFileBytes = Tools.readAllBytes(thisFile);

        if (currentCommit.getBlobHashCodes() != null && currentCommit.getBlobHashCodes().get(filename) != null) {
            String hashCode = currentCommit.getBlobHashCodes().get(filename);
            byte[] commitedFileContents = Tools.getBlob(hashCode).getContents();
            if (Arrays.equals(thisFileBytes, commitedFileContents)) {
                return;
            }
        }

        Blob blob = new Blob(filename, thisFileBytes);
        File addedFile = Utils.join(Dir.add(), filename);
        try {
            boolean success = addedFile.createNewFile();
        } catch (IOException e) {
            throw new GitletException("Failed to add file: " + filename);
        }
        Utils.writeObject(addedFile, blob);
    }

    public static void commit(String message) {
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
                for (File f : addedFiles) {
                    boolean success = f.delete();
                }
            }
            HashSet<String> removedFilenames = Tools.getRemovedFilenames();
            for (String filename : removedFilenames) {
                parentBlobs.remove(filename);
            }
            removedFilenames.clear();
            Utils.writeObject(Dir.remove(), removedFilenames);

            Commit newCommit = new Commit(message, null, Collections.singletonList(parentHashCode), parentBlobs);
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
            Utils.writeObject(Dir.remove(), removedFilenames);
        }
        File workspaceFile = Utils.join(CWD, filename);
        if (workspaceFile.exists()) {
            boolean success = workspaceFile.delete();
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
        modifiedNotStagedFiles.forEach((name, state) -> System.out.printf("%s (%s)\n", name, state));
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

    public static void checkoutBranch(String branchName) {

    }

    public static void branch(String branchName) {
        try {
            String currentCommitHashCode = Tools.getHeadCommitHashCode();
            File branchFile = Utils.join(Dir.heads(), branchName + DOT_HEAD);
            boolean success = branchFile.createNewFile();
            Utils.writeContents(branchFile, currentCommitHashCode);
            Utils.writeContents(Dir.HEAD(), branchName);
        } catch (IOException e) {
            throw new GitletException();
        }
    }
}
