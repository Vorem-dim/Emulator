package Model;

import Mapper.OperationCPUMapper;

import java.util.*;

import static Model.InstructionStages.*;
import static Model.ExecutionStatus.*;

public class EmulatorCPU {
    public static final int MEMORY_SIZE = 256;
    public static final int REGISTERS_COUNT = 8;

    private final OperationCPUMapper operationCPUMapper;
    private final short[] dataMemory, registers;
    private final int[] commandMemory;

    private HashMap<String, Object> decodeInstruction;
    private boolean zeroFlag, signFlag, carryFlag, isHalt;
    private InstructionStages currentStage, previousStage;
    private int programCounter, instructionRegister, countTacts;

    public static void fillInfoStatusCPU(HashMap<String, Object> params, EmulatorCPU emulatorCPU) {
        params.put("registers", emulatorCPU.registers);
        params.put("dataMemory", emulatorCPU.dataMemory);
        params.put("commandMemory", emulatorCPU.commandMemory);

        params.put("IR", emulatorCPU.instructionRegister);
        params.put("PC", emulatorCPU.programCounter);

        params.put("zeroFlag", emulatorCPU.zeroFlag);
        params.put("signFlag", emulatorCPU.signFlag);
        params.put("carryFlag", emulatorCPU.carryFlag);

        params.put("currentStage", emulatorCPU.currentStage);
        params.put("previousStage", emulatorCPU.previousStage);

        params.put("countTacts", emulatorCPU.countTacts);

        Operations operation = emulatorCPU.operationCPUMapper.toOperation(String.format(
                "%02X",
                (emulatorCPU.instructionRegister & Long.parseUnsignedLong("3F000000", 16)) >> 24
        ));

        if (operation == Operations.LD) {
            int index = (emulatorCPU.instructionRegister & Integer.parseUnsignedInt("FF00", 16)) >> 8;
            params.put("dataBus", emulatorCPU.dataMemory[index]);
        }

        if (emulatorCPU.isHalt)
            params.put("comment", "Выполнение операций завершено");

        switch (operation) {
            case HLT -> params.put("commentIR", operation);
            case JMP, JZ -> params.put("commentIR", String.format(
                    "%s %02X",
                    operation,
                    (emulatorCPU.instructionRegister & Integer.parseUnsignedInt("FF0000", 16)) >> 16
            ));
            case ADD -> params.put("commentIR", String.format(
                    "%s %02X %02X %02X",
                    operation,
                    (emulatorCPU.instructionRegister & Integer.parseUnsignedInt("FF0000", 16)) >> 16,
                    (emulatorCPU.instructionRegister & Integer.parseUnsignedInt("FF00", 16)) >> 8,
                    emulatorCPU.instructionRegister & Integer.parseUnsignedInt("FF", 16)
            ));
            default -> params.put("commentIR", String.format(
                    "%s %02X %02X",
                    operation,
                    (emulatorCPU.instructionRegister & Integer.parseUnsignedInt("FF0000", 16)) >> 16,
                    (emulatorCPU.instructionRegister & Integer.parseUnsignedInt("FF00", 16)) >> 8
            ));
        }
    }

    public EmulatorCPU() {
        countTacts = 0;

        isHalt = true;
        zeroFlag = false;
        signFlag = false;
        carryFlag = false;

        currentStage = FETCH;
        previousStage = null;

        decodeInstruction = new HashMap<>();

        operationCPUMapper = new OperationCPUMapper();

        programCounter = 0;
        instructionRegister = 0;

        dataMemory = new short[MEMORY_SIZE];
        commandMemory = new int[MEMORY_SIZE];
        registers = new short[REGISTERS_COUNT];

        Arrays.fill(commandMemory, 0);
        Arrays.fill(dataMemory, (short) 0);
        Arrays.fill(registers, (short) 0);
    }

    public HashMap<String, Object> setEmulatorMemory(short[] dataMemory, int[] commandMemory) {
        isHalt = false;

        System.arraycopy(dataMemory, 0, this.dataMemory, 0, dataMemory.length);

        System.arraycopy(commandMemory, 0, this.commandMemory, 0, commandMemory.length);

        return new HashMap<>(Map.of(
                "data", this.dataMemory,
                "commands", this.commandMemory,
                "comment", "Программа загружена"
        ));
    }

    public HashMap<String, Object> executeInstruction() {
        if (isHalt)
            return new HashMap<>(Map.of("status", OK, "comment", "Выполнение операций завершено", "isHalt", true));

        HashMap<String, Object> executionStatus;

        if (currentStage == FETCH) {
            countTacts++;
            previousStage = currentStage;
            currentStage = DECODE;

            executionStatus = executeStageFetch();

            if (Objects.equals(executionStatus.get("status"), ERROR))
                return executionStatus;
        }

        if (currentStage == DECODE) {
            countTacts++;
            previousStage = currentStage;
            currentStage = EXECUTE;

            executionStatus = executeStageDecode();

            if (Objects.equals(executionStatus.get("status"), ERROR))
                return executionStatus;
        }

        countTacts++;
        programCounter++;
        previousStage = currentStage;
        currentStage = FETCH;

        executionStatus = executeStageExecute();
        executionStatus.put("comment", "Выполнена инструктция процессора");

        return executionStatus;
    }

    public HashMap<String, Object> executeInstructionStage() {
        if (isHalt)
            return new HashMap<>(Map.of("status", OK, "comment", "Выполнение операций завершено", "isHalt", true));

        previousStage = currentStage;
        countTacts++;

        return switch(currentStage) {
            case FETCH -> {
                currentStage = DECODE;
                yield executeStageFetch();
            }
            case DECODE -> {
                currentStage = EXECUTE;
                yield executeStageDecode();
            }
            case EXECUTE -> {
                programCounter++;
                currentStage = FETCH;
                yield executeStageExecute();
            }
        };
    }

    private HashMap<String, Object> executeStageFetch() {
        Integer instruction = readCommandMemory(programCounter);

        if (instruction == null)
            return new HashMap<>(Map.of(
                    "status", ERROR,
                    "comment", "Ошибка чтении памяти команд",
                    "isHalt", isHalt
            ));

        instructionRegister = instruction;

        decodeInstruction = new HashMap<>();

        return new HashMap<>(Map.of("status", OK, "comment", "Выполнен этап инструкции FETCH"));
    }

    private HashMap<String, Object> executeStageDecode() {
        int operationCode = (int) (instructionRegister & Long.parseUnsignedLong("FF000000", 16)) >> 24;

        List<Integer> operands = new ArrayList<>(List.of(
                (instructionRegister & Integer.parseUnsignedInt("FF0000", 16)) >> 16,
                (instructionRegister & Integer.parseUnsignedInt("FF00", 16)) >> 8,
                instructionRegister & Integer.parseUnsignedInt("FF", 16)
        ));

        decodeInstruction = new HashMap<>(Map.of("operation", operationCode, "operands", operands));

        return new HashMap<>(Map.of("status", OK, "isHalt", isHalt, "comment", "Выполнен этап инструкции DECODE"));
    }

    private HashMap<String, Object> executeStageExecute() {
        HashMap<String, Object> result = new HashMap<>(Map.of("status", OK));

        int operationCode = (int) decodeInstruction.get("operation");
        boolean isImmediateValue = (operationCode & Long.parseUnsignedLong("80", 16)) >> 7 == 1;
        boolean isIndirectAddress = (operationCode & Long.parseUnsignedLong("40", 16)) >> 6 == 1;
        Operations operation = operationCPUMapper.toOperation(String.format(
                "%02X",
                operationCode & Long.parseUnsignedLong("3F", 16)
        ));

        List<Integer> operands = (List<Integer>) decodeInstruction.get("operands");

        switch (operation) {
            case Operations.LD -> {
                short dataAddress = isIndirectAddress ? registers[operands.get(1)] : operands.get(1).shortValue();

                Short data = isImmediateValue ? (Short) dataAddress : readDataMemory(dataAddress);
                if (data == null) {
                    result.put("status", ERROR);
                    result.put("comment", "Ошибка чтения памяти данных");

                    break;
                }

                registers[operands.getFirst()] = data;
            }
            case Operations.ADD -> {

                short number1 = registers[operands.get(0)];
                short number2 = registers[operands.get(1)];
                int total = (int) number1 + (int) number2;

                if (operands.contains(0)) {
                    if (carryFlag)
                        total += (registers[0] > 0) ? Short.MAX_VALUE : Short.MIN_VALUE;

                    carryFlag = total > Short.MAX_VALUE || total < Short.MIN_VALUE;

                    short totalShort;
                    if (carryFlag)
                        totalShort = (short) (total - (total > 0 ? Short.MAX_VALUE : Short.MIN_VALUE));
                    else
                        totalShort = (short) total;

                    registers[operands.get(2)] = totalShort;
                } else {
                    registers[operands.get(2)] = (short) total;
                }
            }
            case Operations.STR -> {
                if (!writeMemory(operands.get(1), registers[operands.get(0)])) {
                    result.put("status", ERROR);
                    result.put("comment", "Ошибка записи данных в память");
                }
            }
            case Operations.JMP -> programCounter = operands.getFirst();
            case Operations.JZ -> {
                if (zeroFlag)
                    programCounter = operands.getFirst();
            }
            case INC -> registers[operands.getFirst()] += 1;
            case Operations.CMP -> {
                short total = (short) (registers[operands.get(1)] - registers[operands.get(0)]);

                zeroFlag = total == 0;
                signFlag = total < 0;
            }
            case Operations.HLT -> isHalt = true;
        }

        result.put("isHalt", isHalt);
        result.put("comment", "Выполнен этап инструкции EXECUTE");
        return result;
    }

    private boolean writeMemory(int memoryAddress, short value) {
        if (!checkAddress(memoryAddress))
            return false;

        dataMemory[memoryAddress] = value;

        return true;
    }

    private Integer readCommandMemory(int memoryAddress) {
        return checkAddress(memoryAddress) ? commandMemory[memoryAddress] : null;
    }

    private Short readDataMemory(int memoryAddress) {
        return checkAddress(memoryAddress) ? dataMemory[memoryAddress] : null;
    }

    private boolean checkAddress(int memoryAddress) {
        return memoryAddress >= 0 && memoryAddress < MEMORY_SIZE;
    }
}