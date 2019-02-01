package de.julielab.semedico.core.docmod.base.broadcasting;

import de.julielab.semedico.core.docmod.base.defaultmodule.entities.DefaultSerpItem;
import de.julielab.semedico.core.docmod.base.defaultmodule.entities.DefaultSerpItemCollector;
import de.julielab.semedico.core.docmod.base.entities.SerpItemResult;

/**
 * Adds a {@link DefaultSerpItemCollector} to the BroadcastResult which in turn returns {@link SerpItemResult}&lt;{@link DefaultSerpItem}&gt;.
 */
public class SerpItemCollectorBroadcast implements IResultCollectorBroadcast {
    @Override
    public String getResultBaseName() {
        return "serpitems";
    }

    @Override
    public String toString() {
        return getResultBaseName();
    }
}
