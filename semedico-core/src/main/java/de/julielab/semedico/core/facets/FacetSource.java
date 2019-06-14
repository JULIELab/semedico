package de.julielab.semedico.core.facets;

public class FacetSource {

    private final SourceType srcType;
    private final String srcName;

    public FacetSource(SourceType srcType, String srcName) {
        this.srcType = srcType;
        this.srcName = srcName;
    }

    /**
     * @return the name of the index field for which terms should be counted
     */
    public String getName() {
        return srcName;
    }

    /**
     * @return the type
     */
    public SourceType getType() {
        return srcType;
    }

    public boolean isFlat() {
        return !srcType.isHierarchic();
    }

    public boolean isHierarchic() {
        return srcType.isHierarchic();
    }

    public boolean isDatabaseTermSource() {
        return srcType.isDatabaseTermSource();
    }


    public boolean isStringTermSource() {
        return srcType.isStringTermSource();
    }
    public boolean isAggregation() {
        return srcType.isAggregation();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FacetSource [srcType=" + srcType + ", srcName=" + srcName + "]";
    }

    public static enum SourceType {
        FIELD_STRINGS {

            @Override
            public boolean isDatabaseTermSource() {
                return false;
            }

            @Override
            public boolean isHierarchic() {
                return false;
            }

            @Override
            public boolean isAggregation() {
                return false;
            }
        },
        FIELD_TAXONOMIC_TERMS {

            @Override
            public boolean isDatabaseTermSource() {
                return true;
            }

            @Override
            public boolean isHierarchic() {
                return true;
            }

            @Override
            public boolean isAggregation() {
                return false;
            }
        },
        FIELD_FLAT_TERMS {

            @Override
            public boolean isDatabaseTermSource() {
                return true;
            }

            @Override
            public boolean isHierarchic() {
                return false;
            }

            @Override
            public boolean isAggregation() {
                return false;
            }

        },
        FACET_AGGREGATION {

            @Override
            public boolean isDatabaseTermSource() {
                return true;
            }

            @Override
            public boolean isHierarchic() {
                return false;
            }

            @Override
            public boolean isAggregation() {
                return true;
            }

        },
        KEYWORD {

            @Override
            public boolean isDatabaseTermSource() {
                return false;
            }

            @Override
            public boolean isHierarchic() {
                return false;
            }

            @Override
            public boolean isAggregation() {
                return false;
            }

        };
        /**
         * Determines whether this facet facetSource contains IDs of 'real' terms. Meant with that are terms which have known
         * synonyms, writing variants etc. This is in contrast to author names, for example, where we don't know
         * anything but the author name string itself.
         *
         * @return <code>true</code> iff this facetSource contains term IDs for terms which are managed by the
         *         {@link ITermService}.
         */
        public abstract boolean isDatabaseTermSource();

        /**
         * Determines whether this facet facetSource contains terms which are defined by the exact <em>Lucene</em> terms in
         * an index field. This is the case for authors or for years, for example. These <em>string terms</em> are
         * defined completely by their string appearance and have no known synonyms or writing variants. That is, two
         * different author name strings could, in the real world, refer to the same person, but we don't know about it
         * because we have no way to find out.
         *
         * @return <code>true</code> iff this facetSource contains string terms rather then IDs for full-defined terms.
         */
        public boolean isStringTermSource() {
            return !isDatabaseTermSource();
        }

        /**
         * Determines whether the terms in this facet form a taxonomy.
         *
         * @return <code>true</code> iff the terms contained in this facetSource have a taxonomic structure.
         */
        public abstract boolean isHierarchic();

        /**
         * Determines whether this facet is just an aggregation of other facets.
         *
         * @return <code>true</code> iff the term contents of this facetSource is actually an agglomeration of terms from
         *         other facet sources.
         */
        public abstract boolean isAggregation();
    }
}

