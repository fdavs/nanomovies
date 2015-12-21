package no.skavdahl.udacity.popularmovies.data;

import android.content.ContentValues;

import static no.skavdahl.udacity.popularmovies.data.MovieContracts.*;

/**
 * A collection of test data that can be used in unit tests.
 *
 * @author fdavs
 */
public class TestData {

	public static class Movie1 {
		public static final int ID = 206647;
		public static final String POSTER_PATH = "/1n9D32o30XOHMdMWuIT4AaA5ruI.jpg";
		public static final int POSTER_WIDTH = 185;
		public static final long DOWNLOAD_TIME = System.currentTimeMillis();

		public static ContentValues asContentValues() {
			ContentValues cv = new ContentValues();
			cv.put(MovieContract.Column._ID, ID);
			cv.put(MovieContract.Column.DOWNLOAD_TIME, DOWNLOAD_TIME);
			cv.put(MovieContract.Column.FAVORITE, 0);
			cv.put(MovieContract.Column.JSON,
				"{" +
					"`adult`:false," +
					"`backdrop_path`:`/wVTYlkKPKrljJfugXN7UlLNjtuJ.jpg`," +
					"`genre_ids`:[28,12,80]," +
					"`id`:" + ID + "," +
					"`original_language`:`en`," +
					"`original_title`:`Spectre`," + "" +
					"`overview`:`A cryptic message from Bond’s past ...`," +
					"`release_date`:`2015-11-06`," +
					"`poster_path`:`" + POSTER_PATH + "`," +
					"`popularity`:54.146108," +
					"`title`:`Spectre`," +
					"`video`:false," +
					"`vote_average`:6.5," +
					"`vote_count`:490" +
					"}".replace("`", "\""));
			return cv;
		}
	}

	public static class Movie2 {
		public static final int ID = 135397;
		public static final String POSTER_PATH = "/jjBgi2r5cRt36xF6iNUEhzscEcb.jpg";
		public static final int POSTER_WIDTH = 185;
		public static final long DOWNLOAD_TIME = System.currentTimeMillis();

		public static ContentValues asContentValues() {
			ContentValues cv = new ContentValues();
			cv.put(MovieContract.Column._ID, ID);
			cv.put(MovieContract.Column.DOWNLOAD_TIME, DOWNLOAD_TIME);
			cv.put(MovieContract.Column.FAVORITE, 0);
			cv.put(MovieContract.Column.JSON,
				"{" +
					"`adult`:false," +
					"`backdrop_path`:`/dkMD5qlogeRMiEixC4YNPUvax2T.jpg`," +
					"`genre_ids`:[28,12,878,53]," +
					"`id`:" + ID + "," +
					"`original_language`:`en`," +
					"`original_title`:`Jurassic World`," +
					"`overview`:`Twenty-two years after the events of Jurassic Park...`," +
					"`release_date`:`2015-06-12`," +
					"`poster_path`:`" + POSTER_PATH + "`," +
					"`popularity`:35.837265," +
					"`title`:`Jurassic World`," +
					"`video`:false," +
					"`vote_average`:6.8," +
					"`vote_count`:2941" +
					"}".replace("`", "\""));
			return cv;
		}
	}
}
