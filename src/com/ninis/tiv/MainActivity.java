package com.ninis.tiv;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.ninis.tiv.TivApplication.TrackerName;
import com.parse.Parse;
import com.parse.PushService;

import android.support.v7.app.ActionBarActivity;
import android.text.style.EasyEditSpan;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	private static final String SCREEN_NAME = "MainActivity";

	private WebView mWebView;
	private ProgressBar mProgressBar;

	private ImageView mIvBottomBtnPrev;
	private ImageView mIvBottomBtnNext;

	private long mLastBackPressTime = 0l;

	private Tracker mTrackerMain;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initLayout();

		setGoogleAnalytics();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private void initLayout() {
		mWebView = (WebView) findViewById(R.id.wv_mainbody);
		settingWebview();

		mProgressBar = (ProgressBar) findViewById(R.id.pb_for_webview);
		mProgressBar.setProgressDrawable(getResources().getDrawable(
				R.drawable.custom_progress));

		findViewById(R.id.iv_footer_btn_home).setOnClickListener(this);
		findViewById(R.id.iv_footer_btn_refresh).setOnClickListener(this);
		mIvBottomBtnNext = (ImageView) findViewById(R.id.iv_footer_btn_next);
		mIvBottomBtnNext.setOnClickListener(this);
		mIvBottomBtnNext.setEnabled(false);
		mIvBottomBtnPrev = (ImageView) findViewById(R.id.iv_footer_btn_prev);
		mIvBottomBtnPrev.setOnClickListener(this);
		mIvBottomBtnPrev.setEnabled(false);
	}

	private void settingWebview() {
		if (mWebView == null)
			return;

		mWebView.getSettings().setJavaScriptEnabled(true);

		mWebView.loadUrl(Defines.URL_MAIN);

		mWebView.setWebViewClient(new WebClient());
		mWebView.setWebChromeClient(new ChromeClient());
	}

	@Override
	public void onBackPressed() {
		if (!mWebView.canGoBack()) {
			finishActivity();
		} else {
			mWebView.goBack();
		}
	}

	/**
	 * webview client
	 * 
	 * @author gil-yongbag
	 * 
	 */
	private class WebClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);

			if (mProgressBar != null)
				mProgressBar.setVisibility(View.VISIBLE);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);

			if (mProgressBar != null)
				mProgressBar.setVisibility(View.GONE);

			/**
			 * 네비게이션 버튼 설정
			 */
			if (mWebView.canGoBack()) {
				mIvBottomBtnPrev.setEnabled(true);
			} else {
				mIvBottomBtnPrev.setEnabled(false);
			}
			if (mWebView.canGoForward()) {
				mIvBottomBtnNext.setEnabled(true);
			} else {
				mIvBottomBtnNext.setEnabled(false);
			}
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);

			Toast.makeText(MainActivity.this,
					getString(R.string.web_loading_err), Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * chrome client
	 * 
	 * @author gil-yongbag
	 * 
	 */
	private class ChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);

			if (mProgressBar != null)
				mProgressBar.setProgress(newProgress);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_footer_btn_home: {
			if (mWebView != null) {
				mWebView.loadUrl(Defines.URL_MAIN);
				mWebView.clearHistory();
			}

			if (mTrackerMain != null) {
				mTrackerMain.send(new HitBuilders.EventBuilder()
						.setCategory("Bottom Menu").setAction("Click")
						.setLabel("Home").setValue(1).build());
			}
		}
			break;
		case R.id.iv_footer_btn_refresh: {
			if (mWebView != null) {
				mWebView.reload();
			}

			if (mTrackerMain != null) {
				mTrackerMain.send(new HitBuilders.EventBuilder()
						.setCategory("Bottom Menu").setAction("Click")
						.setLabel("Refresh").setValue(2).build());
			}
		}
			break;
		case R.id.iv_footer_btn_next: {
			if (mWebView != null && mWebView.canGoForward()) {
				mWebView.goForward();
			}

			if (mTrackerMain != null) {
				mTrackerMain.send(new HitBuilders.EventBuilder()
						.setCategory("Bottom Menu").setAction("Click")
						.setLabel("Next").setValue(3).build());
			}
		}
			break;
		case R.id.iv_footer_btn_prev: {
			if (mWebView != null && mWebView.canGoBack()) {
				mWebView.goBack();
			}

			if (mTrackerMain != null) {
				mTrackerMain.send(new HitBuilders.EventBuilder()
						.setCategory("Bottom Menu").setAction("Click")
						.setLabel("Prev").setValue(4).build());
			}
		}
			break;
		}
	}

	protected void finishActivity() {
		long lNow = System.currentTimeMillis();

		if (lNow - mLastBackPressTime < 1500) {
			finish();
			return;
		}
		mLastBackPressTime = lNow;
		Toast.makeText(MainActivity.this,
				getString(R.string.message_exit_check), Toast.LENGTH_SHORT)
				.show();
	}

	private void setGoogleAnalytics() {
		try {
			// Get tracker.
			mTrackerMain = ((TivApplication) getApplicationContext())
					.getTracker(TrackerName.APP_TRACKER);

			// Set screen name.
			mTrackerMain.setScreenName(SCREEN_NAME);

			// Send a screen view.
			mTrackerMain.send(new HitBuilders.AppViewBuilder().build());
		} catch (Exception e) {

		}
	}
}
