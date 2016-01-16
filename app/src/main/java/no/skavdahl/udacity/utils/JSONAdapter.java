package no.skavdahl.udacity.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Base class for adapter objects for JSON data retrieved from themoviedb.org services.
 * This class provides a few utility methods that simplifies access to formatted JSON
 * attribute data.
 *
 * @author fdavs
 */
public abstract class JSONAdapter {

	/** Format string for dates as used in JSON responses from themoviedb.org. */
	private static final String MOVIEDB_DATE_FORMAT = "yyyy-MM-dd";

	/**
	 * Returns the value mapped by name if it exists. If the value does not exist,
	 * or is the value "null", {@code null} is returned.
	 *
	 * @param obj The JSON object from which to read values
	 * @param attrName The attribute of the JSON object to access
	 *
	 * @return the value as a string or {@code null}.
	 *
	 * @throws JSONException if unable to access the attribute value.
	 */
	protected static String getOptString(JSONObject obj, String attrName) throws JSONException {
		String value = obj.optString(attrName, null);
		return "null".equals(value) ? null : value;
	}

	/**
	 * Returns the value mapped by name if it exists and can be parsed as a date string
	 * formatted as yyyy-mm-dd, or throws if no such mapping exists.
	 *
	 * @param obj The JSON object from which to read values
	 * @param attrName The attribute of the JSON object to access. This attribute
	 *                 must have a value formatted as yyyy-mm-dd or be an empty string.
	 *
	 * @return the value as a date or <code>null</code> if the JSON object does not
	 *         specify a date value.
	 *
	 * @throws JSONException if unable to parse the attribute value as a date.
	 */
	protected static Date getOptDate(JSONObject obj, String attrName) throws JSONException {
		String dateString = obj.optString(attrName);
		if (dateString == null)
			return null;

		dateString = dateString.trim();
		if (dateString.length() == 0 || "null".equals(dateString))
			return null;

		try {
			DateFormat dateParser = new SimpleDateFormat(MOVIEDB_DATE_FORMAT, Locale.US);
			return dateParser.parse(dateString);
		}
		catch (ParseException e) {
			throw new JSONException("Invalid date string: '" + dateString + "'");
		}
	}
}
