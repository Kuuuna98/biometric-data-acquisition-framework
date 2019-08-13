package com.kaist.iclab.datamanager;

import com.madrapps.asyncquery.AsyncQueryHandler;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

public class DatabaseHandler extends AsyncQueryHandler {

    public DatabaseHandler(ContentResolver cr) {
        super(cr);
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        super.onInsertComplete(token, cookie, uri);
        Log.d("DatabaseHandler", "Insert Done");
    }

    @Override
    protected void onBulkInsertComplete(int token, Object cookie, int result) {
        super.onBulkInsertComplete(token, cookie, result);
        Log.d("DatabaseHandler", "Bulk Insert Done");
    }
}
