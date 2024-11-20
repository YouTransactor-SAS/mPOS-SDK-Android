/*
 * ============================================================================
 *
 * Copyright (c) 2024 YouTransactor
 *
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of YouTransactor
 * ("Confidential Information"). You  shall not disclose or redistribute such
 * Confidential Information and shall use it only in accordance with the terms of
 * the license agreement you entered into with YouTransactor.
 *
 * This software is provided by YouTransactor AS IS, and YouTransactor
 * makes no representations or warranties about the suitability of the software,
 * either express or implied, including but not limited to the implied warranties
 * of merchantability, fitness for a particular purpose or non-infringement.
 * YouTransactor shall not be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages suffered by licensee as the
 * result of using, modifying or distributing this software or its derivatives.
 *
 * ==========================================================================
 */
package com.youtransactor.sampleapp.emvParamUpdate;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import com.youTransactor.uCube.emv.EmvParamFmt1;
import com.youTransactor.uCube.emv.EmvParamYTModel;
import com.youTransactor.uCube.mdm.EmvParamUpdate1by1FSM;
import com.youTransactor.uCube.mdm.UpdateItem;
import com.youTransactor.uCube.rpc.RPCCommandStatus;
import com.youtransactor.sampleapp.BuildConfig;
import com.youtransactor.sampleapp.R;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.Executors;

public class EmvParamUpdateActivity extends AppCompatActivity {
    private static final String TAG = EmvParamUpdateActivity.class.getSimpleName();

    private static final int PICK_FILE = 1;

    private EmvParamUpdateAdapter updateItemListAdapter;
    private ProgressBar progressBar;
    private boolean updateInProgress = false;
    private TextView progressMsgFld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.emv_param_update);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.emv_param_update);
        }

        updateItemListAdapter = new EmvParamUpdateAdapter(
                this,R.layout.custome_row_layout, R.id.log_message);

        final ListView filelistFld = findViewById(R.id.filelistFldEmvParUpd);
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

        progressBar = findViewById(R.id.progressBarEmvParUpd);
        progressMsgFld = findViewById(R.id.progressMsgFldEmvParUpd);

        TextView versionFld = findViewById(R.id.version_name);
        versionFld.setText(getString(R.string.versionName, BuildConfig.VERSION_NAME));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.emv_param_update, menu);

        MenuItem item = menu.findItem(R.id.menu_select_file_emv_par_upd);
        item.setVisible(!updateInProgress);

        item = menu.findItem(R.id.menu_remove_selected_emv_par_upd);
        item.setVisible(updateItemListAdapter.getSelected() != null &&
                !updateInProgress);

        item = menu.findItem(R.id.menu_start_update_emv_par_upd);
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
        if (item.getItemId() == R.id.menu_remove_selected_emv_par_upd) {
            updateItemListAdapter.remove(updateItemListAdapter.getSelected());
            invalidateOptionsMenu();
            return true;
        }
        if (item.getItemId() == R.id.menu_start_update_emv_par_upd) {
            // startUpdate();
            return true;
        }
        if (item.getItemId() == R.id.menu_select_file_emv_par_upd) {
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
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            startActivityForResult(intent, PICK_FILE);
            return;
        }
        // TODO: sort the file access issue on Android lesser than 14
        onFileSelect(new File("/data/data/com.youtransactor.sampleapp/" +
                "emvParam/20231031-emv_config CERT US 20231006_purchase_and_goods.txt"));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == PICK_FILE) {
            if (resultData == null) {
                return;
            }
            Uri uri = resultData.getData();
            if (uri != null) {
                EmvParamYTModel model;
                try (InputStream input = getContentResolver().openInputStream(uri)) {
                    model = EmvParamFmt1.from(input);
                } catch (Exception e) {
                    Log.d(TAG, "read EMV file error", e);
                    runOnUiThread(() -> Toast.makeText(EmvParamUpdateActivity.this,
                            "read EMV config error", Toast.LENGTH_LONG).show());
                    return;
                }
                startEmvParamUpdate(model);
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    private void onFileSelect(File _file) {
        String label = _file.getName().substring(0, _file.getName().lastIndexOf('.'));
        if (updateItemListAdapter.contains(label)) {
            Toast.makeText(this, R.string.update_item_already_selected,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            EmvParamYTModel model;
            try (InputStream input = new FileInputStream(_file)) {
                model = EmvParamFmt1.from(input);
            } catch (Exception e) {
                Log.d(TAG, "read EMV file error", e);
                runOnUiThread(() -> Toast.makeText(EmvParamUpdateActivity.this, "read EMV config error", Toast.LENGTH_LONG).show());
                return;
            }
            startEmvParamUpdate(model);
        });
    }

    private void displayState(final String text, boolean running) {
        runOnUiThread(() -> {
            progressBar.setVisibility(running ? View.VISIBLE : View.GONE);
            if (text != null) {
                progressMsgFld.setText(text);
            }
        });
    }

    public void startEmvParamUpdate(EmvParamYTModel model) {
        displayState("start EMV param one by one load: FMT 1", true);
        updateInProgress = true;
        EmvParamUpdate1by1FSM svc = new EmvParamUpdate1by1FSM(model);
        svc.execute(((event, params) -> {
            switch (event) {
                case PROGRESS:
                    Log.d(TAG,"EmvParamUpdate1By1Fmt1Svc: state: progress");
                    break;
                case FAILED:
                case CANCELLED:
                    updateInProgress = false;
                    displayState("EMV param one by one load: failed", false);
                    break;
                case SUCCESS:
                    updateInProgress = false;
                    displayState("EMV param one by one load: success", false);
                    break;
            }
        }));
    }
}
