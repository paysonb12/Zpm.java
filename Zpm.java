import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * An interpreter for the Z+- programming language
 * Supports handling of variables, basic control flow, and arithmetic/string operations.
 */
public class Zpm {
    private final Map<String, Object> variables = new HashMap<>();

    public static void main(String[] args) {
        if (args.length == 0 || !args[0].endsWith(".zpm")) {
            System.out.println("Usage: java Zpm <filename.zpm>");
            return;
        }
        Zpm interpreter = new Zpm();
        interpreter.runProgram(args[0]);
    }

    private void runProgram(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                executeLine(line.trim());
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    private void executeLine(String line) {
        if (line.startsWith("FOR")) {
            executeForLoop(line);
        } else if (line.startsWith("PRINT")) {
            executePrintStatement(line);
        } else {
            executeAssignmentStatement(line);
        }
    }

    private void executeForLoop(String line) {
        String[] parts = line.split(" ");
        int loopCount = Integer.parseInt(parts[1]);

        StringBuilder loopBody = new StringBuilder();
        for (int i = 2; !parts[i].equals("ENDFOR"); i++) {
            loopBody.append(parts[i]).append(" ");
        }

        String[] statements = loopBody.toString().trim().split(" ; ");
        for (int i = 0; i < loopCount; i++) {
            for (String statement : statements) {
                executeLine(statement);
            }
        }
    }

    private void executePrintStatement(String line) {
        String varName = line.substring(6, line.length() - 1).trim();
        if (!variables.containsKey(varName)) {
            throw new RuntimeException("Variable " + varName + " is not initialized.");
        }
        System.out.println(varName + " = " + variables.get(varName));
    }

    private void executeAssignmentStatement(String line) {
        String[] parts = line.split(" ");
        String varName = parts[0];
        String operator = parts[1];
        String valueStr = parts[2].replaceAll(";", "");

        //  create of a variable
        if (operator.equals("=")) {
            Object value = determineValue(valueStr);
            variables.put(varName, value);
        } else {
            // Compound assignment 
            if (!variables.containsKey(varName)) {
                throw new RuntimeException("Variable " + varName + " is not initialized.");
            }
            performCompoundAssignment(varName, valueStr, operator);
        }
    }

    private Object determineValue(String valueStr) {
        if (valueStr.matches("-?\\d+")) { 
            return Integer.parseInt(valueStr);
        } else if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
            return valueStr.substring(1, valueStr.length() - 1);
        } else if (variables.containsKey(valueStr)) { 
            return variables.get(valueStr);
        } else {
            throw new RuntimeException("Invalid value or uninitialized variable: " + valueStr);
        }
    }

    private void performCompoundAssignment(String varName, String valueStr, String operator) {
        Object varValue = variables.get(varName);
        Object value = determineValue(valueStr);

        if (varValue instanceof Integer && value instanceof Integer) {
            int varInt = (Integer) varValue;
            int valInt = (Integer) value;
            switch (operator) {
                case "+=":
                    variables.put(varName, varInt + valInt);
                    break;
                case "-=":
                    variables.put(varName, varInt - valInt);
                    break;
                case "*=":
                    variables.put(varName, varInt * valInt);
                    break;
                default:
                    throw new RuntimeException("Unsupported operator for type Integer: " + operator);
            }
        } else if (varValue instanceof String && value instanceof String && operator.equals("+=")) {
            variables.put(varName, varValue.toString() + value.toString());
        } else {
            throw new RuntimeException("Type mismatch or unsupported operation: " + operator);
        }
    }
}
