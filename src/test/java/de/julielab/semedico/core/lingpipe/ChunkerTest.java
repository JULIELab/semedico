package de.julielab.semedico.core.lingpipe;

import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.aliasi.chunk.Chunker;

import de.julielab.semedico.core.TestUtils;

public class ChunkerTest {
	private static Registry registry;
	private static Chunker chunker;

	@BeforeClass
	public static void setup() {
		registry = TestUtils.createTestRegistry();
		chunker = registry.getService(Chunker.class);
	}

	@AfterClass
	public static void shutdown() {
		registry.shutdown();
	}

}
