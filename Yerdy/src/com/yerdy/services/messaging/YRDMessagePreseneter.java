package com.yerdy.services.messaging;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.yerdy.services.Yerdy;
import com.yerdy.services.YerdyMessageDelegate;
import com.yerdy.services.logging.YRDLog;

/**
 * Provides Android popup to handle message and integrate with
 * {@link YerdyMessageDelegate} for additional control.
 */
public class YRDMessagePreseneter {

	YRDMessage _data;

	// View view = null;

	private boolean _hasShown = false;
	private boolean _isVisible = false;
	private boolean _hasReported = false;
	private Context _cxt = null;
	private Dialog _dialog = null;
	private YRDMessagePresenterDelegate _messageDelegate;

	public YRDMessagePreseneter(YRDMessage messageData, Context cxt) {
		_data = messageData;
		_cxt = cxt;
	}

	public void setMessageDelegate(YRDMessagePresenterDelegate messageDelegate) {
		this._messageDelegate = messageDelegate;
	}

	/**
	 * This renders the message on the GUI and triggers it's actions as
	 * necessary.
	 * 
	 * Image styles renders in a custom UI Other styles extend the native
	 * android UI
	 * 
	 * @param activity
	 *            - Required parent activity
	 */
	public void show(final Activity activity) {
		if (_hasShown) {
			return;
		}

		_hasShown = true;

		_messageDelegate.willPresentMessage(activity, this, _data);
		if (_data.style == YRDMessageStyle.IMAGE)
			showImageStyle(activity);
		else
			showSystemStyle(activity);
	}

	public void dismiss() {
		_hasShown = false;
		_isVisible = false;
		_hasReported = false;
		if (_dialog != null)
			_dialog.dismiss();
	}

	/**
	 * Used to render custom image UI dialog
	 * 
	 * @param activity
	 */
	private void showImageStyle(final Activity activity) {
		if (activity == null || activity.isFinishing())
			return;

		String pkg = Yerdy.getInstance().getAppPackage();
		Resources res = activity.getResources();
		final ColorDrawable color = new ColorDrawable(Color.TRANSPARENT);
		final int layout = res.getIdentifier("yerdy_theme_dialog", "layout", pkg);
		final int imageId = res.getIdentifier("yerdy_theme_dialog_image", "id", pkg);
		final int okButtonId = res.getIdentifier("yerdy_theme_dialog_button_ok", "id", pkg);
		final int cancelButtonId = res.getIdentifier("yerdy_theme_dialog_button_cancel", "id", pkg);
		final int titleId = res.getIdentifier("yerdy_theme_dialog_title", "id", pkg);
		final int messageId = res.getIdentifier("yerdy_theme_dialog_message", "id", pkg);
		final int timerId = res.getIdentifier("yerdy_theme_dialog_expiration", "id", pkg);
		final int rootLayoutId = res.getIdentifier("yerdy_theme_dialog_root", "id", pkg);
		final int watermarkId = res.getIdentifier("yerdy_theme_dialog_watermark", "id", pkg);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		if (_data.watermarkLocation != YRDWatermarkPosition.HIDDEN) {
			switch (_data.watermarkLocation) {
			case BOTTOM_CENTER:
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				params.addRule(RelativeLayout.CENTER_HORIZONTAL);
				break;
			case BOTTOM_LEFT:
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				break;
			case BOTTOM_RIGHT:
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				break;
			case MIDDLE_CENTER:
				params.addRule(RelativeLayout.CENTER_IN_PARENT);
				break;
			case MIDDLE_LEFT:
				params.addRule(RelativeLayout.CENTER_VERTICAL);
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				break;
			case MIDDLE_RIGHT:
				params.addRule(RelativeLayout.CENTER_VERTICAL);
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				break;
			case TOP_CENTER:
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				params.addRule(RelativeLayout.CENTER_HORIZONTAL);
				break;
			case TOP_LEFT:
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				break;
			case TOP_RIGHT:
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				break;
			}
		}
		final RelativeLayout.LayoutParams watermarkParams = params;
		
		activity.runOnUiThread(new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				
				final Dialog dialog = new Dialog(activity);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.getWindow().setBackgroundDrawable(color);
				dialog.setContentView(layout);
				
				final ImageView image = (ImageView) dialog.findViewById(imageId);
				final Button okButton = (Button) dialog.findViewById(okButtonId);
				final Button cancelButton = (Button) dialog.findViewById(cancelButtonId);
				final TextView titleText = (TextView) dialog.findViewById(titleId);
				final TextView messageText = (TextView) dialog.findViewById(messageId);
				final TextView timerText = (TextView) dialog.findViewById(timerId);
				final ViewGroup rootLayout = (ViewGroup) dialog.findViewById(rootLayoutId);
				final ImageView watermark = (ImageView) dialog.findViewById(watermarkId);
				
				if (_data.watermarkLocation != YRDWatermarkPosition.HIDDEN) {
					if (_data.watermarkImageURI != null) {
						if (_data.watermarkBitmap != null) {
							watermark.setImageBitmap(_data.watermarkBitmap);
						} else {
							watermark.setImageURI(_data.watermarkImageURI);
						}
						watermark.setVisibility(View.VISIBLE);
						watermark.setLayoutParams(watermarkParams);
					}
				}

				if (_data.imageURI != null) {
					if (_data.imageBitmap != null) {
						image.setImageBitmap(_data.imageBitmap);
					} else {
						image.setImageURI(_data.imageURI);
					}
				} else {
					image.setVisibility(ImageView.GONE);
				}

				if (_data.titleString != null && _data.titleString.trim().length() > 0) {
					titleText.setText(_data.titleString.trim());
					titleText.setTextColor(_data.titleColor);
				} else {
					titleText.setVisibility(TextView.GONE);
					titleText.setTextColor(_data.messageColor);
				}
				
				
				
				if (_data.textString != null && _data.textString.trim().length() > 0) {
					messageText.setText(_data.textString.trim());
				} else {
					messageText.setVisibility(TextView.GONE);
				}
				
				rootLayout.setBackgroundColor(_data.backgroundColor);
				if (_data.confirmString != null
						&& _data.confirmString.trim().length() > 0) {
					okButton.setText(_data.confirmString.trim());
					okButton.setOnClickListener(new Button.OnClickListener() {
						@Override
						public void onClick(View v) {
							onConfirmClicked(dialog, activity);
						}
					});
					okButton.setTextColor(_data.confirmTextColor);
					okButton.getBackground().setColorFilter(_data.confirmButtonColor,
							PorterDuff.Mode.MULTIPLY);
				} else {
					okButton.setVisibility(Button.GONE);
				}

				dialog.setCancelable(true);
				if (_data.cancelString != null
						&& _data.cancelString.trim().length() > 0) {
					cancelButton.setText(_data.cancelString.trim());
					cancelButton.setOnClickListener(new Button.OnClickListener() {
						@Override
						public void onClick(View v) {
							onCancelClicked(dialog, activity);
						}
					});
					cancelButton.setTextColor(_data.cancelTextColor);
					if (_data.cancelDelay > 0) {
						dialog.setCancelable(false);
						cancelButton.setVisibility(Button.INVISIBLE);
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Handler delayHandler = new Handler();
								delayHandler.postDelayed(new Runnable() {
									@Override
									public void run() {
										if (dialog != null) {
											dialog.setCancelable(true);
											cancelButton.setVisibility(Button.VISIBLE);
										}
									}
								}, _data.cancelDelay);
							}
						});
					}
					cancelButton.getBackground().setColorFilter(
							_data.cancelButtonColor, PorterDuff.Mode.SRC_ATOP);
				} else {
					cancelButton.setVisibility(Button.INVISIBLE);
					dialog.setCancelable(false);
				}
				
				dialog.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						onCancelClicked(dialog, activity);
					}
				});
				
				if (_data.expiresUnixTimestamp != 0L) {

					timerText.setText(expiryDateString());
					timerText.setTextColor(_data.expirationColor);
					timerText.setGravity(Gravity.CENTER);
					timerText.setPadding(0, 0, 0, 0);
				} else {
					timerText.setVisibility(TextView.GONE);
				}

				Display disp = activity.getWindowManager().getDefaultDisplay();

				dialog.getWindow().setLayout((int) (disp.getWidth() * 0.8),
						(int) (disp.getHeight() * 0.8));
				dialog.show();
				_dialog = dialog;
				_isVisible = true;
				if (messageText.getVisibility() != TextView.GONE) {
					float ptSize = 23f;
					if (activity != null) {
						ptSize = getFontSize(activity, ptSize, 20);
					}
					messageText
							.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ptSize);
					messageText.setPadding(0, 0, 0, 0);
				}
				_messageDelegate.didPresentMessage(activity,
						YRDMessagePreseneter.this, _data);
			}
		});
	}

	/**
	 * Used to render native system UI dialog
	 * 
	 * @param activity
	 */
	private void showSystemStyle(final Activity activity) {
		if (activity == null || activity.isFinishing())
			return;

		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(_data.titleString);

		// The text from long messages is handled by the expires wrapper in the
		// constructor
		// if( MessageStyle.LONG != data.style ) {
		builder.setMessage(_data.textString);
		// }

		String confirmText = _data.confirmString;
		if (_data.confirmString != null && 0 != _data.confirmString.length()) {
			builder.setPositiveButton(confirmText, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					onConfirmClicked(dialog, activity);
				}
			});
		}

		String cancelText = _data.cancelString;
		if (null != _data.cancelString && 0 != _data.cancelString.length()) {
			builder.setNegativeButton(cancelText, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					onCancelClicked(dialog, activity);
				}
			});
			builder.setCancelable(true);
		} else {
			builder.setCancelable(false);
		}

		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				onCancelClicked(dialog, activity);
			}
		});

		View v = null;
		if (_data.expiresUnixTimestamp != 0L) {

			LinearLayout linearLayout = new LinearLayout(activity);
			linearLayout.setPadding(0, 0, 0, 0);
			linearLayout.setOrientation(LinearLayout.VERTICAL);

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT, 1f);
			lp.setMargins(0, 0, 0, 0);
			linearLayout.setLayoutParams(lp);

			TextView expiresView = new TextView(activity);
			expiresView.setText(expiryDateString());
			expiresView.setTextColor(_data.expirationColor);
			expiresView.setGravity(Gravity.CENTER);
			expiresView.setPadding(0, 0, 0, 0);
			linearLayout.addView(expiresView);

			v = linearLayout;
		}

		final View finalView = v;

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				final AlertDialog dialog = builder.create();

				if (null != finalView) {
					dialog.setView(finalView, 0, 0, 0, 0);
				}

				if (_data.cancelDelay > 0) {
					dialog.setCancelable(false);
					final Button negativeButton = dialog
							.getButton(Dialog.BUTTON_NEGATIVE);
					negativeButton.setVisibility(Button.INVISIBLE);

					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Handler delayHandler = new Handler();
							delayHandler.postDelayed(new Runnable() {
								@Override
								public void run() {
									if (dialog != null) {
										dialog.setCancelable(true);
										negativeButton
												.setVisibility(Button.VISIBLE);
									}
								}
							}, _data.cancelDelay);
						}
					});
				}

				dialog.show();
				_dialog = dialog;
				_isVisible = true;
				TextView msg = (TextView) dialog
						.findViewById(android.R.id.message);
				if (msg != null) {
					float size = 23f;
					if (activity != null) {
						size = getFontSize(activity, size, 20);
					}
					msg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
					msg.setPadding(0, 0, 0, 0);
				}

				_messageDelegate.didPresentMessage(activity,
						YRDMessagePreseneter.this, _data);
			}
		});
	}

	private void onConfirmClicked(DialogInterface dialog, Activity activity) {
		YRDMessageActionType actionType = _data.actionType;
		String action = _data.action;
		switch (actionType) {
		case EXTERNAL_BROWSER:
		case INTERNAL_BROWSER:
			_messageDelegate.willDismissMessage(activity, this, _data,
					actionType, action);
			break;
		case APP:
			YRDAppActionParser parser = YRDAppActionParser.parseAction(action);
			if (parser == null || parser.getActionType() == null
					|| parser.getActionType() == YRDAppActionType.EMPTY) {
				actionType = null;
				action = null;
			}
			_messageDelegate.willDismissMessage(activity, this, _data,
					actionType, action);
			reportClick();
			break;
		default:
			_messageDelegate.willDismissMessage(activity, this, _data,
					actionType, action);
			reportClick();
			break;
		}

		dialog.dismiss();
		_isVisible = false;

		_messageDelegate.didDismissMessage(activity, this, _data, actionType,
				action);
	}

	private void onCancelClicked(DialogInterface dialog, Activity activity) {
		if (_data.forceRefresh)
			_data.forceRefresh = false;
		_messageDelegate.willDismissMessage(activity, this, _data, null, null);
		dialog.dismiss();
		_isVisible = false;
		reportView();
		_messageDelegate.didDismissMessage(activity, this, _data, null, null);
	}

	/**
	 * Calcualtes proper expiration string based on expiration timestamp
	 * 
	 * @return formatted expiration string
	 */
	private String expiryDateString() {

		final int S_PER_HOUR = 60 * 60;
		final int S_PER_DAY = S_PER_HOUR * 24;

		int secondsLeft = (int) (_data.expiresUnixTimestamp - (System
				.currentTimeMillis() / 1000L));

		StringBuilder stringBuilder = new StringBuilder("Expires in ");

		int days = secondsLeft / S_PER_DAY;
		final String separator = ", ";
		boolean useSeparator = false;

		if (1 < days) {
			secondsLeft -= days * S_PER_DAY;
			stringBuilder.append(days).append(" days");
			useSeparator = true;
		} else if (1 == days) {
			secondsLeft -= days * S_PER_DAY;
			stringBuilder.append(days).append(" day");
			useSeparator = true;
		}

		int hours = secondsLeft / S_PER_HOUR;
		if (0 < hours) {
			secondsLeft -= hours * S_PER_HOUR;
		}

		if (1 < hours) {
			if (useSeparator)
				stringBuilder.append(separator);
			stringBuilder.append(hours).append(" hours!");
		} else if (1 == hours) {
			if (useSeparator)
				stringBuilder.append(separator);
			stringBuilder.append(hours).append(" hour!");
		} else if (0 >= days) {
			return "Expires soon!";
		}

		return stringBuilder.toString();
	}

	/**
	 * Sends view report to server
	 */
	protected void reportView() {
		_hasReported = true;
		YRDMessageReportService reportService = new YRDMessageReportService();
		reportService.reportAtURI(_cxt, _data.viewURI);
	}

	/**
	 * Sends click report to server
	 */
	protected void reportClick() {
		_hasReported = true;
		YRDMessageReportService reportService = new YRDMessageReportService();
		reportService.reportAtURI(_cxt, _data.clickURI);
	}

	public boolean hasReportedAction() {
		return _hasReported;
	}

	public boolean hasShown() {
		return _hasShown;
	}

	public boolean isVisible() {
		return _isVisible;
	}

	/**
	 * getFontSize
	 * 
	 * @param activity
	 *            - For resources
	 * @param targetSize
	 *            - arbitrary number to get scaling right. Doesn't actually
	 *            represent a font size or anything
	 * @param maxSize
	 *            - Max return value (font size)
	 * @return return - value is a calculated font size
	 */
	private static int getFontSize(Activity activity, float targetSize,
			int maxSize) {

		DisplayMetrics dMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dMetrics);

		// lets try to get them back a font size realtive to the pixel width of
		// the screen
		final float WIDE = activity.getResources().getDisplayMetrics().widthPixels;
		int valueWide = (int) (WIDE / targetSize / (dMetrics.scaledDensity));
		YRDLog.i(YRDMessagePreseneter.class, "GetFontSize: " + valueWide
				+ ", MAX: " + maxSize);
		valueWide = (valueWide > maxSize) ? (maxSize) : (valueWide);
		return valueWide;
	}

	public YRDMessage getData() {
		return _data;
	}

}
