/*
 * Copyright (C) 2016, YouTransactor. All Rights Reserved.
 *
 * Use of this product is contingent on the existence of an executed license
 * agreement between YouTransactor or one of its sublicensee, and your
 * organization, which specifies this software's terms of use. This software
 * is here defined as YouTransactor Intellectual Property for the purposes
 * of determining terms of use as defined within the license agreement.
 */
package com.youTransactor.uCube;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author gbillard on 6/21/16.
 */
public class LogManager {

	private static final boolean DEFAULT_VALUE_ENABLE = true;
	private static final DateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRENCH);

	@SuppressLint("StaticFieldLeak")
	private static Context context;

	private static boolean isEnabled;

	public static void d(String message) {
		String caller = getCaller();
		debug(caller, message);
	}

	public static void e(String message) {
		String caller = getCaller();
		error(caller, message, null);
	}

	public static void e(String message, Exception e) {
		String caller = getCaller();
		error(caller, message, e);
	}

	public static void setEnabled(boolean state) {
		isEnabled = state;
	}

	public static boolean isEnabled() {
		return isEnabled;
	}

	public static boolean hasLogs() throws Exception {
		if(context == null)
			throw new Exception("LogManager not initialized. Call initialize() first.");

		File target = new File(context.getFilesDir(), "logs");

		if (!target.exists() || !target.isDirectory()) {
			return false;
		}

		File[] files = target.listFiles();
		return files != null;
	}

	public static void getLogs(OutputStream out) {

		File baseDir = context.getFilesDir();

		try (ZipOutputStream zout = new ZipOutputStream(out)) {

			addToZip("logs", baseDir, zout);

		} catch (Exception e) {
			LogManager.debug("AppLog", e.getMessage());
		}
	}

	public static void deleteLogs() {

		File target = new File(context.getFilesDir(), "logs");

		if (!target.exists()) {
			return;
		}

		if (target.isDirectory() && target.listFiles()!= null) {
			File[] files = target.listFiles();
			if (files == null) {
				return;
			}

			for (File child : files) {
				child.delete();
			}

			return;
		}

		target.delete();
	}

	public static void initialize(Context context) {
		LogManager.context = context;

		if(BuildConfig.PropertyPairs.contains("enable_log")) {
			setEnabled((Boolean) BuildConfig.PropertyPairs.get("enable_log"));
		} else
			setEnabled(DEFAULT_VALUE_ENABLE);
	}

	public static boolean storeTransactionLog(byte[] logs1, byte[] logs2) {
		if (context == null || (logs1 == null && logs2 == null)) {
			return false;
		}

		File logDir = new File(context.getFilesDir(), "logs");

		try {
			FileOutputStream out = new FileOutputStream(new File(logDir,
					timestampFormatter.format(new Date()).replace(':','-').replace(' ', '_')));
			if (logs1 != null) {
				IOUtils.copy(new ByteArrayInputStream(logs1), out);
			}

			if (logs2 != null) {
				IOUtils.copy(new ByteArrayInputStream(logs2), out);
			}

			return true;

		} catch (Exception e) {
			e("unable to store transaction logs", e);
			return false;
		}
	}

	public static void debug(String tag, String message) {
		if(isEnabled)
			LoggerFactory.getLogger(tag).debug(message);
		else
			Log.d(tag, message);
	}

	public static void error(String tag, String message, Exception e) {
		if(isEnabled)
			LoggerFactory.getLogger(tag).error(message, e);
		else
			Log.e(tag, message, e);
	}

	private static String getCaller() {
		try {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[4];
			return
					//Thread.currentThread().getName()
					//+":" +
					stackTraceElement.getClassName()
					+":" + stackTraceElement.getMethodName()
					+ " ";
		}catch (Exception ignored) {
			return "caller not found";
		}
	}

	private static void addToZip(String relativePath, File baseDir, ZipOutputStream out) throws Exception {
		File target = new File(baseDir, relativePath);
		if (!target.exists()) {
			return;
		}

		if (target.isDirectory()) {
			String[] childs = target.list();
			if (childs == null) {
				return;
			}

			for (String child : childs) {
				addToZip(relativePath + '/' + child, baseDir, out);
			}

			return;
		}

		try (FileInputStream fis = new FileInputStream(target)) {
			ZipEntry entry = new ZipEntry(relativePath);
			out.putNextEntry(entry);

			IOUtils.copy(fis, out);
			fis.close();

			out.closeEntry();
		}
	}

}
