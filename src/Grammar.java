
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Grammar {

    private List<Production> productions = new ArrayList<>();
    private String startSymbol;
    private boolean grammarValid = false;

    public void readGrammar(Scanner sc) {

        productions.clear();
        grammarValid = false;

        System.out.println("\n========== GRAMMAR INPUT ==========");
        System.out.println("Enter productions one by one.");
        System.out.println("Use spaces between grammar symbols.");
        System.out.println("Enter # when finished.\n");

        while (true) {

            System.out.print("Production: ");
            String line = sc.nextLine().trim();

            if (line.equals("#")) {
                break;
            }

            if (line.isEmpty() || !line.contains("->")) {
                System.out.println("Invalid production format!");
                continue;
            }

            String[] parts = line.split("->", -1);

            if (parts.length != 2) {
                System.out.println("Invalid production! Use one arrow.");
                continue;
            }

            String lhs = parts[0].trim();
            String rhs = parts[1].trim();

            if (lhs.isEmpty() || lhs.contains(" ")) {
                System.out.println("Invalid left-hand side!");
                continue;
            }

            if (rhs.isEmpty()) {
                System.out.println("Right-hand side cannot be empty!");
                continue;
            }

            List<String> symbols =
                    Arrays.asList(rhs.split("\\s+"));

            if ((symbols.contains("ε") || symbols.contains("eps"))
                    && symbols.size() > 1) {
                System.out.println("Epsilon must appear alone!");
                continue;
            }

            productions.add(new Production(lhs, symbols));

            System.out.println("Production added!");
        }

        if (productions.isEmpty()) {
            System.out.println("No productions entered!");
            return;
        }

        System.out.print("\nEnter start symbol: ");
        startSymbol = sc.nextLine().trim();

        validateGrammar();
    }

    public void validateGrammar() {

        System.out.println("\n========== GRAMMAR VALIDATION ==========");

        Set<String> nonTerminals = new HashSet<>();

        for (Production p : productions) {
            nonTerminals.add(p.getLhs());
        }

        boolean valid = true;

        // Check start symbol
        if (!nonTerminals.contains(startSymbol)) {

            System.out.println(
                    "ERROR: Start symbol is not defined!"
            );

            valid = false;
        }

        // Check undefined non-terminals
        for (Production p : productions) {

            for (String symbol : p.getRhs()) {

                if (!symbol.equals("ε")
                        && !symbol.isEmpty()
                        && Character.isUpperCase(symbol.charAt(0))
                        && !nonTerminals.contains(symbol)) {

                    System.out.println(
                            "ERROR: Undefined non-terminal: " + symbol
                    );

                    valid = false;
                }
            }
        }

        grammarValid = valid;

        if (grammarValid) {

            System.out.println("Grammar validation successful!");
            System.out.println("Total Productions: " + productions.size());
            System.out.println("Start Symbol: " + startSymbol);

        } else {

            System.out.println("Grammar validation failed!");
            System.out.println("Please correct the grammar.");
        }
    }

    public void displayGrammar() {

        if (productions.isEmpty()) {
            System.out.println("No grammar available.");
            return;
        }

        System.out.println("\n========== STORED GRAMMAR ==========");

        for (Production p : productions) {
            p.display();
        }

        System.out.println("\nStart Symbol: " + startSymbol);
        System.out.println("Grammar Status: "
                + (grammarValid ? "VALID" : "INVALID"));
    }

    public boolean isGrammarValid() {
        return grammarValid;
    }

    public List<Production> getProductions() {
        return productions;
    }

    public String getStartSymbol() {
        return startSymbol;
    }

public boolean loadGrammar(List<String> lines, String start) {

    productions.clear();
    grammarValid = false;

    startSymbol = start.trim();

    for (String line : lines) {

        line = line.trim();

        if (line.isEmpty()) {
            continue;
        }

        if (!line.contains("->")) {
            return false;
        }

        String[] parts = line.split("->", -1);

        if (parts.length != 2) {
            return false;
        }

        String lhs = parts[0].trim();
        String rhs = parts[1].trim();

        if (lhs.isEmpty() || lhs.contains(" ") || rhs.isEmpty()) {
            return false;
        }

        List<String> symbols =
                Arrays.asList(rhs.split("\\s+"));

        if ((symbols.contains("ε") || symbols.contains("eps"))
                && symbols.size() > 1) {
            return false;
        }

        productions.add(new Production(lhs, symbols));
    }

    if (productions.isEmpty()) {
        return false;
    }

    validateGrammar();

    return grammarValid;
}

}

