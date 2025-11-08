package ViewModel;

import Model.EmulatorCPU;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class EmulatorViewModel {
    private EmulatorCPU emulator = new EmulatorCPU();

    public HashMap<String, Object> executeEmulatorTact() {
        HashMap<String, Object> params = emulator.executeInstructionStage();

        EmulatorCPU.fillInfoStatusCPU(params, emulator);

        return params;
    }

    public HashMap<String, Object> executeEmulatorStep()    {
        HashMap<String, Object> params = emulator.executeInstruction();

        EmulatorCPU.fillInfoStatusCPU(params, emulator);

        return params;
    }

    public HashMap<String, Object> executeEmulatorCommands() {
        HashMap<String, Object> params = executeEmulatorStep();

        boolean isHalt = (boolean) params.get("isHalt");
        while (!isHalt) {
            params = executeEmulatorStep();
            isHalt = (boolean) params.get("isHalt");
        }

        EmulatorCPU.fillInfoStatusCPU(params, emulator);

        return params;
    }

    public void resetEmulator() {
        emulator = new EmulatorCPU();
    }

    public HashMap<String, Object> loadTestProgram() {
        int[] commands = Arrays.stream(new String[] {
                "00070000", // LD R7 0x00 - Кол-во элементов массива
                "80060000", // LD R6 0 - Установка счетчика
                "80050100", // LD R5 0x01 - Установка адреса начала массива

                "05060700", // CMP R6 R7 - Проверка границ массива
                "040A0000", // JZ 0x0A - Выход из массива

                "02050602", // ADD R5 R6 R2 - Адрес элемента массива
                "40010200", // LD R1 R2 - Получение элемента массива
                "02000100", // ADD ACC R1 ACC - Увеличение суммы элементов массива

                "06060000", // INC R6 - Увелечение счетчика
                "03030000", // JMP 0x03 - Возврат в начало цикла

                "01003000", // STR ACC 30
                "3F000000"  // HLT
        }).flatMapToInt(hexNumber -> IntStream.of(Integer.parseUnsignedInt(hexNumber, 16))).toArray();

        short[] data1 = new short[] { 10, 23, 109, 11011, -3048, 29938, 45, -12976, 768, -8756, 14 };
        short[] data2 = new short[] { 10, 456, 1974, -19048, 2132, -24800, -485, 5263, -1478, 23621, -394 };
        short[] data3 = new short[] { 8, 3475, 10322, -5273, 1200, -11724, 2000, 113, -27 };

        int randomNumber = ThreadLocalRandom.current().nextInt(0, 3);

        HashMap<String, Object> params = switch (randomNumber) {
            case 0 -> emulator.setEmulatorMemory(data1, commands);
            case 1 -> emulator.setEmulatorMemory(data2, commands);
            case 2 -> emulator.setEmulatorMemory(data3, commands);
            default -> new HashMap<>();
        };

        EmulatorCPU.fillInfoStatusCPU(params, emulator);

        return params;
    }
}