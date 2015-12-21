package no.skavdahl.udacity.popularmovies.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

import static no.skavdahl.udacity.popularmovies.data.MovieContracts.*;

/**
 * Verifies the implementation of the UriMatcher.
 *
 * @author fdavs
 */
public class UriMatcherTest extends AndroidTestCase {

	private UriMatcher matcher;

	public void setUp() {
		matcher = MovieProvider.buildUriMatcher();
	}

	public void testThatMovieListUriIsMatched() {
		Uri movieListUri = MovieContract.buildMovieListUri("popular", 5);
		assertEquals(MovieProvider.MOVIE_LIST, matcher.match(movieListUri));
	}

	public void testThatMovieDetailUriIsMatched() {
		Uri movieListUri = MovieContract.buildMovieDetailUri(TestData.Movie1.ID);
		assertEquals(MovieProvider.MOVIE_DETAIL, matcher.match(movieListUri));
	}

	public void testThatImageDetailUriIsMatched() {
		Uri movieListUri = ImageContract.buildImageUri(TestData.Movie1.POSTER_PATH, TestData.Movie1.POSTER_WIDTH);
		assertEquals(MovieProvider.IMAGE_DETAIL, matcher.match(movieListUri));
	}
}
