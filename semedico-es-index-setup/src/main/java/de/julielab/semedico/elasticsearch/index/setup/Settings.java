package de.julielab.semedico.elasticsearch.index.setup;

public class Settings {
	public static final int DEFAULT_NUM_REPLICAS = 0;
	public static final int DEFAULT_NUM_SHARDS = 2;
	public final Analysis analysis;

	public Settings(int number_of_replicas, int number_of_shards, Analysis analysis) {
		super();
		this.number_of_replicas = number_of_replicas;
		this.number_of_shards = number_of_shards;
		this.analysis = analysis;
	}

	public Settings(Analysis analysis) {
		this (Settings.DEFAULT_NUM_REPLICAS, Settings.DEFAULT_NUM_SHARDS, analysis);
	}

	public final int number_of_replicas;
	public final int number_of_shards;
}
