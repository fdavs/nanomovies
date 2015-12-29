package no.skavdahl.udacity.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import org.json.JSONObject;

import no.skavdahl.udacity.popularmovies.mdb.MdbJSONAdapter;
import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * Adapter for displaying movie posters in ListViews.
 *
 * @author fdavs
 */
public class MoviePosterAdapter extends CursorAdapter {

	private final String LOG_TAG = getClass().getSimpleName();

	private final int cursorPosterIndex;
	private final int posterViewWidth;
	private final int posterViewHeight;

	private final MdbJSONAdapter movieJsonAdapter;

	/**
	 * Initializes a MoviePosterAdapter.
	 *
	 * @param context The context
	 * @param cursor The cursor from which to get data
	 * @param flags Flags used to determine the behavior of the adapter (see base class)
	 * @param cursorPosterIndex The index of the cursor where we can read the poster path
	 * @param posterViewWidth The poster view width in pixels
	 */
	public MoviePosterAdapter(final Context context, final Cursor cursor, final int flags, final int cursorPosterIndex, final int posterViewWidth) {
		super(context, cursor, flags);

		this.cursorPosterIndex = cursorPosterIndex;
		float posterWidth = context.getResources().getDimension(R.dimen.poster_width);
		float posterHeight = context.getResources().getDimension(R.dimen.poster_height);
		this.posterViewWidth = posterViewWidth;
		this.posterViewHeight = (int) (posterViewWidth * posterHeight /posterWidth);

		movieJsonAdapter = new MdbJSONAdapter(context.getResources());
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		ImageView posterView = new ImageView(context);
		posterView.setPadding(0, 0, 0, 0);
		posterView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		posterView.setLayoutParams(new ViewGroup.LayoutParams(posterViewWidth, posterViewHeight));
		return posterView;
	}

	@Override
	public void bindView(View posterView, Context context, Cursor cursor) {
		// TODO read poster path directly from cursor
		String movieJson = cursor.getString(cursorPosterIndex);

		try {
			Movie m = movieJsonAdapter.toMovie(new JSONObject(movieJson));
			PicassoUtils.displayPosterWithOfflineFallback(context, m, (ImageView) posterView);
		}
		catch (Exception e) {
			Log.e(LOG_TAG, "Unable to bind movie to view: " + movieJson, e);
		}
	}
}
