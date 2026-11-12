
import java.util.*;

public class Parser {

    private Grammar grammar;
    private SLRTable slr;

    public Parser(Grammar grammar, SLRTable slr) {
        this.grammar = grammar;
        this.slr = slr;
    }

    // Existing CLI method — preserve terminal functionality
    public void parse(String input) {

        List<String> results = parseWithResult(input);

        for (String result : results) {
            System.out.println(result);
        }
    }

    // New method — returns results for the website
    public List<String> parseWithResult(String input) {

        List<String> results = new ArrayList<>();

        if (!grammar.isGrammarValid()) {
            results.add("Please enter a valid grammar first.");
            return results;
        }

        if (slr.hasConflict()) {
            results.add("Cannot parse: Grammar is not SLR(1).");
            return results;
        }

        Map<Integer, Map<String, String>> action =
                slr.getActionTable();

        Map<Integer, Map<String, Integer>> gotoTable =
                slr.getGotoTable();

        if (action.isEmpty()) {
            results.add("Please generate the SLR table first.");
            return results;
        }

        // Convert input into tokens
        List<String> tokens = new ArrayList<>();

        if (!input.trim().isEmpty()) {
            tokens.addAll(Arrays.asList(input.trim().split("\\s+")));
        }

        if (tokens.contains("$")) {
            results.add("Do not enter $ in the input string.");
            return results;
        }

        tokens.add("$");

        Stack<Integer> stateStack = new Stack<>();
        Stack<String> symbolStack = new Stack<>();

        stateStack.push(0);

        int pointer = 0;
        int step = 1;

        results.add("========== SHIFT REDUCE PARSING ==========");
        results.add(String.format("%-8s %-25s %-25s %-15s",
                "STEP", "STATE STACK", "SYMBOL STACK", "ACTION"));

        while (true) {

            int currentState = stateStack.peek();
            String currentToken = tokens.get(pointer);

            Map<String, String> stateActions = action.get(currentState);

            String operation = stateActions == null
                    ? null : stateActions.get(currentToken);

            String stackInfo = String.format(
                    "%-8d %-25s %-25s ",
                    step, stateStack, symbolStack
            );

            step++;

            if (operation == null) {
                results.add(stackInfo + "ERROR");
                results.add("Input string REJECTED!");
                return results;
            }

            // SHIFT operation
            if (operation.startsWith("S")) {

                int nextState = Integer.parseInt(
                        operation.substring(1));

                results.add(stackInfo + "SHIFT " + currentToken);

                symbolStack.push(currentToken);
                stateStack.push(nextState);

                pointer++;
            }

            // REDUCE operation
            else if (operation.startsWith("R")) {

                int ruleIndex = Integer.parseInt(
                        operation.substring(1));

                Production production =
                        grammar.getProductions().get(ruleIndex - 1);

                String lhs = production.getLhs();
                List<String> rhs = production.getRhs();

                int popCount = (rhs.size() == 1
                        && (rhs.get(0).equals("eps")
                        || rhs.get(0).equals("ε")))
                        ? 0 : rhs.size();

                if (stateStack.size() <= popCount
                        || symbolStack.size() < popCount) {

                    results.add(stackInfo + "ERROR");
                    results.add("Input string REJECTED!");
                    return results;
                }

                results.add(stackInfo + "REDUCE using "
                        + lhs + " -> "
                        + (popCount == 0
                        ? "eps" : String.join(" ", rhs)));

                for (int i = 0; i < popCount; i++) {
                    symbolStack.pop();
                    stateStack.pop();
                }

                int topState = stateStack.peek();

                Map<String, Integer> stateGotos =
                        gotoTable.get(topState);

                Integer nextState = stateGotos == null
                        ? null : stateGotos.get(lhs);

                if (nextState == null) {
                    results.add("GOTO entry missing!");
                    results.add("Input string REJECTED!");
                    return results;
                }

                symbolStack.push(lhs);
                stateStack.push(nextState);
            }

            // ACCEPT operation
            else if (operation.equals("ACC")) {

                results.add(stackInfo + "ACCEPT");
                results.add("Input string ACCEPTED!");
                return results;
            }

            else {
                results.add(stackInfo + "INVALID ACTION");
                results.add("Input string REJECTED!");
                return results;
            }
        }
    }
}
