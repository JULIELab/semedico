package de.julielab.semedico;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.ParseException;
import org.apache.tapestry5.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
	
	private static final Logger log = LoggerFactory.getLogger(Utils.class);

	/**
	 * Interprets the content of the JSONArray <tt>jsonArray</tt> as bytes encoding a string. If <tt>gunzip</tt> is set
	 * to <tt>true</tt> the byte array is expected to represent a gzipped string and will be gunzipped before writing to
	 * file <tt>outputFilePath</tt>.
	 * 
	 * @param jsonArray
	 * @param outputFilePath
	 * @param gunzip
	 */
	public static void writeByteJsonArrayToStringFile(JSONArray jsonArray, String outputFilePath, boolean gunzip) {
		try {
			byte[] bytes = new byte[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {
				bytes[i] = (byte) jsonArray.getInt(i);
			}
			InputStream is = new ByteArrayInputStream(bytes);
			if (gunzip)
				is = new GZIPInputStream(is);
			byte[] buffer = new byte[2048];
			try (FileOutputStream os = new FileOutputStream(outputFilePath)) {
				int bytesRead = -1;
				while ((bytesRead = IOUtils.read(is, buffer)) > 0) {
					IOUtils.write(new String(buffer, 0, bytesRead), os, "UTF-8");
				}
			}
		} catch (ParseException | IOException e) {
			log.error("ParseException or IOException: ", e);
		}
	}
	
	public static void writeByteArrayToStringFile(byte[] data, String outputFilePath, boolean gunzip) {
		try {
			InputStream is = new ByteArrayInputStream(data);
			if (gunzip)
				is = new GZIPInputStream(is);
			byte[] buffer = new byte[2048];
			try (FileOutputStream os = new FileOutputStream(outputFilePath)) {
				int bytesRead = -1;
				while ((bytesRead = IOUtils.read(is, buffer)) > 0) {
					IOUtils.write(new String(buffer, 0, bytesRead), os, "UTF-8");
				}
			}
		} catch (ParseException | IOException e) {
			log.error("ParseException or IOException: ", e);
		}
	}
}
