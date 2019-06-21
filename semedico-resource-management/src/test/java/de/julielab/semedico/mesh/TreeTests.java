package de.julielab.semedico.mesh;

import de.julielab.semedico.mesh.components.TreeVertex;
import de.julielab.semedico.mesh.exchange.DataImporter;
import de.julielab.semedico.mesh.modifications.DescAdditions;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.*;

public class TreeTests extends TestBase {
	
	/* Tree used in source.xml:
               V01
	           / \
	        V02   V03
	       /   \
	    V04     V05
	*/

	@Test
	public void testParentVertexOfString() {
		Tree source = new Tree("source.xml");
		DescAdditions descs = DataImporter.fromOwnXML(testInPath + "source.xml");
		descs.apply(source);
		TreeVertex vertex = source.parentVertexOf("V05");
		assertEquals("V02", vertex.getName());
	}

	@Test
	public void testParentVertexOfTopString() {
		Tree source = new Tree("source.xml");
		DescAdditions descs = DataImporter.fromOwnXML(testInPath + "source.xml");
		descs.apply(source);
		TreeVertex vertex = source.parentVertexOf("V01");
		assertEquals("root", vertex.getName());
	}

	@Test
	public void testParentVertexOfNotInTreeString() {
		Tree source = new Tree("source.xml");
		DescAdditions descs = DataImporter.fromOwnXML(testInPath + "source.xml");
		descs.apply(source);
		TreeVertex vertex = source.parentVertexOf("NotInTree");
		assertNull(vertex);
	}

	@Test
	public void testIsParentVertexTrue() {
		Tree source = new Tree("source.xml");
		DescAdditions descs = DataImporter.fromOwnXML(testInPath + "source.xml");
		descs.apply(source);
		TreeVertex V05 = source.getVertex("V05");
		TreeVertex V02 = source.getVertex("V02");
		assertTrue(source.isParentVertex(V02, V05));
	}

	@Test
	public void testIsParentVertexFalse() {
		Tree source = new Tree("source.xml");
		DescAdditions descs = DataImporter.fromOwnXML(testInPath + "source.xml");
		descs.apply(source);
		TreeVertex V05 = source.getVertex("V05");
		TreeVertex V01 = source.getVertex("V01");
		assertFalse(source.isParentVertex(V01, V05));
	}

	@Test
	public void testIsParentVertexParentNull() {
		Tree source = new Tree("source.xml");
		DescAdditions descs = DataImporter.fromOwnXML(testInPath + "source.xml");
		descs.apply(source);
		TreeVertex V05 = source.getVertex("V05");
		TreeVertex V02 = source.getVertex(null);
		assertFalse(source.isParentVertex(V02, V05));
	}

	@Test
	public void testIsParentVertexChildNull() {
		Tree source = new Tree("source.xml");
		DescAdditions descs = DataImporter.fromOwnXML(testInPath + "source.xml");
		descs.apply(source);
		TreeVertex V05 = source.getVertex(null);
		TreeVertex V02 = source.getVertex("V02");
		assertFalse(source.isParentVertex(V02, V05));
	}

	@Test
	public void testChildVerticesOfNull() {
		Tree source = new Tree("source.xml");
		DescAdditions descs = DataImporter.fromOwnXML(testInPath + "source.xml");
		descs.apply(source);
		TreeVertex V05 = null;
		List<TreeVertex> children = source.childVerticesOf(V05);
		assertTrue(children.isEmpty());
	}

	@Test
	public void testChildVertices() {
		Tree source = new Tree("source.xml");
		DescAdditions descs = DataImporter.fromOwnXML(testInPath + "source.xml");
		descs.apply(source);
		TreeVertex V02 = source.getVertex("V02");
		List<TreeVertex> children = source.childVerticesOf(V02);
		assertThat(children, hasItems(source.getVertex("V04"), source.getVertex("V05")));
	}

	@Test
	public void testChildVerticesUnknown() {
		Tree source = new Tree("source.xml");
		DescAdditions descs = DataImporter.fromOwnXML(testInPath + "source.xml");
		descs.apply(source);
		TreeVertex unknown = new TreeVertex("partialTreeNumber", "name", "descName", "descUi");
		List<TreeVertex> children = source.childVerticesOf(unknown);
		assertTrue(children.isEmpty());
	}

}
