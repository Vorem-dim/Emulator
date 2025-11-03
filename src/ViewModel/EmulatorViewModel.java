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

    public HashMap<String, Object> executeEmulatorStep() {
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
                "00010000", // LOAD ACC 00
                "00020100", // LOAD R1 01
                "02010200", // ADD R1 R2 ACC
                "00010200", // LOAD R1 02
                "02000100", // ADD ACC R1 ACC
                "00010300", // LOAD R1 03
                "02000100", // ADD ACC R1 ACC
                "01003000", // STORE ACC 30
                "FF000000"  // HALT
        }).flatMapToInt(hexNumber -> IntStream.of(Integer.parseUnsignedInt(hexNumber, 16))).toArray();

        String[] dataHex1 = new String[] {"3D4", "3F", "7FEC", "86FF"};
        String[] dataHex2 = new String[] {"1F4", "A11F", "345A", "F09F"};

        short[] data1 = new short[dataHex1.length];
        short[] data2 = new short[dataHex2.length];

        for (int i = 0; i < dataHex1.length; i++)
            data1[i] = (short) Integer.parseInt(dataHex1[i], 16);

        for (int i = 0; i < dataHex2.length; i++)
            data2[i] = (short) Integer.parseInt(dataHex2[i], 16);

        int randomNumber = ThreadLocalRandom.current().nextInt(0, 2);

        HashMap<String, Object> params = emulator.setEmulatorMemory(randomNumber == 0 ? data1 : data2, commands);

        EmulatorCPU.fillInfoStatusCPU(params, emulator);

        return params;
    }
}