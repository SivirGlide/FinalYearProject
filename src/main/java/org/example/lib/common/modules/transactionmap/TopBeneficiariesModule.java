package org.example.lib.common.modules.transactionmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.dflib.DataFrame;
import org.dflib.Series;
import org.example.lib.transactionmapper.InformativeResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TopBeneficiariesModule implements TransactionMapModule<InformativeResult> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int TOP_N = 5;

    @Override
    public String getModuleName() {
        return "TopBeneficiaries";
    }

    @Override
    public InformativeResult run(DataFrame df) {
        Series<?> nameSeries = df.getColumn("beneficiaryname");

        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < nameSeries.size(); i++) {
            Object val = nameSeries.get(i);
            if (val == null) continue;
            String name = val.toString();
            counts.put(name, counts.getOrDefault(name, 0) + 1);
        }

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        ArrayNode array = MAPPER.createArrayNode();
        for (int i = 0; i < Math.min(TOP_N, sorted.size()); i++) {
            ObjectNode entry = array.addObject();
            entry.put("beneficiary", sorted.get(i).getKey());
            entry.put("transactionCount", sorted.get(i).getValue());
        }

        try {
            return new InformativeResult(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(array));
        } catch (Exception e) {
            return new InformativeResult("{\"error\": \"Failed to serialise: " + e.getMessage() + "\"}");
        }
    }
}