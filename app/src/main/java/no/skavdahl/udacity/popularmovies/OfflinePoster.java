package no.skavdahl.udacity.popularmovies;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

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
	 * @param title The title of the movie
	 *
	 * @return a Drawable able to paint the generated movie poster.
	 */
	public static Drawable forMovie(Context context, String title) {
		// TODO Investigate if it's possible to generate a vector-based drawable rather than a BitmapDrawable
		// My hypothesis is that this would use less memory and have higher image quality
		// Possible classes: ShapeDrawable VectorDrawable

		Resources resources = context.getResources();
		final int posterWidth = (int) resources.getDimension(R.dimen.poster_width);
		final int posterHeight = (int) resources.getDimension(R.dimen.poster_height);

		// set the background color to the movie's fallback color
		Bitmap bitmap = Bitmap.createBitmap(posterWidth, posterHeight, Bitmap.Config.ARGB_8888);
		bitmap.eraseColor(0xFF000000 | generateColorCode(context, title));

		// draw the movie title. The title may be arbitrarily long, more than can fit on one
		// line. We need to break the text into lines, fitting as much as possible on each line.
		drawTextWithWrap(context, bitmap, title);

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

	/**
	 * Generates a color code associated with the given movie title. The title is used
	 * as a base for the calculation in order to generate a reproducible result (the same
	 * color is generated every time). The generated number will be in the range 0xFF000000
	 * through 0xFFFFFFFF and should be interpreted as four bytes representing the alpha,
	 * R, G and B components respectively.
	 *
	 * @param context Context in order to access color resources
	 * @param title The movie title from which to start the calculation
	 *
	 * @return a 4-byte value that can be interpreted as an ARGB color
	 */
	private static int generateColorCode(Context context, String title) {
		//  choose a color from Android's Material Design palette
		int groupIndex = R.array.mdcolor_500;
		Resources resources = context.getResources();

		TypedArray group = resources.obtainTypedArray(groupIndex);
		try {
			int index = Math.abs(title.hashCode() % group.length());
			return group.getColor(index, Color.WHITE);
		}
		finally {
			group.recycle();
		}
	}
}

