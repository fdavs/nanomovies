package no.skavdahl.udacity.popularmovies;

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
	}
}
