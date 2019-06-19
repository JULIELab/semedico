package de.julielab.semedico.core;

import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;

public class TestUtils {

	private static final Logger log = LoggerFactory.getLogger(TestUtils.class);
	public static final String neo4jTestEndpoint = SemedicoCoreTestModule.neo4jTestEndpoint;
	public static final String neo4jTestUser = SemedicoCoreTestModule.neo4jTestUser;
	public static final String neo4jTestPassword = SemedicoCoreTestModule.neo4jTestPassword;

	public static Registry createTestRegistry() {
		return createTestRegistry(SemedicoCoreTestModule.class);
	}
	
	public static Registry createTestRegistry(Class<?> moduleClass) {
//		setTestConfigurationSystemProperties();
		
		RegistryBuilder builder = new RegistryBuilder();

		builder.add(moduleClass);

		return builder.build();
	}

	public static boolean isAddressReachable(String address) {
		boolean reachable = false;
		try {
			URLConnection connection = new URL(address).openConnection();
			connection.connect();
			// If we've come this far without an exception, the connection is
			// available.
			reachable = true;
		} catch (ConnectException e) {
			// don't do anything, the warning will be logged below.
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!reachable)
			log.warn(
					"TESTS INVOLVING ADDRESS \"{}\" ARE NOT PERFORMED BECAUSE THE SERVER COULD NOT BE REACHED.",
					address);
		return reachable;
	}
}
