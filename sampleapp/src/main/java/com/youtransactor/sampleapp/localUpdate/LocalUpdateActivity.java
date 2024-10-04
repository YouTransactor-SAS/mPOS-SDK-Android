/*
 * ============================================================================
 *
 * Copyright (c) 2024 JABIL Payment Solution
 *
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of JABIL Payment Solution
 * ("Confidential Information"). You  shall not disclose or redistribute such
 * Confidential Information and shall use it only in accordance with the terms of
 * the license agreement you entered into with JABIL Payment Solution.
 *
 * This software is provided by JABIL Payment Solution AS IS, and JABIL Payment Solution
 * makes no representations or warranties about the suitability of the software,
 * either express or implied, including but not limited to the implied warranties
 * of merchantability, fitness for a particular purpose or non-infringement.
 * JABIL Payment Solution shall not be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages suffered by licensee as the
 * result of using, modifying or distributing this software or its derivatives.
 *
 * ==========================================================================
 */
package com.youtransactor.sampleapp.localUpdate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.obsez.android.lib.filechooser.ChooserDialog;

import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.mdm.LocalUpdateService;
import com.youTransactor.uCube.mdm.ServiceState;
import com.youTransactor.uCube.mdm.UpdateItem;
import com.youtransactor.sampleapp.BuildConfig;
import com.youtransactor.sampleapp.R;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class LocalUpdateActivity extends AppCompatActivity {

    private static final String TAG = LocalUpdateActivity.class.getSimpleName();

    private static final String SHARE_PREF_NAME = "LocalUpdate";
    private static final String LAST_SELECTION_PATH = "LAST_SELECTION_PATH";

    private UpdateItemAdapter updateItemListAdapter;
    private File lastSelectedFile = null;
    private ProgressBar progressBar;
    private boolean updateInProgress = false;
    private TextView progressMsgFld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.local_update);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.local_update);
        }

        updateItemListAdapter = new UpdateItemAdapter(this,R.layout.custome_row_layout, R.id.log_message);

        final ListView filelistFld = findViewById(R.id.filelistFld);
        filelistFld.setAdapter(updateItemListAdapter);
        filelistFld.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        filelistFld.setOnItemClickListener((parent, view, position, id) -> {
            UpdateItem current = updateItemListAdapter.getItem(position);

            if (updateItemListAdapter.getSelected() == current) {
                filelistFld.setSelection(-1);
                view.setSelected(false);
                updateItemListAdapter.setSelected(null);
            } else {
                filelistFld.setSelection(position);
                view.setSelected(true);
                updateItemListAdapter.setSelected(current);
            }
        });

        progressBar = findViewById(R.id.progressBar);
        progressMsgFld = findViewById(R.id.progressMsgFld);

        TextView versionFld = findViewById(R.id.version_name);
        versionFld.setText(getString(R.string.versionName, BuildConfig.VERSION_NAME));

//        lastSelectedFile = getFilesDir();
        lastSelectedFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

//        SharedPreferences prefs = getSharedPreferences(SHARE_PREF_NAME, MODE_PRIVATE);
//        String val = prefs.getString(LAST_SELECTION_PATH, null);
//        if (val != null) {
//            lastSelectedFile = new File(val);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.local_update, menu);

        MenuItem item = menu.findItem(R.id.menu_select_file);
        item.setVisible(!updateInProgress);

        item = menu.findItem(R.id.menu_remove_selected);
        item.setVisible(updateItemListAdapter.getSelected() != null && !updateInProgress);

        item = menu.findItem(R.id.menu_start_update);
        item.setVisible(!updateItemListAdapter.isEmpty());
        if (updateInProgress) {
            item.setActionView(R.layout.actionbar_indeterminate_progress);
        } else {
            item.setActionView(null);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_remove_selected) {
            updateItemListAdapter.remove(updateItemListAdapter.getSelected());
            invalidateOptionsMenu();
            return true;
        }

        if (item.getItemId() == R.id.menu_start_update) {
            startUpdate();
            return true;
        }

        if (item.getItemId() == R.id.menu_select_file) {
            selectFile();
            return true;
        }

        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void selectFile() {
        new ChooserDialog(LocalUpdateActivity.this)
                .withFilterRegex(false, true, ".*\\.(bin)")
                .withStartFile(lastSelectedFile != null ? lastSelectedFile.getAbsolutePath() : "")
                .withNavigateUpTo(dir -> true)
                .withNavigateTo(file -> true)
//                .withRowLayoutView(resId)
                .displayPath(true)
                .withChosenListener((path, pathFile) -> {
                  onFileSelect(pathFile);
                })
                /* to handle the back key pressed or clicked outside the dialog */
                .withOnCancelListener(dialog -> dialog.cancel())
                .build()
                .show();
    }

    private void onFileSelect(File file) {
        String label = file.getName().substring(0, file.getName().lastIndexOf('.'));

        if (updateItemListAdapter.contains(label)) {
            Toast.makeText(this, R.string.update_item_already_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        displayState(getResources().getString(R.string.readFile), true);

        Executors.newSingleThreadExecutor().execute(() -> {
            _addFile(file);
        });
        lastSelectedFile = file;
//        getSharedPreferences(SHARE_PREF_NAME, MODE_PRIVATE)
//                .edit()
//                .putString(LAST_SELECTION_PATH, file.getParent())
//                .apply();
    }

    private void _addFile(File binFile) {
        File sigFile;

        if (binFile.getName().endsWith(".sig")) {
            sigFile = binFile;
            binFile = new File(binFile.getParent(), binFile.getName().replace(".sig", ".bin"));
        } else {
            sigFile = new File(binFile.getParent(), binFile.getName().replace(".bin", ".sig"));
        }

        if (!binFile.exists() || !sigFile.exists()) {
            Log.d(TAG, "file '" + binFile + "' not found");
            displayState(getResources().getString(R.string.sig_not_found_msg, binFile.getName()), false);
            return;
        }

        try (InputStream binIn = new FileInputStream(binFile);
             InputStream sigIn = new FileInputStream(sigFile))
        {
            final UpdateItem item = new UpdateItem()
                    .label(binFile.getName().replace(".bin", ""))
                    .data(IOUtils.toByteArray(binIn))
                    .signature(IOUtils.toByteArray(sigIn));

            runOnUiThread(() -> {
                updateItemListAdapter.add(item);
                invalidateOptionsMenu();
            });

            displayState("", false);
        }
        catch (Exception e) {
            Log.w(TAG, "load '" + binFile.getName() + "' error'", e);
            displayState(getResources().getString(R.string.read_file_error), true);
        }
    }

    private void displayState(final String text, boolean running) {
        runOnUiThread(() -> {
            progressBar.setVisibility(running ? View.VISIBLE : View.GONE);
            if (text != null) {
                progressMsgFld.setText(text);
            }
        });
    }

    private void startUpdate() {
        displayState("start update", true);

        updateInProgress = true;

        final List<UpdateItem> updateList = new ArrayList<>();

        for (int i = 0; i < updateItemListAdapter.getCount(); i++) {
            updateList.add(updateItemListAdapter.getItem(i));
        }

        final ITaskMonitor monitor = (event, params) -> {
            if ( event != TaskEvent.PROGRESS) {
                updateInProgress = false;
                invalidateOptionsMenu();
            }

            String msg = params.length > 0 && params[0] instanceof ServiceState
                    ? ((ServiceState) params[0]).name()
                    : event.name();

            displayState(msg, event == TaskEvent.PROGRESS);
        };

        Executors.newSingleThreadExecutor().execute(() -> {
            LocalUpdateService lus = new LocalUpdateService();
            lus.execute(updateList, monitor);
        });
    }

}