package no.skavdahl.udacity.popularmovies;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import no.skavdahl.udacity.popularmovies.data.PopularMoviesContract;
import no.skavdahl.udacity.popularmovies.data.SweepDatabaseTask;
import no.skavdahl.udacity.popularmovies.data.UpdateMovieListTask;
import no.skavdahl.udacity.popularmovies.mdb.StandardMovieList;

/**
 * The main Movie Discovery fragment.
 *
 * @author fdavs
 */
public class MainDiscoveryActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	// Log tags must be <= 23 characters
	// see Log.isLoggable() throws description
    private final String LOG_TAG = getClass().getSimpleName().substring(0, 23);

	//private RecyclerView posterGrid;
	private MoviePosterAdapter viewAdapter;

	// --- movie list loading support ---

	/** ID of the movie list loader, used by the LoaderManager. */
	private static final int LOADER_ID = 0;

	/** Constant indicating that no movie data is currently being downloaded. */
	private static final int NO_PAGE = -1;

	/**
	 * Indicates if a page is already being downloaded. This is used to protect against
	 * multiple requests to download the same data. While this variable has a value different
	 * from <tt>NO_PAGE</tt>, additional download requests are ignored.
	 *
	 * <p>Note: access only from the UI thread.</p>
 	 */
	private int currentlyLoadingPage = NO_PAGE;

	// It is recommended to keep a local reference to this ChangeListener to avoid
	// garbage collection -- see the Android API docs at http://goo.gl/0yFyTy
	// (SharedPreferences#registerOnSharedPreferenceChangeListener)
	private SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener;

	// --- cursor configuration ---

	private final static String[] CURSOR_PROJECTION = new String[] {
		PopularMoviesContract.MovieContract.Column._ID,
		PopularMoviesContract.MovieContract.Column.POSTER_PATH,
		PopularMoviesContract.MovieContract.Column.TITLE
	};

	private final static int CURSOR_INDEX_MOVIE_ID = 0;
	private final static int CURSOR_INDEX_MOVIE_POSTER_PATH = 1;
	private final static int CURSOR_INDEX_MOVIE_TITLE = 2;

	// --- other configuration ---

	/** Do not issue database cleanup commands more frequently that this */
	private final static long DATABASE_SWEEP_INTERVAL = 12 * 60 * 60 * 1000; // 12 hours

	// --- Interface through which to report user activity ---

	public interface ItemSelectionListener {
		/** Called when the user selects one movie in the movie gallery. */
		void onItemSelected(Uri contentUri);
	}

	public ItemSelectionListener itemSelectionListener;

	public void setItemSelectionListener(final ItemSelectionListener listener) {
		this.itemSelectionListener = listener;
	}

	/**
	 * Notify the listeners that a new movie has been selected.
	 *
	 * @param movieId Identifies the movie that has been selected
	 */
	private void notifyMovieSelected(final int movieId) {
		if (itemSelectionListener != null) {
			try {
				Uri contentUri = PopularMoviesContract.MovieContract.buildMovieItemUri(movieId);
				itemSelectionListener.onItemSelected(contentUri);
			}
			catch (Exception e) {
				Log.w(LOG_TAG, "Error in handler: " + itemSelectionListener, e);
			}
		}
	}

	@Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
	    setHasOptionsMenu(true);

		Log.v(LOG_TAG, "onCreateView");

		// initialize the view
	    final View view = inflater.inflate(R.layout.fragment_main_discovery, container, false);

	    // Configure the grid display of movie posters
	    final RecyclerView posterGrid = (RecyclerView) view.findViewById(R.id.poster_grid);
		posterGrid.setHasFixedSize(true);

		// -- what should happen when a movie poster is clicked
		MoviePosterAdapter.MovieClickListener movieClickListener = new MoviePosterAdapter.MovieClickListener() {
			@Override
			public void OnMovieClicked(int movieId) {
				notifyMovieSelected(movieId);
			}
		};

		// -- how to display each movie poster
		posterGrid.setAdapter(
			viewAdapter = new MoviePosterAdapter(
				getContext(),
				CURSOR_INDEX_MOVIE_ID,
				CURSOR_INDEX_MOVIE_POSTER_PATH,
				CURSOR_INDEX_MOVIE_TITLE,
				movieClickListener));

	    // -- how many posters to display on each row
		final GridLayoutManager layoutManager = new GridLayoutManager(
			getContext(),
			getResources().getInteger(R.integer.movielist_columns));
		posterGrid.setLayoutManager(layoutManager);

		// -- what to do when we need more data
		posterGrid.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				int visibleCount = layoutManager.getChildCount();
				int itemCount = layoutManager.getItemCount();
				int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
				if ((firstVisibleItemPosition + visibleCount) >= itemCount) {
					int pagesDisplayed = viewAdapter.getItemCount() / 20; // TODO avoid magic constant
					downloadMovieData(pagesDisplayed + 1);
				}
			}
		});

		return view;
    }

	@Override
	public void onActivityCreated(Bundle inState) {
		super.onActivityCreated(inState);

		// choose which movie list to show first
		configureStartupMovieList();

		// initialize the loader
		getLoaderManager().initLoader(LOADER_ID, null, this);

		// schedule database cleanup if enough time has passed since the last cleanup
		long lastSweepTime = UserPreferences.getSweepTime(getActivity());
		long now = System.currentTimeMillis();
		if (now - lastSweepTime > DATABASE_SWEEP_INTERVAL) {
			UserPreferences.setSweepTime(getActivity(), now);
			new SweepDatabaseTask(getContext()).execute();
		}
	}

	/**
	 * Determines which movie list to show when the activity starts.
	 * If the device is offline, the favorite movie list is displayed first.
	 * Otherwise, the same movie list that was viewed last is displayed first.
	 */
	private void configureStartupMovieList() {
		// this operation requires the permission "android.permission.ACCESS_NETWORK_STATE"
		ConnectivityManager connectivityManager =
			(ConnectivityManager) getContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

		if (networkInfo == null || !networkInfo.isConnected())
			UserPreferences.setMovieList(getActivity(), StandardMovieList.FAVORITE);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (prefChangeListener != null) {
			getActivity()
				.getPreferences(Context.MODE_PRIVATE)
				.unregisterOnSharedPreferenceChangeListener(prefChangeListener);
			prefChangeListener = null;
		}
	}

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_discovery, menu);

	    configureOptionsMenu(menu);

	    getActivity().getPreferences(Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(
		    prefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			    @Override
			    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				    if (!UserPreferences.MOVIE_LIST.equals(key))
					    return;

				    configureOptionsMenu(menu);
			    }
		    });
    }

	private void configureOptionsMenu(final Menu menu) {
		// ensure that the movie list radio buttons are checked appropriately
		// NOTE It is possible to store selected menu item id directly in the UserPreferences
		// avoiding the need to map between menu item ids and movie list names. However,
		// I'm not sure how stable a menu item id is. Will it survive an upgrade to a
		// newer version of the app? Saving the movie list name seems more stable and safer
		// (not to mention readable).
		String selectedMovieList = UserPreferences.getMovieList(getActivity());
		int menuItem = mapMovieListToMenuItem(selectedMovieList);
		if (menuItem > 0)
			menu.findItem(menuItem).setChecked(true);

		// set activity title
		int titleId = mapMovieListToTitle(selectedMovieList);
		getActivity().setTitle(titleId);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	    String listName = mapMenuItemToMovieList(item.getItemId());
		if (listName != null) {
			String currentList = UserPreferences.getMovieList(getActivity());
			if (!listName.equals(currentList)) {
				UserPreferences.setMovieList(getActivity(), listName);

				// load the new movie list from the database
				getLoaderManager().restartLoader(LOADER_ID, null, this);

				return true;
			}
		}
		return super.onOptionsItemSelected(item);
    }

	private int mapMovieListToMenuItem(String listName) {
		switch (listName) {
			case StandardMovieList.POPULAR:
				return R.id.action_popular_movies;
			case StandardMovieList.TOP_RATED:
				return R.id.action_top_rated_movies;
			case StandardMovieList.NOW_PLAYING:
				return R.id.action_new_movies;
			case StandardMovieList.UPCOMING:
				return R.id.action_upcoming_movies;
			case StandardMovieList.FAVORITE:
				return R.id.action_favorite_movies;
			default:
				return 0;
		}
	}

	private int mapMovieListToTitle(String listName) {
		switch (listName) {
			case StandardMovieList.POPULAR:
				return R.string.action_popular_movies;
			case StandardMovieList.TOP_RATED:
				return R.string.action_top_rated_movies;
			case StandardMovieList.NOW_PLAYING:
				return R.string.action_new_movies;
			case StandardMovieList.UPCOMING:
				return R.string.action_upcoming_movies;
			case StandardMovieList.FAVORITE:
				return R.string.action_favorite_movies;
			default:
				return R.string.app_name;
		}
	}

	private String mapMenuItemToMovieList(int menuItemId) {
		switch (menuItemId) {
			case R.id.action_popular_movies:
				return StandardMovieList.POPULAR;
			case R.id.action_top_rated_movies:
				return StandardMovieList.TOP_RATED;
			case R.id.action_new_movies:
				return StandardMovieList.NOW_PLAYING;
			case R.id.action_upcoming_movies:
				return StandardMovieList.UPCOMING;
			case R.id.action_favorite_movies:
				return StandardMovieList.FAVORITE;
			default:
				return null;
		}
	}

	// --- LoaderManager.LoaderCallback<Loader> interface ---

	private class SequentialUpdateMovieListTask extends UpdateMovieListTask {
		public SequentialUpdateMovieListTask(Context context) {
			super(context);
		}

		@Override
		protected void onPostExecute(Void unused) {
			currentlyLoadingPage = NO_PAGE;
		}
	}

	/**
	 * Initiates download of additional movie data.
	 *
	 * @param pageToLoad Which "page" in the movie list to download
	 */
	private void downloadMovieData(int pageToLoad) {
		if (currentlyLoadingPage != NO_PAGE)
			return;
		currentlyLoadingPage = pageToLoad;

		final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);

		// verify that we have the permission to perform this operation
		int permissionCheck = ContextCompat.checkSelfPermission(
			this.getActivity(),
			Manifest.permission.INTERNET);

		if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
			if (verbose) Log.w(LOG_TAG, "Operation canceled: permission denied by user: " + Manifest.permission.INTERNET);

			// Inform the user that the operation is aborted due to missing permission
			// so it is clear why the screen doesn't update
			showFailureDialog(R.string.no_permission_try_again, pageToLoad);
			currentlyLoadingPage = NO_PAGE;
			return;
		}

		// permission granted, go ahead with the operation

		if (verbose) Log.v(LOG_TAG, "Load movies, page=" + pageToLoad);

		String listName = UserPreferences.getMovieList(getActivity());

		SequentialUpdateMovieListTask updateTask = new SequentialUpdateMovieListTask(getActivity());
		updateTask.execute(new UpdateMovieListTask.Input(listName, pageToLoad));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String listName = UserPreferences.getMovieList(getActivity());
		String sortOrder =
			PopularMoviesContract.ListMembershipContract.Column.PAGE + " ASC, " +
			PopularMoviesContract.ListMembershipContract.Column.POSITION + " ASC";

		Uri listMemberUri = PopularMoviesContract.ListContract.buildListMemberDirectoryUri(listName);

		return new CursorLoader(
			getContext(),
			listMemberUri,
			CURSOR_PROJECTION,
			null,
			null,
			sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		viewAdapter.swapCursor(cursor);

		// determine whether we should update the list
		// TODO determine whether to update the list from the server

		if (cursor.getCount() > 0)
			return; // database query returned results so we're done

		// there are no movies in the database
		// query the first page

		downloadMovieData(1); // start with page 1
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		viewAdapter.swapCursor(null);
	}

	// --- Error handling ---

	/**
	 * Displays a dialog informing the user that movie data download failed with a critical
	 * error (network down or required permission denied). The user gets the choice between
	 * retrying the operation og closing the activity.
	 */
	private void showFailureDialog(final int messageId, final int pageToLoad) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
		alertDialogBuilder.setMessage(messageId);

		alertDialogBuilder.setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				downloadMovieData(pageToLoad);
			}
		});
		alertDialogBuilder.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getActivity().finish();
			}
		});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
}
