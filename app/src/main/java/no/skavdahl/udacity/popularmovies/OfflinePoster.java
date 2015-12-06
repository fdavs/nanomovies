package no.skavdahl.udacity.popularmovies;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * Utility class for creating default movie posters for situations when the real poster
 * cannot be accessed.
 *
 * @author fdavs
 */
public class OfflinePoster {
	/**
	 * Creates a default movie poster using a color and the movie title.
	 *
	 * @param context The context
	 * @param movie The movie for which to create a poster.
	 *
	 * @return a Drawable able to paint the generated movie poster.
	 */
	public static Drawable forMovie(Context context, Movie movie) {
		// TODO Investigate if it's possible to generate a vector-based drawable rather than a BitmapDrawable
		// My hypothesis is that this would use less memory and have higher image quality
		// Possible classes: ShapeDrawable VectorDrawable

		final int posterWidth = (int) context.getResources().getDimension(R.dimen.poster_width);
		final int posterHeight = (int) context.getResources().getDimension(R.dimen.poster_height);

		// set the background color to the movie's fallback color
		Bitmap bitmap = Bitmap.createBitmap(posterWidth, posterHeight, Bitmap.Config.ARGB_8888);
		bitmap.eraseColor(0xFF000000 | movie.getFallbackColorCode());

		// draw the movie title. The title may be arbitrarily long, more than can fit on one
		// line. We need to break the text into lines, fitting as much as possible on each line.
		drawTextWithWrap(context, bitmap, movie.getTitle());

		return new BitmapDrawable(Resources.getSystem(), bitmap);
	}

	public static void drawTextWithWrap(Context context, Bitmap bitmap, String text) {
		// how much space to reserve for whitespace along the edges
		// when placing the move title text lines

		final int margin = context.getResources().getDimensionPixelOffset(R.dimen.poster_margin_offline);
		final float textSize = context.getResources().getDimension(R.dimen.poster_title_size);

		TextPaint paint = new TextPaint();
		paint.setTextSize(textSize);
		paint.setAntiAlias(true);

		Canvas cs = new Canvas(bitmap);
		cs.translate(margin, margin);

		StaticLayout layout = new StaticLayout(text, paint, bitmap.getWidth() - 2*margin, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		layout.draw(cs);
	}
}

