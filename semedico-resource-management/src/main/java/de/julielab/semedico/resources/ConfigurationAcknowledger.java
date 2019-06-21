package de.julielab.semedico.resources;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.TreeSet;

public class ConfigurationAcknowledger implements IConfigurationAcknowledger {

	private static final Logger log = LoggerFactory.getLogger(ConfigurationAcknowledger.class);
	
	public ConfigurationAcknowledger() {

	}

	@Override
	public int acknowledgeConfiguration() {
		String configFileName = System.getProperty("semedico.configuration");
		if (null == configFileName) {
			String username = System.getProperty("user.name");
			configFileName = "configuration.properties." + username;
		}
		System.out.println("Seeking acknowledgement for configuration file \"" + configFileName + "\"");
		Properties properties = new Properties();
		try {
			InputStream configStream = getClass().getResourceAsStream("/" + configFileName);
			if (null != configStream)
				properties.load(configStream);
			else
				System.out.println("WARNING: Configuration was not found, factory defaults are used.");
			int maxKeyLength = 0;
			for (Object key : properties.keySet()) {
				if (key.toString().length() > maxKeyLength)
					maxKeyLength = key.toString().length();
			}
			System.out.println("The following configuration will be used for this process:");
			for (Object key : new TreeSet<>(properties.keySet())) {
				Object value = properties.get(key);
				System.out.println(StringUtils.rightPad(key.toString(), maxKeyLength) + " : " + value);
			}
			System.out.println("Do you want to proceed with this configuration? (y/n)");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String response;
			while (!(response = br.readLine()).equalsIgnoreCase("y") && !response.equalsIgnoreCase("n")) {
				System.out.println("Please enter (y)es or (n)o.");
			}
			return response.equalsIgnoreCase("y") ? 0 : 1;
		} catch (IOException e) {
			log.error("IOException: ", e);
		}
		return 1;
	}

}
