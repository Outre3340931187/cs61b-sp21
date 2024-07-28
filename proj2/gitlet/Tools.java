package gitlet;

import java.io.*;
import java.nio.file.Files;

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
}
