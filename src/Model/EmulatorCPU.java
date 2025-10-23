package Model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static Model.InstructionStages.*;

public class EmulatorCPU {
    public static boolean IS_HALT = true;
    public static final int MEMORY_SIZE = 256;
    public static InstructionStages CURRENT_STAGE = FETCH;
    public static InstructionStages PREVIOUS_STAGE = FETCH;

    public static boolean ZERO_FLAG = true;
    public static boolean SIGN_FLAG = false;
    public static boolean CARRY_FLAG = false;

    private static final HashMap<Integer, Operations> OPERATIONS = new HashMap<>(Map.of(
            0, Operations.LOAD,
            1, Operations.ADD,
            2, Operations.STORE,
            3, Operations.HALT
    ));
    private HashMap<String, String> decodeInstruction = new HashMap<>();

    private final String[] registers;
    private String programCounter;
    private String instructionRegister;

    private final String[] commandMemory;
    private final String[] dataMemory;

    public static String InstructionRegisterComment(String command) {
        Operations operation = OPERATIONS.get(
                (Integer.parseInt(command, 16) & Integer.parseUnsignedInt("FF000000", 16)) >> 24
        );
        int[] operands = new int[] {
                (Integer.parseInt(command, 16) & Integer.parseUnsignedInt("00FF0000", 16)) >> 16,
                (Integer.parseInt(command, 16) & Integer.parseUnsignedInt("0000FF00", 16)) >> 8,
                (Integer.parseInt(command, 16) & Integer.parseUnsignedInt("000000FF", 16))
        };

        return switch (operation) {
            case HALT -> operation.toString();
            case ADD -> String.format(
                    "%s %s %s %s",
                    operation,
                    operands[0] == 0 ? "ACC" : "R1",
                    operands[1] == 0 ? "ACC" : "R1",
                    operands[2] == 0 ? "ACC" : "R1"
            );
            default -> String.format(
                    "%s %s %s",
                    operation,
                    operands[0] == 0 ? "ACC" : "R1",
                    String.format("%02X", operands[1])
            );
        };
    }

    public EmulatorCPU() {
        commandMemory = new String[MEMORY_SIZE];
        dataMemory = new String[MEMORY_SIZE];

        registers = new String[] { Integer.toHexString(0), Integer.toHexString(0) };
        programCounter = Integer.toHexString(0);
        instructionRegister = Integer.toHexString(0);

        Arrays.fill(commandMemory, Integer.toHexString(0));
        Arrays.fill(dataMemory, Integer.toHexString(0));
    }

    public HashMap<String, String> setEmulatorMemory(String[] dataMemory, String[] commandMemory) {
        System.arraycopy(dataMemory, 0, this.dataMemory, 0, dataMemory.length);

        System.arraycopy(commandMemory, 0, this.commandMemory, 0, commandMemory.length);

        return new HashMap<>(Map.of(
                "Data", Arrays.toString(this.dataMemory),
                "Commands", Arrays.toString(this.commandMemory)
        ));
    }

    public HashMap<String, String> executeInstruction() {
        HashMap<String, String> executionStatus;

        if (IS_HALT) {
            executionStatus = new HashMap<>(Map.of("Comment", "Выполнение операций завершено"));

            return executionStatus;
        }

        if (CURRENT_STAGE == FETCH) {
            executionStatus = executeStageFetch();

            if (Objects.equals(executionStatus.get("State"), "Error"))
                return executionStatus;
        }

        if (CURRENT_STAGE == DECODE) {
            executionStatus = executeStageDecode();

            if (Objects.equals(executionStatus.get("State"), "Error"))
                return executionStatus;
        }

        PREVIOUS_STAGE = CURRENT_STAGE;

        executionStatus = executeStageExecute();

        if (Objects.equals(executionStatus.get("State"), "Error"))
            return executionStatus;

        executionStatus.put("PC", programCounter);
        executionStatus.put("IR", instructionRegister);
        executionStatus.put("registers", Arrays.toString(registers));
        executionStatus.put("Data", Arrays.toString(this.dataMemory));
        executionStatus.put("Commands", Arrays.toString(this.commandMemory));
        executionStatus.put("Comment", String.format(
                "Выполнена команда: %s %s",
                OPERATIONS.get(Integer.parseInt(decodeInstruction.get("operation"), 16)),
                decodeInstruction.get("operands").replaceAll("[, \\[\\]]", "")
        ));

        if (PREVIOUS_STAGE != FETCH) {
            int operation = Integer.parseInt(decodeInstruction.get("operation"), 16);
            int operand = Integer.parseInt(
                    decodeInstruction.get("operands").replaceAll("[\\[\\]]", "").split(", ")[1],
                    16
            );

            if (OPERATIONS.get(operation) == Operations.LOAD)
                executionStatus.put("dataBus", dataMemory[operand]);
        }

        if (IS_HALT)
            executionStatus.put("Comment", executionStatus.get("Comment") + "\n\nВыполнение программы завершено");

        return executionStatus;
    }

    public HashMap<String, String> executeInstructionStage() {
        HashMap<String, String> executionStatus = new HashMap<>();

        if (IS_HALT) {
            executionStatus = new HashMap<>(Map.of("Comment", "Выполнение операций завершено"));

            return executionStatus;
        }

        PREVIOUS_STAGE = CURRENT_STAGE;

        switch(CURRENT_STAGE) {
            case FETCH -> executionStatus = executeStageFetch();
            case DECODE -> executionStatus = executeStageDecode();
            case EXECUTE -> executionStatus = executeStageExecute();
        }

        if (Objects.equals(executionStatus.get("State"), "Error"))
            return executionStatus;

        executionStatus.put("PC", programCounter);
        executionStatus.put("IR", instructionRegister);
        executionStatus.put("registers", Arrays.toString(registers));
        executionStatus.put("Data", Arrays.toString(dataMemory));
        executionStatus.put("Commands", Arrays.toString(commandMemory));
        executionStatus.put("Comment", String.format("Выполнен этап %s", PREVIOUS_STAGE));

        if (PREVIOUS_STAGE != FETCH) {
            int operation = Integer.parseInt(decodeInstruction.get("operation"), 16);
            int operand = Integer.parseInt(
                    decodeInstruction.get("operands").replaceAll("[\\[\\]]", "").split(", ")[1],
                    16
            );

            if (OPERATIONS.get(operation) == Operations.LOAD)
                executionStatus.put("dataBus", dataMemory[operand]);
        }

        if (IS_HALT)
            executionStatus.put("Comment", executionStatus.get("Comment") + "\n\nВыполнение программы завершено");

        return executionStatus;
    }

    private HashMap<String, String> executeStageFetch() {
        HashMap<String, String> result = new HashMap<>(Map.of("State", "OK", "Comment", ""));

        if (Integer.parseInt(programCounter, 16) < 0 || Integer.parseInt(programCounter, 16) >= MEMORY_SIZE) {
            result.put("State", "Error");
            result.put("Comment", "Счетчик выходит за пределы памяти");

            return result;
        }

        instructionRegister = readCommandMemory(programCounter);
        if (instructionRegister == null) {
            result.put("State", "Error");
            result.put("Comment", "Ошибка чтении памяти команд");

            instructionRegister = Integer.toHexString(0);

            return result;
        }

        decodeInstruction = new HashMap<>();

        CURRENT_STAGE = DECODE;

        return result;
    }

    private HashMap<String, String> executeStageDecode() {
        HashMap<String, String> result = new HashMap<>(Map.of("State", "OK", "Comment", ""));

        int instruction = Integer.parseInt(instructionRegister, 16);

        String operationCode = String.format("%02X", (instruction & Integer.parseUnsignedInt("FF000000", 16)) >> 24);

        String[] operands = new String[] {
                String.format("%02X", (instruction & Integer.parseUnsignedInt("00FF0000", 16)) >> 16),
                String.format("%02X", (instruction & Integer.parseUnsignedInt("0000FF00", 16)) >> 8),
                String.format("%02X", (instruction & Integer.parseUnsignedInt("000000FF", 16)))
        };

        decodeInstruction = new HashMap<>(Map.of(
                "operation", operationCode,
                "operands", Arrays.toString(operands)
        ));

        CURRENT_STAGE = EXECUTE;

        return result;
    }

    private HashMap<String, String> executeStageExecute() {
        HashMap<String, String> result = new HashMap<>(Map.of("State", "OK", "Comment", ""));

        Operations operation = OPERATIONS.get(Integer.parseInt(decodeInstruction.get("operation"), 16));

        String[] operands = decodeInstruction.get("operands").replaceAll("[\\[\\]]", "").split(", ");

        switch (operation) {
            case Operations.LOAD -> {
                String data = readDataMemory(operands[1]);
                if (data == null) {
                    result.put("State", "Error");
                    result.put("Comment", "Ошибка чтения памяти данных");

                    break;
                }

                registers[Integer.parseInt(operands[0], 16)] = data;

                changeFlags(Integer.parseInt(registers[0], 16), false);
            }
            case Operations.ADD -> {
                int offset = Integer.parseInt(String.format("0000%s000", CARRY_FLAG ? "8" : "0"), 16);

                if (SIGN_FLAG)
                    offset = -offset - 1;

                short number1 = (short) Integer.parseInt(registers[Integer.parseInt(operands[0], 16)], 16);
                short number2 = (short) Integer.parseInt(registers[Integer.parseInt(operands[1], 16)], 16);
                int total = (int) number1 + (int) number2 + offset;

                changeFlags(total, true);

                short totalShort;
                if (CARRY_FLAG) {
                     totalShort = (short) (total & Integer.parseInt("7FFF", 16));

                    if (SIGN_FLAG)
                        totalShort *= -1;
                }
                else
                    totalShort = (short) total;

                registers[Integer.parseInt(operands[2], 16)] = String.format("%04X", totalShort);
            }
            case Operations.STORE -> {
                if (!writeMemory(operands[1], registers[Integer.parseInt(operands[0], 16)])) {
                    result.put("State", "Error");
                    result.put("Comment", "Ошибка записи данных в память");
                }
            }
            case Operations.HALT -> IS_HALT = true;
        }

        if (Objects.equals(result.get("State"), "Error"))
            return result;

        programCounter = Integer.toHexString(Integer.parseInt(programCounter, 16) + 1);

        CURRENT_STAGE = FETCH;

        return result;
    }

    private void changeFlags(int number, boolean checkCarry) {
        SIGN_FLAG = number < 0;
        ZERO_FLAG = number == 0;

        if (checkCarry)
            CARRY_FLAG = number > Short.MAX_VALUE || number < Short.MIN_VALUE;
    }

    private boolean writeMemory(String memoryAddress, String value) {
        if (!checkAddress(memoryAddress))
            return false;

        dataMemory[Integer.parseInt(memoryAddress, 16)] = value;

        return true;
    }

    private String readCommandMemory(String memoryAddress) {
        return checkAddress(memoryAddress) ? commandMemory[Integer.parseInt(memoryAddress, 16)] : null;
    }

    private String readDataMemory(String memoryAddress) {
        return checkAddress(memoryAddress) ? dataMemory[Integer.parseInt(memoryAddress, 16)] : null;
    }

    private boolean checkAddress(String memoryAddress) {
        return Integer.parseInt(memoryAddress, 16) >= 0 && Integer.parseInt(memoryAddress, 16) < MEMORY_SIZE;
    }
}