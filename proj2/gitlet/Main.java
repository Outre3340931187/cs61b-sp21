package gitlet;

import java.lang.reflect.Array;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author TODO
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                Handle.handleInit(args);
                break;
            case "add":
                Handle.handleAdd(args);
                break;
            case "commit":
                Handle.handleCommit(args);
                break;
            case "checkout":
                Handle.handleCheckout(args);
                break;
            case "log":
                Handle.handleLog(args);
                break;
            case "rm":
                Handle.handleRm(args);
        }
    }
}
