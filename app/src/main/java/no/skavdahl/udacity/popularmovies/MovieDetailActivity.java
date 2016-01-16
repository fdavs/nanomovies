package no.skavdahl.udacity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

/**
 * Activity for displaying movie details.
 *
 * @author fdavs
 */
public class MovieDetailActivity extends AppCompatActivity {

	private final String LOG_TAG = getClass().getSimpleName();

	public final static String INTENT_EXTRA_DATA = "movie";

	/**
	 * Create an intent to show movie details in this activity.
	 *
	 * @param packageContext A context of the application package implementing this intent
	 * @param contentUri Identifies the movie whose details are to be shown
	 *
	 * @see #getContentUriFromIntent(Intent)
	 */
	public static Intent newExplicitIntent(Context packageContext, Uri contentUri) {
		Intent intent = new Intent(packageContext, MovieDetailActivity.class);
		intent.putExtra(MovieDetailActivity.INTENT_EXTRA_DATA, contentUri);
		return intent;
	}

	/**
	 * Retrieves the content URI optionally stored in the given intent.
	 *
	 * @param intent The intent used to start this activity
	 *
	 * @return a content URI or <tt>null</tt> if not specified by the intent
	 * @see #newExplicitIntent(Context, Uri)
	 */
	private static Uri getContentUriFromIntent(Intent intent) {
		if (intent == null)
			return null;

		return intent.getParcelableExtra(MovieDetailActivity.INTENT_EXTRA_DATA);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_movie_detail);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		if (savedInstanceState != null)
			return; // no additional setup now

		Uri contentUri = getContentUriFromIntent(getIntent());
		if (contentUri == null) {
			Log.e(LOG_TAG, "Starting intent did not include required content");
			finish();
			return;
		}

		MovieDetailFragment fragment = MovieDetailFragment.newInstance(contentUri);
		getSupportFragmentManager()
			.beginTransaction()
			.add(R.id.movie_detail_container, fragment)
			.commit();
	}
}
