package de.julielab.semedico.core.search.query;

public class DefaultSearchStrategy implements SearchStrategy {
    public static final SearchStrategy DEFAULT_STRATEGY = new DefaultSearchStrategy();

    @Override
    public String name() {
        return "DefaultSearchStrategy";
    }
}
