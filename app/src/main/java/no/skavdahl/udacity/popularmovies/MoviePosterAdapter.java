package no.skavdahl.udacity.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import no.skavdahl.udacity.popularmovies.mdb.MdbJSONAdapter;
import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * Adapter for displaying movie posters in ListViews.
 *
 * @author fdavs
 */
public class MoviePosterAdapter extends CursorAdapter {

	private final String LOG_TAG = getClass().getSimpleName();

	private final Context context;
	private List<Movie> movies = Collections.emptyList();
	private Date movieLoadTime;
	private final int posterViewWidth;
	private final int posterViewHeight;

	private final MdbJSONAdapter movieJsonAdapter;

	public MoviePosterAdapter(final Context context, final Cursor cursor, final int flags, final int posterViewWidth) {
		super(context, cursor, flags);

		this.context = context;
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
		String movieJson = cursor.getString(1); // TODO remove magic number, use a constant instead

		try {
			Movie m = movieJsonAdapter.toMovie(new JSONObject(movieJson));
			PicassoUtils.displayPosterWithOfflineFallback(context, m, (ImageView) posterView);
		}
		catch (Exception e) {
			Log.e(LOG_TAG, "Unable to bind movie to view: " + movieJson, e);
		}
	}

	/*
	public Date getMovieLoadTime() {
		return movieLoadTime;
	}
	public List<Movie> getMovies() {
		return Collections.unmodifiableList(movies);
	}
	public void setMovies(final Collection<Movie> movies, final Date loadTime) {
		this.movies = new ArrayList<>(movies);
		this.movieLoadTime = loadTime;
		notifyDataSetChanged();
	}*/

	/*@Override
	public int getCount() {
		return movies.size();
	}

	@Override
	public Object getItem(int position) {
		return movies.get(position);
	}

	@Override
	public long getItemId(int position) {
		// we don't use this value so we can return anything
		return 0;
	}*/

	/*
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView posterView;

		if (convertView != null) {
			posterView = (ImageView) convertView;
		}
		else {
			posterView = new ImageView(context);
			posterView.setPadding(0, 0, 0, 0);
			posterView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			posterView.setLayoutParams(new ViewGroup.LayoutParams(posterViewWidth, posterViewHeight));
		}

		PicassoUtils.displayPosterWithOfflineFallback(context, movies.get(position), posterView);

		return posterView;
	}
	*/
}
