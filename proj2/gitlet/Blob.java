package gitlet;

import java.io.Serializable;

public class Blob implements Serializable {
    private final String path;
    private final byte[] contents;
    private final String hashCode;

    public Blob(String path, byte[] contents) {
        this.path = path;
        this.contents = contents;
        this.hashCode = Utils.sha1(Utils.serialize(this));
    }

    public String getPath() {
        return path;
    }

    public byte[] getContents() {
        return contents;
    }

    public String getHashCode() {
        return hashCode;
    }
}
