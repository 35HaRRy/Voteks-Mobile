package com.hayrihabip.voteks.data;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.hayrihabip.voteks.R;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by hayri on 15.07.2016.
 */
public class TorrentAdapter implements ListAdapter {

    private Activity mActivity;
    private JSONArray mTorrents;

    public TorrentAdapter (Activity _mActivity, JSONArray _mTorrents) {
        mActivity = _mActivity;
        mTorrents = _mTorrents;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getCount() {
        return mTorrents.length();
    }

    @Override
    public Object getItem(int i) {
        try {
            return mTorrents.getJSONObject(i);
        } catch (JSONException e) {
            e.printStackTrace();

            return null;
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.torrent, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);



        return rowView;
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
