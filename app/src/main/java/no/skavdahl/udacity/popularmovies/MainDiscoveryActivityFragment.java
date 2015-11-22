package no.skavdahl.udacity.popularmovies;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONException;

import no.skavdahl.udacity.popularmovies.mdb.DiscoverMoviesJSONAdapter;
import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainDiscoveryActivityFragment extends Fragment {

    private final String LOG_TAG = MainDiscoveryActivityFragment.class.getSimpleName();

	private MoviePosterAdapter viewAdapter;

    public MainDiscoveryActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
	    setHasOptionsMenu(true);

	    viewAdapter = new MoviePosterAdapter(getContext());

	    View view = inflater.inflate(R.layout.fragment_main_discovery, container, false);

	    GridView posterGrid = (GridView) view.findViewById(R.id.poster_grid);
	    posterGrid.setAdapter(viewAdapter);
		posterGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Movie clickedMovie = (Movie) viewAdapter.getItem(position);

				String movieData;
				try {
					DiscoverMoviesJSONAdapter adapter = new DiscoverMoviesJSONAdapter();
					movieData = adapter.toJSONString(clickedMovie);
				}
				catch (JSONException e) {
					// this error should never occur so this is mostly verifying an assertion
					Log.e(LOG_TAG, "Unable to convert data to JSON", e);
					return; // abort
				}

				Intent openMovieDetailsIntent = new Intent(getContext(), MovieDetailActivity.class);
				openMovieDetailsIntent.putExtra("movie", movieData);

				Activity contextActivity = MainDiscoveryActivityFragment.this.getActivity();
				if (openMovieDetailsIntent.resolveActivity(contextActivity.getPackageManager()) != null)
					startActivity(openMovieDetailsIntent);
			}
		});

	    int optimalColumnWidth = calculateColumnWidth();
	    posterGrid.setColumnWidth(optimalColumnWidth);

	    return view;
    }

	/**
	 * Calculates the optimal column width based on the width of the display and an ideal
	 * poster width of approximately 1 inch.
	 *
	 * @return tne optimal column width in pixels.
	 */
	private int calculateColumnWidth() {
		DisplayMetrics dm = getResources().getDisplayMetrics();
		double widthInches = ((double)dm.widthPixels) / dm.xdpi;
		int numCols = (int) Math.round(widthInches);

		return dm.widthPixels / numCols;
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_discovery, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            refreshMovies();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Re-issues the current query to themoviedb.org and updates the display with the
     * new results from the query.
     */
    private void refreshMovies() {
        // verify that we have the permission to perform this operation
        int permissionCheck = ContextCompat.checkSelfPermission(
            this.getActivity(),
            Manifest.permission.INTERNET);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.w(LOG_TAG, "Operation canceled: permission denied by user: " + Manifest.permission.INTERNET);
            // TODO We should inform the user that the operation is aborted due to missing permission
            //      hence it is clear why the screen doesn't update
            return;
        }

        // permission granted, go ahead with the operation
        String apiKey = getString(R.string.movie_api_key);
        DiscoverMoviesTask task = new DiscoverMoviesTask(apiKey, viewAdapter);
        task.execute();
    }
}
