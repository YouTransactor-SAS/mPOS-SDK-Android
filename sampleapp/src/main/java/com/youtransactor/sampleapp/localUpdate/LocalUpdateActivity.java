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

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
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
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

public class LocalUpdateActivity extends AppCompatActivity {

    private static final String TAG = LocalUpdateActivity.class.getSimpleName();
    private static final int PICK_FILE = 1;

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
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

            startActivityForResult(intent, PICK_FILE);
            return;
        }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == PICK_FILE) {
            if (resultData == null) {
                return;
            }

            ClipData clipData = resultData.getClipData();
            if (clipData != null) {
                Set<String> labels = new HashSet<>();
                Map<String, Uri> uriByNames = new HashMap<>();

                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    Uri uri = item.getUri();

                    DocumentFile file = DocumentFile.fromSingleUri(LocalUpdateActivity.this, uri);
                    assert file != null;
                    String filename = file.getName();
                    assert filename != null;
                    String label = filename.substring(0, filename.lastIndexOf('.'));
                    labels.add(label);
                    uriByNames.put(filename, uri);
                }

               for (String label : labels) {
                   Uri binUri = uriByNames.get(label + ".bin");
                   Uri sigUri = uriByNames.get(label + ".sig");

                   if (updateItemListAdapter.contains(label)
                        || binUri == null || sigUri == null) {
                       continue;
                   }

                   try (InputStream binIn = getContentResolver().openInputStream(binUri);
                        InputStream sigIn = getContentResolver().openInputStream(sigUri))
                   {
                       assert binIn != null;
                       assert sigIn != null;

                       final UpdateItem item = new UpdateItem()
                               .label(label)
                               .data(IOUtils.toByteArray(binIn))
                               .signature(IOUtils.toByteArray(sigIn));

                       runOnUiThread(() -> {
                           updateItemListAdapter.add(item);
                           invalidateOptionsMenu();
                       });

                       displayState("", false);
                   }
                   catch (Exception e) {
                       Log.w(TAG, "load '" + label + "' error'", e);
                       displayState(getResources().getString(R.string.read_file_error), true);
                   }
               }
                displayState(null, false);
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData);
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

            String msg;
            if (params.length > 1 && params[1] instanceof String) {
                msg = (String) params[1];
            } else if (params.length > 0 && params[0] instanceof ServiceState) {
                msg = ((ServiceState) params[0]).name();
            } else {
                msg = event.name();
            }

            displayState(msg, event == TaskEvent.PROGRESS);
        };

        Executors.newSingleThreadExecutor().execute(() -> {
            LocalUpdateService lus = new LocalUpdateService();
            lus.execute(updateList, monitor);
        });
    }

}