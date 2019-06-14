package de.julielab.semedico.core.services;

import de.julielab.semedico.core.services.interfaces.ITopicModelService;
import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.businessobjects.TMSearchResult;
import de.julielab.topicmodeling.services.MalletTopicModeling;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.tapestry5.ioc.annotations.Symbol;

import java.io.File;

import static de.julielab.semedico.core.services.SemedicoSymbolConstants.TOPIC_MODEL_PATH;

public class TopicModelService implements ITopicModelService {

    private final MalletTopicModeling malletTopicModeling;
    private final XMLConfiguration topicModelConfiguration;
    private final Model topicModel;

    public TopicModelService(@Symbol(TOPIC_MODEL_PATH) File topicModelPath, @Symbol(SemedicoSymbolConstants.TOPIC_MODEL_CONFIG) String tmConfigPath) throws ConfigurationException {
        this.malletTopicModeling = new MalletTopicModeling();
        this.topicModelConfiguration = malletTopicModeling.loadConfig(tmConfigPath);
        this.topicModel = malletTopicModeling.readMalletModel(topicModelPath);
    }

    @Override
    public MalletTopicModeling getTopicModeling() {
        return malletTopicModeling;
    }

    @Override
    public Model getTopicModel() {
        return topicModel;
    }

    @Override
    public XMLConfiguration getTopicModelConfiguration() {
        return topicModelConfiguration;
    }

    @Override
    public TMSearchResult search(Document queryDocument) {
        return malletTopicModeling.search(queryDocument, topicModel, topicModelConfiguration);
    }
}
