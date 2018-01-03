package com.comeback;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class ComeBackActivity extends AppCompatActivity {

	private ListView mComeBackLSv = null;
	private ComeBackAdapter mComeBackAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_come_back);

		mComeBackLSv = (ListView) findViewById(R.id.comeback_lsv);

		FragmentManager mgr = getSupportFragmentManager();
		RetainedFragment retainFrag = (RetainedFragment) mgr
				.findFragmentByTag("Retained");

		// For the first launch
		if (retainFrag == null) {
			retainFrag = new RetainedFragment();
			mgr.beginTransaction().add(retainFrag, "Retained").commit();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mComeBackLSv.getAdapter() == null) {
			Log.v("TAG", "Setting adapter");
			// Reset adapter
			mComeBackAdapter = new ComeBackAdapter(this);
			mComeBackLSv.setAdapter(mComeBackAdapter);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.come_back, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
