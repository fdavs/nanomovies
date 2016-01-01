package no.skavdahl.udacity.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.json.JSONObject;

import no.skavdahl.udacity.popularmovies.mdb.MdbJSONAdapter;
import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * Adapter for displaying movie posters.
 *
 * @author fdavs
 */
public class MoviePosterAdapter extends RecyclerView.Adapter<MoviePosterAdapter.ViewHolder> {

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public ViewHolder(ImageView itemView) {
			super(itemView);
		}
	}

	public interface MovieClickListener {
		void OnMovieClicked(int movieId);
	}

	private final String LOG_TAG = getClass().getSimpleName();

	private Cursor cursor;
	private final Context context;

	private final int cursorMovieIdIndex;
	private final int cursorPosterIndex;
	private final int posterViewWidth;
	private final int posterViewHeight;

	private final MdbJSONAdapter movieJsonAdapter;

	private final MovieClickListener movieClickListener;
	private final View.OnClickListener viewClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Integer clickedMovieId = (Integer) v.getTag();

			if (movieClickListener != null && clickedMovieId != null)
				movieClickListener.OnMovieClicked(clickedMovieId);
		}
	};

	/**
	 * Initializes a MoviePosterAdapter.
	 *
	 * @param context The context
	 * @param cursorMovieIdIndex The index into the cursor for reading the movie id
	 * @param cursorPosterIndex The index into the cursor for reading the poster path
	 * @param posterViewWidth The desired poster view width in pixels
	 * @param clickListener Callback to be notified about clicks on movie poster images
	 */
	public MoviePosterAdapter(
		final Context context,
		final int cursorMovieIdIndex,
		final int cursorPosterIndex,
		final int posterViewWidth,
		final MovieClickListener clickListener) {

		this.context = context;
		this.movieClickListener = clickListener;
		this.cursorMovieIdIndex = cursorMovieIdIndex;
		this.cursorPosterIndex = cursorPosterIndex;

		float posterWidth = context.getResources().getDimension(R.dimen.poster_width);
		float posterHeight = context.getResources().getDimension(R.dimen.poster_height);

		this.posterViewWidth = posterViewWidth;
		this.posterViewHeight = (int) (posterViewWidth * posterHeight /posterWidth);

		movieJsonAdapter = new MdbJSONAdapter(context.getResources());
	}

	// --- RecyclerView.Adapter interface ---

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		ImageView posterView = new ImageView(parent.getContext());
		posterView.setPadding(0, 0, 0, 0);
		posterView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		posterView.setLayoutParams(new ViewGroup.LayoutParams(posterViewWidth, posterViewHeight));
		posterView.setOnClickListener(viewClickListener);

		return new ViewHolder(posterView);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		cursor.moveToPosition(position);
		int movieId = cursor.getInt(cursorMovieIdIndex);
		String movieJson = cursor.getString(cursorPosterIndex); // TODO read poster path directly from cursor

		try {
			Movie m = movieJsonAdapter.toMovie(new JSONObject(movieJson));

			ImageView posterView = (ImageView) holder.itemView;

			posterView.setTag(movieId);
			PicassoUtils.displayPosterWithOfflineFallback(context, m, posterView);
		}
		catch (Exception e) {
			Log.e(LOG_TAG, "Unable to bind movie to view #" + position, e);
		}
	}

	@Override
	public int getItemCount() {
		return (cursor == null) ? 0 : cursor.getCount();
	}

	// --- Cursor loader support ---

	public Cursor swapCursor(final Cursor newCursor) {
		if (cursor == newCursor)
			return null;

		Cursor oldCursor = this.cursor;
		this.cursor = newCursor;
		if (cursor != null) {
			this.notifyDataSetChanged();
		}
		return oldCursor;
	}
}
