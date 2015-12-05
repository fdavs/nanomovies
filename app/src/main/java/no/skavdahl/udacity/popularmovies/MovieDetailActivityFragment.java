package no.skavdahl.udacity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import no.skavdahl.udacity.popularmovies.mdb.DiscoverMoviesJSONAdapter;
import no.skavdahl.udacity.popularmovies.mdb.Request;
import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * @author fdavs
 */
public class MovieDetailActivityFragment extends Fragment {

	private final String LOG_TAG = getClass().getSimpleName();

	@Override
	public View onCreateView(LayoutInflater inflater,
	                         ViewGroup container,
	                         Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		final Movie movie;
		try {
			Intent startingIntent = getActivity().getIntent();
			DiscoverMoviesJSONAdapter adapter = new DiscoverMoviesJSONAdapter(getResources());
			JSONObject obj = new JSONObject(startingIntent.getStringExtra("movie"));
			movie = adapter.toMovie(obj);
		}
		catch (Exception e) { // NPE if no intent, JSONException if parse error
			Log.e(LOG_TAG, "Unable to access Intent extra data", e);
			getActivity().finish(); // abort the execution of this activity
			return null;
		}

		final View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);
		final Context context = getContext();

		TextView movieTitleView = (TextView) view.findViewById(R.id.movie_title_textview);
		movieTitleView.setText(context.getString(R.string.movie_title, movie.getTitle()));

		TextView synopsisView = (TextView) view.findViewById(R.id.synopsis_textview);
		synopsisView.setText(
			context.getString(R.string.movie_synopsis, formatOptString(movie.getSynopsis(), "")));

		TextView releaseDateTextView = (TextView) view.findViewById(R.id.release_rate_textview);
		releaseDateTextView.setText(
			context.getString(R.string.movie_release_date, formatOptDate(movie.getReleaseDate(),
				context.getString(R.string.data_unknown))));

		TextView userRatingTextView = (TextView) view.findViewById(R.id.user_rating_textview);
		userRatingTextView.setText(
			context.getString(R.string.movie_user_rating, movie.getVoteAverage(), movie.getVoteCount()));

		// Show the thumbnail poster image first while loading a higher-resolution image
		final ImageView posterView = (ImageView) view.findViewById(R.id.poster_imageview);
		PicassoUtils.displayWithFallback(context, movie, posterView,
			new DownloadHiresPosterCallback(context, movie, posterView));

		final ImageView backdropView = (ImageView) view.findViewById(R.id.backdrop_imageview);
		String path = movie.getBackdropPath();
		if (path == null)
			path = movie.getPosterPath();
		Picasso.with(context)
			.load(Request.getPosterHiresDownloadURL(context, path))
			.into(backdropView);

		return view;
	}

	private String formatOptString(final String str, final String fallback) {
		return (str != null) ? str : fallback;
	}

	private String formatOptDate(final Date date, final String fallback) {
		if (date == null)
			return fallback;

		Calendar cal = GregorianCalendar.getInstance(Locale.getDefault());
		cal.setTime(date);

		return Integer.toString(cal.get(Calendar.YEAR));
		//
		//DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		//return dateFormat.format(date);
	}

	/**
	 * Picasso callback that downloads a high resolution image in the background
	 * and updates the target ImageView when (read: not before) the download has completed.
	 */
	private static class DownloadHiresPosterCallback extends OfflinePosterCallback {
		private final Context context;

		public DownloadHiresPosterCallback(final Context context, final Movie movie, final ImageView targetView) {
			super(movie, targetView);
			this.context = context;
		}

		@Override
		public void onSuccess() {
			final String downloadURL = Request.getPosterHiresDownloadURL(context, getMovie().getPosterPath());

			// preload the hi-res poster image with "fetch" rather than "load", putting it
			// in Picasso's cache.
			// "load" makes Picasso take control over the target ImageView immediately,
			// overriding the low-res picture with a "progress picture" until the download
			// operation has completed. "fetch()" on the other hand doesn't touch the
			// ImageView at all but simply puts the downloaded picture in the cache.
			Picasso.with(context)
				.load(downloadURL)
				.fetch(new com.squareup.picasso.Callback() {
					@Override
					public void onSuccess() {
						// Display the hires image now that it is cached
						Picasso.with(context)
							.load(downloadURL)
							.into(getTargetView());
					}

					@Override
					public void onError() {
						// Keep the lowres version
					}
				});
		}

		@Override
		public void onError() {
			displayOfflinePoster();
		}
	}
}
