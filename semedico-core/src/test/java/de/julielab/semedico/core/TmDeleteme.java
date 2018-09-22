package de.julielab.semedico.core;

import de.julielab.topicmodeling.businessobjects.Model;
import de.julielab.topicmodeling.businessobjects.Topic;
import de.julielab.topicmodeling.services.MalletTopicModeling;
import org.testng.annotations.Test;

import java.io.File;
import java.util.stream.Collectors;

public class TmDeleteme {

    @Test
    public void testZeugs(){
        MalletTopicModeling malletTopicModeling = new MalletTopicModeling();
        Model model = malletTopicModeling.readModel("/Users/faessler/tmp/tm-random100k.mod-h1.coling.uni-jena.de-28969@h1.coling.uni-jena.de");
        model.index.forEach((pmid, topiclist) -> System.out.println(pmid + ": " + topiclist.stream().map(t -> ""+t.id).collect(Collectors.joining(", "))));
        System.out.println("Size of index: " + model.index.size());
    }
}
