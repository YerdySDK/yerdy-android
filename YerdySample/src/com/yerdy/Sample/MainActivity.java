package com.yerdy.Sample;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.yerdy.Sample.CurrencyManager.CURRENCIES;
import com.yerdy.services.Yerdy;
import com.yerdy.services.YerdyDelegate;
import com.yerdy.services.YerdyMessageDelegate;
import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.logging.YRDLogLevel;
import com.yerdy.services.messaging.YRDInAppPurchase;
import com.yerdy.services.messaging.YRDItemPurchase;
import com.yerdy.services.messaging.YRDReward;
import com.yerdy.services.messaging.YRDRewardItem;
import com.yerdy.services.purchases.YRDPurchaseGoogle;

/*
 * @author Chis
 * 
 * This a sample application intended to show different potential use cases for leveraging with your Yerdy integration
 */

public class MainActivity extends Activity implements YerdyDelegate,
		YerdyMessageDelegate {

	// You can get your publisher key from the Yerdy Dashboard
	private final String PUBLISHER_KEY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	Random randoms = new Random();
	private final String adNetworkName = "FakeAd";
	private CurrencyManager _currencyManager;

	// For tracking portions of bank screen dialog
	private TextView _gold = null;
	private TextView _sivler = null;
	private TextView _bronze = null;
	private TextView _diamonds = null;
	private TextView _pearls = null;
	private TextView _rubies = null;
	private EditText _editGold = null;
	private EditText _editSivler = null;
	private EditText _editBronze = null;
	private EditText _editDiamonds = null;
	private EditText _editPearls = null;
	private EditText _editRubies = null;
	private CheckBox _onSale = null;
	private EditText _itemName = null;

	/*
	 * Launch and setup Yerdy
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		_currencyManager = new CurrencyManager(this);

		YRDLog.SetLogLevel(YRDLogLevel.YRDLogVerbose);
		Yerdy.getInstance().configureMessageDelegate(this);
		Yerdy.getInstance().configureCurrencies(
				this,
				new String[] { CURRENCIES.GOLD.getName(),
						CURRENCIES.SILVER.getName(),
						CURRENCIES.BRONZE.getName(),
						CURRENCIES.DIAMONDS.getName(),
						CURRENCIES.PEARLS.getName(),
						CURRENCIES.RUBIES.getName() });
		Yerdy.getInstance().startWithPublisherKey(this, PUBLISHER_KEY);
		Yerdy.getInstance().logScreenVisit("main");
	}

	/*
	 * Reports logged event to Yerdy (RGB buttons)
	 */
	public void onLogButtonPressed(View v) {
		String label = getButtonLabel(v);
		if (label != null) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("title", label);
			Yerdy.getInstance().logEvent("buttonPressed", params);
		}
	}

	/*
	 * Reports level progressions ("level-1" - "level-9" buttons)
	 */
	public void onPlayerProgression(View v) {
		String label = getButtonLabel(v);
		if (label != null) {
			Yerdy.getInstance().playerProgression("level", label);
		}
	}

	/*
	 * Emulation of requesting ads from an ad network that has a only partial
	 * fill rate
	 */
	public void onShowFakeAd(View v) {
		Yerdy.getInstance().logAdRequest(adNetworkName);
		if (randoms.nextFloat() > 0.25) {
			showAlert(
					"FakeAd",
					"This is a fake test ad, for the purposes of testing ad requests/fills tracking");
			Yerdy.getInstance().logAdFill(adNetworkName);
		} else {
			// Ad Failed
		}
	}

	/*
	 * Checks for and displays pull messages if they exist
	 */
	public void onGetMessageClick(View v) {
		boolean hasMessage = Yerdy.getInstance().isMessageAvailiable("test");
		if (!hasMessage) {
			showAlert("No Messages",
					"No messages are currently availiable for the selected placement");
		} else {
			Yerdy.getInstance().showMessage(this, "test");
		}
	}

	/*
	 * Shows bank screen as custom dialog, instead of separate activity/fragment
	 */
	@SuppressWarnings("deprecation")
	public void onShowBank(View v) {
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		final ColorDrawable color = new ColorDrawable(Color.TRANSPARENT);
		dialog.getWindow().setBackgroundDrawable(color);
		dialog.setContentView(R.layout.activity_bank);
		dialog.setCancelable(true);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				Yerdy.getInstance().logScreenVisit("main");
			}
		});

		_gold = (TextView) dialog.findViewById(R.id.bank_gold);
		_sivler = (TextView) dialog.findViewById(R.id.bank_silver);
		_bronze = (TextView) dialog.findViewById(R.id.bank_bronze);
		_diamonds = (TextView) dialog.findViewById(R.id.bank_diamonds);
		_pearls = (TextView) dialog.findViewById(R.id.bank_pearls);
		_rubies = (TextView) dialog.findViewById(R.id.bank_rubies);

		_editGold = (EditText) dialog.findViewById(R.id.input_gold);
		_editSivler = (EditText) dialog.findViewById(R.id.input_silver);
		_editBronze = (EditText) dialog.findViewById(R.id.input_bronze);
		_editDiamonds = (EditText) dialog.findViewById(R.id.input_diamonds);
		_editPearls = (EditText) dialog.findViewById(R.id.input_pearls);
		_editRubies = (EditText) dialog.findViewById(R.id.input_rubies);

		_onSale = (CheckBox) dialog.findViewById(R.id.is_on_sale);
		_itemName = (EditText) dialog.findViewById(R.id.input_item_name);

		Button btn = (Button) dialog.findViewById(R.id.on_earn);
		btn.setOnClickListener(onEarnCurrency);
		btn = (Button) dialog.findViewById(R.id.on_buy_item);
		btn.setOnClickListener(onBuyItem);
		btn = (Button) dialog.findViewById(R.id.on_buy_gold);
		btn.setOnClickListener(onBuyGold);
		btn = (Button) dialog.findViewById(R.id.on_buy_jewels);
		btn.setOnClickListener(onBuyJewels);

		Display disp = this.getWindowManager().getDefaultDisplay();

		dialog.getWindow().setLayout((int) (disp.getWidth() * 0.9),
				LayoutParams.WRAP_CONTENT);

		dialog.show();
		updateShownBalance();
		Yerdy.getInstance().logScreenVisit("bank");
	}

	/*
	 * Update text labels in bank screen based on persisted values
	 */
	private void updateShownBalance() {
		if (_gold != null) {
			_gold.setText(Integer.toString(_currencyManager.getGold()));
			_sivler.setText(Integer.toString(_currencyManager.getSilver()));
			_bronze.setText(Integer.toString(_currencyManager.getBronze()));
			_diamonds.setText(Integer.toString(_currencyManager.getDiamonds()));
			_pearls.setText(Integer.toString(_currencyManager.getPearls()));
			_rubies.setText(Integer.toString(_currencyManager.getRubies()));
		}
	}

	/*
	 * Handles Bank Screen Earn click Parsing the currencies from the text
	 * inputs it gives them to the currency manager for persistence and reports
	 * the earn to the Yerdy SDK
	 */
	View.OnClickListener onEarnCurrency = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int g = 0;
			int s = 0;
			int b = 0;
			int d = 0;
			int p = 0;
			int r = 0;

			if (_editGold != null) {
				g = parseInt(_editGold.getText().toString());
				s = parseInt(_editSivler.getText().toString());
				b = parseInt(_editBronze.getText().toString());
				d = parseInt(_editDiamonds.getText().toString());
				p = parseInt(_editPearls.getText().toString());
				r = parseInt(_editRubies.getText().toString());
			}

			_currencyManager.add(g, s, b, d, p, r);

			Map<String, Integer> currencies = new HashMap<String, Integer>();
			currencies.put(CURRENCIES.GOLD.getName(), g);
			currencies.put(CURRENCIES.SILVER.getName(), s);
			currencies.put(CURRENCIES.BRONZE.getName(), b);
			currencies.put(CURRENCIES.DIAMONDS.getName(), d);
			currencies.put(CURRENCIES.PEARLS.getName(), p);
			currencies.put(CURRENCIES.RUBIES.getName(), r);

			Yerdy.getInstance().earnedCurrency(currencies);

			updateShownBalance();
		}
	};

	/*
	 * Handles Bank Screen Buy click Parsing the currencies and name from inputs
	 * to emulate a in game purchase and reports it to the Yerdy SDK
	 */
	View.OnClickListener onBuyItem = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String itemName = _itemName.getText().toString();

			if (itemName == null || itemName.trim().length() <= 0) {
				showAlert("", "Please enter and item name");
				return;
			}

			int g = 0;
			int s = 0;
			int b = 0;
			int d = 0;
			int p = 0;
			int r = 0;

			if (_editGold != null) {
				g = parseInt(_editGold.getText().toString());
				s = parseInt(_editSivler.getText().toString());
				b = parseInt(_editBronze.getText().toString());
				d = parseInt(_editDiamonds.getText().toString());
				p = parseInt(_editPearls.getText().toString());
				r = parseInt(_editRubies.getText().toString());
			}

			if (!_currencyManager.subtract(g, s, b, d, p, r)) {
				showAlert("", "Insufficient Funds");
			} else {

				Map<String, Integer> currencies = new HashMap<String, Integer>();
				currencies.put(CURRENCIES.GOLD.getName(), g);
				currencies.put(CURRENCIES.SILVER.getName(), s);
				currencies.put(CURRENCIES.BRONZE.getName(), b);
				currencies.put(CURRENCIES.DIAMONDS.getName(), d);
				currencies.put(CURRENCIES.PEARLS.getName(), p);
				currencies.put(CURRENCIES.RUBIES.getName(), r);

				Yerdy.getInstance().purchasedItem(itemName, currencies,
						_onSale.isChecked());
				updateShownBalance();
			}
		}
	};

	/*
	 * Handles Bank Screen Buy Gold click awarding 50 gold and reporting it to
	 * the Yerdy SDK to emulate an IAP transaction
	 */
	View.OnClickListener onBuyGold = new View.OnClickListener() {
		public void onClick(View v) {
			int goldAmount = 50;
			_currencyManager.add(goldAmount, 0, 0, 0, 0, 0);

			YRDPurchaseGoogle purchase = new YRDPurchaseGoogle(
					"com.yerdy.Sample.Gold", "$1.99", "Insert Receipt",
					"Insert Signature", false);
			Yerdy.getInstance().purchasedInApp(purchase,
					CURRENCIES.GOLD.getName(), goldAmount);

			updateShownBalance();
		}
	};

	/*
	 * Handles Bank Screen Buy Jewels click awarding 50 of each jewel and
	 * reporting it to the Yerdy SDK to emulate an IAP transaction
	 */
	View.OnClickListener onBuyJewels = new View.OnClickListener() {
		public void onClick(View v) {
			int jewelAmount = 50;
			_currencyManager
					.add(0, 0, 0, jewelAmount, jewelAmount, jewelAmount);

			YRDPurchaseGoogle purchase = new YRDPurchaseGoogle(
					"com.yerdy.Sample.JewelPack", "$4.99", "Insert Receipt",
					"Insert Signature", false);

			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put(CURRENCIES.DIAMONDS.getName(), jewelAmount);
			map.put(CURRENCIES.PEARLS.getName(), jewelAmount);
			map.put(CURRENCIES.RUBIES.getName(), jewelAmount);

			Yerdy.getInstance().purchasedInApp(purchase, map);
			updateShownBalance();
		}
	};

	/*
	 * onPause events need to be reported to Yerdy
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Yerdy.getInstance().onPause();
	}

	/*
	 * onResume events need to be reported to Yerdy
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Yerdy.getInstance().onResume(this);
	}

	/*
	 * onWindowFocusChanged need to be reported to Yerdy
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		Yerdy.getInstance().onWindowFocusChanged(hasFocus, this);
	}

	/*
	 * Yerdy Message Delegate handlers
	 */
	@Override
	public void willPresentMessageForPlacement(String placement) {
		Log.i("MessageDelegate", "Will presenet message");
	}

	@Override
	public void didPresentMessageForPlacement(String placement) {
		Log.i("MessageDelegate", "Did presenet message");
	}

	@Override
	public void willDismissMessageForPlacement(String placement) {
		Log.i("MessageDelegate", "Will dismiss message");
	}

	@Override
	public void didDismissMessageForPlacement(String placement) {
		Log.i("MessageDelegate", "Did dismiss message");
	}

	@Override
	public void handleInAppPurchase(YRDInAppPurchase purchase) {
		showAlert("IAP", purchase.getProductIdentifier());
	}

	@Override
	public void handleItemPurchase(YRDItemPurchase purchase) {
		showAlert("Virtual", purchase.getItem());
	}

	@Override
	public void handleReward(YRDReward reward) {
		for (YRDRewardItem item : reward.getRewards()) {
			String itemName = item.getName();
			int quantity = item.getAmount();

			CURRENCIES currency = CURRENCIES.match(itemName);

			if (currency != null) {
				switch (currency) {
				case GOLD:
					_currencyManager.add(quantity, 0, 0, 0, 0, 0);
					Yerdy.getInstance().earnedCurrency(CURRENCIES.GOLD.getName(), quantity);
					break;
				case SILVER:
					_currencyManager.add(0, quantity, 0, 0, 0, 0);
					Yerdy.getInstance().earnedCurrency(CURRENCIES.SILVER.getName(), quantity);
					break;
				case BRONZE:
					_currencyManager.add(0, 0, quantity, 0, 0, 0);
					Yerdy.getInstance().earnedCurrency(CURRENCIES.BRONZE.getName(), quantity);
					break;
				case DIAMONDS:
					_currencyManager.add(0, 0, 0, quantity, 0, 0);
					Yerdy.getInstance().earnedCurrency(CURRENCIES.BRONZE.getName(), quantity);
					break;
				case PEARLS:
					_currencyManager.add(0, 0, 0, 0, quantity, 0);
					Yerdy.getInstance().earnedCurrency(CURRENCIES.BRONZE.getName(), quantity);
					break;
				case RUBIES:
					_currencyManager.add(0, 0, 0, 0, 0, quantity);
					Yerdy.getInstance().earnedCurrency(CURRENCIES.BRONZE.getName(), quantity);
					break;
				}
			} else {
				// Assume it was an item and reward item as needed
				// itemManager.give(itemName, quantity);
			}

			showAlert("Reward", reward.getRewards().get(0).getName());
		}
	}

	@Override
	public void handleNavigation(String screen) {
		showAlert("Navigation", screen);
	}

	/*
	 * Yerdy Delegate Handler
	 */
	@Override
	public void yerdyConnected(boolean success) {
		Log.i("YerdyConnected", "Yerdy conneciton status changed");
	}

	/*
	 * Util: show alert
	 */
	private void showAlert(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.show();
	}

	/*
	 * Util: get label off of a button
	 */
	private String getButtonLabel(View v) {
		if (v instanceof Button) {
			return ((Button) v).getText().toString();
		} else {
			return null;
		}
	}

	/*
	 * Util: parse integers without replication of try catch
	 */
	private int parseInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
