package no.skavdahl.udacity.utils.picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import no.skavdahl.udacity.popularmovies.BuildConfig;

/**
 * A Picasso Target saving image data to a disk file.
 *
 * @author fdavs
 */
public abstract class FileTarget implements Target {

	private final String LOG_TAG = getClass().getSimpleName();


	private final Context context;
	private final String imageName;

	/** Image quality of the compressed image (0-100, where 0 is no compression). */
	private final int IMAGE_QUALITY = 80;

	private static final String STORAGE_PATH = "MoviePosters";


	public FileTarget(final Context context, final String imageName) {
		this.context = context;
		this.imageName = imageName;
	}

	@SuppressWarnings("TryFinallyCanBeTryWithResources") // requires API level 19
	@Override
	public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
		File targetFile = null;

		try {
			targetFile = getStorageFile();
			FileOutputStream targetStream = new FileOutputStream(targetFile);

			try {
				bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, targetStream);
				targetStream.flush();

				boolean debug = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.DEBUG);
				if (debug) Log.d(LOG_TAG, "Image data saved to " + targetFile);
			}
			finally {
				targetStream.close();
			}

			// notify listener that operation succeeded
			notifySuccess(targetFile.getAbsolutePath());
		}
		catch (Exception e) {
			Log.e(LOG_TAG, "Error saving image to local storage: " + targetFile, e);
		}
	}

	/**
	 * Choose a local file for the image data.
	 *
	 * Image files can be sizable so it's preferable to store them in "external" storage,
	 * if this is available on the device right now. Otherwise there is no choice but to
	 * store them in the "internal" storage.
	 */
	private File getStorageFile() throws IOException {
		File targetDir = context.getExternalFilesDir(null); // returns non-null iff mounted and available
		if (targetDir == null)
			targetDir = context.getDir(STORAGE_PATH, Context.MODE_PRIVATE);

		if (!targetDir.exists())
			targetDir.mkdirs();

		return new File(targetDir, imageName);
	}

	/**
	 * Called when the image has been successfully saved to local storage.
	 *
	 * @param imagePath The absolute file name of the image.
	 */
	public abstract void onSuccess(String imagePath);

	protected void notifySuccess(String imagePath) {
		try {
			onSuccess(imagePath);
		}
		catch (Exception e) {
			Log.e(LOG_TAG, "Error reported by onSuccess handler for image " + imagePath, e);
		}
	}

	@Override
	public void onBitmapFailed(Drawable errorDrawable) {
		Log.w(LOG_TAG, "Bitmap image could not be loaded: " + imageName);
	}

	@Override
	public void onPrepareLoad(Drawable placeHolderDrawable) {
		// no operation
	}
}
