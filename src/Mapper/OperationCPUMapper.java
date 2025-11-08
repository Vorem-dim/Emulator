package Mapper;

import Model.Operations;

import java.util.HashMap;
import java.util.Map;

public class OperationCPUMapper {
    private final HashMap<String, Operations> operations = new HashMap<>(Map.of(
            "00", Operations.LD,
            "01", Operations.STR,
            "02", Operations.ADD,
            "03", Operations.JMP,
            "04", Operations.JZ,
            "05", Operations.CMP,
            "06", Operations.INC,
            "3F", Operations.HLT
    ));

    public Operations toOperation(String code) {
        return operations.get(code);
    }

    public String toHexCode(Operations operation) {
        for (Map.Entry<String, Operations> operationEntry: operations.entrySet())
            if (operationEntry.getValue() == operation)
                return operationEntry.getKey();

        return null;
    }
}