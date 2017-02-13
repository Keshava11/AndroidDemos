package com.comeback;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ComeBackAdapter extends BaseAdapter {

	private LayoutInflater mLayoutInflater = null;
	private HandlerThread mHandlerThread = null;
	private Handler mResponseHandler = null;
	private RetainedFragment mRetainedFragment = null;

	private Activity mActivity = null;

	public ComeBackAdapter(Activity iActivity) {
		mLayoutInflater = LayoutInflater.from(iActivity);
		mActivity = iActivity;

		mRetainedFragment = (RetainedFragment) ((ComeBackActivity) mActivity)
				.getSupportFragmentManager().findFragmentByTag("Retained");
	}

	@Override
	public int getCount() {
		return RetainedFragment.mImageArr.length;
	}

	@Override
	public Object getItem(int position) {
		return RetainedFragment.mImageArr[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;

		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.view_item_img, null);
			holder = new ViewHolder();
			holder.mImg = (ImageView) convertView.findViewById(R.id.imv);
			holder.mTxv = (TextView) convertView.findViewById(R.id.txv);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.mTxv.setText(RetainedFragment.mImageArr[position]);

		// Creating HandlerThread for once and start it so that underlying
		// should start looping for the messages.
		if (mHandlerThread == null) {
			mHandlerThread = new HandlerThread("MyThread");
			mHandlerThread.start();

			// Creating a handler to which download request will be posted
			mResponseHandler = new Handler(mHandlerThread.getLooper());
		}

		// Handler posting download requests to the message queue in the thread.
		if (mRetainedFragment.getmBitmapArr() == null
				|| mRetainedFragment.getmBitmapArr().get(position) == null) {
			
			mResponseHandler.post(new Runnable() {
				@Override
				public void run() {
					Log.e("TAG", "Begin Loading  Bmp");
					mRetainedFragment.loadAndSaveBitmap(position);
				}
			});
			
			Log.v("TAG", "Posting Data");
		}
		else
		{
			Log.e("TAG", "Else Setting adapter");
		}

		// Set TextView and ImageView
		holder.mImg.setImageBitmap(mRetainedFragment.getmBitmapArr().get(
				position));

		return convertView;
	}

	class ViewHolder {
		private ImageView mImg = null;
		private TextView mTxv = null;
	}

}
