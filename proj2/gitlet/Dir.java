package gitlet;

import java.io.File;

public class Dir {
    public static File blobs() {
        return Utils.join(Repository.GITLET_DIR, "objects", "blobs");
    }

    public static File commits() {
        return Utils.join(Repository.GITLET_DIR, "objects", "commits");
    }

    public static File heads() {
        return Utils.join(Repository.GITLET_DIR, "refs", "heads");
    }

    public static File add() {
        return Utils.join(Repository.GITLET_DIR, "staging", "add");
    }

    public static File remove() {
        return Utils.join(Repository.GITLET_DIR, "staging", "remove");
    }

    public static File head() {
        return Utils.join(Repository.GITLET_DIR, "HEAD");
    }
}
