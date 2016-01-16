package no.skavdahl.udacity.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

import no.skavdahl.udacity.popularmovies.data.ToggleFavoriteTask;
import no.skavdahl.udacity.popularmovies.data.UpdateMovieTask;
import no.skavdahl.udacity.popularmovies.mdb.MdbJSONAdapter;
import no.skavdahl.udacity.popularmovies.model.Movie;
import no.skavdahl.udacity.popularmovies.model.Review;
import no.skavdahl.udacity.popularmovies.model.Video;

import static no.skavdahl.udacity.popularmovies.data.PopularMoviesContract.*;

/**
 * Fragment displaying movie details.
 *
 * @author fdavs
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private final String LOG_TAG = getClass().getSimpleName();

	// --- content ---

	/** Key to find the content URI in the fragment arguments (savedInstanceState). */
	public static final String CONTENT_URI = "contentUri";

	///** URI to the content (movie) being displayed by this fragment. May be <tt>null</tt>. */
	private Uri contentUri;

	/** The maximum number of video and trailer links to display. */
	private static final int MAX_VIDEOS = 10;

	/** The maximum number of reviews to display. */
	private static final int MAX_REVIEWS = 10;

	// --- loader ---

	/** Identity of the movie details loader within the LoaderManager. */
	private static final int LOADER_ID = 0;

	// --- cursor configuration ---

	private final static String[] CURSOR_PROJECTION = new String[] {
		MovieContract.Column._ID,
		MovieContract.Column.MODIFIED,
		MovieContract.Column.TITLE,
		MovieContract.Column.POSTER_PATH,
		MovieContract.Column.BACKDROP_PATH,
		MovieContract.Column.SYNOPSIS,
		MovieContract.Column.VOTE_AVERAGE,
		MovieContract.Column.VOTE_COUNT,
		MovieContract.Column.RELEASE_DATE,
		MovieContract.Column.EXTENDED_DATA,
		MovieContract.Column.REVIEWS_JSON,
		MovieContract.Column.VIDEOS_JSON,
		MovieContract.Column.FAVORITE
	};

	private final static int CURSOR_INDEX_ID = 0;
	private final static int CURSOR_INDEX_MODIFIED = 1;
	private final static int CURSOR_INDEX_TITLE = 2;
	private final static int CURSOR_INDEX_POSTER_PATH = 3;
	private final static int CURSOR_INDEX_BACKDROP_PATH = 4;
	private final static int CURSOR_INDEX_SYNOPSIS = 5;
	private final static int CURSOR_INDEX_VOTE_AVERAGE = 6;
	private final static int CURSOR_INDEX_VOTE_COUNT = 7;
	private final static int CURSOR_INDEX_RELEASE_DATE = 8;
	private final static int CURSOR_INDEX_EXTENDED_DATA = 9;
	private final static int CURSOR_INDEX_REVIEWS_JSON = 10;
	private final static int CURSOR_INDEX_VIDEOS_JSON = 11;
	private final static int CURSOR_INDEX_FAVORITE = 12;

	// --- share configuration ---

	private ShareActionProvider shareActionProvider;

	private static final String SHARE_YOUTUBE_LINK = "http://www.youtube.com/watch?v=";
	private static final String SHARE_THEMOVIEDB_LINK = "https://www.themoviedb.org/movie/";

	// --- UI ---

	protected @Bind(R.id.movie_title_textview) TextView titleView;
	protected @Bind(R.id.synopsis_textview) TextView synopsisView;
	protected @Bind(R.id.release_rate_textview) TextView releaseDateView;
	protected @Bind(R.id.user_rating_textview) TextView userRatingView;
	protected @Bind(R.id.poster_imageview) ImageView posterView;
	protected @Bind(R.id.backdrop_imageview) ImageView backdropView;
	protected @Bind(R.id.favorite_button) ImageButton favoriteBtn;
	protected @Bind(R.id.videos_container) ViewGroup videosContainer;
	protected @Bind(R.id.reviews_container) ViewGroup reviewsContainer;


	/**
	 * Factory method.
	 */
	public static MovieDetailFragment newInstance(final Uri contentUri) {
		Bundle args = bundleContentUri(contentUri);

		MovieDetailFragment fragment = new MovieDetailFragment();
		fragment.setArguments(args);

		return fragment;
	}

	private static Bundle bundleContentUri(Uri contentUri) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(CONTENT_URI, contentUri);
		return bundle;
	}

	private static Uri unbundleContentUri(Bundle bundle) {
		if (bundle == null)
			return null;
		return bundle.getParcelable(CONTENT_URI);
	}

	/**
	 * Default constructor (required).
	 */
	public MovieDetailFragment() {
		setHasOptionsMenu(true); // required for "share" actions
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
	                         ViewGroup container,
	                         Bundle savedInstanceState) {

		// retrieve fragment arguments
		contentUri =  unbundleContentUri(getArguments());
		Log.w(LOG_TAG, "onCreate: contentUri=" + contentUri); // TODO delete this log statement

		// create the view
		View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);
		ButterKnife.bind(this, view);
		bindEmptyModelToView();
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		if (contentUri != null) {
			inflater.inflate(R.menu.menu_movie_details, menu);
			MenuItem shareMenuItem = menu.findItem(R.id.menu_item_share);
			shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// initialize the loader
		if (contentUri != null) {
			Bundle loaderArgs = bundleContentUri(contentUri);
			getLoaderManager().initLoader(LOADER_ID, loaderArgs, this);
		}

		Log.d(LOG_TAG, "onActivityCreated contentUri=" + contentUri); // TODO remove logging
	}

	/**
	 * Clears all views that have some non-empty placeholder content (typically from xliff tags).
	 */
	private void bindEmptyModelToView() {
		titleView.setText("");
		synopsisView.setText("");
		releaseDateView.setText("");
		userRatingView.setText("");
	}

	/**
	 * Updates the view with content from the given cursor.
	 */
	private void bindModelToView(final Cursor cursor) {
		final Context context = getActivity();

		// Some fields can change when the movie information is updated from the online
		// database. Other fields are more static. In order to avoid a "flashing" effect,
		// prevent updating the static fields unless it's necessary.
		String currentTitle = titleView.getText().toString(); // non-null
		String cursorTitle = cursor.getString(CURSOR_INDEX_TITLE);
		boolean hasChanged = !currentTitle.equals(cursorTitle);

		if (hasChanged) {
			titleView.setText(cursorTitle);
			synopsisView.setText(formatOptString(cursor.getString(CURSOR_INDEX_SYNOPSIS), ""));

			releaseDateView.setText(
				context.getString(
					R.string.movie_release_date,
					formatOptYear(
						new Date(cursor.getLong(CURSOR_INDEX_RELEASE_DATE)),
						context.getString(R.string.data_unknown))));

			PicassoUtils.displayPosterWithOfflineFallback(
				context,
				cursor.getString(CURSOR_INDEX_POSTER_PATH),
				cursor.getString(CURSOR_INDEX_TITLE),
				posterView);

			PicassoUtils.displayBackdrop(
				context,
				cursor.getString(CURSOR_INDEX_BACKDROP_PATH),
				cursor.getString(CURSOR_INDEX_POSTER_PATH),
				backdropView);
		}

		// the following fields may change when movie details are downloaded again
		userRatingView.setText(
			context.getResources().getQuantityString(
				R.plurals.movie_user_rating, // id
				cursor.getInt(CURSOR_INDEX_VOTE_COUNT), // quantity
				cursor.getDouble(CURSOR_INDEX_VOTE_AVERAGE), cursor.getInt(CURSOR_INDEX_VOTE_COUNT))); // format args

		configureFavoriteBtn(
			cursor.getInt(CURSOR_INDEX_ID),
			cursor.getString(CURSOR_INDEX_POSTER_PATH),
			cursor.getString(CURSOR_INDEX_BACKDROP_PATH),
			cursor.getInt(CURSOR_INDEX_FAVORITE) > 0);

		// videos
		bindVideosToView(cursor, videosContainer);

		// reviews
		bindReviewsToView(cursor, reviewsContainer);

		// configure the share action
		configureShareIntent(
			cursor.getInt(CURSOR_INDEX_ID),
			MdbJSONAdapter.toVideoList(cursor.getString(CURSOR_INDEX_VIDEOS_JSON)));
	}

	private void bindReviewsToView(Cursor cursor, ViewGroup container) {
		final LayoutInflater inflater = LayoutInflater.from(getActivity());

		container.removeAllViews();

		List<Review> reviewList = MdbJSONAdapter.toReviewList(cursor.getString(CURSOR_INDEX_REVIEWS_JSON));
		if (reviewList.isEmpty()) {
			TextView textView = (TextView) inflater.inflate(R.layout.simple_textview, container, false);
			textView.setText(R.string.no_reviews);
			container.addView(textView);
		}
		else {
			int numberToDisplay = Math.min(reviewList.size(), MAX_REVIEWS);
			for (int i = 0; i < numberToDisplay; ++i) {
				Review review = reviewList.get(i);
				View reviewView = inflater.inflate(R.layout.review_detail, container, false);

				TextView reviewTextView = (TextView) reviewView.findViewById(R.id.review_content_textview);
				reviewTextView.setText(review.getContent());

				TextView authorTextView = (TextView) reviewView.findViewById(R.id.review_author_textview);
				authorTextView.setText(review.getAuthor());

				container.addView(reviewView);
			}
		}
	}

	private void bindVideosToView(Cursor cursor, ViewGroup container) {
		final LayoutInflater inflater = LayoutInflater.from(getActivity());

		container.removeAllViews();

		List<Video> allVideos = MdbJSONAdapter.toVideoList(cursor.getString(CURSOR_INDEX_VIDEOS_JSON));
		List<Video> youtubeVideos = Movie.filterVideosBySite(allVideos, Video.SITE_YOUTUBE);
		if (youtubeVideos.isEmpty()) {
			TextView textView = (TextView) inflater.inflate(R.layout.simple_textview, container, false);
			textView.setText(R.string.no_videos);
			container.addView(textView);
		}
		else {
			int numberToDisplay = Math.min(youtubeVideos.size(), MAX_VIDEOS);
			for (int i = 0; i < numberToDisplay; ++i) {
				final Video video = youtubeVideos.get(i);

				View video_view = inflater.inflate(R.layout.trailer_detail, container, false);

				TextView titleTextView = (TextView) video_view.findViewById(R.id.video_title);
				titleTextView.setText(video.getName());

				video_view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						playVideo(video);
					}
				});

				container.addView(video_view);
			}
		}
	}

	/**
	 * Configure the favorite button, settings its on/off state and installing an appropriate
	 * click listener to toggle the favorite state.
	 */
	private void configureFavoriteBtn(
		final int movieId,
		final String posterPath,
		final String backdropPath,
		final boolean isFavorite) {

		favoriteBtn.setImageResource(
			isFavorite
				? android.R.drawable.btn_star_big_on
				: android.R.drawable.btn_star_big_off);

		favoriteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleFavorite(movieId, posterPath, backdropPath, isFavorite);
			}
		});
	}

	/**
	 * Set up the share menu with a suitable link.
	 *
	 * If the displayed movie has at least one YouTube trailer, the share action will send
	 * a link to the trailer. Otherwise, it will send a link to the movie on themoviedb.org.
	 */
	private void configureShareIntent(int shareMovieId, List<Video> shareVideos) {
		if (shareActionProvider == null || shareMovieId == 0 || shareVideos == null)
			return;

		// pick the first YouTube video:
		// videos.stream().filter(v -> v.site == YouTube).findFirst()
		List<Video> youtubeVideos = Movie.filterVideosBySite(shareVideos, Video.SITE_YOUTUBE);
		Video shareableVideo = youtubeVideos.isEmpty() ? null : youtubeVideos.get(0);
		String linkToShare = (shareableVideo != null)
			? SHARE_YOUTUBE_LINK + shareableVideo.getKey()
			: SHARE_THEMOVIEDB_LINK + shareMovieId;

		String messageToFriend = getContext().getString(R.string.share_video_message, linkToShare);

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(android.content.Intent.EXTRA_TEXT, messageToFriend);

		shareActionProvider.setShareIntent(intent);
	}

	/**
	 * Plays the given video. If YouTube is installed on the device, the video is opened
	 * in that app. Otherwise, fall back to the web browser or similar app to play the
	 * video there.
	 *
	 * @param video Reference to the video that is to be played
	 */
	private void playVideo(Video video) {
		String key = video.getKey();
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + key));
			startActivity(intent);
		}
		catch (ActivityNotFoundException ex) {
			Intent intent = new Intent(
				Intent.ACTION_VIEW,
				Uri.parse("http://www.youtube.com/watch?v=" + key));
			startActivity(intent);
		}
	}

	/**
	 * Toggles whether the currently displayed movie is one of the user's favorite movies
	 * or not.
	 *
	 * @param movieId The movie being added or removed from the favorite movies list
	 * @param posterPath The path to the movie poster
	 * @param backdropPath The path to the movie backdrop
	 * @param isFavorite Whether the movie is currently a favorite movie
	 */
	private void toggleFavorite(int movieId, String posterPath, String backdropPath, boolean isFavorite) {
		// optimistically update the "make favorite" button assuming success
		configureFavoriteBtn(movieId, posterPath, backdropPath, !isFavorite);

		if (isFavorite)
			ToggleFavoriteTask.removeFromFavorites(getContext(), movieId, posterPath, backdropPath);
		else
			ToggleFavoriteTask.addToFavorites(getActivity(), movieId, posterPath, backdropPath);
	}

	private String formatOptString(final String str, final String fallback) {
		return (str != null) ? str : fallback;
	}

	private String formatOptYear(final Date date, final String fallback) {
		if (date == null)
			return fallback;

		Calendar cal = GregorianCalendar.getInstance(Locale.getDefault());
		cal.setTime(date);

		return Integer.toString(cal.get(Calendar.YEAR));
	}

	// --- LoaderManager.LoaderCallback<Loader> interface ---

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {
		if (loaderArgs == null)
			return null;

		Uri contentUri = unbundleContentUri(loaderArgs);
		if (contentUri == null)
			return null;

		return new CursorLoader(
			getActivity(),
			contentUri,
			CURSOR_PROJECTION,
			null, // selection
			null, // selection args
			null); // sort order
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor == null || !cursor.moveToFirst())
			return; // no data to handle so just stop

		// update the view
		bindModelToView(cursor);

		// evaluate the data: do we need to issue an web update?
		// we need to decide if the data is current and up-to-date or need to be refreshed
		// It is up-to-date if
		//   a) we have extended movie data (reviews, videos)
		//   b) the data are not "too old"
		// "too old" is determined by the quality of the network connection. A faster and cheaper
		// connection makes "too old" a shorter amount of time
		// TODO include network quality (none, metered, broadband) in the decision of "too old"

		long dataModifiedTime = cursor.getLong(CURSOR_INDEX_MODIFIED);
		boolean hasExtendedData = cursor.getInt(CURSOR_INDEX_EXTENDED_DATA) > 0;

		long dataAge = System.currentTimeMillis() - dataModifiedTime;
		long maxAge = BuildConfig.MOVIE_DATA_TIMEOUT;

		if (!hasExtendedData || dataAge > maxAge) {
			final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);
			if (verbose) Log.v(LOG_TAG, "Movie data is missing or stale - updating");

			int movieId = cursor.getInt(CURSOR_INDEX_ID);

			UpdateMovieTask asyncTask = new UpdateMovieTask(getContext());
			asyncTask.execute(movieId);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// no action
	}

	// --- End LoaderManager.LoaderCallback<Loader> interface ---
}
