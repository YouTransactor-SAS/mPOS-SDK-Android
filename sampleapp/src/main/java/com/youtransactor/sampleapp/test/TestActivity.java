/*
 * ============================================================================
 *
 * Copyright (c) 2022 YouTransactor
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
package com.youtransactor.sampleapp.test;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.log.ILogListener;
import com.youtransactor.sampleapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TestActivity extends AppCompatActivity implements ILogListener {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM HH:mm:ss.SSS", Locale.FRANCE);

    private Button startDaemonBtn, stopDaemonBtn;
    private EditText startRunDelayEditText, numberOfRunsEditText;
    private List<String> logsList;
    private RecyclerView rvLogs;
    private LogsAdapter adapter;
    private TextView ticketDescriptionTextView;

    private Test testToReproduce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);

        initView();

        UCubeAPI.registerLogListener(this);
    }

    @Override
    protected void onDestroy() {
        stopTestDaemon();

        super.onDestroy();
    }

    private void initView() {
        startDaemonBtn = findViewById(R.id.start_daemon);
        stopDaemonBtn = findViewById(R.id.stop_daemon);
        startRunDelayEditText = findViewById(R.id.start_run_delay);
        numberOfRunsEditText = findViewById(R.id.number_of_runs);
        ticketDescriptionTextView = findViewById(R.id.ticket_description);
        rvLogs = findViewById(R.id.logs);
        Button runLogsBt = findViewById(R.id.run_logs);

        logsList = new ArrayList<>();
        adapter = new LogsAdapter(logsList);
        rvLogs.setAdapter(adapter);
        LinearLayoutManager lm = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        rvLogs.setLayoutManager(lm);

        /*auto test calls*/
        startDaemonBtn.setOnClickListener(v -> startTestDaemon());
        stopDaemonBtn.setOnClickListener(v -> stopTestDaemon());

        final Spinner ticketSwitch = findViewById(R.id.ticketSwitch);
        ticketSwitch.setAdapter(new TestAdapter());
        ticketSwitch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                testToReproduce = (Test) ticketSwitch.getSelectedItem();

                ticketDescriptionTextView.setText(testToReproduce.getDescription());
                startRunDelayEditText.setText(String.valueOf(testToReproduce.getDelayInMilleseconds()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                testToReproduce = null;
            }
        });

        runLogsBt.setOnClickListener(v -> LogsExecutor.runLogs(this));
    }

    private static TestDaemon DAEMON;

    private void startTestDaemon() {
        int startRunDelay = Integer.parseInt(startRunDelayEditText.getText().toString());
        int numberOfRuns = Integer.parseInt(numberOfRunsEditText.getText().toString());

        DAEMON = new TestDaemon(testToReproduce, startRunDelay, numberOfRuns);

        new Thread(DAEMON).start();

        startDaemonBtn.setVisibility(View.GONE);
        stopDaemonBtn.setVisibility(View.VISIBLE);
        adapter.clear();
    }

    private void stopTestDaemon() {
        if (DAEMON != null) {
            DAEMON.end();
        }

        startDaemonBtn.setVisibility(View.VISIBLE);
        stopDaemonBtn.setVisibility(View.GONE);
    }

    @Override
    public void onDebugLogged(String tag, String line) {
        onNewLogReceived(line);
    }

    @Override
    public void onErrorLogged(String tag, String line) {
        onNewLogReceived(line);
    }

    @Override
    public void onWarningLogged(String tag, String line) {
        onNewLogReceived(line);
    }

    private void onNewLogReceived(String line) {
        runOnUiThread(() -> {
            String currentDate = DATE_FORMAT.format(new Date());
            logsList.add(currentDate + " " + line);
            adapter.notifyItemInserted(logsList.size() - 1);
            rvLogs.scrollToPosition(adapter.getItemCount() - 1);
        });
    }
}
