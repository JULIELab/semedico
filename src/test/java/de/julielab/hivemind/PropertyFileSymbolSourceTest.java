package de.julielab.hivemind;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PropertyFileSymbolSourceTest {

	private final String TEST_PROPERTIES = "src/test/resources/test.properties";
	
	@Test
	public void testValueForSymbol(){
		System.getProperties().put(PropertiesFileSymbolSource.FILE_KEY, TEST_PROPERTIES);
		PropertiesFileSymbolSource propertyFileSymbolSource = new PropertiesFileSymbolSource();
		
		assertEquals("value1",propertyFileSymbolSource.valueForSymbol("key1"));
	}
	
}
