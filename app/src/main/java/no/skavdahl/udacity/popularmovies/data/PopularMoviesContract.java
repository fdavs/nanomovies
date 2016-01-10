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
		public static Uri buildListMemberItemUri(String listName, int movieId) {
			return CONTENT_URI.buildUpon()
				.appendPath(listName)
				.appendPath(CONTENT_URI_PATH_MEMBER)
				.appendPath(Integer.toString(movieId))
				.build();
		}

		// --- database ---

		public static final String TABLE_NAME = "list";

		/** An invalid list type, similar in semantics to <code>null</code>. */
		public static final int LISTTYPE_NONE = 0;

		/** Identifies a standard list (popular, upcoming, top_rated, now_playing) */
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
		public static final String TABLE_EX_NAME = "movieex";

		public static final class Column implements BaseColumns {

			// int -- the id of the movie in this position in the list

			/**
			 * Timestamp -- when the data in this entry were downloaded from the server.
			 * This timestamp is used decide which movie entries are old and elegible for
			 * purging from the database to free space.
			 */
			public static final String MODIFIED = "modified";

			/** String -- the movie title. */
			public static final String TITLE = "title";

			/** String -- the path to the movie poster (if any). */
			public static final String POSTER_PATH = "poster";

			/** String -- the path to the movie backdrop (if any). */
			public static final String BACKDROP_PATH = "backdrop";

			/** String -- the movie synopsis (if any). */
			public static final String SYNOPSIS = "synopsis";

			/** Double -- the popularity rating of the movie. */
			public static final String POPULARITY = "popularity";

			/** Double -- the user vote average. */
			public static final String VOTE_AVERAGE = "voteavg";

			/** Int -- the user vote count. */
			public static final String VOTE_COUNT = "votecount";

			/** Date -- the release date (if known). */
			public static final String RELEASE_DATE = "releasedate";

			/** Integer -- if true (1) extended movie data (reviews etc.) has been downloaded. */
			public static final String EXTENDED_DATA = "extdata";

			/** String -- movie reviews stored as a JSON array of strings. */
			public static final String REVIEWS_JSON = "reviews";

			/** String -- video links stored as a JSON array of strings. */
			public static final String VIDEOS_JSON = "videos";

			/**
			 * Integer -- if true (1), the movie is a favorite movie. If false (0 or null),
			 * this movie is not a favorite movie. This column is only available in queries
			 * in the extended movie table.
			 */
			public static final String FAVORITE = "favorite";
		}
	}
}
