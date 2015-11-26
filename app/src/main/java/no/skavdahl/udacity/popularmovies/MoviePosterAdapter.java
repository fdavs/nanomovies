package no.skavdahl.udacity.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.Collections;
import java.util.List;

import no.skavdahl.udacity.popularmovies.mdb.DiscoverMovies;
import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * Adapter for displaying movie posters in ListViews.
 *
 * @author fdavs
 */
public class MoviePosterAdapter extends BaseAdapter {
	private final Context context;
	private List<Movie> movies = Collections.emptyList();
	private final int posterViewWidth;
	private final int posterViewHeight;

	public MoviePosterAdapter(final Context context, final int posterViewWidth) {
		this.context = context;
		this.posterViewWidth = posterViewWidth;
		this.posterViewHeight = posterViewWidth * DiscoverMovies.POSTER_SIZE_PIXELS.y / DiscoverMovies.POSTER_SIZE_PIXELS.x;
	}

	public void setMovies(List<Movie> movies) {
		this.movies = movies;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return movies.size();
	}

	@Override
	public Object getItem(int position) {
		return movies.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0; // TODO Implement this method
	}

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

		PicassoUtils.displayWithFallback(context, movies.get(position), posterView, null);

		return posterView;
	}
}
