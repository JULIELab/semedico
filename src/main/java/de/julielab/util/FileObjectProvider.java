package de.julielab.util;

import java.io.File;

import org.apache.hivemind.Location;
import org.apache.hivemind.internal.Module;
import org.apache.hivemind.service.ObjectProvider;

public class FileObjectProvider implements ObjectProvider {

	@Override
	public Object provideObject(Module module, Class clazz, String file,
			Location location) {
		return new File(file);
	}

}
