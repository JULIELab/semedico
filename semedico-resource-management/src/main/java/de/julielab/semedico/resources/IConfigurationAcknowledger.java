package de.julielab.semedico.resources;

public interface IConfigurationAcknowledger {
	/**
	 * Prompts the user to accept or reject the currently active configuration to avoid harm made by unintentionally
	 * using the wrong configuration.
	 * 
	 * @return 0 if the user accepted, 1 otherwise.
	 */
	int acknowledgeConfiguration();
}
