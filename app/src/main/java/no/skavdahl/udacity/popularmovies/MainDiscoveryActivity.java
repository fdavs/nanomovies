package no.skavdahl.udacity.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Activity for browsing and discovering movies.
 *
 * @author fdavs
 */
public class MainDiscoveryActivity
	extends AppCompatActivity
	implements MainDiscoveryActivityFragment.ItemSelectionListener {

    // --- dual pane support (tablets) ---

    /**
     * If true, the device is large enough to support both the discovery and the detail views
     * at the same time.
     */
    private boolean isDualPane;

    /** Tag used by the FragmentManager to identify the movie detail fragment. */
    private static final String MOVIE_DETAIL_TAG = "MDF"; // Movie Detail Fragment


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    setContentView(R.layout.activity_main_discovery);

	    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

	    ((MainDiscoveryActivityFragment) getSupportFragmentManager().
		    findFragmentById(R.id.fragment_discovery))
		    .setItemSelectionListener(this);

	    isDualPane = findViewById(R.id.movie_detail_container) != null;
	    if (isDualPane) {
		    if (savedInstanceState == null) {
			    getSupportFragmentManager()
				    .beginTransaction()
				    .replace(R.id.movie_detail_container, new MovieDetailActivityFragment(), MOVIE_DETAIL_TAG)
				    .commit();
		    }

	    }
    }

	// --- MainDiscoveryActivityFragment.UserActionListener ---

	@Override
	public void onItemSelected(Uri contentUri) {
		if (isDualPane) {
			Bundle args = new Bundle();
			args.putParcelable(MovieDetailActivityFragment.CONTENT_URI, contentUri);

			MovieDetailActivityFragment fragment = new MovieDetailActivityFragment();
			fragment.setArguments(args);

			getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.movie_detail_container, fragment, MOVIE_DETAIL_TAG)
				.commit();
		}
		else {
			Intent openMovieDetailsIntent = new Intent(this, MovieDetailActivity.class);
			openMovieDetailsIntent.putExtra(MovieDetailActivity.INTENT_EXTRA_DATA, contentUri);

			assert openMovieDetailsIntent.resolveActivity(getPackageManager()) != null;
			startActivity(openMovieDetailsIntent);
		}
	}
}
