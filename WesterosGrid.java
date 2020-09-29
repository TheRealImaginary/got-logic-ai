import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Random;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.StringBuilder;

/**
 * Westeros Grid
 */
public class WesterosGrid  {

    private static enum CellType {
        J,
        E,
        O,
        W,
        D;
    }

    private static PrintWriter out;
    private static Scanner sc;

    static {
        try {
            out = new PrintWriter("westeros.pl");
            sc = new Scanner(System.in);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateSaveWesterosProblem(int numberOfRows, int numberOfColumns, int numberOfDragonGlasses, int numberOfWhiteWalkers, int numberOfObstacles) throws IOException {
        Random random = new Random(123);

        writePrologComment("Number Of Rows");
        writePrologRule("numberOfRows", String.format("%s", numberOfRows));
        out.println();

        writePrologComment("Number Of Columns");
        writePrologRule("numberOfColumns", String.format("%s", numberOfColumns));
        out.println();

        CellType[][] initialGrid = new CellType[numberOfRows][numberOfColumns];
        for (int row = 0; row < numberOfRows; row++)
            Arrays.fill(initialGrid[row], CellType.E);

        if (numberOfWhiteWalkers + numberOfObstacles + 2 >= numberOfRows * numberOfColumns)
            throw new RuntimeException(String.format("Too many objects to place in this grid ! (%s,%s) WW=%s, O=%s !",
                    numberOfRows, numberOfColumns, numberOfWhiteWalkers, numberOfObstacles));
        // Place Jon
        initialGrid[numberOfRows - 1][numberOfColumns - 1] = CellType.J;
        writePrologComment("Jon's Starting Position");
        writePrologRule("jon_location", String.format("(%s, %s)", numberOfRows - 1, numberOfColumns - 1));

        out.println();
        writePrologComment("White Walkers Starting Positions");
        // Place White Walkers
        for (int i = 1; i <= numberOfWhiteWalkers; ) {
            int row = random.nextInt(numberOfRows);
            int col = random.nextInt(numberOfColumns);
            if (initialGrid[row][col] == CellType.E) {
                initialGrid[row][col] = CellType.W;
                writePrologRule("whiteWalker_location", String.format("(%s, %s)", row, col));
                i++;
            }
        }

        out.println();
        writePrologComment("Obstacles Positions");
        // Place Obstacles
        for (int i = 1; i <= numberOfObstacles; ) {
            int row = random.nextInt(numberOfRows);
            int col = random.nextInt(numberOfColumns);
            if (initialGrid[row][col] == CellType.E) {
                initialGrid[row][col] = CellType.O;
                writePrologRule("obstacle_location", String.format("(%s, %s)", row, col));
                i++;
            }
        }

        out.println();
        writePrologComment("Dragon Stone Position");
        // Place Dragon Stone
        while (true) {
            int row = random.nextInt(numberOfRows);
            int col = random.nextInt(numberOfColumns);
            if (initialGrid[row][col] == CellType.E) {
                initialGrid[row][col] = CellType.D;
                writePrologRule("dragonStone_location", String.format("(%s, %s)", row, col));
                break;
            }
        }

        out.println();
        writePrologComment("Empty Cells Positions");
        for(int row = 0; row < numberOfRows;row++) 
            for(int col = 0;col < numberOfColumns;col++)
                if(initialGrid[row][col] == CellType.E)
                    writePrologRule("empty_location", String.format("(%s, %s)", row, col));

        out.println();
        writePrologComment("Maximum Number of Allowed Dragon Glasses");
        writePrologRule("maxDragonGlasses", String.format("%s", numberOfDragonGlasses));

        out.println();
        writePrologComment("Number of Dragon Glasses Jon has");
        writePrologRule("dragonGlasses", "0");

        out.println();
        for (CellType[] row: initialGrid)
            writePrologComment(Arrays.toString(row));

        out.flush();
        out.close();
    }

    private static void writePrologComment(String comment) {
        out.print("% ");
        out.println(comment); 
    }

    private static void writePrologRule(String ruleName, String...ruleParams) {
        StringBuilder sb = new StringBuilder();
        for (String param : ruleParams) {
            if (sb.length() != 0) sb.append(", ");
            sb.append(param);
        }
        out.printf("%s(%s).\n", ruleName, sb);
    }

    private static int scanInt(String prompt) {
        System.out.println(prompt);
        return sc.nextInt();
    }

    public static void main(String[] args) throws IOException {
        int numberOfRows = scanInt("Enter the desired Number Of Rows");
        int numberOfColumns = scanInt("Enter the desired Number Of Columns");
        int numberOfDragonGlasses = scanInt("Enter the desired Number Of Dragon Glasses");
        int numberOfWhiteWalkers = scanInt("Enter the desired Number Of White Walkers");
        int numberOfObstacles = scanInt("Enter the desired Number Of Obstacles");
        generateSaveWesterosProblem(numberOfRows, numberOfColumns, numberOfDragonGlasses, numberOfWhiteWalkers, numberOfObstacles);
    }
}