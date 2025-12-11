/**
 * 2006-2025 YOUTRANSACTOR ALL RIGHTS RESERVED. YouTransactor,
 * 32, rue Brancion 75015 Paris France, RCS PARIS: B 491 208 500, YOUTRANSACTOR
 * CONFIDENTIAL AND PROPRIETARY INFORMATION , CONTROLLED DISTRIBUTION ONLY,
 * THEREFORE UNDER NDA ONLY. YOUTRANSACTOR Authorized Parties and who have
 * signed NDA do not have permission to distribute this documentation further.
 * Unauthorized recipients must destroy any electronic and hard-copies
 * and without reading and notify Gregory Mardinian, CTO, YOUTRANSACTOR
 * immediately at gregory_mardinian@jabil.com.
 *
 * @date: dec. 11, 2025
 * @author: Amani Moussa (amani_moussa@jabil.com)
 */

package com.youtransactor.sampleapp.manual_entry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.youtransactor.sampleapp.R;
import com.youtransactor.sampleapp.features.SdseSession;

public class ManualEntryActivity extends AppCompatActivity {

    private Button StartManualEntry;
    private SwitchCompat is_luhn_key_check;
    private SdseSession sdseSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_entry);
        is_luhn_key_check = findViewById(R.id.enable_luhn_key_check);
        StartManualEntry = findViewById(R.id.start_manual_entry);
        StartManualEntry.setOnClickListener(v -> startManualEntry());
        is_luhn_key_check.setChecked(true);
    }

    private void startManualEntry() {
        Intent sdseIntent = new Intent(this, SdsePromptActivity.class);
        sdseIntent.putExtra(SdsePromptActivity.INTENT_EXTRA_SDSE_PROMPT_MSG, getString(R.string.set_pan));
        sdseIntent.putExtra(SdsePromptActivity.INTENT_EXTRA_SDSE_PROMPT_TYPE, SdseSession.SDSE_TYPE_PAN);

        startActivity(sdseIntent);
        this.sdseSession = new SdseSession(this, is_luhn_key_check.isChecked());
        this.sdseSession.start();
    }

    @Override
    protected void onDestroy() {
        this.sdseSession.cancel();
        super.onDestroy();
    }
}