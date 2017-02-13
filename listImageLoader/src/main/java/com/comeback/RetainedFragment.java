package com.comeback;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;

public class RetainedFragment extends Fragment {

	private SparseArray<Bitmap> mBitmapArr = new SparseArray<Bitmap>(10);
	public static final String[] mImageArr = {
		"https://www.gstatic.com/webp/gallery2/1.png",
		"https://www.gstatic.com/webp/gallery2/2.png",
		"https://www.gstatic.com/webp/gallery2/3.png",
		"https://www.gstatic.com/webp/gallery2/4.png",
		"https://www.gstatic.com/webp/gallery2/5.png",
		"https://www.gstatic.com/webp/gallery3/1.png",
		"https://www.gstatic.com/webp/gallery3/2.png",
		"https://www.gstatic.com/webp/gallery3/3.png",
		"https://www.gstatic.com/webp/gallery3/4.png",
		"https://www.gstatic.com/webp/gallery3/5.png" };


	public SparseArray<Bitmap> getmBitmapArr() {
		return mBitmapArr;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	public void loadAndSaveBitmap(int iPosition) {

		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(mImageArr[iPosition]);
		try {
			HttpEntity entity = client.execute(get).getEntity();
			Log.v("TAG", "This image will get downloaded. : "
					+ mImageArr[iPosition]);

			final Bitmap bmp = BitmapFactory.decodeStream(entity.getContent());
			Log.d("TAG", "ImageView " + bmp);

			// Adding bitmap to the array
			mBitmapArr.put(iPosition, bmp);
			Log.e("TAG", "Added successfully "+iPosition+"---- "+bmp);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
