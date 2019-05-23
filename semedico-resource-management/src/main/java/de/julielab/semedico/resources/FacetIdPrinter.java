package de.julielab.semedico.resources;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.IFacetService;

public class FacetIdPrinter implements IFacetIdPrinter {

	private IFacetService facetService;

	public FacetIdPrinter(IFacetService facetService) {
		this.facetService = facetService;
	}

	@Override
	public void printFacetIds() {
		List<Facet> facets = facetService.getFacets();
		int numColumns = 3;
		int facetsPerColumn = (int) Math.ceil(facets.size() / (double) numColumns);
		List<List<Facet>> columns = new ArrayList<>();
		for (int i = 0; i < numColumns; i++)
			columns.add(new ArrayList<Facet>());
		int facetInColumnCounter = 0;
		int columnIndex = 0;
		for (Facet facet : facets) {
			columns.get(columnIndex).add(facet);
			facetInColumnCounter++;
			if (facetInColumnCounter >= facetsPerColumn) {
				columnIndex++;
				facetInColumnCounter = 0;
			}
		}
		List<List<String>> outputColumns = new ArrayList<>();
		for (int i = 0; i < numColumns; i++)
			outputColumns.add(new ArrayList<String>());
		for (int i = 0; i < numColumns; i++) {
			List<Facet> column = columns.get(i);
			List<String> outputColumn = outputColumns.get(i);
			int maxLength = 0;
			for (Facet facet : column) {
				String output = facet.getName() + ": " + facet.getId();
				outputColumn.add(output);
				if (output.length() > maxLength)
					maxLength = output.length();
			}
			for (int j = 0; j < outputColumn.size(); j++) {
				String outputString = outputColumn.get(j);
				outputColumn.set(j, StringUtils.rightPad(outputString, maxLength));
			}

		}
		for (int i = 0; i < outputColumns.get(0).size(); i++) {
			List<String> outputStrings = new ArrayList<>();
			for (List<String> column : outputColumns) {
				String output = "";
				if (i < columns.size()) {
					output = column.get(i);
				}
				outputStrings.add(output);
			}

			System.out.println(StringUtils.join(outputStrings, "\t\t\t"));
		}

	}

}
