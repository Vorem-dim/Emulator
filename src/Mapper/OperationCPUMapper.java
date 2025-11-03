package Mapper;

import Model.Operations;

import java.util.HashMap;
import java.util.Map;

public class OperationCPUMapper {
    private final HashMap<String, Operations> operations = new HashMap<>(Map.of(
            "00", Operations.LOAD,
            "01", Operations.STORE,
            "02", Operations.ADD,
            "03", Operations.JUMP,
            "04", Operations.COMPARE,
            "FF", Operations.HALT
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