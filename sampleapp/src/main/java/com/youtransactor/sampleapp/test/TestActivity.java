/*
 * Copyright (C) 2011-2021, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
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

    private Ticket ticketToReproduce;

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
        ticketSwitch.setAdapter(new TicketAdapter());
        ticketSwitch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ticketToReproduce = (Ticket) ticketSwitch.getSelectedItem();

                ticketDescriptionTextView.setText(ticketToReproduce.getDescription());
                startRunDelayEditText.setText(String.valueOf(ticketToReproduce.getDelay()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                ticketToReproduce = null;
            }
        });

        runLogsBt.setOnClickListener(v -> LogsExecutor.runLogs(this));
    }

    private static TestDaemon DAEMON;

    private void startTestDaemon() {
        int startRunDelay = Integer.parseInt(startRunDelayEditText.getText().toString());
        int numberOfRuns = Integer.parseInt(numberOfRunsEditText.getText().toString());

        DAEMON = new TestDaemon(ticketToReproduce, startRunDelay, numberOfRuns);

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
