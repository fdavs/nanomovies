package no.skavdahl.udacity.popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import no.skavdahl.udacity.popularmovies.data.PopularMoviesContract;
import no.skavdahl.udacity.popularmovies.mdb.DiscoverMoviesJSONAdapter;
import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * @author fdavs
 */
public class MovieDetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private final String LOG_TAG = getClass().getSimpleName();

	private final int LOADER_ID = 0;

	private final String LOADER_ARGS_MOVIE_ID = "movieid";

	// --- cursor configuration ---

	private final static String[] CURSOR_PROJECTION = new String[] {
		PopularMoviesContract.MovieContract.Column.JSONDATA
	};

	private final static int CURSOR_INDEX_MOVIE_JSON = 0;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// register the loader
		Intent startingIntent = getActivity().getIntent();
		int movieId = startingIntent.getIntExtra(MovieDetailActivity.INTENT_EXTRA_DATA, 0);
		if (movieId == 0) {
			Log.e(LOG_TAG, "Intent extra does not specify movie id");
			getActivity().finish(); // abort the execution of this activity
			return;
		}

		Bundle loaderArgs = new Bundle();
		loaderArgs.putInt(LOADER_ARGS_MOVIE_ID, movieId);

		getLoaderManager().initLoader(LOADER_ID, loaderArgs, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
	                         ViewGroup container,
	                         Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		final View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);
		return view;
	}

	private void bindCursorToView(Cursor cursor) {
		// TODO ideally this deserialization should happen on a background thread
		Movie movie;
		try {
			String movieJson = cursor.getString(CURSOR_INDEX_MOVIE_JSON);
			DiscoverMoviesJSONAdapter jsonAdapter = new DiscoverMoviesJSONAdapter(getResources());
			movie = jsonAdapter.toMovie(new JSONObject(movieJson));
		}
		catch (JSONException e) {
			Log.e(LOG_TAG, "Unable to deserialize movie from json", e); // TODO include more details
			return;
		}

		final View view = getView();
		final Context context = getContext();

		TextView movieTitleView = (TextView) view.findViewById(R.id.movie_title_textview);
		movieTitleView.setText(context.getString(R.string.movie_title, movie.getTitle()));

		TextView synopsisView = (TextView) view.findViewById(R.id.synopsis_textview);
		synopsisView.setText(
			context.getString(R.string.movie_synopsis, formatOptString(movie.getSynopsis(), "")));

		TextView releaseDateTextView = (TextView) view.findViewById(R.id.release_rate_textview);
		releaseDateTextView.setText(
			context.getString(R.string.movie_release_date, formatOptYear(movie.getReleaseDate(),
				context.getString(R.string.data_unknown))));

		TextView userRatingTextView = (TextView) view.findViewById(R.id.user_rating_textview);
		userRatingTextView.setText(
			context.getResources().getQuantityString(
				R.plurals.movie_user_rating, // id
				movie.getVoteCount(), // quantity
				movie.getVoteAverage(), movie.getVoteCount())); // format args

		final ImageView posterView = (ImageView) view.findViewById(R.id.poster_imageview);
		PicassoUtils.displayPosterWithOfflineFallback(context, movie, posterView);

		final ImageView backdropView = (ImageView) view.findViewById(R.id.backdrop_imageview);
		PicassoUtils.displayBackdrop(context, movie, backdropView);
	}


	private String formatOptString(final String str, final String fallback) {
		return (str != null) ? str : fallback;
	}

	private String formatOptYear(final Date date, final String fallback) {
		if (date == null)
			return fallback;

		Calendar cal = GregorianCalendar.getInstance(Locale.getDefault());
		cal.setTime(date);

		return Integer.toString(cal.get(Calendar.YEAR));
	}

	// --- LoaderManager.LoaderCallback<Loader> interface ---

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {
		// Sort order:  Ascending, by date.
		int movieId = loaderArgs.getInt(LOADER_ARGS_MOVIE_ID);

		Uri listMemberUri = PopularMoviesContract.MovieContract.buildMovieItemUri(movieId);

		return new android.content.CursorLoader(
			getActivity().getBaseContext(),
			listMemberUri,
			CURSOR_PROJECTION,
			null, // selection
			null, // selection args
			null); // sort order
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		bindCursorToView(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// no action
	}

	// --- End LoaderManager.LoaderCallback<Loader> interface ---
}
