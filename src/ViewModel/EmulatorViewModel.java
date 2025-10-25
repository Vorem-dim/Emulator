package ViewModel;

import Model.EmulatorCPU;
import Model.InstructionStages;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class EmulatorViewModel {
    private EmulatorCPU emulator = new EmulatorCPU();

    public HashMap<String, String> executeEmulatorTact() {
        return emulator.executeInstructionStage();
    }

    public HashMap<String, String> executeEmulatorStep() {
        return emulator.executeInstruction();
    }

    public HashMap<String, String> executeEmulatorCommands() {
        HashMap<String, String> params = executeEmulatorStep();

        while (!EmulatorCPU.IS_HALT)
            params = executeEmulatorStep();

        return params;
    }

    public void resetEmulator() {
        EmulatorCPU.TACT = 0;
        EmulatorCPU.IS_HALT = true;
        EmulatorCPU.ZERO_FLAG = true;
        EmulatorCPU.SIGN_FLAG = false;
        EmulatorCPU.CARRY_FLAG = false;
        EmulatorCPU.PREVIOUS_STAGE = InstructionStages.FETCH;
        EmulatorCPU.CURRENT_STAGE = InstructionStages.FETCH;

        emulator = new EmulatorCPU();
    }

    public HashMap<String, String> loadTestProgram() {
        String[] commands = new String[] {
                "00000000", // LOAD ACC 00
                "00010100", // LOAD R1 01
                "01000100", // ADD ACC R1 ACC
                "00010200", // LOAD R1 02
                "01000100", // ADD ACC R1 ACC
                "00010300", // LOAD R1 03
                "01000100", // ADD ACC R1 ACC
                "02003000", // STORE ACC 30
                "03000000"  // HALT
        };
        String[] data1 = new String[] {"3D4", "3F", "7FEC", "86FF"};
        String[] data2 = new String[] {"1F4", "A11F", "345A", "F09F"};

        int randomNumber = ThreadLocalRandom.current().nextInt(0, 2);

        HashMap<String, String> params = emulator.setEmulatorMemory(randomNumber == 0 ? data1 : data2, commands);
        params.put("Comment", "Программа загружена");

        EmulatorCPU.IS_HALT = false;

        return params;
    }
}