package de.julielab.semedico.core.services.interfaces;

import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.businessobjects.TMSearchResult;
import de.julielab.topicmodeling.services.MalletTopicModeling;
import org.apache.commons.configuration2.XMLConfiguration;

public interface ITopicModelService {
    MalletTopicModeling getTopicModeling();

    Model getTopicModel();

    XMLConfiguration getTopicModelConfiguration();

    TMSearchResult search(Document queryDocument);
}
