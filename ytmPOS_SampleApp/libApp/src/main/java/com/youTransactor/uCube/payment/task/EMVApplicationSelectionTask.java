/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube.payment.task;

import com.youTransactor.uCube.ITaskMonitor;
import com.youTransactor.uCube.TaskEvent;
import com.youTransactor.uCube.payment.PaymentContext;
import com.youTransactor.uCube.rpc.EMVApplicationDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gbillard on 5/23/16.
 */
public class EMVApplicationSelectionTask implements IApplicationSelectionTask {

	private List<EMVApplicationDescriptor> applicationList;
	private List<EMVApplicationDescriptor> candidateList;
	private PaymentContext context;

	@Override
	public void setAvailableApplication(List<EMVApplicationDescriptor> applicationList) {
		this.applicationList = applicationList;
	}

	@Override
	public List<EMVApplicationDescriptor> getSelection() {
		return candidateList;
	}

	@Override
	public PaymentContext getContext() {
		return context;
	}

	@Override
	public void setContext(PaymentContext paymentContext) {
		this.context = paymentContext;
	}

	@Override
	public void execute(ITaskMonitor monitor) {
		candidateList = new ArrayList<>();

		if (applicationList != null) {
			for (EMVApplicationDescriptor app : applicationList) {
				if (candidateList.isEmpty()) {
					candidateList.add(app);

				} else if (app.getPriority() >= candidateList.get(0).getPriority()) {
					if (app.getPriority() > candidateList.get(0).getPriority()) {
						candidateList.clear();
					}

					candidateList.add(app);
					//todo eliminate all  blocked aid
				}
			}
		}

		monitor.handleEvent(TaskEvent.SUCCESS);
	}

}
