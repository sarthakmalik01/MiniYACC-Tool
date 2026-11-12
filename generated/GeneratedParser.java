import java.util.*;
public class GeneratedParser {
static Map<Integer, Map<String, String>> action = new HashMap<>();
static Map<Integer, Map<String, Integer>> gotoTable = new HashMap<>();
static {
Map<String, String> a0 = new HashMap<>();
a0.put("(", "S4");
a0.put("id", "S5");
action.put(0, a0);
Map<String, String> a1 = new HashMap<>();
a1.put("+", "S6");
a1.put("$", "ACC");
action.put(1, a1);
Map<String, String> a2 = new HashMap<>();
a2.put("*", "S7");
a2.put("$", "R2");
a2.put("+", "R2");
a2.put(")", "R2");
action.put(2, a2);
Map<String, String> a3 = new HashMap<>();
a3.put("$", "R4");
a3.put("+", "R4");
a3.put("*", "R4");
a3.put(")", "R4");
action.put(3, a3);
Map<String, String> a4 = new HashMap<>();
a4.put("(", "S4");
a4.put("id", "S5");
action.put(4, a4);
Map<String, String> a5 = new HashMap<>();
a5.put("$", "R6");
a5.put("+", "R6");
a5.put("*", "R6");
a5.put(")", "R6");
action.put(5, a5);
Map<String, String> a6 = new HashMap<>();
a6.put("(", "S4");
a6.put("id", "S5");
action.put(6, a6);
Map<String, String> a7 = new HashMap<>();
a7.put("(", "S4");
a7.put("id", "S5");
action.put(7, a7);
Map<String, String> a8 = new HashMap<>();
a8.put("+", "S6");
a8.put(")", "S11");
action.put(8, a8);
Map<String, String> a9 = new HashMap<>();
a9.put("*", "S7");
a9.put("$", "R1");
a9.put("+", "R1");
a9.put(")", "R1");
action.put(9, a9);
Map<String, String> a10 = new HashMap<>();
a10.put("$", "R3");
a10.put("+", "R3");
a10.put("*", "R3");
a10.put(")", "R3");
action.put(10, a10);
Map<String, String> a11 = new HashMap<>();
a11.put("$", "R5");
a11.put("+", "R5");
a11.put("*", "R5");
a11.put(")", "R5");
action.put(11, a11);
Map<String, Integer> g0 = new HashMap<>();
g0.put("E", 1);
g0.put("T", 2);
g0.put("F", 3);
gotoTable.put(0, g0);
Map<String, Integer> g1 = new HashMap<>();
gotoTable.put(1, g1);
Map<String, Integer> g2 = new HashMap<>();
gotoTable.put(2, g2);
Map<String, Integer> g3 = new HashMap<>();
gotoTable.put(3, g3);
Map<String, Integer> g4 = new HashMap<>();
g4.put("E", 8);
g4.put("T", 2);
g4.put("F", 3);
gotoTable.put(4, g4);
Map<String, Integer> g5 = new HashMap<>();
gotoTable.put(5, g5);
Map<String, Integer> g6 = new HashMap<>();
g6.put("T", 9);
g6.put("F", 3);
gotoTable.put(6, g6);
Map<String, Integer> g7 = new HashMap<>();
g7.put("F", 10);
gotoTable.put(7, g7);
Map<String, Integer> g8 = new HashMap<>();
gotoTable.put(8, g8);
Map<String, Integer> g9 = new HashMap<>();
gotoTable.put(9, g9);
Map<String, Integer> g10 = new HashMap<>();
gotoTable.put(10, g10);
Map<String, Integer> g11 = new HashMap<>();
gotoTable.put(11, g11);
}
static String[] lhs = {
"E",
"E",
"T",
"T",
"F",
"F",
};
static String[][] rhs = {
{"E","+","T",},
{"T",},
{"T","*","F",},
{"F",},
{"(","E",")",},
{"id",},
};
public static void parse(String input) {
List<String> tokens = new ArrayList<>();
if (!input.trim().isEmpty()) tokens.addAll(Arrays.asList(input.trim().split("\\s+")));
if (tokens.contains("$")) { System.out.println("Do not enter $"); return; }
tokens.add("$");
Stack<Integer> states = new Stack<>();
Stack<String> symbols = new Stack<>();
states.push(0);
int pointer = 0;
while (true) {
int state = states.peek();
String token = tokens.get(pointer);
String operation = action.get(state).get(token);
if (operation == null) {
System.out.println("Input string REJECTED!");
return;
}
if (operation.startsWith("S")) {
int next = Integer.parseInt(operation.substring(1));
symbols.push(token);
states.push(next);
pointer++;
} else if (operation.startsWith("R")) {
int rule = Integer.parseInt(operation.substring(1)) - 1;
int count = rhs[rule].length;
if (states.size() <= count || symbols.size() < count) { System.out.println("Input string REJECTED!"); return; }
for (int i = 0; i < count; i++) {
symbols.pop();
states.pop();
}
String left = lhs[rule];
Integer next = gotoTable.get(states.peek()).get(left);
if (next == null) { System.out.println("Input string REJECTED!"); return; }
symbols.push(left);
states.push(next);
} else if (operation.equals("ACC")) {
System.out.println("Input string ACCEPTED!");
return;
} else {
System.out.println("Invalid parsing action!");
return;
}
}
}
public static void main(String[] args) {
Scanner sc = new Scanner(System.in);
System.out.println("Enter input string:");
String input = sc.nextLine();
parse(input);
}
}
