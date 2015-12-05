package no.skavdahl.udacity.popularmovies.mdb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
	private final String MOVIEDB_DATE_FORMAT = "yyyy-MM-dd";

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
	protected String getOptString(JSONObject obj, String attrName) throws JSONException {
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
	protected Date getOptDate(JSONObject obj, String attrName) throws JSONException {
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

	/**
	 * Maps name to value, clobbering any existing name/value mapping with the same name.
	 * If the value is null, any existing mapping for name is removed.
	 *
	 * @param obj The JSON object to mutate
	 * @param attrName The name of the attribute to update
	 * @param value The attribute value, <code>null</code> allowed.
	 *
	 * @throws JSONException if unable to store the value
	 *
	 * @see org.json.JSONObject#put(String, Object)
	 */
	protected void putOptDate(JSONObject obj, String attrName, Date value) throws JSONException {
		if (value == null)
			obj.put(attrName, null); // removes the existing value for the attribute
		else {
			DateFormat dateFormat = new SimpleDateFormat(MOVIEDB_DATE_FORMAT, Locale.US);
			String dateString = dateFormat.format(value);
			obj.put(attrName, dateString);
		}
	}

	/**
	 * Returns the contents of a JSON array as an array of integers.
	 *
	 * @param arr The JSON array from which to read values
	 *
	 * @return a (possibly empty) list of integers.
	 *
	 * @throws JSONException if unable to parse the array.
	 */
	protected List<Integer> getOptIntArray(JSONArray arr) throws JSONException {
		if (arr == null)
			return Collections.emptyList();

		final int len = arr.length();
		List<Integer> result = new ArrayList<>(len);
		for (int i = 0; i < len; ++i) {
			result.add(arr.getInt(i));
		}

		return result;
	}

	/**
	 * Returns the contents of a JSON array as an array of integers.
	 *
	 * @param obj The JSON object to mutate
	 * @param attrName The name of the attribute to update
	 * @param list The attribute value, <code>null</code> allowed.
	 *
	 * @return a (possibly empty) list of integers.
	 *
	 * @throws JSONException if unable to parse the array.
	 */
	protected void putOptArray(JSONObject obj, String attrName, List<Integer> list) throws JSONException {
		if (list == null)
			obj.put(attrName, null);

		JSONArray arr = new JSONArray(list);
		obj.put(attrName, arr);
	}
}
