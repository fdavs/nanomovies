package no.skavdahl.udacity.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import no.skavdahl.udacity.popularmovies.data.SweepDatabaseTask;

/**
 * Activity for browsing and discovering movies.
 *
 * @author fdavs
 */
public class DiscoveryActivity
	extends AppCompatActivity
	implements DiscoveryFragment.ItemSelectionListener {

    /**
     * If true, the device is large enough to support both the discovery and the detail views
     * at the same time.
     */
    private boolean isDualPane;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    setContentView(R.layout.activity_discovery);

	    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

	    getDiscoveryFragment().setItemSelectionListener(this);

	    isDualPane = findViewById(R.id.movie_detail_container) != null;
	    /* if (isDualPane) {
		    if (savedInstanceState == null) {
			    getSupportFragmentManager()
				    .beginTransaction()
				    .replace(R.id.movie_detail_container, new MovieDetailFragment(), MOVIE_DETAIL_TAG)
				    .commit();
		    }
	    } */

	    // schedule database cleanup if enough time has passed since the last cleanup
	    long lastSweepTime = UserPreferences.getSweepTime(this);
	    long now = System.currentTimeMillis();
	    if (now - lastSweepTime > BuildConfig.DATABASE_SWEEP_INTERVAL) {
		    UserPreferences.setSweepTime(this, now);
		    new SweepDatabaseTask(this).execute();
	    }

    }

	private DiscoveryFragment getDiscoveryFragment() {
		return (DiscoveryFragment)
			getSupportFragmentManager().findFragmentById(R.id.fragment_discovery);
	}

	// --- DiscoveryFragment.UserActionListener ---

	@Override
	public void onItemSelected(Uri contentUri) {
		if (isDualPane) {
			MovieDetailFragment fragment = MovieDetailFragment.newInstance(contentUri);
			getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.movie_detail_container, fragment)
				.commit();
		}
		else {
			// resolution of this explicit intent should always succeed since it's part of the app
			Intent showMovieIntent = MovieDetailActivity.newExplicitIntent(this, contentUri);
			startActivity(showMovieIntent);
		}
	}
}
