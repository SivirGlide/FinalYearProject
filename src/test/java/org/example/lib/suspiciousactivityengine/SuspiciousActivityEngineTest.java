package org.example.lib.suspiciousactivityengine;

import org.dflib.DataFrame;
import org.example.lib.common.modules.suspiciousactivityengine.SuspiciousActivityEngineModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuspiciousActivityEngineTest {

    @Mock
    private DataFrame transaction;

    @Mock
    private DataFrame customerProfile;

    @Mock
    private SuspiciousActivityEngineModule module;

    private Object transactionMapResult;
    private SuspiciousActivityEngine engine;

    @BeforeEach
    void setUp() {
        transactionMapResult = new Object();
        engine = new SuspiciousActivityEngine(transaction, customerProfile, transactionMapResult);
    }

    @Test
    void addModule_validModule_addsModuleAndReturnsSameEngineInstance() {
        SuspiciousActivityEngine returned = engine.addModule(module);

        assertSame(engine, returned);
        assertEquals(1, engine.moduleCount());
    }

    @Test
    void addModule_nullModule_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> engine.addModule(null)
        );

        assertEquals("Module must not be null.", exception.getMessage());
        assertEquals(0, engine.moduleCount());
    }

    @Test
    void moduleCount_noModulesAdded_returnsZero() {
        assertEquals(0, engine.moduleCount());
    }

    @Test
    void moduleCount_multipleModulesAdded_returnsNumberOfAddedModules() {
        SuspiciousActivityEngineModule firstModule  = mock(SuspiciousActivityEngineModule.class);
        SuspiciousActivityEngineModule secondModule = mock(SuspiciousActivityEngineModule.class);

        engine.addModule(firstModule);
        engine.addModule(secondModule);

        assertEquals(2, engine.moduleCount());
    }

    @Test
    void run_noModules_returnsEmptyResult() {
        SuspiciousActivityEngineResult result = engine.run();

        assertNotNull(result);
        assertEquals(0, result.size());
        assertTrue(result.all().isEmpty());
    }

    @Test
    void run_successfulModule_addsModuleOutputToResult() {
        HashMap<String, Object> moduleOutput = new HashMap<>();
        moduleOutput.put("Module Name", "Test Module");
        moduleOutput.put("Module Ran", true);
        moduleOutput.put("Risk Score", 15);
        moduleOutput.put("Comments", "Module completed successfully.");

        when(module.run(transaction, customerProfile, transactionMapResult))
                .thenReturn(moduleOutput);

        SuspiciousActivityEngineResult result = engine
                .addModule(module)
                .run();

        assertEquals(1, result.size());
        assertEquals(moduleOutput, result.all().get(0));

        verify(module).getModuleName();
        verify(module).run(transaction, customerProfile, transactionMapResult);
    }

    @Test
    void run_multipleSuccessfulModules_addsOutputsInExecutionOrder() {
        SuspiciousActivityEngineModule firstModule  = mock(SuspiciousActivityEngineModule.class);
        SuspiciousActivityEngineModule secondModule = mock(SuspiciousActivityEngineModule.class);

        HashMap<String, Object> firstOutput = new HashMap<>();
        firstOutput.put("Module Name", "First Module");

        HashMap<String, Object> secondOutput = new HashMap<>();
        secondOutput.put("Module Name", "Second Module");

        when(firstModule.run(transaction, customerProfile, transactionMapResult)).thenReturn(firstOutput);
        when(secondModule.run(transaction, customerProfile, transactionMapResult)).thenReturn(secondOutput);

        SuspiciousActivityEngineResult result = engine
                .addModule(firstModule)
                .addModule(secondModule)
                .run();

        assertEquals(2, result.size());
        assertEquals(firstOutput, result.all().get(0));
        assertEquals(secondOutput, result.all().get(1));
    }

    @Test
    void run_moduleThrowsException_addsStandardisedErrorOutput() {
        when(module.getModuleName()).thenReturn("Failing Module");
        when(module.run(transaction, customerProfile, transactionMapResult))
                .thenThrow(new RuntimeException("Unexpected failure"));

        SuspiciousActivityEngineResult result = engine
                .addModule(module)
                .run();

        assertEquals(1, result.size());

        HashMap<String, Object> errorOutput = result.all().get(0);
        assertEquals("Failing Module", errorOutput.get("Module Name"));
        assertEquals(false,            errorOutput.get("Module Ran"));
        assertEquals(-1,               errorOutput.get("Risk Score"));
        assertEquals("Module threw RuntimeException: Unexpected failure", errorOutput.get("Comments"));
    }

    @Test
    void run_oneModuleFailsAndOneModuleSucceeds_returnsBothResults() {
        SuspiciousActivityEngineModule failingModule    = mock(SuspiciousActivityEngineModule.class);
        SuspiciousActivityEngineModule successfulModule = mock(SuspiciousActivityEngineModule.class);

        HashMap<String, Object> successOutput = new HashMap<>();
        successOutput.put("Module Name", "Successful Module");
        successOutput.put("Module Ran", true);

        when(failingModule.getModuleName()).thenReturn("Failing Module");
        when(failingModule.run(transaction, customerProfile, transactionMapResult))
                .thenThrow(new IllegalStateException("Invalid state"));
        when(successfulModule.run(transaction, customerProfile, transactionMapResult))
                .thenReturn(successOutput);

        SuspiciousActivityEngineResult result = engine
                .addModule(failingModule)
                .addModule(successfulModule)
                .run();

        assertEquals(2, result.size());

        HashMap<String, Object> errorOutput = result.all().get(0);
        assertEquals("Failing Module",                                    errorOutput.get("Module Name"));
        assertEquals(false,                                               errorOutput.get("Module Ran"));
        assertEquals(-1,                                                  errorOutput.get("Risk Score"));
        assertEquals("Module threw IllegalStateException: Invalid state", errorOutput.get("Comments"));

        assertEquals(successOutput, result.all().get(1));
    }
}