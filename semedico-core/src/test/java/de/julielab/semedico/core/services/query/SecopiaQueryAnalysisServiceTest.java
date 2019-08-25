package de.julielab.semedico.core.services.query;

import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.parsing.SecopiaParse;
import org.apache.tapestry5.ioc.Registry;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SecopiaQueryAnalysisServiceTest {
    private Registry registry;

    @BeforeClass
    public void setup() {
        registry = TestUtils.createTestRegistry();
    }

    @AfterClass
    public void shutdown() {
        registry.shutdown();
    }

    @Test
    public void testWildcard() {
        final ISecopiaQueryAnalysisService analysisService = registry.getService(ISecopiaQueryAnalysisService.class);
        final SecopiaParse secopiaParse = analysisService.analyseQueryString("*");
        System.out.println(secopiaParse);
    }
}
