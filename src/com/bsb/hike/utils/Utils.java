package com.bsb.hike.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;

import com.bsb.hike.HikeConstants;
import com.bsb.hike.HikeMessengerApp;
import com.bsb.hike.HikePubSub;
import com.bsb.hike.R;
import com.bsb.hike.models.ContactInfo;
import com.bsb.hike.models.ConvMessage;
import com.bsb.hike.models.ConvMessage.State;
import com.bsb.hike.models.GroupConversation;
import com.bsb.hike.models.GroupParticipant;
import com.bsb.hike.models.HikeFile.HikeFileType;
import com.bsb.hike.models.utils.JSONSerializable;
import com.bsb.hike.service.HikeService;
import com.bsb.hike.tasks.CheckForUpdateTask;
import com.bsb.hike.ui.SignupActivity;
import com.bsb.hike.ui.WelcomeActivity;
import com.bsb.hike.utils.AccountUtils.AccountInfo;
import com.google.android.maps.GeoPoint;

public class Utils {
	public static Pattern shortCodeRegex;
	public static Pattern msisdnRegex;
	public static Pattern pinRegex;

	public static String shortCodeIntent;

	private static Animation mOutToRight;
	private static Animation mInFromLeft;

	private static TranslateAnimation mOutToLeft;

	private static TranslateAnimation mInFromRight;

	public static float densityMultiplier;

	static {
		shortCodeRegex = Pattern.compile("\\*\\d{3,10}#");
		msisdnRegex = Pattern.compile("\\[(\\+\\d*)\\]");
		pinRegex = Pattern.compile("\\d{6}");
	}

	public static String join(Collection<?> s, String delimiter,
			String startWith, String endWith) {
		StringBuilder builder = new StringBuilder();
		Iterator<?> iter = s.iterator();
		while (iter.hasNext()) {
			if (!TextUtils.isEmpty(startWith)) {
				builder.append(startWith);
			}
			builder.append(iter.next());
			if (!TextUtils.isEmpty(endWith)) {
				builder.append(endWith);
			}
			if (!iter.hasNext()) {
				break;
			}
			builder.append(delimiter);
		}
		Log.d("Utils", "Joined string is: " + builder.toString());
		return builder.toString();
	}

	/*
	 * serializes the given collection into an object. Ignores exceptions
	 */
	public static JSONArray jsonSerialize(
			Collection<? extends JSONSerializable> elements) {
		JSONArray arr = new JSONArray();
		for (JSONSerializable elem : elements) {
			try {
				arr.put(elem.toJSON());
			} catch (JSONException e) {
				Log.e("Utils", "error json serializing", e);
			}
		}
		return arr;
	}

	public static JSONObject jsonSerialize(
			Map<String, ? extends JSONSerializable> elements)
			throws JSONException {
		JSONObject obj = new JSONObject();
		for (Map.Entry<String, ? extends JSONSerializable> element : elements
				.entrySet()) {
			obj.put(element.getKey(), element.getValue().toJSON());
		}
		return obj;
	}

	static final private int ANIMATION_DURATION = 400;

	public static Animation inFromRightAnimation(Context ctx) {
		if (mInFromRight == null) {
			synchronized (Utils.class) {
				mInFromRight = new TranslateAnimation(
						Animation.RELATIVE_TO_PARENT, +1.0f,
						Animation.RELATIVE_TO_PARENT, 0.0f,
						Animation.RELATIVE_TO_PARENT, 0.0f,
						Animation.RELATIVE_TO_PARENT, 0.0f);
				mInFromRight.setDuration(ANIMATION_DURATION);
				mInFromRight.setInterpolator(new AccelerateInterpolator());
			}
		}
		return mInFromRight;
	}

	public static Animation outToLeftAnimation(Context ctx) {
		if (mOutToLeft == null) {
			synchronized (Utils.class) {
				mOutToLeft = new TranslateAnimation(
						Animation.RELATIVE_TO_PARENT, 0.0f,
						Animation.RELATIVE_TO_PARENT, -1.0f,
						Animation.RELATIVE_TO_PARENT, 0.0f,
						Animation.RELATIVE_TO_PARENT, 0.0f);
				mOutToLeft.setDuration(ANIMATION_DURATION);
				mOutToLeft.setInterpolator(new AccelerateInterpolator());
			}
		}

		return mOutToLeft;
	}

	public static Animation outToRightAnimation(Context ctx) {
		if (mOutToRight == null) {
			synchronized (Utils.class) {
				if (mOutToRight == null) {
					mOutToRight = new TranslateAnimation(
							Animation.RELATIVE_TO_PARENT, 0.0f,
							Animation.RELATIVE_TO_PARENT, 1.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f);
					mOutToRight.setDuration(ANIMATION_DURATION);
					mOutToRight.setInterpolator(new AccelerateInterpolator());
				}
			}
		}
		return mOutToRight;
	}

	public static Animation inFromLeftAnimation(Context ctx) {
		if (mInFromLeft == null) {
			synchronized (Utils.class) {
				if (mInFromLeft == null) {
					mInFromLeft = new TranslateAnimation(
							Animation.RELATIVE_TO_PARENT, -1.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f,
							Animation.RELATIVE_TO_PARENT, 0.0f);
					mInFromLeft.setDuration(ANIMATION_DURATION);
					mInFromLeft.setInterpolator(new AccelerateInterpolator());
				}
			}
		}
		return mInFromLeft;
	}

	public static Intent createIntentFromContactInfo(
			final ContactInfo contactInfo) {
		Intent intent = new Intent();

		// If the contact info was made using a group conversation, then the
		// Group ID is in the contact ID
		intent.putExtra(
				HikeConstants.Extras.MSISDN,
				Utils.isGroupConversation(contactInfo.getMsisdn()) ? contactInfo
						.getId() : contactInfo.getMsisdn());
		return intent;
	}

	static private int iconHash(String s) {
		/*
		 * ignore everything after :: so that your large icon by default matches
		 * your msisdn
		 */
		s = s.split("::")[0];
		int count = 0;
		for (int i = 0; i < s.length(); ++i) {
			count += s.charAt(i);
		}

		return count;
	}

	public static Drawable getDefaultIconForUser(Context context, String msisdn) {
		if (isGroupConversation(msisdn)) {
			return context.getResources().getDrawable(
					R.drawable.ic_group_avatar);
		}
		int count = 1;
		int id;
		switch (iconHash(msisdn) % count) {
		case 0:
			id = R.drawable.ic_avatar0;
			break;
		default:
			id = R.drawable.ic_avatar0;
			break;
		}

		return context.getResources().getDrawable(id);
	}

	/** Create a File for saving an image or video */
	public static File getOutputMediaFile(HikeFileType type,
			String orgFileName, String fileKey) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		StringBuilder path = new StringBuilder(
				HikeConstants.HIKE_MEDIA_DIRECTORY_ROOT);
		switch (type) {
		case PROFILE:
			path.append("/hike Profile Images");
			break;
		case IMAGE:
			path.append("/hike Images");
			break;
		case VIDEO:
			path.append("/hike Videos");
			break;
		case AUDIO:
			path.append("/hike Audios");
		}

		File mediaStorageDir = new File(path.toString());
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("Hike", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());

		// File name should only be blank in case of profile images or while
		// capturing new media.
		if (TextUtils.isEmpty(orgFileName)) {
			switch (type) {
			case PROFILE:
			case IMAGE:
				orgFileName = "IMG_" + timeStamp + ".jpg";
				break;
			case VIDEO:
				orgFileName = "MOV_" + timeStamp + ".mp4";
			case AUDIO:
				orgFileName = "AUD_" + timeStamp + ".m4a";
			}
		}

		String fileName = getUniqueFileName(orgFileName, fileKey);

		return new File(mediaStorageDir, fileName);
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = 4;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	public static void savedAccountCredentials(AccountInfo accountInfo,
			SharedPreferences.Editor editor) {
		AccountUtils.setToken(accountInfo.token);
		editor.putString(HikeMessengerApp.MSISDN_SETTING, accountInfo.msisdn);
		editor.putString(HikeMessengerApp.TOKEN_SETTING, accountInfo.token);
		editor.putString(HikeMessengerApp.UID_SETTING, accountInfo.uid);
		editor.putInt(HikeMessengerApp.SMS_SETTING, accountInfo.smsCredits);
		editor.putInt(HikeMessengerApp.INVITED, accountInfo.all_invitee);
		editor.putInt(HikeMessengerApp.INVITED_JOINED,
				accountInfo.all_invitee_joined);
		editor.commit();
	}

	/*
	 * Extract a pin code from a specially formatted message to the application.
	 * 
	 * @return null iff the message isn't an SMS pincode, otherwise return the
	 * pincode
	 */
	public static String getSMSPinCode(String body) {
		Matcher m = pinRegex.matcher(body);
		return m.find() ? m.group() : null;
	}

	public static boolean requireAuth(Activity activity) {
		SharedPreferences settings = activity.getSharedPreferences(
				HikeMessengerApp.ACCOUNT_SETTINGS, 0);

		if (!settings.getBoolean(HikeMessengerApp.ACCEPT_TERMS, false)) {
			disconnectAndStopService(activity);
			activity.startActivity(new Intent(activity, WelcomeActivity.class));
			activity.finish();
			return true;
		}

		if (settings.getString(HikeMessengerApp.NAME_SETTING, null) == null) {
			disconnectAndStopService(activity);
			activity.startActivity(new Intent(activity, SignupActivity.class));
			activity.finish();
			return true;
		}

		Log.d("Utils",
				"auth token is "
						+ settings.getString(HikeMessengerApp.TOKEN_SETTING,
								null));
		return false;
	}

	public static void disconnectAndStopService(Activity activity) {
		// Added these lines to prevent the bad username/password bug.
		HikeMessengerApp app = (HikeMessengerApp) activity
				.getApplicationContext();
		app.disconnectFromService();
		activity.stopService(new Intent(activity, HikeService.class));
	}

	public static int[] getNumberImage(String msisdn) {
		int[] msisdnRes = new int[msisdn.length()];

		for (int i = 0; i < msisdnRes.length; i++) {
			char c = msisdn.charAt(i);
			switch (c) {
			case '+':
				msisdnRes[i] = R.drawable.no_plus;
				break;
			case '0':
				msisdnRes[i] = R.drawable.no0;
				break;
			case '1':
				msisdnRes[i] = R.drawable.no1;
				break;
			case '2':
				msisdnRes[i] = R.drawable.no2;
				break;
			case '3':
				msisdnRes[i] = R.drawable.no3;
				break;
			case '4':
				msisdnRes[i] = R.drawable.no4;
				break;
			case '5':
				msisdnRes[i] = R.drawable.no5;
				break;
			case '6':
				msisdnRes[i] = R.drawable.no6;
				break;
			case '7':
				msisdnRes[i] = R.drawable.no7;
				break;
			case '8':
				msisdnRes[i] = R.drawable.no8;
				break;
			case '9':
				msisdnRes[i] = R.drawable.no9;
				break;
			case '-':
				msisdnRes[i] = R.drawable.no_dash;
				break;
			default:
				msisdnRes[i] = R.drawable.no0;
				break;
			}
		}
		return msisdnRes;
	}

	public static String formatNo(String msisdn) {
		StringBuilder sb = new StringBuilder(msisdn);
		sb.insert(msisdn.length() - 4, '-');
		sb.insert(msisdn.length() - 7, '-');
		Log.d("Fomat MSISD", "Fomatted number is:" + sb.toString());

		return sb.toString();
	}

	public static boolean isValidEmail(Editable text) {
		return (!TextUtils.isEmpty(text) && android.util.Patterns.EMAIL_ADDRESS
				.matcher(text).matches());
	}

	public static void logEvent(Context context, String event) {
		logEvent(context, event, 1);
	}

	/**
	 * Used for logging the UI based events from the clients side.
	 * 
	 * @param context
	 * @param event
	 *            : The event which is to be logged.
	 * @param time
	 *            : This is only used to signify the time the user was on a
	 *            screen for. For cases where this is not relevant we send 0.s
	 */
	public static void logEvent(Context context, String event, long increment) {
		SharedPreferences prefs = context.getSharedPreferences(
				HikeMessengerApp.ANALYTICS, 0);

		long currentVal = prefs.getLong(event, 0) + increment;

		Editor editor = prefs.edit();
		editor.putLong(event, currentVal);
		editor.commit();
	}

	public static List<String> splitSelectedContacts(String selections) {
		Matcher matcher = msisdnRegex.matcher(selections);
		List<String> contacts = new ArrayList<String>();
		if (matcher.find()) {
			do {
				contacts.add(matcher.group().substring(1,
						matcher.group().length() - 1));
				Log.d("Utils",
						"Adding: "
								+ matcher.group().substring(1,
										matcher.group().length() - 1));
			} while (matcher.find(matcher.end()));
		}
		return contacts;
	}

	public static List<String> splitSelectedContactsName(String selections) {
		String[] selectedContacts = selections.split(", ");
		List<String> contactNames = new ArrayList<String>(
				selectedContacts.length);
		for (int i = 0; i < selectedContacts.length; i++) {
			if (!selectedContacts[i].contains("[")) {
				continue;
			}
			contactNames.add(selectedContacts[i].substring(0,
					selectedContacts[i].indexOf("[")));
		}
		return contactNames;
	}

	public static boolean isGroupConversation(String msisdn) {
		return !msisdn.startsWith("+");
	}

	public static String defaultGroupName(
			Map<String, GroupParticipant> participantList) {
		List<GroupParticipant> groupParticipants = new ArrayList<GroupParticipant>();
		for (Entry<String, GroupParticipant> participant : participantList
				.entrySet()) {
			if (!participant.getValue().hasLeft()) {
				groupParticipants.add(participant.getValue());
			}
		}
		Collections.sort(groupParticipants);

		switch (groupParticipants.size()) {
		case 0:
			return "";
		case 1:
			return groupParticipants.get(0).getContactInfo().getFirstName();
		case 2:
			return groupParticipants.get(0).getContactInfo().getFirstName()
					+ " and "
					+ groupParticipants.get(1).getContactInfo().getFirstName();
		default:
			return groupParticipants.get(0).getContactInfo().getFirstName()
					+ " and " + (groupParticipants.size() - 1) + " others";
		}
	}

	public static String getGroupJoinHighlightText(
			JSONArray participantInfoArray, GroupConversation conversation) {
		JSONObject participant = (JSONObject) participantInfoArray.opt(0);
		String highlight = ((GroupConversation) conversation)
				.getGroupParticipant(
						participant.optString(HikeConstants.MSISDN))
				.getContactInfo().getFirstName();

		if (participantInfoArray.length() == 2) {
			JSONObject participant2 = (JSONObject) participantInfoArray.opt(1);
			String name2 = ((GroupConversation) conversation)
					.getGroupParticipant(
							participant2.optString(HikeConstants.MSISDN))
					.getContactInfo().getFirstName();

			highlight += " and " + name2;
		} else if (participantInfoArray.length() > 2) {
			highlight += " and " + (participantInfoArray.length() - 1)
					+ " others";
		}
		return highlight;
	}

	public static JSONObject getDeviceDetails(Context context) {
		try {
			// {"t": "le", "d"{"tag":"cbs", "device_id":
			// "54330bc905bcf18a","_os": "DDD","_os_version": "EEE","_device":
			// "FFF","_resolution": "GGG","_carrier": "HHH", "_app_version" :
			// "x.x.x"}}
			int height;
			int width;
			JSONObject object = new JSONObject();
			JSONObject data = new JSONObject();

			/*
			 * Doing this to avoid the ClassCastException when the context is
			 * sent from the BroadcastReceiver. As it is, we don't need to send
			 * the resolution from the BroadcastReceiver since it should have
			 * already been sent to the server.
			 */
			if (context instanceof Activity) {
				height = ((Activity) context).getWindowManager()
						.getDefaultDisplay().getHeight();
				width = ((Activity) context).getWindowManager()
						.getDefaultDisplay().getWidth();
				String resolution = height + "x" + width;
				data.put(HikeConstants.LogEvent.RESOLUTION, resolution);
			}
			TelephonyManager manager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			String osVersion = Build.VERSION.RELEASE;
			String deviceId = Secure.getString(context.getContentResolver(),
					Secure.ANDROID_ID);
			String os = "Android";
			String carrier = manager.getNetworkOperatorName();
			String device = Build.MANUFACTURER + " " + Build.MODEL;
			String appVersion = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;

			object.put(HikeConstants.TYPE,
					HikeConstants.MqttMessageTypes.ANALYTICS_EVENT);
			Map<String, String> referralValues = retrieveReferralParams(context);
			if (!referralValues.isEmpty()) {
				for (Entry<String, String> entry : referralValues.entrySet()) {
					data.put(entry.getKey(), entry.getValue());
				}
			}
			data.put(HikeConstants.LogEvent.TAG, "cbs");
			data.put(HikeConstants.LogEvent.DEVICE_ID, deviceId);
			data.put(HikeConstants.LogEvent.OS, os);
			data.put(HikeConstants.LogEvent.OS_VERSION, osVersion);
			data.put(HikeConstants.LogEvent.DEVICE, device);
			data.put(HikeConstants.LogEvent.CARRIER, carrier);
			data.put(HikeConstants.LogEvent.APP_VERSION, appVersion);
			object.put(HikeConstants.DATA, data);

			return object;
		} catch (JSONException e) {
			Log.e("Utils", "Invalid JSON", e);
			return null;
		} catch (NameNotFoundException e) {
			Log.e("Utils", "Package not found", e);
			return null;
		}

	}

	public static JSONObject getDeviceStats(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				HikeMessengerApp.ANALYTICS, 0);
		Editor editor = prefs.edit();
		Map<String, ?> keys = prefs.getAll();

		JSONObject data = new JSONObject();
		JSONObject obj = new JSONObject();

		try {
			if (keys.isEmpty()) {
				obj = null;
			} else {
				for (String key : keys.keySet()) {
					Log.d("Utils", "Getting keys: " + key);
					data.put(key, prefs.getLong(key, 0));
					editor.remove(key);
				}
				editor.commit();
				data.put(HikeConstants.LogEvent.TAG, "mob");
				data.put(HikeConstants.LogEvent.DEFAULT_SMS_CLIENT,
						PreferenceManager.getDefaultSharedPreferences(context)
								.getBoolean(HikeConstants.SMS_PREF, false));

				obj.put(HikeConstants.TYPE,
						HikeConstants.MqttMessageTypes.ANALYTICS_EVENT);
				obj.put(HikeConstants.DATA, data);
			}
		} catch (JSONException e) {
			Log.e("Utils", "Invalid JSON", e);
		}

		return obj;
	}

	public static CharSequence addContactName(String firstName,
			CharSequence message) {
		SpannableStringBuilder messageWithName = new SpannableStringBuilder(
				firstName + HikeConstants.SEPARATOR + message);
		messageWithName.setSpan(new StyleSpan(Typeface.BOLD), 0,
				firstName.length() + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return messageWithName;
	}

	/**
	 * Used for setting the density multiplier, which is to be multiplied with
	 * any pixel value that is programmatically given
	 * 
	 * @param activity
	 */
	public static void setDensityMultiplier(Activity activity) {
		if (Utils.densityMultiplier == 0.0f) {
			DisplayMetrics metrics = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			Utils.densityMultiplier = metrics.scaledDensity;
		}
	}

	public static CharSequence getFormattedParticipantInfo(String info,
			String textToHighight) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(info);
		ssb.setSpan(new ForegroundColorSpan(0xff666666),
				info.indexOf(textToHighight), info.indexOf(textToHighight)
						+ textToHighight.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return ssb;
	}

	/**
	 * Used for preventing the cursor from being shown initially on the text box
	 * in touch screen devices. On touching the text box the cursor becomes
	 * visible
	 * 
	 * @param editText
	 */
	public static void hideCursor(final EditText editText, Resources resources) {
		if (resources.getConfiguration().keyboard == Configuration.KEYBOARD_NOKEYS
				|| resources.getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
			editText.setCursorVisible(false);
			editText.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						editText.setCursorVisible(true);
					}
					return false;
				}
			});
		}
	}

	public static ContactInfo getUserContactInfo(SharedPreferences prefs) {
		String myMsisdn = prefs
				.getString(HikeMessengerApp.MSISDN_SETTING, null);
		String myName = prefs.getString(HikeMessengerApp.NAME_SETTING, null);
		return new ContactInfo(myMsisdn, myMsisdn, myName, myMsisdn, true);
	}

	public static boolean wasScreenOpenedNNumberOfTimes(
			SharedPreferences prefs, String whichScreen) {
		return prefs.getInt(whichScreen, 0) >= HikeConstants.NUM_TIMES_SCREEN_SHOULD_OPEN_BEFORE_TOOL_TIP;
	}

	public static void incrementNumTimesScreenOpen(SharedPreferences prefs,
			String whichScreen) {
		Editor editor = prefs.edit();
		editor.putInt(whichScreen, prefs.getInt(whichScreen, 0) + 1);
		editor.commit();
	}

	public static boolean isUpdateRequired(String version, Context context) {
		try {
			String appVersion = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;

			StringTokenizer updateVersion = new StringTokenizer(version, ".");
			StringTokenizer currentVersion = new StringTokenizer(appVersion,
					".");
			while (currentVersion.hasMoreTokens()) {
				if (!updateVersion.hasMoreTokens()) {
					return false;
				}
				int currentVersionToken = Integer.parseInt(currentVersion
						.nextToken());
				int updateVersionToken = Integer.parseInt(updateVersion
						.nextToken());
				if (updateVersionToken > currentVersionToken) {
					return true;
				} else if (updateVersionToken < currentVersionToken) {
					return false;
				}
			}
			while (updateVersion.hasMoreTokens()) {
				if (Integer.parseInt(updateVersion.nextToken()) > 0) {
					return true;
				}
			}
			return false;
		} catch (NameNotFoundException e) {
			Log.e("Utils", "Package not found...", e);
			return false;
		}
	}

	/*
	 * Stores the referral parameters in the app's sharedPreferences.
	 */
	public static void storeReferralParams(Context context,
			List<NameValuePair> params) {
		SharedPreferences storage = context.getSharedPreferences(
				HikeMessengerApp.REFERRAL, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = storage.edit();

		for (NameValuePair nameValuePair : params) {
			String name = nameValuePair.getName();
			String value = nameValuePair.getValue();
			editor.putString(name, value);
		}

		editor.commit();
	}

	/*
	 * Returns a map with the Market Referral parameters pulled from the
	 * sharedPreferences.
	 */
	public static Map<String, String> retrieveReferralParams(Context context) {
		Map<String, String> params = new HashMap<String, String>();
		SharedPreferences storage = context.getSharedPreferences(
				HikeMessengerApp.REFERRAL, Context.MODE_PRIVATE);

		for (String key : storage.getAll().keySet()) {
			String value = storage.getString(key, null);
			if (value != null) {
				params.put(key, value);
			}
		}
		// We don't need these values anymore
		Editor editor = storage.edit();
		editor.clear();
		editor.commit();
		return params;
	}

	public static boolean isUserOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		return (cm != null && cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isAvailable() && cm
				.getActiveNetworkInfo().isConnected());
	}

	/**
	 * Requests the server to send an account info packet
	 */
	public static void requestAccountInfo() {
		Log.d("Utils", "Requesting account info");
		JSONObject requestAccountInfo = new JSONObject();
		try {
			requestAccountInfo.put(HikeConstants.TYPE,
					HikeConstants.MqttMessageTypes.REQUEST_ACCOUNT_INFO);
			HikeMessengerApp.getPubSub().publish(HikePubSub.MQTT_PUBLISH,
					requestAccountInfo);
		} catch (JSONException e) {
			Log.e("Utils", "Invalid JSON", e);
		}
	}

	public static String ellipsizeName(String name) {
		return name.length() <= HikeConstants.MAX_CHAR_IN_NAME ? name : (name
				.substring(0, HikeConstants.MAX_CHAR_IN_NAME - 3) + "...");
	}

	public static String getInviteMessage(Context context) {
		String inviteUrlWithToken = context
				.getString(R.string.default_invite_url)
				+ context.getSharedPreferences(
						HikeMessengerApp.ACCOUNT_SETTINGS, 0).getString(
						HikeConstants.INVITE_TOKEN, "");
		// Adding the user's invite token to the invite url
		String inviteMessage = context.getString(R.string.invite_share_message);
		String defaultInviteURL = context
				.getString(R.string.default_invite_url);
		return inviteMessage.replace(defaultInviteURL, inviteUrlWithToken);
	}

	public static void startShareIntent(Context context, String message) {
		Intent s = new Intent(android.content.Intent.ACTION_SEND);
		s.setType("text/plain");
		s.putExtra(Intent.EXTRA_TEXT, message);
		context.startActivity(s);
	}

	public static void bytesToFile(byte[] bytes, File dst) {
		OutputStream out = null;
		try {
			out = new FileOutputStream(dst);
			out.write(bytes, 0, bytes.length);
		} catch (IOException e) {
			Log.e("Utils", "Excecption while copying the file", e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					Log.e("Utils", "Excecption while closing the stream", e);
				}
			}
		}
	}

	public static byte[] fileToBytes(File file) {
		byte[] bytes = new byte[(int) file.length()];
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bytes);
			return bytes;
		} catch (IOException e) {
			Log.e("Utils",
					"Excecption while reading the file " + file.getName(), e);
			return null;
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					Log.e("Utils",
							"Excecption while closing the file "
									+ file.getName(), e);
				}
			}
		}
	}

	public static Drawable stringToDrawable(String encodedString) {
		if (TextUtils.isEmpty(encodedString)) {
			return null;
		}
		byte[] thumbnailBytes = Base64.decode(encodedString, Base64.DEFAULT);
		return new BitmapDrawable(BitmapFactory.decodeByteArray(thumbnailBytes,
				0, thumbnailBytes.length));
	}

	public static Bitmap scaleDownImage(String filePath, int dimensionLimit,
			boolean makeSquareThumbnail) {
		Bitmap thumbnail = null;

		int currentWidth = 0;
		int currentHeight = 0;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(filePath, options);
		currentHeight = options.outHeight;
		currentWidth = options.outWidth;

		options.inSampleSize = Math
				.round((currentHeight > currentWidth ? currentHeight
						: currentWidth) / (dimensionLimit));
		options.inJustDecodeBounds = false;

		thumbnail = BitmapFactory.decodeFile(filePath, options);
		if (makeSquareThumbnail) {
			dimensionLimit = thumbnail.getWidth() < thumbnail.getHeight() ? thumbnail
					.getWidth() : thumbnail.getHeight();

			int startX = thumbnail.getWidth() > dimensionLimit ? (int) ((thumbnail
					.getWidth() - dimensionLimit) / 2) : 0;
			int startY = thumbnail.getHeight() > dimensionLimit ? (int) ((thumbnail
					.getHeight() - dimensionLimit) / 2) : 0;

			Log.d("Utils", "StartX: " + startX + " StartY: " + startY
					+ " WIDTH: " + thumbnail.getWidth() + " Height: "
					+ thumbnail.getHeight());
			Bitmap squareThumbnail = Bitmap.createBitmap(thumbnail, startX,
					startY, dimensionLimit, dimensionLimit);

			if (squareThumbnail != thumbnail) {
				thumbnail.recycle();
			}
			thumbnail = null;
			return squareThumbnail;
		}

		return thumbnail;
	}

	public static byte[] bitmapToBytes(Bitmap bitmap,
			Bitmap.CompressFormat format) {
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		bitmap.compress(format, 50, bao);
		return bao.toByteArray();
	}

	public static String getRealPathFromUri(Uri contentUri, Activity activity) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = activity.managedQuery(contentUri, proj, null, null,
				null);
		if (cursor == null || cursor.getCount() == 0) {
			return null;
		}
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public static enum ExternalStorageState {
		WRITEABLE, READ_ONLY, NONE
	}

	public static ExternalStorageState getExternalStorageState() {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			return ExternalStorageState.WRITEABLE;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			return ExternalStorageState.READ_ONLY;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			return ExternalStorageState.NONE;
		}
	}

	public static String getFirstName(String name) {
		return name.split(" ", 2)[0];
	}

	public static double getFreeSpace() {
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
				.getPath());
		double sdAvailSize = (double) stat.getAvailableBlocks()
				* (double) stat.getBlockSize();
		return sdAvailSize;
	}

	public static boolean copyFile(String srcFilePath, String destFilePath,
			HikeFileType hikeFileType) {
		try {
			InputStream src;
			if (hikeFileType == HikeFileType.IMAGE) {
				String imageOrientation = Utils
						.getImageOrientation(srcFilePath);
				Bitmap tempBmp = Utils.scaleDownImage(srcFilePath,
						HikeConstants.MAX_DIMENSION_FULL_SIZE_PX, false);
				tempBmp = Utils.rotateBitmap(tempBmp,
						Utils.getRotatedAngle(imageOrientation));
				// Temporary fix for when a user uploads a file through Picasa
				// on ICS or higher.
				if (tempBmp == null) {
					return false;
				}
				byte[] fileBytes = Utils.bitmapToBytes(tempBmp,
						Bitmap.CompressFormat.JPEG);
				tempBmp.recycle();
				src = new ByteArrayInputStream(fileBytes);
			} else {
				src = new FileInputStream(new File(srcFilePath));
			}
			OutputStream dest = new FileOutputStream(new File(destFilePath));

			byte[] buffer = new byte[HikeConstants.MAX_BUFFER_SIZE_KB * 1024];
			int len;

			while ((len = src.read(buffer)) > 0) {
				dest.write(buffer, 0, len);
			}

			src.close();
			dest.close();

			return true;
		} catch (FileNotFoundException e) {
			Log.e("Utils", "File not found while copying", e);
			return false;
		} catch (IOException e) {
			Log.e("Utils", "Error while reading/writing/closing file", e);
			return false;
		}
	}

	public static String getImageOrientation(String filePath) {
		ExifInterface exif;
		try {
			exif = new ExifInterface(filePath);
			return exif.getAttribute(ExifInterface.TAG_ORIENTATION);
		} catch (IOException e) {
			Log.e("Utils", "Error while opening file", e);
			return null;
		}
	}

	public static int getRotatedAngle(String imageOrientation) {
		if (!TextUtils.isEmpty(imageOrientation)) {
			switch (Integer.parseInt(imageOrientation)) {
			case ExifInterface.ORIENTATION_ROTATE_180:
				return 180;
			case ExifInterface.ORIENTATION_ROTATE_270:
				return 270;
			case ExifInterface.ORIENTATION_ROTATE_90:
				return 90;
			}
		}
		return 0;
	}

	public static Bitmap rotateBitmap(Bitmap b, int degrees) {
		if (degrees != 0 && b != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) b.getWidth() / 2,
					(float) b.getHeight() / 2);
			try {
				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
						b.getHeight(), m, true);
				if (b != b2) {
					b.recycle();
					b = b2;
				}
			} catch (OutOfMemoryError e) {
				Log.e("Utils", "Out of memory", e);
			}
		}
		return b;
	}

	public static void setupServerURL(boolean isProductionServer) {
		AccountUtils.HOST = isProductionServer ? AccountUtils.PRODUCTION_HOST
				: AccountUtils.STAGING_HOST;
		AccountUtils.PORT = isProductionServer ? AccountUtils.PRODUCTION_PORT
				: AccountUtils.STAGING_PORT;
		if (isProductionServer) {
			AccountUtils.BASE = "http://" + AccountUtils.HOST + "/v1";
		} else {
			AccountUtils.BASE = "http://" + AccountUtils.HOST + ":"
					+ Integer.toString(AccountUtils.PORT) + "/v1";
		}
		AccountUtils.FILE_TRANSFER_HOST = isProductionServer ? AccountUtils.PRODUCTION_FT_HOST
				: AccountUtils.STAGING_HOST;
		AccountUtils.FILE_TRANSFER_UPLOAD_BASE = "http://"
				+ AccountUtils.FILE_TRANSFER_HOST + ":"
				+ Integer.toString(AccountUtils.PORT) + "/v1";

		CheckForUpdateTask.UPDATE_CHECK_URL = isProductionServer ? CheckForUpdateTask.PRODUCTION_URL
				: CheckForUpdateTask.STAGING_URL;

		AccountUtils.FILE_TRANSFER_BASE_DOWNLOAD_URL = AccountUtils.BASE
				+ AccountUtils.FILE_TRANSFER_DOWNLOAD_BASE;
		AccountUtils.FILE_TRANSFER_BASE_VIEW_URL = isProductionServer ? AccountUtils.FILE_TRANSFER_BASE_VIEW_URL_PRODUCTION
				: AccountUtils.FILE_TRANSFER_BASE_VIEW_URL_STAGING;
	}

	public static boolean shouldChangeMessageState(ConvMessage convMessage,
			int stateOrdinal) {
		if (convMessage == null) {
			return false;
		}
		int minStatusOrdinal;
		int maxStatusOrdinal;
		// No need to change the message state for typing notifications
		if (HikeConstants.IS_TYPING.equals(convMessage.getMessage())
				&& convMessage.getMsgID() == -1
				&& convMessage.getMappedMsgID() == -1) {
			return false;
		}
		if (stateOrdinal <= State.SENT_DELIVERED_READ.ordinal()) {
			minStatusOrdinal = State.SENT_UNCONFIRMED.ordinal();
			maxStatusOrdinal = stateOrdinal;
		} else {
			minStatusOrdinal = State.RECEIVED_UNREAD.ordinal();
			maxStatusOrdinal = stateOrdinal;
		}

		int convMessageStateOrdinal = convMessage.getState().ordinal();

		if (convMessageStateOrdinal <= maxStatusOrdinal
				&& convMessageStateOrdinal >= minStatusOrdinal) {
			return true;
		}
		return false;
	}

	public static ConvMessage makeHike2SMSInviteMessage(String msisdn,
			Context context) {
		long time = (long) System.currentTimeMillis() / 1000;

		// Adding the user's invite token to the invite url
		String inviteMessage = context.getString(R.string.invite_message);
		String defaultInviteURL = context
				.getString(R.string.default_invite_url);
		String inviteToken = context.getSharedPreferences(
				HikeMessengerApp.ACCOUNT_SETTINGS, 0).getString(
				HikeConstants.INVITE_TOKEN, "");
		inviteMessage = inviteMessage.replace(defaultInviteURL,
				defaultInviteURL + inviteToken);

		ConvMessage convMessage = new ConvMessage(inviteMessage, msisdn, time,
				ConvMessage.State.SENT_UNCONFIRMED);
		convMessage.setInvite(true);

		return convMessage;
	}

	public static String getAddressFromGeoPoint(GeoPoint geoPoint,
			Context context) {
		try {
			Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
			List<Address> addresses = geoCoder.getFromLocation(
					geoPoint.getLatitudeE6() / 1E6,
					geoPoint.getLongitudeE6() / 1E6, 1);

			final StringBuilder address = new StringBuilder();
			if (!addresses.isEmpty()) {
				for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++)
					address.append(addresses.get(0).getAddressLine(i) + "\n");
			}

			return address.toString();
		} catch (IOException e) {
			Log.e("Utils", "IOException", e);
			return "";
		}
	}

	public static void addFileName(String fileName, String fileKey) {
		File hikeFileList = new File(HikeConstants.HIKE_MEDIA_DIRECTORY_ROOT,
				HikeConstants.HIKE_FILE_LIST_NAME);

		JSONObject currentFiles = getHikeFileListData(hikeFileList);

		if (currentFiles == null) {
			Log.d("Utils", "File did not exist. Will create a new one");
			currentFiles = new JSONObject();
		}
		FileOutputStream fileOutputStream = null;
		ByteArrayInputStream byteArrayInputStream = null;
		try {
			Log.d("Utils", "Adding data : " + "File Name: " + fileName
					+ " File Key: " + fileKey);
			currentFiles.put(fileName, fileKey);
			fileOutputStream = new FileOutputStream(hikeFileList);
			byteArrayInputStream = new ByteArrayInputStream(currentFiles
					.toString().getBytes("UTF-8"));

			int b;
			byte[] data = new byte[8];
			while ((b = byteArrayInputStream.read(data)) != -1) {
				fileOutputStream.write(data, 0, b);
			}
		} catch (FileNotFoundException e) {
			Log.e("Utils", "File not found", e);
		} catch (JSONException e) {
			Log.e("Utils", "Invalid JSON", e);
		} catch (UnsupportedEncodingException e) {
			Log.e("Utils", "Unsupported Encoding Exception", e);
		} catch (IOException e) {
			Log.e("Utils", "IOException", e);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					Log.e("Utils", "Exception while closing the output stream",
							e);
				}
			}
		}
	}

	public static String getUniqueFileName(String orgFileName, String fileKey) {
		File hikeFileList = new File(HikeConstants.HIKE_MEDIA_DIRECTORY_ROOT,
				HikeConstants.HIKE_FILE_LIST_NAME);
		JSONObject currentFiles = getHikeFileListData(hikeFileList);
		if(currentFiles == null || !currentFiles.has(orgFileName)) {
			Log.d("Utils", "File with this name does not exist");
			return orgFileName;
		}

		String fileExtension = orgFileName.contains(".") ? orgFileName
				.substring(orgFileName.lastIndexOf("."), orgFileName.length())
				: "";
		String orgFileNameWithoutExtension = !TextUtils.isEmpty(fileExtension) ? orgFileName
				.substring(0, orgFileName.indexOf(fileExtension)) : orgFileName;
		StringBuilder newFileName = new StringBuilder(
				orgFileNameWithoutExtension);

		String currentNameToCheck = orgFileName;
		int i = 1;
		Log.d("Utils", "File name: " + newFileName.toString() + " Extension: "
				+ fileExtension);
		while (true) {
			String existingFileKey = currentFiles.optString(currentNameToCheck);
			if (TextUtils.isEmpty(existingFileKey)
					|| existingFileKey.equals(fileKey)) {
				break;
			} else {
				newFileName = new StringBuilder(orgFileNameWithoutExtension
						+ "_" + i++);
				currentNameToCheck = newFileName + fileExtension;
			}
		}
		Log.d("Utils", "NewFile name: " + newFileName.toString() + " Extension: "
				+ fileExtension);
		newFileName.append(fileExtension);
		return newFileName.toString();
	}

	public static void makeNewFileWithExistingData(JSONObject data) {
		File hikeFileList = new File(HikeConstants.HIKE_MEDIA_DIRECTORY_ROOT,
				HikeConstants.HIKE_FILE_LIST_NAME);

		Log.d("Utils", "Writing data: " + data.toString());

		FileOutputStream fileOutputStream = null;
		ByteArrayInputStream byteArrayInputStream = null;
		try {
			fileOutputStream = new FileOutputStream(hikeFileList);
			byteArrayInputStream = new ByteArrayInputStream(data
					.toString().getBytes("UTF-8"));

			int b;
			byte[] d = new byte[8];
			while ((b = byteArrayInputStream.read(d)) != -1) {
				fileOutputStream.write(d, 0, b);
			}
		} catch (FileNotFoundException e) {
			Log.e("Utils", "File not found", e);
		} catch (UnsupportedEncodingException e) {
			Log.e("Utils", "Unsupported Encoding Exception", e);
		} catch (IOException e) {
			Log.e("Utils", "IOException", e);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					Log.e("Utils", "Exception while closing the output stream",
							e);
				}
			}
		}
	}

	private static JSONObject getHikeFileListData(File hikeFileList) {
		if(!hikeFileList.exists()) {
			return null;
		}
		FileInputStream fileInputStream = null;
		JSONObject currentFiles = null;
		try {
			fileInputStream = new FileInputStream(hikeFileList);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(fileInputStream));

			StringBuilder builder = new StringBuilder();
			CharBuffer target = CharBuffer.allocate(10000);
			int read = reader.read(target);

			while (read >= 0) {
				builder.append(target.array(), 0, read);
				target.clear();
				read = reader.read(target);
			}

			currentFiles = new JSONObject(builder.toString());
			Log.d("Utils",
					"File found: Current data: " + builder.toString());
		} catch (FileNotFoundException e) {
			Log.e("Utils", "File not found", e);
			hikeFileList.delete();
		} catch (IOException e) {
			Log.e("Utils", "IOException", e);
			hikeFileList.delete();
		} catch (JSONException e) {
			Log.e("Utils", "Invalid JSON", e);
			hikeFileList.delete();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					Log.e("Utils",
							"Exception while closing the input stream", e);
				}
			}
		}
		return currentFiles;
	}
}
