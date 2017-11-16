package com.wuyr.hideimagemaker.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.wuyr.hideimagemaker.R;
import com.wuyr.hideimagemaker.adapter.ImageListAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by wuyr on 17-11-15 下午1:54.
 */

public class ImageSelectorActivity extends AppCompatActivity {

    private ImageListAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_image_selector_view);
        init();
    }

    private void init() {
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(mAdapter = new ImageListAdapter(this, null, R.layout.adapter_image_list_item_view));
        mAdapter.setOnItemOnClickListener(path -> {
            Intent intent = new Intent();
            intent.putExtra("path", path);
            setResult(RESULT_OK, intent);
            finish();
        });
        initImages();
    }

    private void initImages() {
        getSupportLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {

            private final String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media._ID};

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                try {
                    return new CursorLoader(ImageSelectorActivity.this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            projection, null, null, projection[2] + " DESC");
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if (loader != null && data != null) {
                    int count = data.getCount();
                    if (count > 0) {
                        data.moveToFirst();
                        List<File> images = new ArrayList<>();
                        do {
                            images.add(new File(data.getString(data.getColumnIndexOrThrow(projection[0]))));
                        } while (data.moveToNext());
                        Collections.sort(images, (o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));
                        mAdapter.setData(images);
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }
}
