import java.io.*;

/**
 * Test class for OS_RBTree. All items are integers between 1 and 999. <br>
 * <br>
 * Get commands from file and execute them line-by-line. <br>
 * Validity of result is checked by separate class using array. <br>
 * <br>
 * @author 오지현 (자연과학대학 생명과학부, ID: 2017-16544)
 */
public class OS_RBTreeTest
{
    public static OS_RBTree<Integer> tree = new OS_RBTree<>();

    public static void main(String[] args)
    {
        String filename = "";

        try
        {
            if (args.length == 1) filename = args[0];
            else throw new IllegalArgumentException();
        }
        catch(IllegalArgumentException e)
        {
            System.out.println("ERROR: Input format is incorrect.");
            System.out.println("The command should follow this format: java OS_RBTreeTest <String>filename.");
            System.exit(1);
        }

        String command = "";

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            while ((command = br.readLine()) != null)
            {
                if (command.matches("[IDSR] \\d+")) parse(command);
                else throw new IllegalArgumentException();
            }
            br.close();
        }
        catch(FileNotFoundException e)
        {
            System.out.printf("ERROR: File '%s' is not found.\n", command);
            System.exit(1);
        }
        catch (IllegalArgumentException e)
        {
            System.out.printf("ERROR: Command '%s' does not follow format.\n", command);
            System.exit(1);
        }
        catch(Exception e)
        {
            System.out.println("Unexpected error has occurred.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Parse command and execute it if valid
     * @param command command to execute
     */
    public static void parse(String command)
    {
        try
        {
            int x = Integer.parseInt(command.substring(2));
            if (x == 0 || x > 999) throw new IllegalArgumentException();
            int treeResult;
            int correctResult;
            switch (command.charAt(0))
            {
                case 'I':
                    treeResult = convert(tree.insert(x));
                    correctResult = OS_RBTreeCheck.insert(x);
                    break;
                case 'D':
                    treeResult = convert(tree.delete(x));
                    correctResult = OS_RBTreeCheck.delete(x);
                    break;
                case 'S':
                    treeResult = convert(tree.select(x));
                    correctResult = OS_RBTreeCheck.select(x);
                    break;
                case 'R':
                    treeResult = tree.rank(x);
                    correctResult = OS_RBTreeCheck.rank(x);
                    break;
                default: throw new Exception(); // case must be found
            }
            System.out.println(command);
            System.out.printf("OS_RBTree output: %d\nCorrect   output: %d\n\n", treeResult, correctResult);

            // validity check
            if (!tree.isValid()) // order statistics or red-black property is broken
            {
                // tree is already printed while executing isValid method
                System.out.println("Tree format: ([R or B] item,(left node),(right node))");
                System.out.printf("ERROR: Upon executing command '%s', OS_RB property is broken.\n", command);
                System.exit(1);
            }
            if (treeResult != correctResult) // execution result is incorrect
            {
                System.out.println(tree);
                System.out.println("Tree format: ([R or B] item,(left node),(right node))");
                System.out.printf("ERROR: result of executing command '%s' is incorrect.\n", command);
                System.exit(1);
            }
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("ERROR: Integer should be in range 1-999.");
            System.exit(1);
        }
        catch (Exception e)
        {
            System.out.println(tree);
            e.printStackTrace();
            System.out.printf("Unexpected error has occurred parsing command: %s\n", command);
            System.exit(1);
        }
    }

    /**
     * Convert null to 0
     * @param x Integer object
     * @return 0 if x is null, otherwise x as int
     */
    public static int convert(Integer x) { return (x == null) ? 0 : x; } // OS_RBTree returns null instead of 0
}

/**
 * Insert, Delete, Select, and Rank operation is supported using array.
 *
 * Used to check whether result of OS_RBTree executing command is correct.
 */
class OS_RBTreeCheck
{
    public static final int ARRAY_SIZE = 1000;
    private static int[] rank = new int[ARRAY_SIZE];
    /*
        rank[x] stores number of elements <= x in OS_RBTree
            -> x is in OS_RBTree iff rank[x - 1] != rank[x]
            -> rank[ARR_SIZE - 1] equals size of the OS_RBTree
     */

    public static int insert(int x)
    {
        if (rank[x - 1] != rank[x]) return 0; // x is in OS_RBTree
        for (int i = x ; i < ARRAY_SIZE ; i++) rank[i]++;
        return x;
    }

    public static int delete(int x)
    {
        if (rank[x - 1] == rank[x]) return 0; // x is not in OS_RBTree
        for (int i = x ; i < ARRAY_SIZE ; i++) rank[i]--;
        return x;
    }

    public static int select(int i)
    {
        if (i > rank[ARRAY_SIZE - 1]) return 0; // i is greater than the size of OS_RBTree
        int start = 1, end = ARRAY_SIZE - 1;
        int mid;
        do // binary search
        {
            mid = (start + end) / 2;
            int comp = rank[mid] - i;
            if (comp < 0) start = mid + 1;
            else if (comp > 0) end = mid - 1;
            else
            {
                do mid--; while (rank[mid] == i);
                return ++mid;
            }
        }
        while (start < end);
        return start;
    }

    public static int rank(int x) // if x is in OS_RBTree, rank of x equals arr[x]
    {
        return rank[x - 1] != rank[x] ? rank[x] : 0;
    }

    public static void clear() { rank = new int[ARRAY_SIZE]; }
}
