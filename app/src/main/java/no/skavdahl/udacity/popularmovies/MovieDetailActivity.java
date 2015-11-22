package no.skavdahl.udacity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

import no.skavdahl.udacity.popularmovies.mdb.DiscoverMovies;
import no.skavdahl.udacity.popularmovies.mdb.DiscoverMoviesJSONAdapter;
import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * Activity for displaying movie details.
 *
 * @author fdavs
 */
public class MovieDetailActivity extends AppCompatActivity {

	private final String LOG_TAG = getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_movie_detail);

		final Movie movie;
		try {
			Intent startingIntent = getIntent();
			DiscoverMoviesJSONAdapter adapter = new DiscoverMoviesJSONAdapter();
			JSONObject obj = new JSONObject(startingIntent.getStringExtra("movie"));
			movie = adapter.toMovie(obj);
		}
		catch (Exception e) { // NPE if no intent, JSONException if parse error
			Log.e(LOG_TAG, "Unable to access Intent extra data", e);
			finish(); // abort the execution of this activity
			return;
		}

		TextView movieTitleView = (TextView) findViewById(R.id.movie_title_textview);
		movieTitleView.setText(movie.getTitle());

		TextView synopsisView = (TextView) findViewById(R.id.synopsis_textview);
		synopsisView.setText(movie.getSynopsis());

		TextView releaseDateTextView = (TextView) findViewById(R.id.release_date_textview);
		releaseDateTextView.setText(formatOptDate(movie.getReleaseDate()));

		TextView userRatingTextView = (TextView) findViewById(R.id.user_rating_textview);
		DecimalFormat numberFormat = new DecimalFormat("0.0");
		userRatingTextView.setText(numberFormat.format(movie.getUserRating()));

		// Show the thumbnail poster image first while loading a higher-resolution image
		final ImageView posterView = (ImageView) findViewById(R.id.poster_imageview);
		Picasso.with(this)
			.load(DiscoverMovies.getPosterThumbnailDownloadURL(movie.getPosterPath()))
			.into(posterView, new DownloadHiresPosterCallback(this, movie.getPosterPath(), posterView));
	}

	private String formatOptDate(final Date date) {
		if (date == null)
			return "";

		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
		return dateFormat.format(date);
	}

	/**
	 * Picasso callback that downloads a high resolution image in the background
	 * and updates the target ImageView when (read: not before) the download has completed.
	 */
	private static class DownloadHiresPosterCallback implements com.squareup.picasso.Callback {
		private final String LOG_TAG = getClass().getSimpleName();

		private final Context context;
		private final String posterPath;
		private final ImageView targetView;

		public DownloadHiresPosterCallback(
			final Context context,
			final String posterPath,
			final ImageView targetView) {

			this.context = context;
			this.posterPath = posterPath;
			this.targetView = targetView;
		}

		@Override
		public void onSuccess() {
			final String downloadURL = DiscoverMovies.getPosterHiresDownloadURL(context, posterPath);

			Picasso.with(context)
				.load(downloadURL)
				.fetch(new com.squareup.picasso.Callback() {
					@Override
					public void onSuccess() {
						// Display the hires image now that it is cached
						Picasso.with(context)
							.load(downloadURL)
							.into(targetView);
					}

					@Override
					public void onError() {
						logError(); // download of hires image failed
					}
				});
		}

		@Override
		public void onError() {
			logError(); // download of lowres image failed
		}

		private void logError() {
			Log.w(LOG_TAG, "Picasso download failed with error");
		}
	}
}
