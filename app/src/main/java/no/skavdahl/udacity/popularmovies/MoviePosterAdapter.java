package no.skavdahl.udacity.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

	private Cursor cursor;
	private final Context context;

	private final int cursorMovieIdIndex;
	private final int cursorPosterIndex;
	private final int cursorTitleIndex;

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
	 * @param clickListener Callback to be notified about clicks on movie poster images
	 */
	public MoviePosterAdapter(
		final Context context,
		final int cursorMovieIdIndex,
		final int cursorPosterIndex,
		final int cursorTitleIndex,
		final MovieClickListener clickListener) {

		this.context = context;
		this.movieClickListener = clickListener;
		this.cursorMovieIdIndex = cursorMovieIdIndex;
		this.cursorPosterIndex = cursorPosterIndex;
		this.cursorTitleIndex = cursorTitleIndex;
	}

	// --- RecyclerView.Adapter interface ---

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		ImageView posterView = (ImageView) inflater.inflate(R.layout.movie_poster_item, parent, false);

		posterView.setOnClickListener(viewClickListener);

		return new ViewHolder(posterView);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		cursor.moveToPosition(position);

		int movieId = cursor.getInt(cursorMovieIdIndex);
		String posterPath = cursor.getString(cursorPosterIndex);
		String title = cursor.getString(cursorTitleIndex);

		ImageView posterView = (ImageView) holder.itemView;
		posterView.setTag(movieId);
		PicassoUtils.displayPosterWithOfflineFallback(context, posterPath, title, posterView);
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
		if (newCursor != null) {
			this.notifyDataSetChanged();
		}
		return oldCursor;
	}
}
