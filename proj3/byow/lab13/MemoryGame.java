package byow.lab13;

import byow.Core.RandomUtils;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.sql.Time;
import java.util.Random;

public class MemoryGame {
    /**
     * The width of the window of this game.
     */
    private int width;
    /**
     * The height of the window of this game.
     */
    private int height;
    /**
     * The current round the user is on.
     */
    private int round;
    /**
     * The Random object used to randomly generate Strings.
     */
    private Random rand;
    /**
     * Whether or not the game is over.
     */
    private boolean gameOver;
    /**
     * Whether or not it is the player's turn. Used in the last section of the
     * spec, 'Helpful UI'.
     */
    private boolean playerTurn;
    /**
     * The characters we generate random Strings from.
     */
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    /**
     * Encouraging phrases. Used in the last section of the spec, 'Helpful UI'.
     */
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
            "You got this!", "You're a star!", "Go Bears!",
            "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        long seed = Long.parseLong(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        //TODO: Initialize random number generator
        rand = new Random(seed);
    }

    public String generateRandomString(int n) {
        //TODO: Generate random string of letters of length n
        char[] chars = new char[n];
        for (int i = 0; i < n; i++) {
            chars[i] = CHARACTERS[rand.nextInt(CHARACTERS.length)];
        }
        return new String(chars);
    }

    public void drawFrame(String s) {
        //TODO: Take the string and display it in the center of the screen
        //TODO: If game is not over, display relevant game information at the top of the screen
        StdDraw.text(width / 2.0, height / 2.0, s);
        StdDraw.show();
    }

    public void flashSequence(String letters) {
        //TODO: Display each character in letters, making sure to blank the screen between letters
        try {
            for (char c : letters.toCharArray()) {
                drawFrame(Character.toString(c));
                Thread.sleep(1000);
                StdDraw.clear(Color.BLACK);
                StdDraw.show();
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            System.exit(0);
        }
    }

    public String solicitNCharsInput(int n) {
        //TODO: Read n letters of player input
        StringBuilder inputChars = new StringBuilder();
        int index = 0;
        while (index < n) {
            if (StdDraw.hasNextKeyTyped()) {
                inputChars.append(StdDraw.nextKeyTyped());
                StdDraw.clear(Color.BLACK);
                drawFrame(inputChars.toString());
                StdDraw.show();
                index++;
            }
        }
        return inputChars.toString();
    }

    public void startGame() {
        try {
            //TODO: Set any relevant variables before the game starts
            round = 0;
            StdDraw.setPenColor(Color.WHITE);
            //TODO: Establish Engine loop
            while (!gameOver) {
                round++;
                drawFrame("Round: " + round);
                Thread.sleep(1000);
                StdDraw.clear(Color.BLACK);
                StdDraw.show();
                String keyString = generateRandomString(round);
                flashSequence(keyString);
                String inputString = solicitNCharsInput(round);
                Thread.sleep(1000);
                StdDraw.clear(Color.BLACK);
                StdDraw.show();
                if (!keyString.equals(inputString)) {
                    gameOver = true;
                }
            }
            drawFrame("Game Over! You made it to round:" + round);
        } catch (InterruptedException e) {
            System.exit(0);
        }
    }
}
