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
                    Utils.restrictedDelete(f);
                }
            }
            HashSet<String> removedFiles = Utils.readObject(Dir.remove(), HashSet.class);
            for (String filename : removedFiles) {
                parentBlobs.remove(filename);
            }
            removedFiles.clear();
            Utils.writeObject(Dir.remove(), removedFiles);

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
            HashSet<String> removedFiles = Utils.readObject(Dir.remove(), HashSet.class);
            removedFiles.add(filename);
            Utils.writeObject(Dir.remove(), removedFiles);
        }
        File workspaceFile = Utils.join(CWD, filename);
        if (workspaceFile.exists()) {
            Utils.restrictedDelete(workspaceFile);
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
}
