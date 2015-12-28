package no.skavdahl.udacity.popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
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

import no.skavdahl.udacity.popularmovies.data.UpdateMovieTask;
import no.skavdahl.udacity.popularmovies.mdb.MdbJSONAdapter;
import no.skavdahl.udacity.popularmovies.model.Movie;

import static no.skavdahl.udacity.popularmovies.data.PopularMoviesContract.*;

/**
 * Fragment displaying movie details.
 *
 * @author fdavs
 */
public class MovieDetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private final String LOG_TAG = getClass().getSimpleName();

	private final int LOADER_ID = 0;

	private final String LOADER_ARGS_MOVIE_ID = "movieid";

	// --- cursor configuration ---

	private final static String[] CURSOR_PROJECTION = new String[] {
		MovieContract.Column.MODIFIED,
		MovieContract.Column.JSONDATA
	};

	private final static int CURSOR_INDEX_MODIFIED = 0;
	private final static int CURSOR_INDEX_MOVIE_JSON = 1;

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

	private void bindModelToView(Movie movie) {
		final View view = getView();
		final Context context = getActivity();

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

		Uri listMemberUri = MovieContract.buildMovieItemUri(movieId);

		return new android.content.CursorLoader(
			getActivity(),
			listMemberUri,
			CURSOR_PROJECTION,
			null, // selection
			null, // selection args
			null); // sort order
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor == null || !cursor.moveToFirst())
			return; // no data to handle so just stop

		// convert the cursor data into a movie model
		// TODO ideally this deserialization should happen on a background thread
		String jsonData = cursor.getString(CURSOR_INDEX_MOVIE_JSON);
		long dataModifiedTime = cursor.getLong(CURSOR_INDEX_MODIFIED);

		Movie movie;
		try {
			MdbJSONAdapter jsonAdapter = new MdbJSONAdapter(getResources());
			movie = jsonAdapter.toMovie(new JSONObject(jsonData));
		}
		catch (JSONException e) {
			Log.e(LOG_TAG, "Unable to deserialize movie from json", e); // TODO include more details
			return;
		}

		// update the view
		bindModelToView(movie);

		// evaluate the data: do we need to issue an web update?
		// we need to decide if the data is current and up-to-date or need to be refreshed
		// It is up-to-date if
		//   a) we have extended movie data (reviews, videos)
		//   b) the data are not "too old"
		// "too old" is determined by the quality of the network connection. A better and faster
		// connection makes "too old" a shorter amount of time
		// TODO include network quality in the calculation of "too old"
		long dataAge = System.currentTimeMillis() - dataModifiedTime;
		long maxAge = BuildConfig.MOVIE_DATA_TIMEOUT;

		if (MdbJSONAdapter.containsExtendedData(jsonData) && dataAge <= maxAge)
			Log.v(LOG_TAG, "Movie data is up to date - no further action");
		else {
			Log.v(LOG_TAG, "Movie data is missing or stale - updating");

			UpdateMovieTask asyncTask = new UpdateMovieTask(this.getActivity());
			asyncTask.execute(movie.getMovieDbId());
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// no action
	}

	// --- End LoaderManager.LoaderCallback<Loader> interface ---
}
