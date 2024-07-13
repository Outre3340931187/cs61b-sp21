package timingtest;

import edu.princeton.cs.algs4.Stopwatch;

import java.util.List;

/**
 * Created by hug.
 */
public class TimeAList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeAListConstruction();
    }

    public static double solve(int n) {
        AList<Integer> aList = new AList<>();
        Stopwatch stopwatch = new Stopwatch();
        for (int i = 0; i < n; i++) {
            aList.addLast(0);
        }
        return stopwatch.elapsedTime();
    }

    public static void timeAListConstruction() {
        // TODO: YOUR CODE HERE
        List<Integer> numbers = List.of(1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000);
        AList<Integer> Ns = new AList<>();
        for (Integer i : numbers) {
            Ns.addLast(i);
        }
        AList<Double> times = new AList<>();
        for (Integer i : numbers) {
            times.addLast(solve(i));
        }
        printTimingTable(Ns, times, Ns);
    }
}
