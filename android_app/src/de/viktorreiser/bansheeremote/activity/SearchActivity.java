package de.viktorreiser.bansheeremote.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import de.viktorreiser.bansheeremote.R;
import de.viktorreiser.bansheeremote.data.BansheeConnection.Command;
import de.viktorreiser.bansheeremote.data.BansheeDatabase;
import de.viktorreiser.bansheeremote.data.BansheeDatabase.Track;
import de.viktorreiser.toolbox.util.L;

/**
 * This will provide a simple search interface
 * 
 * @author Jens Radtke <nb@fin-sn.de>
 */
public class SearchActivity extends Activity implements OnItemClickListener{

	private Track[] mTrackEntries;

	private EditText searchTerm;
	private ListView searchList;
	private TrackAdapter adapter;
	
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
		adapter = new TrackAdapter();
		searchList.setAdapter(adapter);
		searchList.setOnItemClickListener(this);

		((Button) findViewById(R.id.search_start))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startSearch(searchTerm.getText().toString());
					}
				});
	}

	private void startSearch(String term) {
		mTrackEntries = BansheeDatabase.getTracksByTitle(term);
		adapter.notifyDataSetChanged();
		L.e("BRS", "Found:_" + mTrackEntries.length);
		
	}

	private class TrackAdapter extends BaseAdapter {

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
			return mTrackEntries.length;
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
			Track i = mTrackEntries[position];
			holder.track.setText(i.getTitle());


			return convertView;
		}

	}

	private static class ViewHolder {
		public TextView track;
	}
	
	@Override
	public void onItemClick(AdapterView<?> a, View v, int p, long id) {
		CurrentSongActivity.getConnection().sendCommand(Command.PLAYLIST,
				Command.Playlist.encodePlayTrack(mTrackEntries[p].getId()));
		finish();
		return;
	}
}
