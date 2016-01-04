package no.skavdahl.udacity.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Activity for displaying movie details.
 *
 * @author fdavs
 */
public class MovieDetailActivity extends AppCompatActivity {

	public final static String INTENT_EXTRA_DATA = "movie";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_movie_detail);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		if (savedInstanceState == null) {
			MovieDetailActivityFragment detailFragment = new MovieDetailActivityFragment();

			Intent startingIntent = getIntent();
			if (startingIntent != null) {
				Uri contentUri = startingIntent.getParcelableExtra(MovieDetailActivity.INTENT_EXTRA_DATA);
				if (contentUri != null) {
					Bundle args = new Bundle();
					args.putParcelable(MovieDetailActivityFragment.CONTENT_URI, contentUri);

					detailFragment.setArguments(args);
				}
			}

			getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.movie_detail_container, detailFragment)
				.commit();
		}
	}
}
