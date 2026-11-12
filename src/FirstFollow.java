
import java.util.*;

public class FirstFollow {

    private Grammar grammar;

    private Map<String, Set<String>> first = new LinkedHashMap<>();
    private Map<String, Set<String>> follow = new LinkedHashMap<>();

    private Set<String> nonTerminals = new LinkedHashSet<>();

    private static final String EPSILON = "eps";
    private static final String END = "$";

    public FirstFollow(Grammar grammar) {
        this.grammar = grammar;
    }

    // Initialize FIRST and FOLLOW sets
    private void initialize() {

        first.clear();
        follow.clear();
        nonTerminals.clear();

        for (Production p : grammar.getProductions()) {
            nonTerminals.add(p.getLhs());
        }

        for (String nt : nonTerminals) {
            first.put(nt, new LinkedHashSet<>());
            follow.put(nt, new LinkedHashSet<>());
        }
    }

    // Calculate FIRST sets
    private void calculateFirst() {

        boolean changed;

        do {
            changed = false;

            for (Production p : grammar.getProductions()) {

                String lhs = p.getLhs();
                List<String> rhs = p.getRhs();

                Set<String> lhsFirst = first.get(lhs);

                if (rhs.size() == 1 && rhs.get(0).equals(EPSILON)) {
                    changed |= lhsFirst.add(EPSILON);
                    continue;
                }

                boolean allNullable = true;

                for (String symbol : rhs) {

                    if (!nonTerminals.contains(symbol)) {

                        changed |= lhsFirst.add(symbol);
                        allNullable = false;
                        break;
                    }

                    Set<String> symbolFirst = first.get(symbol);

                    for (String terminal : symbolFirst) {
                        if (!terminal.equals(EPSILON)) {
                            changed |= lhsFirst.add(terminal);
                        }
                    }

                    if (!symbolFirst.contains(EPSILON)) {
                        allNullable = false;
                        break;
                    }
                }

                if (allNullable) {
                    changed |= lhsFirst.add(EPSILON);
                }
            }

        } while (changed);
    }

    // Calculate FIRST of a sequence of symbols
    private Set<String> firstOfSequence(List<String> symbols) {

        Set<String> result = new LinkedHashSet<>();

        if (symbols.isEmpty()) {
            result.add(EPSILON);
            return result;
        }

        boolean allNullable = true;

        for (String symbol : symbols) {

            if (!nonTerminals.contains(symbol)) {
                result.add(symbol);
                allNullable = false;
                break;
            }

            Set<String> symbolFirst = first.get(symbol);

            for (String terminal : symbolFirst) {
                if (!terminal.equals(EPSILON)) {
                    result.add(terminal);
                }
            }

            if (!symbolFirst.contains(EPSILON)) {
                allNullable = false;
                break;
            }
        }

        if (allNullable) {
            result.add(EPSILON);
        }

        return result;
    }

    // Calculate FOLLOW sets
    private void calculateFollow() {

        follow.get(grammar.getStartSymbol()).add(END);

        boolean changed;

        do {
            changed = false;

            for (Production p : grammar.getProductions()) {

                String lhs = p.getLhs();
                List<String> rhs = p.getRhs();

                for (int i = 0; i < rhs.size(); i++) {

                    String symbol = rhs.get(i);

                    if (!nonTerminals.contains(symbol)) {
                        continue;
                    }

                    List<String> beta = new ArrayList<>(
                            rhs.subList(i + 1, rhs.size())
                    );

                    Set<String> betaFirst = firstOfSequence(beta);

                    for (String terminal : betaFirst) {
                        if (!terminal.equals(EPSILON)) {
                            changed |= follow.get(symbol).add(terminal);
                        }
                    }

                    if (betaFirst.contains(EPSILON)) {

                        for (String terminal : follow.get(lhs)) {
                            changed |= follow.get(symbol).add(terminal);
                        }
                    }
                }
            }

        } while (changed);
    }

    // Display FIRST and FOLLOW sets

    public boolean calculateSets() {

        if (!grammar.isGrammarValid()) {
            return false;
        }

        initialize();

        calculateFirst();
        calculateFollow();

        return true;
    }


    public void calculateAndDisplay() {

        if (!calculateSets()) {
            System.out.println(
                    "Cannot calculate FIRST and FOLLOW. "
                    + "Please enter a valid grammar first."
            );
            return;
        }

        System.out.println("\n========== FIRST AND FOLLOW ==========");

        for (String nt : nonTerminals) {

            System.out.println("\nNon-Terminal: " + nt);

            System.out.println(
                    "FIRST(" + nt + ") = " + first.get(nt)
            );

            System.out.println(
                    "FOLLOW(" + nt + ") = " + follow.get(nt)
            );
        }

        System.out.println("\nFIRST and FOLLOW calculated successfully!");
    }


    public Map<String, Set<String>> getFirst() {
        return first;
    }

    public Map<String, Set<String>> getFollow() {
        return follow;
    }
}
