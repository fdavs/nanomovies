package no.skavdahl.udacity.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.Collections;
import java.util.List;

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
		float posterWidth = context.getResources().getDimension(R.dimen.poster_width);
		float posterHeight = context.getResources().getDimension(R.dimen.poster_height);
		this.posterViewWidth = posterViewWidth;
		this.posterViewHeight = (int) (posterViewWidth * posterHeight /posterWidth);
	}

	public void setMovies(final List<Movie> movies) {
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
		// we don't use this value so we can return anything
		return 0;
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
