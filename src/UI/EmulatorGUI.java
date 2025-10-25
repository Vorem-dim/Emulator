package UI;

import Model.EmulatorCPU;
import ViewModel.EmulatorViewModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;

public class EmulatorGUI {
    private static final String COMMAND_FORMAT = "%08X";
    private static final String NUMBER_FORMAT = "%04X";

    private JTextArea statusArea;
    private JCheckBox zeroFlag, signFlag, carryFlag;
    private JTable registersTable, dataMemoryTable, commandMemoryTable;
    private JTextField dataBus, commandBus, executeStage, currentStage, tact;

    private final EmulatorViewModel emulator = new EmulatorViewModel();

    public EmulatorGUI() {
        JFrame frame = new JFrame("Эмулятор трехадресной Гарвардской архитектуры");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel statusPanel = createStatusPanel();
        JPanel controlPanel = createControlPanel();
        JPanel registersPanel = createRegistersPanel();
        JPanel dataMemoryPanel = createMemoryPanel("Память данных", false);
        JPanel programMemoryPanel = createMemoryPanel("Память программ", true);

        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        topPanel.add(registersPanel);
        topPanel.add(statusPanel);

        JPanel memoryPanel = new JPanel(new GridLayout(1, 2));
        memoryPanel.add(programMemoryPanel);
        memoryPanel.add(dataMemoryPanel);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(memoryPanel, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createRegistersPanel() {
        String[] columnNames = { "Регистр", "Значение (hex)", "Пояснение" };

        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        tableModel.addRow(new Object[] {"R0 (Аккумулятор)", String.format(NUMBER_FORMAT, 0), 0});
        tableModel.addRow(new Object[] {"R1", String.format(NUMBER_FORMAT, 0), 0});
        tableModel.addRow(new Object[] {"IR (Регистр команд)", String.format(COMMAND_FORMAT, 0), "—"});
        tableModel.addRow(new Object[] {"PC (Счетчик команд)", String.format(COMMAND_FORMAT, 0), 0});

        registersTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(registersTable);

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBorder(BorderFactory.createTitledBorder("Регистры общего назначения"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMemoryPanel(String title, boolean isCommandMemory) {
        String[] columnNames = new String[9];

        columnNames[0] = "Адрес";
        for (int i = 0; i < 8; i++)
            columnNames[i + 1] = String.format("%02X", i);

        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        for (int i = 0; i < EmulatorCPU.MEMORY_SIZE; i += 8) {
            String[] row = new String[17];

            row[0] = String.format("%02X", i);
            for (int j = 0; j < 16 && i + j < EmulatorCPU.MEMORY_SIZE; j++)
                row[j + 1] = String.format(isCommandMemory ? COMMAND_FORMAT : NUMBER_FORMAT, 0);

            tableModel.addRow(row);
        }

        JTable table = new JTable(tableModel);
        if (isCommandMemory)
            commandMemoryTable = table;
        else
            dataMemoryTable = table;

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatusPanel() {
        statusArea = new JTextArea(5, 30);
        statusArea.setEditable(false);
        statusArea.setText("Эмулятор готов к работе");

        zeroFlag = new JCheckBox("Zero", EmulatorCPU.ZERO_FLAG);
        zeroFlag.setEnabled(false);

        signFlag = new JCheckBox("Sign", EmulatorCPU.SIGN_FLAG);
        signFlag.setEnabled(false);

        carryFlag = new JCheckBox("Carry", EmulatorCPU.CARRY_FLAG);
        carryFlag.setEnabled(false);

        dataBus = new JTextField(String.format(NUMBER_FORMAT, 0), 10);
        dataBus.setEditable(false);

        commandBus = new JTextField(String.format(COMMAND_FORMAT, 0), 10);
        commandBus.setEditable(false);

        executeStage = new JTextField(10);
        executeStage.setEditable(false);

        currentStage = new JTextField(10);
        currentStage.setEditable(false);

        tact = new JTextField(10);
        tact.setText(String.valueOf(EmulatorCPU.TACT));
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
        busPanel.setBorder(BorderFactory.createTitledBorder("Системные шины"));
        busPanel.add(commandBusPanel);
        busPanel.add(dataBusPanel);

        JScrollPane statusScroll = new JScrollPane(statusArea);

        JPanel panel = new JPanel(new GridLayout(5, 1, 0, 0));

        panel.setBorder(BorderFactory.createTitledBorder("Состояние процессора"));
        panel.add(flagsPanel);
        panel.add(countTacts);
        panel.add(emulatorStages);
        panel.add(busPanel);
        panel.add(statusScroll);

        return panel;
    }

    private JPanel createControlPanel() {
        JButton tactButton = new JButton("Такт");
        tactButton.addActionListener(_ -> updateDisplay(emulator.executeEmulatorTact()));

        JButton stepButton = new JButton("Шаг");
        stepButton.addActionListener(_ -> updateDisplay(emulator.executeEmulatorStep()));

        JButton runButton = new JButton("Выполнить");
        runButton.addActionListener(_ -> updateDisplay(emulator.executeEmulatorCommands()));

        JButton resetButton = new JButton("Сброс");
        resetButton.addActionListener(_ -> {
            emulator.resetEmulator();

            resetDisplay();
        });

        JButton loadButton = new JButton("Загрузить программу");
        loadButton.addActionListener(_ -> {
            emulator.resetEmulator();

            resetDisplay();

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

    private void resetDisplay() {
        DefaultTableModel tableModel = (DefaultTableModel) registersTable.getModel();

        tableModel.setValueAt(String.format(NUMBER_FORMAT, 0), 0, 1);
        tableModel.setValueAt(String.format(NUMBER_FORMAT, 0), 1, 1);
        tableModel.setValueAt(String.format(COMMAND_FORMAT, 0), 2, 1);
        tableModel.setValueAt(String.format(COMMAND_FORMAT, 0), 3, 1);

        tableModel.setValueAt(0, 0, 2);
        tableModel.setValueAt(0, 1, 2);
        tableModel.setValueAt("—", 2, 2);
        tableModel.setValueAt(0, 3, 2);

        String[] emptyMemory = new String[EmulatorCPU.MEMORY_SIZE];
        Arrays.fill(emptyMemory, "0");

        updateMemoryTable(dataMemoryTable, emptyMemory, false);
        updateMemoryTable(commandMemoryTable, emptyMemory, true);

        statusArea.setText("Эмулятор готов к работе");

        zeroFlag.setSelected(EmulatorCPU.ZERO_FLAG);
        signFlag.setSelected(EmulatorCPU.SIGN_FLAG);
        carryFlag.setSelected(EmulatorCPU.CARRY_FLAG);

        dataBus.setText(String.format(NUMBER_FORMAT, 0));
        commandBus.setText(String.format(COMMAND_FORMAT, 0));

        executeStage.setText("");
        currentStage.setText("");

        tact.setText(String.valueOf(EmulatorCPU.TACT));
    }

    private void updateDisplay(HashMap<String, String> params) {
        DefaultTableModel tableModel = (DefaultTableModel) registersTable.getModel();

        String registers = params.get("registers");
        if (registers != null) {
            String[] registersArray = registers.replaceAll("[\\[\\]]", "").split(", ");

            tableModel.setValueAt(String.format(NUMBER_FORMAT, Integer.parseUnsignedInt(registersArray[0], 16)), 0, 1);
            tableModel.setValueAt(String.format(NUMBER_FORMAT, Integer.parseUnsignedInt(registersArray[1], 16)), 1, 1);

            tableModel.setValueAt((short) Integer.parseInt(registersArray[0], 16), 0, 2);
            tableModel.setValueAt((short) Integer.parseInt(registersArray[1], 16), 1, 2);
        }

        String instructionRegister = params.get("IR");
        if (instructionRegister != null) {
            tableModel.setValueAt(String.format(
                    COMMAND_FORMAT,
                    Integer.parseUnsignedInt(instructionRegister, 16)
            ), 2, 1);

            tableModel.setValueAt(EmulatorCPU.InstructionRegisterComment(instructionRegister), 2, 2);

            commandBus.setText(String.format(
                    COMMAND_FORMAT,
                    Integer.parseUnsignedInt(instructionRegister, 16)
            ));
        }

        String programCounter = params.get("PC");
        if (programCounter != null) {
            tableModel.setValueAt(String.format(COMMAND_FORMAT, Integer.parseUnsignedInt(programCounter, 16)), 3, 1);

            tableModel.setValueAt(Integer.parseInt(programCounter, 16), 3, 2);
        }

        String dataMemory = params.get("Data");
        if (dataMemory != null)
            updateMemoryTable(dataMemoryTable, dataMemory.replaceAll("[\\[\\]]", "").split(", "), false);

        String commandMemory = params.get("Commands");
        if (commandMemory != null)
            updateMemoryTable(commandMemoryTable, commandMemory.replaceAll("[\\[\\]]", "").split(", "), true);

        String comment = params.get("Comment");
        if (comment != null)
            statusArea.setText(comment);

        String dataBus = params.get("dataBus");
        if (dataBus != null)
            this.dataBus.setText(String.format(NUMBER_FORMAT, (short) Integer.parseInt(dataBus, 16)));

        zeroFlag.setSelected(EmulatorCPU.ZERO_FLAG);
        signFlag.setSelected(EmulatorCPU.SIGN_FLAG);
        carryFlag.setSelected(EmulatorCPU.CARRY_FLAG);

        if (EmulatorCPU.PREVIOUS_STAGE != EmulatorCPU.CURRENT_STAGE)
            executeStage.setText(EmulatorCPU.PREVIOUS_STAGE.toString());
        currentStage.setText(EmulatorCPU.CURRENT_STAGE.toString());

        tact.setText(String.valueOf(EmulatorCPU.TACT));
    }

    private void updateMemoryTable(JTable table, String[] memory, boolean isCommandMemory) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            int addressOffset = i * 8;

            for (int j = 0; j < 8; j++)
                model.setValueAt(String.format(
                        isCommandMemory ? COMMAND_FORMAT : NUMBER_FORMAT,
                        Integer.parseUnsignedInt(memory[addressOffset + j], 16)
                ), i, j + 1);
        }
    }
}