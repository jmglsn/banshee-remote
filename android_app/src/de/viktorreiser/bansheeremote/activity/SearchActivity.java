package de.viktorreiser.bansheeremote.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import de.viktorreiser.bansheeremote.R;
import de.viktorreiser.bansheeremote.data.BansheeConnection.Command;
import de.viktorreiser.bansheeremote.data.BansheeDatabase;
import de.viktorreiser.bansheeremote.data.BansheeDatabase.Album;
import de.viktorreiser.bansheeremote.data.BansheeDatabase.Artist;
import de.viktorreiser.bansheeremote.data.BansheeDatabase.Track;

/**
 * This will provide a simple search interface
 * 
 * @author Jens Radtke <nb@fin-sn.de>
 */
public class SearchActivity extends Activity implements OnItemClickListener{

	private Track[] mTrackEntries = new Track[0];
	private Artist[] mArtistEntries = new Artist[0];
	private Album[] mAlbumEntries = new Album[0];

	private byte searchTyp = 0;
	
	private EditText searchTerm;
	private String lastTerm = null;
	private ListView searchList;
	private SearchListAdapter adapter;
	private Spinner searchSpinner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (CurrentSongActivity.getConnection() == null) {
			finish();
			return;
		}
		
		setContentView(R.layout.search);

		mTrackEntries = new Track[0];

		searchTerm = (EditText) findViewById(R.id.search_term);
		searchList = (ListView) findViewById(R.id.search_list);
		searchSpinner = (Spinner) findViewById(R.id.search_spinner);
		
		searchSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				refreshList();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// nothing :)
			}
			
		});
		
		adapter = new SearchListAdapter();
		searchList.setAdapter(adapter);
		searchList.setOnItemClickListener(this);

		((Button) findViewById(R.id.search_start))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						refreshList();
					}
				});
	}


	/**
	 * searches for tracks, artists and albums containing the search term in title / name
	 */
	private void refreshListData() {
		String term = searchTerm.getText().toString();
		
		if (term == null || term.trim().length() == 0) {
			return;
		} else if(term.equals(lastTerm)) {
			return;
		}
		
		lastTerm = term;
		
		mArtistEntries = BansheeDatabase.getArtistsByTitle(term);
		mAlbumEntries = BansheeDatabase.getAlbumsByTitle(term);
		mTrackEntries = BansheeDatabase.getTracksByTitle(term);
		
	}
	
	/**
	 * show selected result list
	 */
	private void refreshList() {
		refreshListData();
		
		String typ = (String) searchSpinner.getSelectedItem();
		if (typ == null) {
			return;
		}
		if (typ.equalsIgnoreCase("Artist")) {
			searchTyp = 1;
			
		} else if (typ.equalsIgnoreCase("Album")) {
			searchTyp = 2;
			
		} else {
			searchTyp = 0;
		}
		adapter.notifyDataSetChanged();
	}

	
	private class SearchListAdapter extends BaseAdapter {

		@Override
		public int getItemViewType(int position) {
			return 0;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public int getCount() {
			if (searchTyp == 0) {
				return mTrackEntries.length;				
			} else if (searchTyp == 1) {
				return  mArtistEntries.length;
			} else {
				return mAlbumEntries.length;
			}
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				ViewHolder holder = new ViewHolder();
				convertView = getLayoutInflater().inflate(R.layout.search_item, null);
				holder.track = (TextView) convertView.findViewById(R.id.search_item_title);
				convertView.setTag(holder);
			}

			ViewHolder holder = (ViewHolder) convertView.getTag();
			if (searchTyp == 0) {
				holder.track.setText(mTrackEntries[position].getTitle());				
			} else if (searchTyp == 1) {
				holder.track.setText(mArtistEntries[position].getName());
			} else if (searchTyp == 2) {
				holder.track.setText(mAlbumEntries[position].getTitle());
			}
			
			

			return convertView;
		}

	}

	private static class ViewHolder {
		public TextView track;
	}
	
	@Override
	public void onItemClick(AdapterView<?> a, View v, int p, long id) {
		if (searchTyp == 0) {
			CurrentSongActivity.getConnection().sendCommand(Command.PLAYLIST,
					Command.Playlist.encodePlayTrack(mTrackEntries[p].getId()));
			finish();			
		} else if (searchTyp == 1) {
			BansheeDatabase.setupDbCache();
			Intent showArtistTracks = new Intent(this, TrackActivity.class);
			showArtistTracks.putExtra(TrackActivity.EXTRA_ARTIST_ID, mArtistEntries[p].getId());
			startActivityForResult(showArtistTracks, 1);
			finish();
		} else if (searchTyp == 2) {
			BansheeDatabase.setupDbCache();
			Intent showAlbumTracks = new Intent(this, TrackActivity.class);
			showAlbumTracks.putExtra(TrackActivity.EXTRA_ALBUM_ID, mAlbumEntries[p].getId());
			startActivityForResult(showAlbumTracks, 1);
			finish();
		}
		return;
	}
}
