
import java.util.*;

public class LR0generator {

    // Represents a production internally
    private static class Rule {
        String lhs;
        List<String> rhs;

        Rule(String lhs, List<String> rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }
    }

    // Represents an LR(0) item
    private static class Item {
        int ruleIndex;
        int dotPosition;

        Item(int ruleIndex, int dotPosition) {
            this.ruleIndex = ruleIndex;
            this.dotPosition = dotPosition;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Item)) return false;

            Item other = (Item) obj;

            return ruleIndex == other.ruleIndex
                    && dotPosition == other.dotPosition;
        }

        @Override
        public int hashCode() {
            return Objects.hash(ruleIndex, dotPosition);
        }
    }

    private Grammar grammar;

    private List<Rule> rules = new ArrayList<>();

    private Set<String> nonTerminals = new LinkedHashSet<>();
    private Set<String> symbols = new LinkedHashSet<>();

    private List<Set<Item>> states = new ArrayList<>();

    private Map<Integer, Map<String, Integer>> transitions =
            new LinkedHashMap<>();

    private String augmentedStart;

    public LR0generator(Grammar grammar) {
        this.grammar = grammar;
    }

    // Prepare augmented grammar
    private void prepareGrammar() {

        rules.clear();
        nonTerminals.clear();
        symbols.clear();

        String start = grammar.getStartSymbol();

        augmentedStart = start + "'";

        boolean collision = true;

        while (collision) {
            collision = false;

            for (Production p : grammar.getProductions()) {
                if (p.getLhs().equals(augmentedStart)) {
                    augmentedStart += "'";
                    collision = true;
                    break;
                }
            }
        }

        nonTerminals.add(augmentedStart);

        for (Production p : grammar.getProductions()) {
            nonTerminals.add(p.getLhs());
        }

        // Add augmented production
        rules.add(new Rule(
                augmentedStart,
                Arrays.asList(start)
        ));

        // Copy original productions
        for (Production p : grammar.getProductions()) {

            List<String> rhs = new ArrayList<>(p.getRhs());

            // Epsilon is represented by an empty RHS internally
            if (rhs.size() == 1 && rhs.get(0).equals("eps")) {
                rhs.clear();
            }

            rules.add(new Rule(p.getLhs(), rhs));
        }

        // Collect grammar symbols
        for (Rule rule : rules) {

            for (String symbol : rule.rhs) {
                symbols.add(symbol);
            }
        }

        symbols.addAll(nonTerminals);
    }

    // Find the symbol immediately after the dot
    private String symbolAfterDot(Item item) {

        Rule rule = rules.get(item.ruleIndex);

        if (item.dotPosition >= rule.rhs.size()) {
            return null;
        }

        return rule.rhs.get(item.dotPosition);
    }

    // Closure operation
    private Set<Item> closure(Set<Item> items) {

        Set<Item> result = new LinkedHashSet<>(items);

        boolean changed;

        do {
            changed = false;

            List<Item> currentItems = new ArrayList<>(result);

            for (Item item : currentItems) {

                String symbol = symbolAfterDot(item);

                if (symbol != null && nonTerminals.contains(symbol)) {

                    for (int i = 0; i < rules.size(); i++) {

                        if (rules.get(i).lhs.equals(symbol)) {

                            Item newItem = new Item(i, 0);

                            if (result.add(newItem)) {
                                changed = true;
                            }
                        }
                    }
                }
            }

        } while (changed);

        return result;
    }

    // GOTO operation
    private Set<Item> goTo(Set<Item> items, String symbol) {

        Set<Item> movedItems = new LinkedHashSet<>();

        for (Item item : items) {

            String nextSymbol = symbolAfterDot(item);

            if (symbol.equals(nextSymbol)) {

                movedItems.add(
                        new Item(
                                item.ruleIndex,
                                item.dotPosition + 1
                        )
                );
            }
        }

        if (movedItems.isEmpty()) {
            return movedItems;
        }

        return closure(movedItems);
    }

    // Generate canonical LR(0) item sets
    public void generateStates() {

        if (!grammar.isGrammarValid()) {
            System.out.println(
                    "Please enter a valid grammar first."
            );
            return;
        }

        prepareGrammar();

        states.clear();
        transitions.clear();

        Set<Item> initialItems = new LinkedHashSet<>();

        initialItems.add(new Item(0, 0));

        states.add(closure(initialItems));

        // Generate states until no new state is found
        for (int i = 0; i < states.size(); i++) {

            Set<Item> currentState = states.get(i);

            Map<String, Integer> stateTransitions =
                    new LinkedHashMap<>();

            for (String symbol : symbols) {

                Set<Item> nextState = goTo(currentState, symbol);

                if (nextState.isEmpty()) {
                    continue;
                }

                int stateIndex = states.indexOf(nextState);

                if (stateIndex == -1) {

                    states.add(nextState);
                    stateIndex = states.size() - 1;
                }

                stateTransitions.put(symbol, stateIndex);
            }

            transitions.put(i, stateTransitions);
        }
    }

    // Display states only when using the CLI
    public void generateAndDisplayStates() {
        generateStates();

        if (grammar.isGrammarValid()) {
            displayStates();
        }
}

    // Display LR(0) item sets
    private void displayStates() {

        System.out.println("\n========== CANONICAL LR(0) ITEMS ==========");

        for (int i = 0; i < states.size(); i++) {

            System.out.println("\nI" + i + ":");

            for (Item item : states.get(i)) {

                Rule rule = rules.get(item.ruleIndex);

                List<String> rhs = new ArrayList<>(rule.rhs);

                rhs.add(item.dotPosition, ".");

                System.out.println(
                        rule.lhs + " -> " + String.join(" ", rhs)
                );
            }

            Map<String, Integer> stateTransitions =
                    transitions.get(i);

            System.out.println("Transitions:");

            if (stateTransitions.isEmpty()) {
                System.out.println("  None");
            } else {
                for (Map.Entry<String, Integer> entry
                        : stateTransitions.entrySet()) {

                    System.out.println(
                            "  " + entry.getKey()
                            + " -> I" + entry.getValue()
                    );
                }
            }
        }

                System.out.println("\nTotal LR(0) States: " + states.size());
        System.out.println("LR(0) item generation completed!");
    }

    // Getter: Returns all LR(0) states
    public List<List<int[]>> getStateData() {

        List<List<int[]>> result = new ArrayList<>();

        for (Set<Item> state : states) {

            List<int[]> items = new ArrayList<>();

            for (Item item : state) {
                items.add(new int[]{
                    item.ruleIndex,
                    item.dotPosition
                });
            }

            result.add(items);
        }

        return result;
    }

    // Getter: Returns the left-hand side of all rules
    public List<String> getRuleLHS() {

        List<String> result = new ArrayList<>();

        for (Rule rule : rules) {
            result.add(rule.lhs);
        }

        return result;
    }

    // Getter: Returns the right-hand side of all rules
    public List<List<String>> getRuleRHS() {

        List<List<String>> result = new ArrayList<>();

        for (Rule rule : rules) {
            result.add(new ArrayList<>(rule.rhs));
        }

        return result;
    }

    // Getter: Returns all state transitions
    public Map<Integer, Map<String, Integer>> getTransitions() {
        return transitions;
    }

    // Getter: Returns the augmented start symbol
    public String getAugmentedStart() {
        return augmentedStart;
    }
}
    
