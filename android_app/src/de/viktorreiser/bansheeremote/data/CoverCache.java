package de.viktorreiser.bansheeremote.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import de.viktorreiser.toolbox.os.SoftPool;

/**
 * Global cover cache pool.<br>
 * <br>
 * You can persist and load covers here. Every loaded cover is cached in memory for quick access.
 * 
 * @author Viktor Reiser &lt;<a href="mailto:viktorreiser@gmx.de">viktorreiser@gmx.de</a>&gt;
 */
public class CoverCache {
	
	// PRIVATE ====================================================================================
	
	private static SoftPool<Bitmap> mCoverCache = new SoftPool<Bitmap>();
	
	// PUBLIC =====================================================================================
	
	/**
	 * Does cover already exist?
	 * 
	 * @param id
	 *            cover ID returned by server
	 * 
	 * @return {@code true} if cover is available locally
	 */
	public static boolean coverExists(String id) {
		return mCoverCache.get(id.hashCode()) != null
				|| new File(App.BANSHEE_PATH + id + ".jpg").exists();
	}
	
	/**
	 * Get cover.
	 * 
	 * @param id
	 *            cover ID returned by server
	 * 
	 * @return cover as bitmap or {@code null} when there is no cover for given ID
	 */
	public static Bitmap getCover(String id) {
		int hash = id.hashCode();
		Bitmap cover = mCoverCache.get(hash);
		
		if (cover != null) {
			return cover;
		}
		
		File file = new File(App.BANSHEE_PATH + id + ".jpg");
		
		if (file.exists()) {
			cover = BitmapFactory.decodeFile(file.getAbsolutePath());
			
			if (cover != null) {
				mCoverCache.put(hash, cover);
			}
		}
		
		return cover;
	}
	
	/**
	 * Put cover into cache.
	 * 
	 * @param id
	 *            cover ID for which the cover should be persisted
	 * @param bitmapData
	 *            image data as raw byte array
	 * 
	 * @return cover as bitmap or {@code null} if failed to persist or the given data was invalid
	 */
	public static Bitmap addCover(String id, byte [] bitmapData) {
		Bitmap cover = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
		
		if (cover != null) {
			mCoverCache.put(id.hashCode(), cover);
			
			try {
				File file = new File(App.BANSHEE_PATH + id + ".jpg");
				file.getParentFile().mkdir();
				file.delete();
				cover.compress(CompressFormat.JPEG, 80, new FileOutputStream(file));
			} catch (FileNotFoundException e) {
				// too bad, we have a valid cover but failed to persist
				// return null, the fail is NOT a acceptable behavior
				cover = null;
			}
		}
		
		return cover;
	}
}
