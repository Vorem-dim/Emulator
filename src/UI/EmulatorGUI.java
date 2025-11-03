package UI;

import Model.EmulatorCPU;
import ViewModel.EmulatorViewModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class EmulatorGUI {
    private final String numberFormat = "%04X";
    private final String commandFormat = "%08X";

    private final EmulatorViewModel emulator = new EmulatorViewModel();

    private JTextArea statusArea;
    private JCheckBox zeroFlag, signFlag, carryFlag;
    private JTable registersTable, dataMemoryTable, commandMemoryTable;
    private JTextField dataBus, commandBus, executeStage, currentStage, tact;

    public EmulatorGUI() {
        List<HashMap<String, String>> registers = new ArrayList<>(List.of(
                new HashMap<>(Map.of(
                        "Register", "R0 (Аккумулятор)",
                        "Value", String.format(numberFormat, 0),
                        "Description", "0"
                )),
                new HashMap<>(Map.of(
                        "Register", "R1",
                        "Value", String.format(numberFormat, 0),
                        "Description", "0"
                )),
                new HashMap<>(Map.of(
                        "Register", "R2",
                        "Value", String.format(numberFormat, 0),
                        "Description", "0"
                )),
                new HashMap<>(Map.of(
                        "Register", "R3",
                        "Value", String.format(numberFormat, 0),
                        "Description", "0"
                )),
                new HashMap<>(Map.of(
                        "Register", "R4",
                        "Value", String.format(numberFormat, 0),
                        "Description", "0"
                )),
                new HashMap<>(Map.of(
                        "Register", "R5",
                        "Value", String.format(numberFormat, 0),
                        "Description", "0"
                )),
                new HashMap<>(Map.of(
                        "Register", "R6",
                        "Value", String.format(numberFormat, 0),
                        "Description", "0"
                )),
                new HashMap<>(Map.of(
                        "Register", "R7",
                        "Value", String.format(numberFormat, 0),
                        "Description", "0"
                )),
                new HashMap<>(Map.of(
                        "Register", "IR (Регистр команд)",
                        "Value", String.format(commandFormat, 0),
                        "Description", "—"
                )),
                new HashMap<>(Map.of(
                        "Register", "PC (Счетчик команд)",
                        "Value", String.format(commandFormat, 0),
                        "Description", "0"
                ))
        ));

        JPanel statusPanel = createStatusPanel();
        JPanel controlPanel = createControlPanel(registers);
        JPanel registersPanel = createRegistersPanel(registers);
        JPanel dataMemoryPanel = createMemoryPanel("Память данных", false);
        JPanel programMemoryPanel = createMemoryPanel("Память программ", true);

        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        topPanel.add(registersPanel);
        topPanel.add(statusPanel);

        JPanel memoryPanel = new JPanel(new GridLayout(1, 2));
        memoryPanel.add(programMemoryPanel);
        memoryPanel.add(dataMemoryPanel);

        JFrame frame = new JFrame("Эмулятор трехадресной Гарвардской архитектуры");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(memoryPanel, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);
    }

    private JPanel createRegistersPanel(List<HashMap<String, String>> registers) {
        String[] columnNames = { "Регистр", "Значение", "Пояснение" };

        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        registers.forEach(register -> tableModel.addRow(new Object[] {
                register.get("Register"),
                register.get("Value"),
                register.get("Description")
        }));

        registersTable = new JTable(tableModel);

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBorder(BorderFactory.createTitledBorder("Регистры общего назначения"));
        panel.add(new JScrollPane(registersTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMemoryPanel(String title, boolean isCommandMemory) {
        String[] columnNames = new String[9];

        columnNames[0] = "Адрес";
        for (int i = 1; i < 9; i++)
            columnNames[i] = String.format("%02X", i);

        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        for (int i = 0; i < EmulatorCPU.MEMORY_SIZE; i += 8) {
            String[] row = new String[17];

            row[0] = String.format("%02X", i);
            for (int j = 0; j < 16 && i + j < EmulatorCPU.MEMORY_SIZE; j++)
                row[j + 1] = String.format(isCommandMemory ? commandFormat : numberFormat, 0);

            tableModel.addRow(row);
        }

        JTable table = new JTable(tableModel);
        if (isCommandMemory)
            commandMemoryTable = table;
        else
            dataMemoryTable = table;

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatusPanel() {
        statusArea = new JTextArea("Эмулятор готов к работе", 5, 30);
        statusArea.setEditable(false);

        zeroFlag = new JCheckBox("Zero", true);
        zeroFlag.setEnabled(false);

        signFlag = new JCheckBox("Sign", false);
        signFlag.setEnabled(false);

        carryFlag = new JCheckBox("Carry", false);
        carryFlag.setEnabled(false);

        dataBus = new JTextField(String.format(numberFormat, 0), 10);
        dataBus.setEditable(false);

        commandBus = new JTextField(String.format(commandFormat, 0), 10);
        commandBus.setEditable(false);

        executeStage = new JTextField(10);
        executeStage.setEditable(false);

        currentStage = new JTextField(10);
        currentStage.setEditable(false);

        tact = new JTextField(String.valueOf(0), 10);
        tact.setEditable(false);

        JPanel flagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        flagsPanel.add(new JLabel("Флаги:"));
        flagsPanel.add(zeroFlag);
        flagsPanel.add(signFlag);
        flagsPanel.add(carryFlag);

        JPanel countTacts = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countTacts.add(new JLabel("Количество тактов:"));
        countTacts.add(tact);

        JPanel emulatorExecuteStage = new JPanel(new FlowLayout(FlowLayout.LEFT));
        emulatorExecuteStage.add(new JLabel("Выполненный этап:"));
        emulatorExecuteStage.add(executeStage);

        JPanel emulatorCurrentStage = new JPanel(new FlowLayout(FlowLayout.LEFT));
        emulatorCurrentStage.add(new JLabel("Следующий этап:"));
        emulatorCurrentStage.add(currentStage);

        JPanel emulatorStages = new JPanel(new GridLayout(2, 1));
        emulatorStages.add(emulatorCurrentStage);
        emulatorStages.add(emulatorExecuteStage);

        JPanel commandBusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        commandBusPanel.add(new JLabel("Шина команд:"));
        commandBusPanel.add(commandBus);

        JPanel dataBusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dataBusPanel.add(new JLabel("Шина данных:"));
        dataBusPanel.add(dataBus);

        JPanel busPanel = new JPanel(new GridLayout(2, 1));
        busPanel.add(commandBusPanel);
        busPanel.add(dataBusPanel);

        JPanel panel = new JPanel(new GridLayout(5, 1));
        panel.setBorder(BorderFactory.createTitledBorder("Состояние процессора"));
        panel.add(flagsPanel);
        panel.add(countTacts);
        panel.add(emulatorStages);
        panel.add(busPanel);
        panel.add(new JScrollPane(statusArea));

        return panel;
    }

    private JPanel createControlPanel(List<HashMap<String, String>> registers) {
        JButton tactButton = new JButton("Такт");
        tactButton.addActionListener(_ -> updateDisplay(emulator.executeEmulatorTact()));

        JButton stepButton = new JButton("Шаг");
        stepButton.addActionListener(_ -> updateDisplay(emulator.executeEmulatorStep()));

        JButton runButton = new JButton("Выполнить");
        runButton.addActionListener(_ -> updateDisplay(emulator.executeEmulatorCommands()));

        JButton resetButton = new JButton("Сброс");
        resetButton.addActionListener(_ -> {
            emulator.resetEmulator();

            resetDisplay(registers);
        });

        JButton loadButton = new JButton("Загрузить программу");
        loadButton.addActionListener(_ -> {
            emulator.resetEmulator();

            resetDisplay(registers);

            updateDisplay(emulator.loadTestProgram());
        });

        JPanel panel = new JPanel(new FlowLayout());

        panel.add(runButton);
        panel.add(stepButton);
        panel.add(tactButton);
        panel.add(resetButton);
        panel.add(loadButton);

        return panel;
    }

    private void resetDisplay(List<HashMap<String, String>> registers) {
        DefaultTableModel tableModel = (DefaultTableModel) registersTable.getModel();

        for (int i = 0; i < registers.size(); i++) {
            tableModel.setValueAt(registers.get(i).get("Value"), i, 1);
            tableModel.setValueAt(registers.get(i).get("Description"), i, 2);
        }

        int[] emptyCommandMemory = new int[EmulatorCPU.MEMORY_SIZE];
        short[] emptyDataMemory = new short[EmulatorCPU.MEMORY_SIZE];

        Arrays.fill(emptyCommandMemory, 0);
        Arrays.fill(emptyDataMemory, (short) 0);

        updateMemoryTable(commandMemoryTable, emptyCommandMemory);
        updateMemoryTable(dataMemoryTable, emptyDataMemory);

        statusArea.setText("Эмулятор готов к работе");

        zeroFlag.setSelected(true);
        signFlag.setSelected(false);
        carryFlag.setSelected(false);

        dataBus.setText(String.format(numberFormat, 0));
        commandBus.setText(String.format(commandFormat, 0));

        executeStage.setText("");
        currentStage.setText("");

        tact.setText("0");
    }

    private void updateDisplay(HashMap<String, Object> params) {
        DefaultTableModel tableModel = (DefaultTableModel) registersTable.getModel();

        short[] registers = (short[]) params.get("registers");
        if (registers != null) {
            for (int i = 0; i < registers.length; i++) {
                tableModel.setValueAt(String.format(numberFormat, registers[i]), i, 1);
                tableModel.setValueAt(registers[i], i, 2);
            }
        }

        Integer instructionRegister = (Integer) params.get("IR");
        if (instructionRegister != null) {
            tableModel.setValueAt(String.format(commandFormat, instructionRegister), EmulatorCPU.REGISTERS_COUNT, 1);
            tableModel.setValueAt(params.getOrDefault("commentIR", "Без комментариев"), EmulatorCPU.REGISTERS_COUNT, 2);

            commandBus.setText(String.format(commandFormat, instructionRegister));
        }

        Integer programCounter = (Integer) params.get("PC");
        if (programCounter != null) {
            tableModel.setValueAt(String.format(commandFormat, programCounter), EmulatorCPU.REGISTERS_COUNT + 1, 1);
            tableModel.setValueAt(programCounter, EmulatorCPU.REGISTERS_COUNT + 1, 2);
        }

        short[] dataMemory = (short[]) params.get("dataMemory");
        if (dataMemory != null)
            updateMemoryTable(dataMemoryTable, dataMemory);

        int[] commandMemory = (int[]) params.get("commandMemory");
        if (commandMemory != null)
            updateMemoryTable(commandMemoryTable, commandMemory);

        String comment = (String) params.get("comment");
        if (comment != null)
            statusArea.setText(comment);

        Short dataBus = (Short) params.get("dataBus");
        if (dataBus != null)
            this.dataBus.setText(String.format(numberFormat, dataBus));

        zeroFlag.setSelected((boolean) params.getOrDefault("zeroFlag", zeroFlag.isSelected()));
        signFlag.setSelected((boolean) params.getOrDefault("signFlag", signFlag.isSelected()));
        carryFlag.setSelected((boolean) params.getOrDefault("carryFlag", carryFlag.isSelected()));

        if (params.get("previousStage") != null)
            executeStage.setText(params.get("previousStage").toString());

        currentStage.setText(params.get("currentStage").toString());

        tact.setText(params.getOrDefault("countTacts", 0).toString());
    }

    private void updateMemoryTable(JTable table, int[] memory) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            int addressOffset = i * 8;

            for (int j = 0; j < 8; j++)
                model.setValueAt(String.format(commandFormat, memory[addressOffset + j]), i, j + 1);
        }
    }

    private void updateMemoryTable(JTable table, short[] memory) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            int addressOffset = i * 8;

            for (int j = 0; j < 8; j++)
                model.setValueAt(String.format(numberFormat, memory[addressOffset + j]), i, j + 1);
        }
    }
}