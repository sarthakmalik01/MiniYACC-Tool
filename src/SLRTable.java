
import java.util.*;

public class SLRTable {

    private Grammar grammar;
    private LR0generator lr0;
    private FirstFollow ff;

    private Map<Integer, Map<String, String>> action =
            new LinkedHashMap<>();

    private Map<Integer, Map<String, Integer>> gotoTable =
            new LinkedHashMap<>();

    private List<String> terminals = new ArrayList<>();
    private List<String> nonTerminals = new ArrayList<>();

    private boolean conflictFound = false;

    public SLRTable(Grammar grammar, LR0generator lr0,
                    FirstFollow ff) {

        this.grammar = grammar;
        this.lr0 = lr0;
        this.ff = ff;
    }

    // Add an ACTION entry and detect conflicts
   
    private void addAction(int state, String symbol, String value) {

        Map<String, String> row = action.get(state);

        if (row.containsKey(symbol)
                && !row.get(symbol).equals(value)) {

            conflictFound = true;
            return;
        }

        row.put(symbol, value);
    }


    // Generate SLR parsing table
    public void generateTable() {

        if (!grammar.isGrammarValid()) {
            System.out.println("Please enter a valid grammar first.");
            return;
        }

        // Generate LR(0) states if not generated yet
        if (lr0.getStateData().isEmpty()) {
            System.out.println("Generating LR(0) states first...");
            lr0.generateStates();
        }

        // Calculate FOLLOW sets
        if (!ff.calculateSets()) {
            System.out.println("Failed to calculate FOLLOW sets.");
            return;
        }

        Map<String, Set<String>> follow = ff.getFollow();

        List<List<int[]>> states = lr0.getStateData();
        List<String> ruleLHS = lr0.getRuleLHS();
        List<List<String>> ruleRHS = lr0.getRuleRHS();

        String augmentedStart = lr0.getAugmentedStart();

        Set<String> ntSet = new LinkedHashSet<>();

        for (Production p : grammar.getProductions()) {
            ntSet.add(p.getLhs());
        }

        nonTerminals.clear();
        nonTerminals.addAll(ntSet);

        // Identify terminals
        Set<String> terminalSet = new LinkedHashSet<>();

        for (Production p : grammar.getProductions()) {

            for (String symbol : p.getRhs()) {

                if (!symbol.equals("eps")
                        && !ntSet.contains(symbol)) {

                    terminalSet.add(symbol);
                }
            }
        }

        terminals.clear();
        terminals.addAll(terminalSet);
        terminals.add("$");

        action.clear();
        gotoTable.clear();

        conflictFound = false;

        // Initialize table rows
        for (int i = 0; i < states.size(); i++) {

            action.put(i, new LinkedHashMap<>());
            gotoTable.put(i, new LinkedHashMap<>());
        }

        Map<Integer, Map<String, Integer>> transitions =
                lr0.getTransitions();

        // Fill SHIFT and GOTO entries
        for (int state = 0; state < states.size(); state++) {

            Map<String, Integer> row = transitions.get(state);

            if (row == null) continue;

            for (Map.Entry<String, Integer> entry
                    : row.entrySet()) {

                String symbol = entry.getKey();
                int nextState = entry.getValue();

                if (ntSet.contains(symbol)) {

                    gotoTable.get(state).put(
                            symbol, nextState
                    );

                } else {

                    addAction(
                            state,
                            symbol,
                            "S" + nextState
                    );
                }
            }
        }

        // Fill REDUCE and ACCEPT entries
        for (int state = 0; state < states.size(); state++) {

            for (int[] item : states.get(state)) {

                int ruleIndex = item[0];
                int dotPosition = item[1];

                List<String> rhs = ruleRHS.get(ruleIndex);

                // Check whether dot is at the end
                if (dotPosition == rhs.size()) {

                    // Augmented production completed
                    if (ruleIndex == 0) {

                        addAction(state, "$", "ACC");

                    } else {

                        String lhs = ruleLHS.get(ruleIndex);

                        Set<String> followSet = follow.get(lhs);

                        if (followSet == null) continue;

                        for (String terminal : followSet) {

                            addAction(
                                    state,
                                    terminal,
                                    "R" + ruleIndex
                            );
                        }
                    }
                }
            }
        }

        

       
    }

    // Display ACTION and GOTO tables
    private void displayTable() {

        System.out.println("\n========== SLR PARSING TABLE ==========");

        System.out.printf("%-8s", "STATE");

        for (String t : terminals) {
            System.out.printf("%-8s", t);
        }

        System.out.print("|| ");

        for (String nt : nonTerminals) {
            System.out.printf("%-8s", nt);
        }

        System.out.println();

        int width = 8 * (1 + terminals.size()
                + nonTerminals.size());

        System.out.println("-".repeat(width + 3));

        for (int state = 0; state < action.size(); state++) {

            System.out.printf("%-8d", state);

            for (String t : terminals) {

                String value = action.get(state).getOrDefault(
                        t, "-"
                );

                System.out.printf("%-8s", value);
            }

            System.out.print("|| ");

            for (String nt : nonTerminals) {

                String value = gotoTable.get(state).containsKey(nt)
                        ? String.valueOf(gotoTable.get(state).get(nt))
                        : "-";

                System.out.printf("%-8s", value);
            }

            System.out.println();
        }

                System.out.println("========================================");
    }

    // Getter for ACTION table
    public Map<Integer, Map<String, String>> getActionTable() {
        return action;
    }

    // Getter for GOTO table
    public Map<Integer, Map<String, Integer>> getGotoTable() {
        return gotoTable;
    }


    // Check whether conflicts were found
    public boolean hasConflict() {
        return conflictFound;
    }

    // Getter for terminal symbols
    public List<String> getTerminals() {
        return terminals;
    }

    // Getter for non-terminal symbols
    public List<String> getNonTerminals() {
        return nonTerminals;
    }

    public void generateAndDisplayTable() {

        generateTable();

        if (!grammar.isGrammarValid()) {
            return;
        }

        displayTable();

        if (conflictFound) {

            System.out.println(
                    "\nGrammar is NOT SLR(1) because conflicts exist."
            );

        } else {

            System.out.println(
                    "\nSLR parsing table generated successfully!"
            );
        }
    }
}

