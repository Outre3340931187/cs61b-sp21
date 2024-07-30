package gitlet;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Tools {

    public static String getCurrentBranchName() {
        return Utils.readContentsAsString(Dir.HEAD());
    }

    public static String getHeadCommitHashCode(String branchName) {
        File headFile = Utils.join(Dir.heads(), branchName + Repository.DOT_HEAD);
        return Utils.readContentsAsString(headFile);
    }

    public static String getHeadCommitHashCode() {
        return getHeadCommitHashCode(getCurrentBranchName());
    }

    public static Commit getCommit(String commitHashCode) {
        File commitFile = Utils.join(Dir.commits(), commitHashCode + Repository.DOT_COMMIT);
        return Utils.readObject(commitFile, Commit.class);
    }

    public static Commit getHeadCommit(String branchName) {
        String hashCode = getHeadCommitHashCode(branchName);
        return getCommit(hashCode);
    }

    public static Commit getHeadCommit() {
        return getHeadCommit(getCurrentBranchName());
    }

    public static File getHeadFile(String branchName) {
        return Utils.join(Dir.heads(), branchName + Repository.DOT_HEAD);
    }

    public static File getHeadFile() {
        return getHeadFile(getCurrentBranchName());
    }

    public static void clearAddStaging() {
        File[] addFiles = Dir.add().listFiles();
        if (addFiles != null) {
            for (File file : addFiles) {
                boolean success = file.delete();
            }
        }
    }

    public static void clearRemoveStaging() {
        Utils.writeObject(Dir.remove(), new HashSet<String>());
    }

    public static void clearStaging() {
        clearAddStaging();
        clearRemoveStaging();
    }

    public static byte[] readAllBytes(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new GitletException();
        }
    }

    public static Blob getBlob(String hashCode) {
        File blobFile = Utils.join(Dir.blobs(), hashCode + Repository.DOT_BLOB);
        return Utils.readObject(blobFile, Blob.class);
    }

    public static String addStagingToBlobs(File file) {
        try {
            Blob blob = Utils.readObject(file, Blob.class);
            String hashCode = blob.getHashCode();
            File blobFile = Utils.join(Dir.blobs(), hashCode + Repository.DOT_BLOB);
            boolean success = blobFile.createNewFile();
            Utils.writeObject(blobFile, blob);
            return hashCode;
        } catch (IOException e) {
            throw new GitletException();
        }
    }

    public static HashSet<String> getRemovedFilenames() {
        return Utils.readObject(Dir.remove(), HashSet.class);
    }

    public static TreeMap<String, String> getModifiedNotStagedFiles() {
        TreeMap<String, String> modifiedNotStagedFiles = new TreeMap<>();
        File[] addedFiles = Dir.add().listFiles();
        Map<String, String> committedFiles = getHeadCommit().getBlobHashCodes();
        if (addedFiles != null) {
            for (File file : addedFiles) {
                committedFiles.remove(file.getName());
                if (!Utils.join(Repository.CWD, file.getName()).exists()) {
                    modifiedNotStagedFiles.put(file.getName(), "deleted");
                    continue;
                }
                byte[] addedContents = Utils.readObject(file, Blob.class).getContents();
                byte[] currentContents = readAllBytes(Utils.join(Repository.CWD, file.getName()));
                if (!Arrays.equals(addedContents, currentContents)) {
                    modifiedNotStagedFiles.put(file.getName(), "modified");
                }
            }
        }

        for (String filename : committedFiles.keySet()) {
            boolean delete = !Utils.join(Repository.CWD, filename).exists();
            boolean remove = getRemovedFilenames().contains(filename);
            byte[] committedContents = getBlob(committedFiles.get(filename)).getContents();
            byte[] currentContents = readAllBytes(Utils.join(Repository.CWD, filename));
            boolean modified = !Arrays.equals(committedContents, currentContents);
            if (delete && !remove) {
                modifiedNotStagedFiles.put(filename, "deleted");
            }
            if (!delete && modified) {
                modifiedNotStagedFiles.put(filename, "modified");
            }
        }
        return modifiedNotStagedFiles;
    }

    public static TreeSet<String> getUntrackedFiles() {
        TreeSet<String> untrackedFiles = new TreeSet<>();
        List<String> workspaceFiles = Utils.plainFilenamesIn(Repository.CWD);
        if (workspaceFiles == null) {
            return untrackedFiles;
        }
        for (String filename : workspaceFiles) {
            boolean add = Utils.join(Dir.add(), filename).exists();
            boolean tracked = getHeadCommit().getBlobHashCodes().containsKey(filename);
            if (!add && !tracked) {
                untrackedFiles.add(filename);
            }
        }
        return untrackedFiles;
    }
}
