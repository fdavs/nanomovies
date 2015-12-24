package no.skavdahl.udacity.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Data contract for movie information.
 *
 * @author fdavs
 */
public class PopularMoviesContract {

	public static final int CONTRACT_VERSION = 1;

	public static final String CONTENT_AUTHORITY = "no.skavdahl.udacity.popularmovies";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	/** Defines the contract for movie data. */
	public static final class ListContract {

		// --- content provider ---

		public static final String CONTENT_URI_PATH = "list";
		public static final String CONTENT_URI_PATH_MEMBER = "movie";

		public static final Uri CONTENT_URI =
			BASE_CONTENT_URI.buildUpon().appendPath(CONTENT_URI_PATH).build();

		public static final String CONTENT_LIST_DIR_TYPE =
			ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + CONTENT_URI_PATH + "_v" + CONTRACT_VERSION;
		public static final String CONTENT_LIST_ITEM_TYPE =
			ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + CONTENT_URI_PATH + "_v" + CONTRACT_VERSION;

		public static final String CONTENT_MEMBER_DIR_TYPE =
			ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + CONTENT_URI_PATH + "." + CONTENT_URI_PATH_MEMBER + "_v" + CONTRACT_VERSION;
		public static final String CONTENT_MEMBER_ITEM_TYPE =
			ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + CONTENT_URI_PATH + "." + CONTENT_URI_PATH_MEMBER + "_v" + CONTRACT_VERSION;

		// AUTHORITY/list                       -- query all lists
		// AUTHORITY/list/name*                 -- query one specific list

		/** Constructs a URI to access the list directory. */
		public static Uri buildListDirectoryUri() {
			return CONTENT_URI;
		}

		/** Constructs a URI to access one specific list. */
		public static Uri buildListItemUri(String listName) {
			return CONTENT_URI.buildUpon()
				.appendPath(listName)
				.build();
		}

		// AUTHORITY/list/name*/movie           -- query list contents
		// AUTHORITY/list/name*/movie/id#       -- query one specific list member

		/** Constructs a URI to access a list's member directory. */
		public static Uri buildListMemberDirectoryUri(String listName) {
			return CONTENT_URI.buildUpon()
				.appendPath(listName)
				.appendPath(CONTENT_URI_PATH_MEMBER)
				.build();
		}

		/** Constructs a URI to access one specific list member. */
		public static Uri buildListMemberItemUri(String listName, int id) {
			return CONTENT_URI.buildUpon()
				.appendPath(listName)
				.appendPath(CONTENT_URI_PATH_MEMBER)
				.appendPath(Integer.toString(id))
				.build();
		}

		// --- database ---

		public static final String TABLE_NAME = "list";

		/** Identifies a standard list (popular, upcoming, toprated, nowplaying) */
		public static final int LISTTYPE_STANDARD = 1;

		/** Identifies the user's list of favorite movies */
		public static final int LISTTYPE_FAVORITE = 2;

		/** Identifies a public user-managed list on themoviedb.org */
		public static final int LISTTYPE_PUBLIC = 3;

		public static final class Column implements BaseColumns {

			/** String -- the name of the list */
			public static final String NAME = "name";

			/**
			 * Int -- The type of list.
			 *
			 * @see #LISTTYPE_STANDARD
			 * @see #LISTTYPE_FAVORITE
			 * @see #LISTTYPE_PUBLIC
			 **/
			public static final String TYPE = "listtype";
		}
	}

	/**
	 * Defines the contract for movie membership in lists.
	 *
	 * This table is not accessed directly through Content Providers. See instead
	 * {@link ListContract}.
	 */
	public static final class ListMembershipContract {

		public static final String TABLE_NAME = "listmember";

		public static final class Column implements BaseColumns {

			/** String -- the name of the list */
			public static final String LIST_ID = "listid";

			/** Timestamp (long) -- when the list contents were last refreshed */
			public static final String PAGE = "page";

			/** Timestamp (long) -- when the list contents were last refreshed */
			public static final String POSITION = "position";

			/** Timestamp (long) -- when the list contents were last refreshed */
			public static final String MOVIE_ID = "movieid";

			/** Timestamp (long) -- when the member was added to the list */
			public static final String ADDED = "added";
		}
	}
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

		public static final String CONTENT_DIR_TYPE =
			ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + CONTENT_URI_PATH + "_v" + CONTRACT_VERSION;
		public static final String CONTENT_ITEM_TYPE =
			ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + CONTENT_URI_PATH + "_v" + CONTRACT_VERSION;

		// AUTHORITY/movie                      -- query movie directory
		// AUTHORITY/movie/id#                  -- query movie data

		public static Uri buildMovieDirectoryUri() {
			return CONTENT_URI;
		}

		public static Uri buildMovieItemUri(long movieId) {
			return ContentUris.withAppendedId(CONTENT_URI, movieId);
		}

		// --- database ---

		public static final String TABLE_NAME = "movie";

		public static final class Column implements BaseColumns {

			// int -- the id of the movie in this position in the list

			/**
			 * Timestamp -- when the data in this entry were downloaded from the server.
			 * This timestamp is used decide which movie entries are old and elegible for
			 * purging from the database to free space.
			 */
			public static final String MODIFIED = "modified";

			/** String -- the movie information encoded as a JSON string. */
			public static final String JSONDATA = "jsondata";
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

		public static final String CONTENT_DIR_TYPE =
			ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + CONTENT_URI_PATH + "_v" + CONTRACT_VERSION;;
		public static final String CONTENT_ITEM_TYPE =
			ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + CONTENT_URI_PATH + "_v" + CONTRACT_VERSION;;

		// AUTHORITY/image                      -- query image directory
		// AUTHORITY/image/id#                  -- query image data

		public static Uri buildImageDirectoryUri() {
			return CONTENT_URI;
		}

		public static Uri buildImageItemUri(int imageId) {
			return ContentUris.withAppendedId(CONTENT_URI, imageId);
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
}
