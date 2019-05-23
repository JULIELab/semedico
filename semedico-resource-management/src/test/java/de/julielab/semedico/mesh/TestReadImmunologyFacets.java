package de.julielab.semedico.mesh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import de.julielab.semedico.mesh.components.Descriptor;
import de.julielab.semedico.mesh.exchange.DataImporter;

/**
 * Just a small test to be sure that the immunology facets are imported with their correct structure.
 * 
 * Those facets should not be updated to MeSH because they are quite custom-made. We just read them from their original
 * XML format and import them into the Semedico database. Thus we want to check here that reading already results in the
 * correct structure.
 * 
 * @author faessler
 * 
 */
public class TestReadImmunologyFacets {
	@Test
	public void testReadImmunologyFacets() throws Exception {
		Tree data = new Tree("Immunology");
		DataImporter.fromUserDefinedMeshXml("data/input/ud-mesh/immunology", data);

		// Check that all facets are there and at the right place.
		// First, make a hand-crafted list of expected facet nodes (here represented as descriptors in the tree).
		ArrayList<Descriptor> expectedFacets = Lists.newArrayList(data.getDescriptorByUi("F_Blood Cells"),
				data.getDescriptorByUi("F_Epitopes and Binding Sites"),
				data.getDescriptorByUi("F_Hematopoietic Progenitor Cells"),
				data.getDescriptorByUi("F_Immune Processes"),
				data.getDescriptorByUi("F_Immunoglobulins and Antibodies"),
				data.getDescriptorByUi("F_Minor Histocompatibility Antigens"),
				data.getDescriptorByUi("F_Transplantation"));
		List<Descriptor> facetDescriptors = data.childDescriptorsOf(data.getRootDesc());
		Collections.sort(facetDescriptors);
		assertEquals(expectedFacets, facetDescriptors);

		Descriptor desc;
		Descriptor expectedParentDesc;
		// epitopes:
		// <term id="D000939">
		desc = data.getDescriptorByUi("D000939");
		assertTrue(data.parentDescriptorsOf(desc).contains(data.getDescriptorByUi("F_Epitopes and Binding Sites")));
		// <term id="D018984" parent-id="D000939">
		desc = data.getDescriptorByUi("D018984");
		expectedParentDesc = data.getDescriptorByUi("D000939");
		assertTrue(data.parentDescriptorsOf(desc).contains(expectedParentDesc));
		
		// igs_antibodies_supp:
		// <term id="D051925" parent-id="D011947">
		desc = data.getDescriptorByUi("D051925");
		expectedParentDesc = data.getDescriptorByUi("D011947");
		assertTrue(data.parentDescriptorsOf(desc).contains(expectedParentDesc));
		// <term id="C108577" parent-id="D000911">
		desc = data.getDescriptorByUi("C108577");
		expectedParentDesc = data.getDescriptorByUi("D000911");
		assertTrue(data.parentDescriptorsOf(desc).contains(expectedParentDesc));
		
		// transplantation
		// <term id="D018380" parent-id="D033581">
		desc = data.getDescriptorByUi("D018380");
		expectedParentDesc = data.getDescriptorByUi("D033581");
		assertTrue(data.parentDescriptorsOf(desc).contains(expectedParentDesc));
		
		// leukocytes
		// <term id="D007801" parent-id="myeloid_dendritic_cell">
		desc = data.getDescriptorByUi("D007801");
		expectedParentDesc = data.getDescriptorByUi("myeloid_dendritic_cell");
		assertTrue(data.parentDescriptorsOf(desc).contains(expectedParentDesc));		
	}
}
