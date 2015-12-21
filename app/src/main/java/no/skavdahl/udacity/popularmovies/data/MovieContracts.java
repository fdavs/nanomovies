package no.skavdahl.udacity.popularmovies.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Data contract for movie information.
 *
 * @author fdavs
 */
public class MovieContracts {

	public static final int CONTRACT_VERSION = 1;

	public static final String CONTENT_AUTHORITY = "no.skavdahl.udacity.popularmovies";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	/**
	 * Defines the contract for movie data.
	 *
	 * <p>Supported content provider URIs:
	 * <ul>
	 *   <li>Movie lists:<br/>
	 *       <tt>content://no.skavdahl.udacity.popularmovies/movie/[listname *]/[page #]</tt><br/>
	 *       Accesses one page of the specified movie list</li>
	 *
	 *   <li>Movie details:<br/>
	 *       <tt>content://no.skavdahl.udacity.popularmovies/movie/[id #]</tt><br/>
	 *       Accesses detailed information for one specific movie</li>
	 * </ul></p>
	 */
	public static final class MovieContract {

		// --- content provider ---

		public static final String CONTENT_URI_PATH = "movie";

		public static final Uri CONTENT_URI =
			BASE_CONTENT_URI.buildUpon().appendPath(CONTENT_URI_PATH).build();

		public static final String CONTENT_DIR_YPE =
			ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + CONTENT_URI_PATH + "_v" + CONTRACT_VERSION;
		public static final String CONTENT_ITEM_TYPE =
			ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + CONTENT_URI_PATH + "_v" + CONTRACT_VERSION;

		public static Uri buildMovieListUri(String listName, int page) {
			return CONTENT_URI.buildUpon()
				.appendPath(listName)
				.appendPath(Integer.toString(page))
				.build();
		}

		public static Uri buildMovieDetailUri(int movieId) {
			return CONTENT_URI.buildUpon()
				.appendPath(Integer.toString(movieId))
				.build();
		}

		// --- database ---

		public static final String TABLE_NAME = "movie";

		public static final class Column implements BaseColumns {

			/**
			 * Bool -- if true (1), this movie is a favorite movie.
			 */
			public static final String FAVORITE = "favorite";

			/**
			 * Timestamp -- when the data in this entry were downloaded from the server.
			 * This timestamp is used decide which movie entries are old and elegible for
			 * purging from the database to free space.
			 */
			public static final String DOWNLOAD_TIME = "dltime";

			/**
			 * String -- the movie information encoded as a JSON string.
			 */
			public static final String JSON = "datajson";
		}
	}

	/**
	 * Defines the contract for image data.
	 *
	 * <p>Supported content provider URIs:
	 * <ul>
	 *     <li>Image data:<br/>
	 *         <tt>content://no.skavdahl.udacity.popularmovies/image/[path *]/[width #]</tt><br/>
	 *         Accesses the image associated with the specified path and width</li>
	 * </ul></p>
	 */
	public static final class ImageContract {

		public static final String CONTENT_URI_PATH = "image";

		public static final Uri CONTENT_URI =
			BASE_CONTENT_URI.buildUpon().appendPath(CONTENT_URI_PATH).build();

		//public static final String CONTENT_DIR_TYPE =
		//	ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + CONTENT_URI_PATH + "_v" + CONTRACT_VERSION;;
		public static final String CONTENT_ITEM_TYPE =
			ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + CONTENT_URI_PATH + "_v" + CONTRACT_VERSION;;

		public static Uri buildImageUri(String path, int width) {
			return CONTENT_URI.buildUpon()
				.appendPath(path)
				.appendPath(Integer.toString(width))
				.build();
		}

		public static final String TABLE_NAME = "image";

		public static final class Column implements BaseColumns {

			/**
			 * Integer -- references the movie this image is associated with. If that movie
			 * is deleted from the database, then this image should be deleted as well.
			 */
			public static final String MOVIE_ID = "movieid";

			/** String -- the unique path to this image. */
			public static final String PATH = "path";

			/**
			 * Image width specification, corresponding to the image size specifier that was used
			 * to download the image from themoviedb.org. Most often this will be a number like
			 * 800 (pixels) but it can also be the string 'original'.
			 */
			public static final String WIDTH = "width";

			/**
			 * Blob -- the image data.
			 */
			public static final String IMAGEDATA = "imagedata";
		}
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
