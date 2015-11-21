package no.skavdahl.udacity.popularmovies;

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

import no.skavdahl.udacity.popularmovies.mdb.DiscoverMovies;
import no.skavdahl.udacity.popularmovies.mdb.DiscoverMoviesJSONAdapter;
import no.skavdahl.udacity.popularmovies.model.Movie;

public class MovieDetailActivity extends AppCompatActivity {

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
		catch (Exception e) { // NPE if no intent, JSONException if parse error
			Log.e(LOG_TAG, "Unable to access Intent extra data", e);
			finish(); // abort the execution of this activity
			return;
		}

		TextView movieTitleView = (TextView) findViewById(R.id.movie_title_textview);
		movieTitleView.setText(movie.getTitle());

		TextView synopsisView = (TextView) findViewById(R.id.synopsis_textview);
		synopsisView.setText(movie.getSynopsis());

		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
		TextView releaseDateTextView = (TextView) findViewById(R.id.release_date_textview);
		releaseDateTextView.setText(dateFormat.format(movie.getReleaseDate()));

		TextView userRatingTextView = (TextView) findViewById(R.id.user_rating_textview);
		DecimalFormat numberFormat = new DecimalFormat("0.0");
		userRatingTextView.setText(numberFormat.format(movie.getUserRating()));

		ImageView posterView = (ImageView) findViewById(R.id.poster_imageview);
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