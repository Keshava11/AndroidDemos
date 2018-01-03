
package com.trycouchbaselite;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Document.ChangeEvent;
import com.couchbase.lite.Document.ChangeListener;
import com.couchbase.lite.Manager;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.UnsavedRevision;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple implementation demonstrating configuration of Couchbase Lite database and creating, updating and deleting of
 * the documents
 * 
 * @author ravikumar
 */
public class TryCouchbaseLiteActivity extends AppCompatActivity implements OnClickListener
{
	private final String TAG = TryCouchbaseLiteActivity.class.getSimpleName();
	private final String DOC_ID_KEY = "doc_id_key";
	private final String DOC_CONTENT_KEY = "doc_content";
	private final String DOC_TIME_KEY = "doc_time";
	private final String DATABASE_NAME = "first_database";

	private EditText mDocContentEdt = null;
	private Button mCreateBtn = null;
	private Button mDeleteBtn = null;
	private Button mShowBtn = null;
	private TextView mShowTxv = null;

	private LinearLayout mAttachmentDemoLayout = null;
	private Button mReadAttachmentBtn = null;
	private Button mCreateAttachmentBtn = null;
	private Button mDeleteAttachmentBtn = null;

	private Manager mCouchbaseManager = null;
	private Database mCouchbaseDatabase = null;

	private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
	private Calendar mCalendar = Calendar.getInstance();

	private final int GALLERY_CODE = 11;
	private static final int WIDTH_HEIGHT = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_try_couchbase_lite);
		initializeViews();
	}

	/**
	 * This method is used to initialize views from the activity's content
	 */
	private void initializeViews()
	{
		mDocContentEdt = (EditText) findViewById(R.id.doc_content_edt);
		mCreateBtn = (Button) findViewById(R.id.create_doc_btn);
		mDeleteBtn = (Button) findViewById(R.id.delete_doc_btn);
		mShowBtn = (Button) findViewById(R.id.show_doc_btn);
		mShowTxv = (TextView) findViewById(R.id.show_doc_txv);
		mAttachmentDemoLayout = (LinearLayout) findViewById(R.id.attachment_demo_ll);
		mReadAttachmentBtn = (Button) findViewById(R.id.read_attachment_btn);
		mCreateAttachmentBtn = (Button) findViewById(R.id.create_attachment_btn);
		mDeleteAttachmentBtn = (Button) findViewById(R.id.delete_attachment_btn);

		registerListeners();
		initializeCouchbase();
	}

	/**
	 * Registering activity to listen for various events on views.
	 */
	private void registerListeners()
	{
		mCreateBtn.setOnClickListener(this);
		mDeleteBtn.setOnClickListener(this);
		mShowBtn.setOnClickListener(this);

		mCreateAttachmentBtn.setOnClickListener(this);
		mReadAttachmentBtn.setOnClickListener(this);
		mDeleteAttachmentBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View iView)
	{
		String docId = PreferenceManager.getDefaultSharedPreferences(this).getString(DOC_ID_KEY, null);
		switch (iView.getId())
		{
		case R.id.create_doc_btn:
			checkContentAndCreateDoc();
			break;
		case R.id.delete_doc_btn:
			deleteDoc(docId);
			break;
		case R.id.show_doc_btn:
			showDocument(docId);
			break;
		case R.id.read_attachment_btn:
			readAttachment(docId);
			break;
		case R.id.create_attachment_btn:
			// Attachment using image from assets options
			// createAttachment(docId, null);

			// Attachment using Gallery options
			final Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
			galleryIntent.setType("image/*");
			startActivityForResult(galleryIntent, GALLERY_CODE);
			break;
		case R.id.delete_attachment_btn:
			deleteAttachment(docId);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent iData)
	{
		super.onActivityResult(requestCode, resultCode, iData);

		switch (resultCode)
		{
		case RESULT_OK:

			if (requestCode == GALLERY_CODE)
			{				
				String docId = PreferenceManager.getDefaultSharedPreferences(this).getString(DOC_ID_KEY, null);
				// Image stream from Gallery
				try
				{
					createAttachment(docId, getContentResolver().openInputStream(iData.getData()));
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
					showToast("Image file not found.");
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * This method is used to initialize the shared Couchbase Manager that acts as controller and collection of
	 * databases.
	 */
	private void initializeCouchbase()
	{
		try
		{
			mCouchbaseManager = new Manager(getApplicationContext().getFilesDir(), Manager.DEFAULT_OPTIONS);

			// Initializing database
			createDatabase(DATABASE_NAME);
		}
		catch (IOException e)
		{
			showToast("Cannot create manager object");
		}
	}

	/**
	 * @param iDatabaseName
	 */
	private void createDatabase(String iDatabaseName)
	{
		if (Manager.isValidDatabaseName(iDatabaseName))
		{
			try
			{
				mCouchbaseDatabase = mCouchbaseManager.getDatabase(iDatabaseName);

				// Enable the buttons Here
				mCreateBtn.setEnabled(true);
				mDeleteBtn.setEnabled(true);
				mShowBtn.setEnabled(true);

			}
			catch (CouchbaseLiteException e)
			{
				e.printStackTrace();
				showToast("Cannot get or retrieve database");
			}
		}
		else
		{
			showToast("Invalid database name");
		}
	}

	/**
	 * This method actually checks for the existence of document whose id is saved in Preferences
	 * 
	 * @param iContent
	 *            a var arg for various
	 * @throws CouchbaseLiteException
	 */
	private void createUpdateDocument(String... iContent) throws CouchbaseLiteException
	{
		String docId = PreferenceManager.getDefaultSharedPreferences(this).getString(DOC_ID_KEY, null);
		Document document = null;

		final Map<String, Object> docContent = new HashMap<String, Object>();
		docContent.put(DOC_CONTENT_KEY, iContent[0]);
		docContent.put(DOC_TIME_KEY, mDateFormat.format(mCalendar.getTime()));

		if (docId == null)
		{
			// Create a document and add the following content to it.
			document = mCouchbaseDatabase.createDocument();
			document.addChangeListener(mDocChangeListener);
			document.putProperties(docContent);

			Log.v(TAG, "Created Document details :: " + document.getId() + "---" + document.getCurrentRevisionId());

			// Save this document id in the preferences for later retrieval
			docId = document.getId();
			PreferenceManager.getDefaultSharedPreferences(this).edit().putString(DOC_ID_KEY, docId).commit();
		}
		else
		{
			document = mCouchbaseDatabase.getDocument(docId);
			document.addChangeListener(mDocChangeListener);
			document.putProperties(docContent);

			Log.e(TAG, "Updated Document details :: " + document.getId() + "---" + document.getCurrentRevisionId());
		}

		// Show the content of document
		showDocument(docId);
	}

	/**
	 * Document change listener
	 */
	private ChangeListener mDocChangeListener = new ChangeListener()
	{
		@Override
		public void changed(ChangeEvent iChangeEvent)
		{
			String content = (String) iChangeEvent.getSource().getProperties().get(DOC_CONTENT_KEY);

			// Setting visibility for layout containing views for attachment actions
			mAttachmentDemoLayout.setVisibility((content == null) ? (View.GONE) : (View.VISIBLE));
		}
	};

	/**
	 * This method checks for the document if created previously and show its details.
	 * 
	 * @param docId
	 *            id of document to be deleted
	 */
	private void showDocument(String docId)
	{
		if (docId != null)
		{
			Document document = mCouchbaseDatabase.getDocument(docId);
			// Show the content of document
			StringBuffer updatedDocBuffer = new StringBuffer();

			updatedDocBuffer.append(document);
			updatedDocBuffer.append("\n");
			updatedDocBuffer.append(document.getProperties().get(DOC_CONTENT_KEY));
			updatedDocBuffer.append("\n");
			updatedDocBuffer.append(document.getProperties().get(DOC_TIME_KEY));
			updatedDocBuffer.append("\n");
			updatedDocBuffer.append(document.getCurrentRevision().getSequence());

			mShowTxv.setText(updatedDocBuffer.toString());
		}
		else
		{
			showToast("No document found with the id saved in preference. Create a new one.");
		}
	}

	/**
	 * Implementation for deleting a document
	 * 
	 * @param docId
	 *            id of document to be deleted
	 */
	private void deleteDoc(String docId)
	{
		boolean deleteDocSuccess = false;
		try
		{
			deleteDocSuccess = mCouchbaseDatabase.getDocument(docId).delete();
		}
		catch (CouchbaseLiteException e)
		{
			e.printStackTrace();
			showToast("Error while deleting document");
			return;
		}

		showToast((deleteDocSuccess) ? ("Document was successfully deleted.") : ("Document was not deleted."));

	}

	/**
	 * Implementation for checking the empty content and call for creation of document with empty content
	 */
	private void checkContentAndCreateDoc()
	{
		String docContent = mDocContentEdt.getText().toString().trim();
		if (docContent.length() > 0)
		{
			try
			{
				createUpdateDocument(docContent);
			}
			catch (CouchbaseLiteException e)
			{
				e.printStackTrace();
				showToast("Error creating Document.");
			}
		}
		else
		{
			showToast("Empty data.");
		}
	}

	/**
	 * Implementation for reading an already added attachment
	 * 
	 * @param iDocId
	 *            id of document
	 */
	private void readAttachment(String iDocId)
	{
		try
		{
			final Document currentDocument = mCouchbaseDatabase.getDocument(iDocId);
			final SavedRevision savedRevision = currentDocument.getCurrentRevision();
			Attachment att = savedRevision.getAttachment("droid.jpg");
			if (att != null)
			{
				InputStream is = att.getContent();
				Drawable attachDrawable = Drawable.createFromStream(is, "droid.jpg");

				ImageView imv = new ImageView(this);
				imv.setImageDrawable(attachDrawable);
				LayoutParams pars = new LayoutParams(WIDTH_HEIGHT, WIDTH_HEIGHT);
				imv.setLayoutParams(pars);

				// Set ImageView to the Toast
				Toast testToast = new Toast(this);
				testToast.setView(imv);
				testToast.setDuration(Toast.LENGTH_LONG);
				testToast.show();
			}
		}
		catch (CouchbaseLiteException e)
		{
			e.printStackTrace();
			showToast("Error while reading document's attachment");
		}
	}

	/**
	 * Implementation for checking creating an attachment to the document
	 * 
	 * @param iDocId
	 *            id of document
	 */
	private void createAttachment(String iDocId, InputStream is)
	{
		try
		{
			final Document currentDocument = mCouchbaseDatabase.getDocument(iDocId);
			UnsavedRevision newRev = currentDocument.getCurrentRevision().createRevision();
			if (is == null)
			{
				is = getAssets().open("droid.jpg");
				newRev.setAttachment("droid.jpg", "image/jpg", is);
				newRev.save();
			}
		}
		catch (CouchbaseLiteException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Implementation for deleting an attachment added to a document
	 * 
	 * @param iDocId
	 *            id of document
	 */
	private void deleteAttachment(String iDocId)
	{
		try
		{
			final Document currentDocument = mCouchbaseDatabase.getDocument(iDocId);
			UnsavedRevision newRev = currentDocument.getCurrentRevision().createRevision();
			newRev.removeAttachment("droid.jpg");
			newRev.save();
		}
		catch (CouchbaseLiteException e)
		{
			e.printStackTrace();
			showToast("Error while deleting attachment to the document.");
		}
	}

	/**
	 * Toast notifications for error messages
	 * 
	 * @param iMessage
	 */
	private void showToast(String iMessage)
	{
		Toast.makeText(this, iMessage, Toast.LENGTH_SHORT).show();
	}

}
