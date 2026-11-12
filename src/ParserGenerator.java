
import java.io.*;
import java.util.*;

public class ParserGenerator {

    private Grammar grammar;
    private SLRTable slr;

    public ParserGenerator(Grammar grammar, SLRTable slr) {
        this.grammar = grammar;
        this.slr = slr;
    }

    // Generate an independent Java parser file
    
    public File generateParserFile() throws IOException {

        if (!grammar.isGrammarValid()) {
            throw new IllegalStateException(
                    "Please enter a valid grammar first."
            );
        }

        if (slr.hasConflict()) {
            throw new IllegalStateException(
                    "Cannot generate parser: Grammar is not SLR(1)."
            );
        }

        if (slr.getActionTable().isEmpty()) {
            throw new IllegalStateException(
                    "Please generate the SLR table first."
            );
        }

        generateParser();

        File file = new File("generated/GeneratedParser.java");

        if (!file.exists()) {
            throw new IOException("Parser file was not generated.");
        }

        return file;
    }

    public void generateParser() {

        if (!grammar.isGrammarValid()) {
            System.out.println("Please enter a valid grammar first.");
            return;
        }

        if (slr.hasConflict()) {
            System.out.println("Cannot generate parser: Grammar is not SLR(1).");
            return;
        }

        Map<Integer, Map<String, String>> action =
                slr.getActionTable();

        Map<Integer, Map<String, Integer>> gotoTable =
                slr.getGotoTable();

        if (action.isEmpty()) {
            System.out.println("Please generate the SLR table first.");
            return;
        }

        try {

            File directory = new File("generated");

            if (!directory.exists()) {
                directory.mkdirs();
            }

            PrintWriter writer = new PrintWriter(
                    new File(directory, "GeneratedParser.java")
            );

            writer.println("import java.util.*;");
            writer.println("public class GeneratedParser {");

            // Embed ACTION table
            writer.println("static Map<Integer, Map<String, String>> action = new HashMap<>();");

            // Embed GOTO table
            writer.println("static Map<Integer, Map<String, Integer>> gotoTable = new HashMap<>();");

            writer.println("static {");

            for (int state : action.keySet()) {

                writer.println("Map<String, String> a" + state + " = new HashMap<>();");

                for (Map.Entry<String, String> entry :
                        action.get(state).entrySet()) {

                    writer.println("a" + state + ".put(\""
                            + entry.getKey() + "\", \""
                            + entry.getValue() + "\");");
                }

                writer.println("action.put(" + state + ", a" + state + ");");
            }

            for (int state : gotoTable.keySet()) {

                writer.println("Map<String, Integer> g" + state + " = new HashMap<>();");

                for (Map.Entry<String, Integer> entry :
                        gotoTable.get(state).entrySet()) {

                    writer.println("g" + state + ".put(\""
                            + entry.getKey() + "\", "
                            + entry.getValue() + ");");
                }

                writer.println("gotoTable.put(" + state + ", g" + state + ");");
            }

            writer.println("}");

            // Embed production information
            writer.println("static String[] lhs = {");

            List<Production> productions = grammar.getProductions();

            for (Production p : productions) {
                writer.println("\"" + p.getLhs() + "\",");
            }

            writer.println("};");

            writer.println("static String[][] rhs = {");

            for (Production p : productions) {

                List<String> symbols = p.getRhs();

                if (symbols.size() == 1
                        && symbols.get(0).equals("eps")) {

                    writer.println("{},");

                } else {

                    writer.print("{");

                    for (String symbol : symbols) {
                        writer.print("\"" + symbol + "\",");
                    }

                    writer.println("},");
                }
            }

            writer.println("};");

            // Independent parser logic
            writer.println("public static void parse(String input) {");

            writer.println("List<String> tokens = new ArrayList<>();");
            writer.println("if (!input.trim().isEmpty()) tokens.addAll(Arrays.asList(input.trim().split(\"\\\\s+\")));");
            writer.println("if (tokens.contains(\"$\")) { System.out.println(\"Do not enter $\"); return; }");
            writer.println("tokens.add(\"$\");");

            writer.println("Stack<Integer> states = new Stack<>();");
            writer.println("Stack<String> symbols = new Stack<>();");
            writer.println("states.push(0);");

            writer.println("int pointer = 0;");

            writer.println("while (true) {");

            writer.println("int state = states.peek();");
            writer.println("String token = tokens.get(pointer);");

            writer.println("String operation = action.get(state).get(token);");

            writer.println("if (operation == null) {");
            writer.println("System.out.println(\"Input string REJECTED!\");");
            writer.println("return;");
            writer.println("}");

            writer.println("if (operation.startsWith(\"S\")) {");

            writer.println("int next = Integer.parseInt(operation.substring(1));");
            writer.println("symbols.push(token);");
            writer.println("states.push(next);");
            writer.println("pointer++;");

            writer.println("} else if (operation.startsWith(\"R\")) {");

            writer.println("int rule = Integer.parseInt(operation.substring(1)) - 1;");
            writer.println("int count = rhs[rule].length;");

            writer.println("if (states.size() <= count || symbols.size() < count) { System.out.println(\"Input string REJECTED!\"); return; }");

            writer.println("for (int i = 0; i < count; i++) {");
            writer.println("symbols.pop();");
            writer.println("states.pop();");
            writer.println("}");

            writer.println("String left = lhs[rule];");
            writer.println("Integer next = gotoTable.get(states.peek()).get(left);");

            writer.println("if (next == null) { System.out.println(\"Input string REJECTED!\"); return; }");

            writer.println("symbols.push(left);");
            writer.println("states.push(next);");

            writer.println("} else if (operation.equals(\"ACC\")) {");

            writer.println("System.out.println(\"Input string ACCEPTED!\");");
            writer.println("return;");

            writer.println("} else {");
            writer.println("System.out.println(\"Invalid parsing action!\");");
            writer.println("return;");
            writer.println("}");

            writer.println("}");
            writer.println("}");

            // Main method
            writer.println("public static void main(String[] args) {");
            writer.println("Scanner sc = new Scanner(System.in);");
            writer.println("System.out.println(\"Enter input string:\");");
            writer.println("String input = sc.nextLine();");
            writer.println("parse(input);");
            writer.println("}");
            writer.println("}");

            writer.close();

            System.out.println(
                    "\nIndependent Java parser generated successfully!"
            );

            System.out.println(
                    "File location: generated/GeneratedParser.java"
            );

        } catch (IOException e) {

            System.out.println(
                    "Error generating parser: " + e.getMessage()
            );
        }
    }
}
