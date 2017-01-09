package de.julielab.semedico.core.lingpipe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;

import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.services.interfaces.ITermService;

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

	@Test
	public void testEventTerms() {
		String test = "notindict regulates notindict";
		Chunking chunking = chunker.chunk(test);

		// we have to event terms with a synonym for "regulation"
		assertEquals(2, chunking.chunkSet().size());
		for (Chunk c : chunking.chunkSet()) {
			String chunkString = test.substring(c.start(), c.end());
			assertEquals("regulates", chunkString);
		}
	}

	@Test
	public void testSpecialTerms1() {
		String test = "any binds notindict";
		Chunking chunking = chunker.chunk(test);

		Iterator<Chunk> it = chunking.chunkSet().iterator();
		Chunk c;

		assertTrue(it.hasNext());
		c = it.next();
		assertEquals(0, c.start());
		assertEquals(3, c.end());
		assertEquals("any", test.substring(c.start(), c.end()));
		assertEquals(ITermService.CORE_TERM_PREFIX + 0, c.type());

//		assertTrue(it.hasNext());
//		c = it.next();
//		assertEquals(0, c.start());
//		assertEquals(3, c.end());
//		assertEquals("any", test.substring(c.start(), c.end()));
//		assertEquals(ITermService.CORE_TERM_PREFIX + 2, c.type());

//		assertTrue(it.hasNext());
//		c = it.next();
//		assertEquals(0, c.start());
//		assertEquals(3, c.end());
//		assertEquals("any", test.substring(c.start(), c.end()));
//		assertEquals(ITermService.CORE_TERM_PREFIX + 1, c.type());

		assertTrue(it.hasNext());
		c = it.next();
		assertEquals(4, c.start());
		assertEquals(9, c.end());
		assertEquals("binds", test.substring(c.start(), c.end()));
	}

	@Test
	public void testSpecialTerms2() {
		String test = "* binds notindict";
		Chunking chunking = chunker.chunk(test);

		Iterator<Chunk> it = chunking.chunkSet().iterator();
		Chunk c;

		assertTrue(it.hasNext());
		c = it.next();
		assertEquals(0, c.start());
		assertEquals(1, c.end());
		assertEquals("*", test.substring(c.start(), c.end()));
		assertTrue(c.type().equals(
				ITermService.CORE_TERM_PREFIX + 1)
				|| c.type().equals(ITermService.CORE_TERM_PREFIX + 0));

		assertTrue(it.hasNext());
//		c = it.next();
//		assertEquals(0, c.start());
//		assertEquals(1, c.end());
//		assertEquals("*", test.substring(c.start(), c.end()));
//		assertTrue(c.type().equals(
//				ITermService.CORE_TERM_PREFIX + 1)
//				|| c.type().equals(ITermService.CORE_TERM_PREFIX + 0));

//		assertTrue(it.hasNext());
//		c = it.next();
//		assertEquals(0, c.start());
//		assertEquals(1, c.end());
//		assertEquals("*", test.substring(c.start(), c.end()));
//		assertTrue(c.type().equals(ITermService.CORE_TERM_PREFIX + 2) || c.type().equals(
//				ITermService.CORE_TERM_PREFIX + 1)
//				|| c.type().equals(ITermService.CORE_TERM_PREFIX + 0));

		assertTrue(it.hasNext());
		c = it.next();
		assertEquals(2, c.start());
		assertEquals(7, c.end());
		assertEquals("binds", test.substring(c.start(), c.end()));
	}
}
