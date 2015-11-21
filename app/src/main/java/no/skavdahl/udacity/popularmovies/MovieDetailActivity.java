package no.skavdahl.udacity.popularmovies;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;

import no.skavdahl.udacity.popularmovies.mdb.DiscoverMovies;
import no.skavdahl.udacity.popularmovies.mdb.DiscoverMoviesJSONAdapter;
import no.skavdahl.udacity.popularmovies.model.Movie;

public class MovieDetailActivity extends Activity {

	private final String LOG_TAG = getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_movie_detail);

		Movie movie;
		try {
			Intent startingIntent = getIntent();
			DiscoverMoviesJSONAdapter adapter = new DiscoverMoviesJSONAdapter();
			JSONObject obj = new JSONObject(startingIntent.getStringExtra("movie"));
			movie = adapter.toMovie(obj);
		}
		catch (JSONException e) {
			Log.e(LOG_TAG, "Unable to access Intent extra data", e);
			finish(); // abort the execution of this activity
			return;
		}

		TextView movieTitleView = (TextView) findViewById(R.id.movieTitleTextView);
		movieTitleView.setText(movie.getTitle());

		TextView synopsisView = (TextView) findViewById(R.id.synopsisTextView);
		synopsisView.setText(movie.getSynopsis());

		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
		TextView releaseDateTextView = (TextView) findViewById(R.id.releaseDateTextView);
		releaseDateTextView.setText(dateFormat.format(movie.getReleaseDate()));

		TextView userRatingTextView = (TextView) findViewById(R.id.userRatingTextView);
		DecimalFormat numberFormat = new DecimalFormat("0.0");
		userRatingTextView.setText(numberFormat.format(movie.getUserRating()));

		ImageView posterView = (ImageView) findViewById(R.id.posterImageView);
		Picasso.with(this)
			.load(DiscoverMovies.getPosterFullsizeDownloadURL(movie.getPosterPath()))
				// TODO .placeholder(R.drawable.user_placeholder)
				// TODO .error(R.drawable.user_placeholder_error)
				//.resize(50, 50)
				//.centerCrop()
			.into(posterView);
	}

	@Override
	public Intent getIntent() {
		return super.getIntent();
	}
}
