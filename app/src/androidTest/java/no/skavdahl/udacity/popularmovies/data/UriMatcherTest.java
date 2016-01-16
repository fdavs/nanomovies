package no.skavdahl.udacity.popularmovies.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

import static no.skavdahl.udacity.popularmovies.data.PopularMoviesContract.*;

/**
 * Verifies the implementation of the UriMatcher.
 *
 * @author fdavs
 */
public class UriMatcherTest extends AndroidTestCase {

	private UriMatcher matcher;

	// --- Test data ---

	private final String LIST_NAME = "favorites";
	private final int MOVIE_ID = 5;

	public void setUp() {
		matcher = MovieProvider.buildUriMatcher();
	}

	public void testThatListDirectoryUriIsMatched() {
		Uri movieListUri = ListContract.buildListDirectoryUri();
		assertEquals(MovieProvider.LIST_DIRECTORY, matcher.match(movieListUri));
	}

	public void testThatListItemUriIsMatched() {
		Uri movieListUri = ListContract.buildListItemUri(LIST_NAME);
		assertEquals(MovieProvider.LIST_ITEM, matcher.match(movieListUri));
	}

	public void testThatListMemberDirectoryUriIsMatched() {
		Uri movieListUri = ListContract.buildListMemberDirectoryUri(LIST_NAME);
		assertEquals(MovieProvider.LIST_MEMBER_DIRECTORY, matcher.match(movieListUri));
	}

	public void testThatListMemberItemUriIsMatched() {
		Uri movieListUri = ListContract.buildListMemberItemUri(LIST_NAME, MOVIE_ID);
		assertEquals(MovieProvider.LIST_MEMBER_ITEM, matcher.match(movieListUri));
	}

	public void testThatMovieDirectoryUriIsMatched() {
		Uri movieListUri = MovieContract.buildMovieDirectoryUri();
		assertEquals(MovieProvider.MOVIE_DIRECTORY, matcher.match(movieListUri));
	}

	public void testThatMovieItemUriIsMatched() {
		Uri movieListUri = MovieContract.buildMovieItemUri(MOVIE_ID);
		assertEquals(MovieProvider.MOVIE_ITEM, matcher.match(movieListUri));
	}
}
