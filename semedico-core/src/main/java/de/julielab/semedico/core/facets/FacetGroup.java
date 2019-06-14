/**
 * FacetGroup.java
 * <p>
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * <p>
 * Author: faessler
 * <p>
 * Current version: 1.0
 * Since version:   1.0
 * <p>
 * Creation date: 02.08.2011
 */

/**
 *
 */
package de.julielab.semedico.core.facets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.julielab.neo4j.plugins.datarepresentation.constants.FacetGroupConstants;
import de.julielab.semedico.commons.concepts.FacetGroupLabels;
import org.apache.commons.lang.NotImplementedException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


/**
 * A class to assemble all <code>Facet</code> objects belonging to the same display group.
 * <p>
 * This grouping serves the sole purpose to reflect which facets to display together (e.g. by selecting "BioMed",
 * "Immunology" etc. in the front end) and in which order displaying should occur.
 * </p>
 * <p>
 * The display order is determined by the positions of <code>Facet</code> objects within the <code>FacetGroup</code>
 * (which is derived from <code>ArrayList&lt;Facet&gt;</code> and thus ordered).
 * </p>
 *
 * @author faessler
 *
 */
public class FacetGroup<T extends Facet> extends ArrayList<T> implements Comparable<FacetGroup<T>> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private  Multimap<String, T> facetsBySourceName;
    /**
     * Determines the display position of this <code>FacetGroup</code> on the front end.
     */
    private int position;
    private String name;
    private String id;
    @JsonProperty(FacetGroupConstants.KEY_LABELS)
    private Set<FacetGroupLabels> labels;
    private FacetGroupLabels.Type type;

    public Set<FacetGroupLabels> getLabels() {
        return labels;
    }

    public void setLabels(Set<FacetGroupLabels> labels) {
        this.labels = labels;
    }

    public FacetGroup() {
    }

    public FacetGroup(String name, int position
                      // , boolean showForBTerms
    ) {
        this.name = name;
        this.position = position;
        // this.showForBTerms = showForBTerms;
        facetsBySourceName = HashMultimap.create();
    }

    public String getId() {
        return id;
    }

    // private final boolean showForBTerms;

    public void setId(String id) {
        this.id = id;
    }

    public Collection<T> getElementsBySourceName(String srcName) {
        Collection<T> cachedFacet = facetsBySourceName.get(srcName);
        if (cachedFacet.isEmpty()) {
            for (T facet : this) {
                if (facet.getSource().getName().equals(srcName)) {
                    facetsBySourceName.put(srcName, facet);
                }
            }
        }
        return facetsBySourceName.get(srcName);
    }

    public Collection<T> getTaxonomicalElements() {
        Collection<T> facets = new HashSet<T>();
        for (T facet : this)
            if (facet.isHierarchic())
                facets.add(facet);
        return facets;
    }

    public Collection<T> getFlatElements() {
        Collection<T> facets = new HashSet<T>();
        for (T facet : this)
            if (facet.isFlat())
                facets.add(facet);
        return facets;
    }

    public UIFacetGroup getUiFacetGroup() {
        return new UIFacetGroup(name, position
                // , showForBTerms
        );
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(FacetGroup<T> facetGroup) {
        return this.position - facetGroup.position;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the showForBTerms
     */
    public boolean showForBTerms() {
        throw new NotImplementedException("I thought we wouldn't need that");
        // return showForBTerms;
    }

    public FacetGroupLabels.Type getType() {
        return type;
    }

    public void setType(FacetGroupLabels.Type facetGroupType) {
        this.type = facetGroupType;

    }

}
