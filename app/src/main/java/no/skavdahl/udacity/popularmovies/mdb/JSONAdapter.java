package no.skavdahl.udacity.popularmovies.mdb;

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
	private final String MOVIEDB_DATE_FORMAT = "yyyy-MM-dd";

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
		String dateString = obj.optString(attrName).trim();
		if (dateString.length() == 0)
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
}
