package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {
    @Test
    public void testStudentArrayDeque() {
        StudentArrayDeque<Integer> studentArrayDeque = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> arrayDequeSolution = new ArrayDequeSolution<>();
        StringBuilder information = new StringBuilder();
        while (true) {
            int option = StdRandom.uniform(0, 4);
            if (option == 0) {
                int rand = StdRandom.uniform(Integer.MAX_VALUE);
                studentArrayDeque.addFirst(rand);
                arrayDequeSolution.addFirst(rand);
                information.append("addFirst(").append(rand).append(")\n");
            } else if (option == 1) {
                int rand = StdRandom.uniform(Integer.MAX_VALUE);
                studentArrayDeque.addLast(rand);
                arrayDequeSolution.addLast(rand);
                information.append("addLast(").append(rand).append(")\n");
            } else if (option == 2) {
                if (!arrayDequeSolution.isEmpty()) {
                    Integer actual = studentArrayDeque.removeFirst();
                    Integer expected = arrayDequeSolution.removeFirst();
                    information.append("removeFirst()\n");
                    assertEquals(information.toString(), expected, actual);
                }
            } else if (option == 3) {
                if (!arrayDequeSolution.isEmpty()) {
                    Integer actual = studentArrayDeque.removeLast();
                    Integer expected = arrayDequeSolution.removeLast();
                    information.append("removeLast()\n");
                    assertEquals(information.toString(), expected, actual);
                }
            }
        }
    }
}
