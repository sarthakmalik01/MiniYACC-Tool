
import java.util.Scanner;

public class MiniYACC {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Grammar grammar = new Grammar();
        FirstFollow ff = new FirstFollow(grammar);
        LR0generator lr0 = new LR0generator(grammar);
        SLRTable slr = new SLRTable(grammar, lr0, ff);
        Parser parser = new Parser(grammar, slr);
        ParserGenerator parserGenerator =
        new ParserGenerator(grammar, slr);
        int choice;

        System.out.println("==============================================");
        System.out.println("          MINI YACC - PARSER GENERATOR");
        System.out.println("==============================================");
        System.out.println("Welcome to Mini YACC!");
        System.out.println("SLR(1) Parser Generator using Java");
        
        do {
            System.out.println("\n============== MAIN MENU ==============");
            System.out.println("1. Enter Grammar");
            System.out.println("2. Display Grammar");
            System.out.println("3. Calculate FIRST and FOLLOW");
            System.out.println("4. Generate LR(0) Items");
            System.out.println("5. Generate SLR Parsing Table");
            System.out.println("6. Parse Input String");
            System.out.println("7. Generate Java Parser File");
            System.out.println("8. Exit");
            System.out.println("=======================================");

            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    grammar.readGrammar(sc);
                    break;

                case 2:
                    grammar.displayGrammar();
                    break;

                case 3:
                    ff.calculateAndDisplay();
                    break;

                case 4:
                    lr0.generateAndDisplayStates();
                    break;

                case 5:
                    slr.generateAndDisplayTable();
                    break;

                case 6:
                    System.out.println("Enter input string (tokens separated by spaces):");
                    String input = sc.nextLine();
                    parser.parse(input);
                    break;

                case 7:
                    parserGenerator.generateParser();
                    break;

                case 8:
                    System.out.println("Thank you for using Mini YACC!");
                    break;

                default:
                    System.out.println("Invalid choice! Please select 1-8.");
            }

        } while (choice != 8);

        sc.close();
    }
}
