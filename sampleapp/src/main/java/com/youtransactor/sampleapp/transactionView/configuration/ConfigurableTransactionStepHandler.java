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
 * @date: oct. 06, 2025
 * @author: Emmanuel COLAS (emmanuel_colas@jabil.com)
 */

package com.youtransactor.sampleapp.transactionView.configuration;

import android.util.Log;

import com.jps.secureService.api.product_manager.ProductIdentifier;
import com.youTransactor.uCube.rpc.Event;
import com.youTransactor.uCube.rpc.command.event.EventCommand;
import com.youtransactor.sampleapp.transactionView.TransactionViewBase;
import com.youtransactor.sampleapp.transactionView.steps.display_authorization.DefaultDisplayAuthorizationStep;
import com.youtransactor.sampleapp.transactionView.steps.display_idle.DefaultDisplayIdleStep;
import com.youtransactor.sampleapp.transactionView.steps.display_integrity_check_warning.DefaultDisplayIntegrityCheckWarningStep;
import com.youtransactor.sampleapp.transactionView.steps.display_payment_result.DefaultDisplayPaymentResultStep;
import com.youtransactor.sampleapp.transactionView.steps.display_read_card.DefaultDisplayReadCardStep;
import com.youtransactor.sampleapp.transactionView.steps.pin_prompt.DefaultPinPromptStep;
import com.youtransactor.sampleapp.transactionView.steps.select_aid.UserChoiceSelectAidStep;
import com.youtransactor.sampleapp.transactionView.steps.select_language.UserChoiceSelectLanguageStep;
import com.youtransactor.sampleapp.transactionView.steps.wait_card.BladeWaitCardStep;
import com.youtransactor.sampleapp.transactionView.steps.wait_card.StickWaitCardStep;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ConfigurableTransactionStepHandler implements TransactionStepHandler {

    private static final String TAG = ConfigurableTransactionStepHandler.class.getSimpleName();

    private final Map<Event, Consumer<EventCommand>> eventActionMap = new HashMap<>();

    @Override
    public void handle(EventCommand eventCommand) {
        final Consumer<EventCommand> consumer = eventActionMap.get(eventCommand.getEvent());
        Log.d(TAG, "Handling event: " + eventCommand.getEvent());
        if (consumer == null) {
            return;
        }
        consumer.accept(eventCommand);
    }

    public static Builder defaultForProductAndTransactionViewBase(final ProductIdentifier productIdentifier, final TransactionViewBase transactionViewBase) {
        return switch (productIdentifier) {
            case blade -> defaultForBlade(transactionViewBase);
            case stick -> defaultForStick(transactionViewBase);
            default ->
                    throw new IllegalArgumentException("Unsupported product identifier: " + productIdentifier);
        };
    }

    private static Builder defaultBase(final TransactionViewBase transactionViewBase) {
        return new Builder()
                .forEvent(Event.dsp_payment_result, new DefaultDisplayPaymentResultStep(transactionViewBase)::execute)
                .forEvent(Event.dsp_authorisation, new DefaultDisplayAuthorizationStep(transactionViewBase)::execute)
                .forEvent(Event.dsp_read_card, new DefaultDisplayReadCardStep(transactionViewBase)::execute)
                .forEvent(Event.ppt_pin, new DefaultPinPromptStep(transactionViewBase)::execute)
                .forEvent(Event.dsp_listbox_select_lang, new UserChoiceSelectLanguageStep(transactionViewBase)::execute)
                .forEvent(Event.pay_select_aid, new UserChoiceSelectAidStep(transactionViewBase)::execute)
                .forEvent(Event.dsp_idle, new DefaultDisplayIdleStep(transactionViewBase)::execute)
                .forEvent(Event.dsp_integ_check_24h_warning, new DefaultDisplayIntegrityCheckWarningStep(transactionViewBase)::execute);

    }

    private static Builder defaultForBlade(final TransactionViewBase transactionViewBase) {
        return defaultBase(transactionViewBase)
                .forEvent(Event.dsp_wait_card, new BladeWaitCardStep(transactionViewBase)::execute);

    }

    private static Builder defaultForStick(final TransactionViewBase transactionViewBase) {
        return defaultBase(transactionViewBase)
                .forEvent(Event.dsp_wait_card, new StickWaitCardStep(transactionViewBase)::execute);
    }

    public static class Builder {
        private final ConfigurableTransactionStepHandler handler;

        public Builder() {
            handler = new ConfigurableTransactionStepHandler();
        }

        public Builder forEvent(Event event, Consumer<EventCommand> action) {
            handler.eventActionMap.put(event, action);
            return this;
        }

        public ConfigurableTransactionStepHandler build() {
            return handler;
        }
    }
}