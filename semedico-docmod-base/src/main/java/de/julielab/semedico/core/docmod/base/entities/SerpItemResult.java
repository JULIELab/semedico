package de.julielab.semedico.core.docmod.base.entities;

import de.julielab.semedico.core.search.results.highlighting.AbstractSerpItem;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;

import java.util.ArrayList;
import java.util.List;

public class SerpItemResult<S extends AbstractSerpItem> extends SemedicoSearchResult {
    private List<S> items;

    public SerpItemResult(List<S> items) {
        setItems(items);
    }

    public SerpItemResult() {
    }

    public List<S> getItems() {
        return items;
    }

    public void setItems(List<S> items) {
        this.items = items;
    }

    public void addSerpItem(S item) {
        if (items == null)
            items = new ArrayList<>();
        items.add(item);

    }
}
