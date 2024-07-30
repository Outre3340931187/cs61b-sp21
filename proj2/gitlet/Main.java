package gitlet;

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
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        if (!"init".equals(firstArg) && !Repository.initialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
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
            case "log":
                Handle.handleLog(args);
                break;
            case "rm":
                Handle.handleRm(args);
                break;
            case "global-log":
                Handle.handleGlobalLog(args);
                break;
            case "find":
                Handle.handleFind(args);
                break;
            case "status":
                Handle.handleStatus(args);
                break;
            case "checkout":
                Handle.handleCheckout(args);
                break;
            case "branch":
                Handle.handleBranch(args);
                break;
            case "rm-branch":
                Handle.handleRmBranch(args);
                break;
            case "reset":
                Handle.handleReset(args);
                break;
            default:
                System.out.println("No command with that name exists.");
        }
    }
}
