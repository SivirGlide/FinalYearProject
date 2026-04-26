package org.example.lib.suspiciousactivityengine;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SuspiciousActivityEngineResultTest {

    @Test
    void size_noResultsAdded_returnsZero() {
        SuspiciousActivityEngineResult result = new SuspiciousActivityEngineResult();

        assertEquals(0, result.size());
    }

    @Test
    void all_noResultsAdded_returnsEmptyList() {
        SuspiciousActivityEngineResult result = new SuspiciousActivityEngineResult();

        assertTrue(result.all().isEmpty());
    }

    @Test
    void add_validModuleOutput_increasesResultSize() {
        SuspiciousActivityEngineResult result = new SuspiciousActivityEngineResult();

        HashMap<String, Object> moduleOutput = new HashMap<>();
        moduleOutput.put("Module Name", "Test Module");

        result.add(moduleOutput);

        assertEquals(1, result.size());
    }

    @Test
    void all_resultAdded_returnsAddedResult() {
        SuspiciousActivityEngineResult result = new SuspiciousActivityEngineResult();

        HashMap<String, Object> moduleOutput = new HashMap<>();
        moduleOutput.put("Module Name", "Test Module");
        moduleOutput.put("Module Ran", true);
        moduleOutput.put("Risk Score", 5);
        moduleOutput.put("Comments", "Success");

        result.add(moduleOutput);

        assertEquals(1, result.all().size());
        assertEquals(moduleOutput, result.all().get(0));
    }

    @Test
    void all_multipleResultsAdded_preservesInsertionOrder() {
        SuspiciousActivityEngineResult result = new SuspiciousActivityEngineResult();

        HashMap<String, Object> firstOutput = new HashMap<>();
        firstOutput.put("Module Name", "First Module");

        HashMap<String, Object> secondOutput = new HashMap<>();
        secondOutput.put("Module Name", "Second Module");

        result.add(firstOutput);
        result.add(secondOutput);

        assertEquals(firstOutput,  result.all().get(0));
        assertEquals(secondOutput, result.all().get(1));
    }

    @Test
    void all_returnedListIsUnmodifiable() {
        SuspiciousActivityEngineResult result = new SuspiciousActivityEngineResult();

        List<HashMap<String, Object>> results = result.all();

        assertThrows(
                UnsupportedOperationException.class,
                () -> results.add(new HashMap<>())
        );
    }

    @Test
    void toJson_noResultsAdded_returnsEmptyJsonArray() {
        SuspiciousActivityEngineResult result = new SuspiciousActivityEngineResult();

        assertEquals("[ ]", result.toJson());
    }

    @Test
    void toJson_resultAdded_returnsPrettyPrintedJsonArray() {
        SuspiciousActivityEngineResult result = new SuspiciousActivityEngineResult();

        HashMap<String, Object> moduleOutput = new HashMap<>();
        moduleOutput.put("Module Name", "Test Module");
        moduleOutput.put("Module Ran", true);
        moduleOutput.put("Risk Score", 10);
        moduleOutput.put("Comments", "Success");

        result.add(moduleOutput);

        String json = result.toJson();

        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
        assertTrue(json.contains("\"Module Name\" : \"Test Module\""));
        assertTrue(json.contains("\"Module Ran\" : true"));
        assertTrue(json.contains("\"Risk Score\" : 10"));
        assertTrue(json.contains("\"Comments\" : \"Success\""));
    }

    @Test
    void toJson_multipleResultsAdded_serialisesAllResults() {
        SuspiciousActivityEngineResult result = new SuspiciousActivityEngineResult();

        HashMap<String, Object> firstOutput = new HashMap<>();
        firstOutput.put("Module Name", "First Module");

        HashMap<String, Object> secondOutput = new HashMap<>();
        secondOutput.put("Module Name", "Second Module");

        result.add(firstOutput);
        result.add(secondOutput);

        String json = result.toJson();

        assertTrue(json.contains("\"Module Name\" : \"First Module\""));
        assertTrue(json.contains("\"Module Name\" : \"Second Module\""));
    }
}
