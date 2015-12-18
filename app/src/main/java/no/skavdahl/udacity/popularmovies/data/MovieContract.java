package no.skavdahl.udacity.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Data contract for movie information.
 *
 * @author fdavs
 */
public class MovieContract {

	public static final String CONTENT_AUTHORITY = "no.skavdahl.udacity.popularmovies";

	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	public static final String PATH_MOVIE = "movie";
	public static final String PATH_IMAGE = "image";

	/**
	 * Defines the table contents for movie data.
	 */
	public static final class MovieTable implements BaseColumns {
		public static final String TABLE_NAME = "movie";

		/** Bool -- if true (1), this movie is a favorite movie. */
		public static final String COLUMN_FAVORITE = "favorite";

		/**
		 * Timestamp -- when the data in this entry were downloaded from the server.
		 * This timestamp is used decide which movie entries are old and elegible for
		 * purging from the database to free space.
		 */
		public static final String COLUMN_DOWNLOAD_TIME = "dltime";

		/**
		 * String -- the movie information encoded as a JSON string.
		 */
		public static final String COLUMN_JSON = "datajson";
	}

	/**
	 * Defines the table contents for image data.
	 */
	public static final class ImageTable implements BaseColumns {
		public static final String TABLE_NAME = "image";

		/**
		 * Integer -- references the movie this image is associated with. If that movie
		 * is deleted from the database, then this image should be deleted as well.
		 */
		public static final String COLUMN_MOVIE_ID = "movieid";

		/**
		 * Image width specification, corresponding to the image size specifier that was used
		 * to download the image from themoviedb.org. Most often this will be a number like
		 * 800 (pixels) but it can also be the string 'original'.
		 */
		public static final String COLUMN_WIDTH = "width";

		/** Blob -- the image data. */
		public static final String COLUMN_IMAGEDATA = "imagedata";
	}

	/**
	 * Defines the table contents for enumerations like genre (id and label).
	 *
	 * /genre/movie/list
	 *   {
	 *	   "id": 28,
	 *	   "name": "Action"
	 *	 },
	 *	 {
	 *	   "id": 12,
	 *	   "name": "Adventure"
	 '	 }
	 */
	public static final class EnumerationTable implements BaseColumns {

		//public static final Uri CONTENT_URI =
		//	BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

		//public static final String CONTENT_TYPE =
		//	ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
		//public static final String CONTENT_ITEM_TYPE =
		//	ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

		public static final String TABLE_NAME = "enumeration";

		public static final String COLUMN_ECLASS = "enumclass";  // genre
		public static final String COLUMN_EID = "enumid";
		public static final String COLUMN_ENAME = "enumname";

		//public static Uri buildLocationUri(long id) {
		//	return ContentUris.withAppendedId(CONTENT_URI, id);
		//}
	}
}
