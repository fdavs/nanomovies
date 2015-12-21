package no.skavdahl.udacity.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import static no.skavdahl.udacity.popularmovies.data.MovieContracts.*;

/**
 * Content provider for movie data.
 *
 * @author fdavs
 */
public class MovieProvider extends ContentProvider {

	static final int MOVIE_LIST = 100;
	static final int MOVIE_DETAIL = 101;
	static final int IMAGE_DETAIL = 200;

	static UriMatcher buildUriMatcher() {
		UriMatcher m = new UriMatcher(UriMatcher.NO_MATCH);

		m.addURI(CONTENT_AUTHORITY, MovieContract.CONTENT_URI_PATH + "/#", MOVIE_DETAIL);
		m.addURI(CONTENT_AUTHORITY, MovieContract.CONTENT_URI_PATH + "/*/#", MOVIE_LIST);

		m.addURI(CONTENT_AUTHORITY, ImageContract.CONTENT_URI_PATH + "/*/#", IMAGE_DETAIL);

		return m;
	}


	@Override
	public boolean onCreate() {
		return false;
	}

	@Nullable
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		return null;
	}

	@Nullable
	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Nullable
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
}
