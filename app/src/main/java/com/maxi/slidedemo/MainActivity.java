package com.maxi.slidedemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.maxi.slidedemo.observablescrollview.CacheFragmentStatePagerAdapter;
import com.maxi.slidedemo.observablescrollview.ScrollUtils;
import com.maxi.slidedemo.observablescrollview.Scrollable;
import com.maxi.slidedemo.observablescrollview.ui.BaseActivity;
import com.maxi.slidedemo.observablescrollview.viewpages.FlexibleSpaceWithImageBaseFragment;
import com.maxi.slidedemo.observablescrollview.viewpages.FlexibleSpaceWithImageRecyclerViewFragment;
import com.maxi.slidedemo.observablescrollview.viewpages.SlidingTabLayout;
import com.maxi.slidedemo.observablescrollview.widget.TopOverLay;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

public class MainActivity extends BaseActivity implements LocationListener {

    protected static final float MAX_TEXT_SCALE_DELTA = 0.3f;
    private static final String MAP_URL = "http://www.google.cn/maps";

    private WebView mWebView;
    private TextView returnView;
    private TextView titleView;
    private TopOverLay overlayView;
    private ViewPager mPager;
    private NavigationAdapter mPagerAdapter;
    private SlidingTabLayout mSlidingTabLayout;
    private int mFlexibleSpaceHeight;
    private int mTabAndAppBarHeight;
    private int mTabHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        overlayView = (TopOverLay) findViewById(R.id.overlay);

        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mFlexibleSpaceHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mTabHeight = getResources().getDimensionPixelSize(R.dimen.tab_height);
        mTabAndAppBarHeight = getResources().getDimensionPixelSize(R.dimen.tab_appbar_height);

        returnView = (TextView) findViewById(R.id.return_tv);
        returnView.setText("返回");
        returnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"return back",Toast.LENGTH_SHORT).show();
            }
        });
        titleView = (TextView) findViewById(R.id.title);
        titleView.setText("我的行程");

        getLocation();
        setupWebView();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.accent));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mPager);

        // Initialize the first Fragment's state when layout is completed.
        ScrollUtils.addOnGlobalLayoutListener(mSlidingTabLayout, new Runnable() {
            @Override
            public void run() {
                translateTab(0, false);
            }
        });
    }

    /**
     * Called by children Fragments when their scrollY are changed.
     * They all call this method even when they are inactive
     * but this Activity should listen only the active child,
     * so each Fragments will pass themselves for Activity to check if they are active.
     *
     * @param scrollY scroll position of Scrollable
     * @param s       caller Scrollable view
     */
    public void onScrollChanged(int scrollY, Scrollable s) {
        FlexibleSpaceWithImageBaseFragment fragment =
                (FlexibleSpaceWithImageBaseFragment) mPagerAdapter.getItemAt(mPager.getCurrentItem());
        if (fragment == null) {
            return;
        }
        View view = fragment.getView();
        if (view == null) {
            return;
        }
        Scrollable scrollable = (Scrollable) view.findViewById(R.id.scroll);
        if (scrollable == null) {
            return;
        }
        if (scrollable == s) {
            // This method is called by not only the current fragment but also other fragments
            // when their scrollY is changed.
            // So we need to check the caller(S) is the current fragment.
            int adjustedScrollY = Math.min(scrollY, mFlexibleSpaceHeight - mTabAndAppBarHeight);

            translateTab(adjustedScrollY, false);
            propagateScroll(adjustedScrollY);
        }
    }

    private void translateTab(int scrollY, boolean animated) {
        int flexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        int tabHeight = getResources().getDimensionPixelSize(R.dimen.tab_appbar_height);
//        TextView titleView = (TextView) findViewById(R.id.title);

        // Translate overlay and image
        float flexibleRange = flexibleSpaceImageHeight - mTabAndAppBarHeight;
        int minOverlayTransitionY = tabHeight - overlayView.getHeight();
        ViewHelper.setTranslationY(overlayView, ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0));
        ViewHelper.setTranslationY(mWebView, ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0));
//        ViewHelper.setTranslationY(mWebView, ScrollUtils.getFloat(-scrollY / 2, minOverlayTransitionY, 0));

        // Change alpha of overlay
        ViewHelper.setAlpha(overlayView, ScrollUtils.getFloat((float) scrollY / flexibleRange, 0, 1));

        // Scale title text
//        float scale = 1 + ScrollUtils.getFloat((flexibleRange - scrollY - tabHeight) / flexibleRange, 0, MAX_TEXT_SCALE_DELTA);
//        setPivotXToTitle(titleView);
//        ViewHelper.setPivotY(titleView, 0);
//        ViewHelper.setScaleX(titleView, scale);
//        ViewHelper.setScaleY(titleView, scale);

        // Translate title text
//        int maxTitleTranslationY = flexibleSpaceImageHeight - tabHeight - getActionBarSize();
//        int titleTranslationY = maxTitleTranslationY - scrollY;
//        ViewHelper.setTranslationY(titleView, titleTranslationY);

        // If tabs are moving, cancel it to start a new animation.
        ViewPropertyAnimator.animate(mSlidingTabLayout).cancel();
        // Tabs will move between the top of the screen to the bottom of the image.
        float translationY = ScrollUtils.getFloat(-scrollY + mFlexibleSpaceHeight - mTabHeight, 0, mFlexibleSpaceHeight - mTabHeight);
        if (animated) {
            // Animation will be invoked only when the current tab is changed.
            ViewPropertyAnimator.animate(mSlidingTabLayout)
                    .translationY(translationY)
                    .setDuration(200)
                    .start();
        } else {
            // When Fragments' scroll, translate tabs immediately (without animation).
            ViewHelper.setTranslationY(mSlidingTabLayout, translationY);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setPivotXToTitle(View view) {
        final TextView mTitleView = (TextView) view.findViewById(R.id.title);
        Configuration config = getResources().getConfiguration();
        if (Build.VERSION_CODES.JELLY_BEAN_MR1 <= Build.VERSION.SDK_INT
                && config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            ViewHelper.setPivotX(mTitleView, view.findViewById(android.R.id.content).getWidth());
        } else {
            ViewHelper.setPivotX(mTitleView, 0);
        }
    }

    private void propagateScroll(int scrollY) {
        // Set scrollY for the fragments that are not created yet
        mPagerAdapter.setScrollY(scrollY);

        // Set scrollY for the active fragments
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            // Skip current item
            if (i == mPager.getCurrentItem()) {
                continue;
            }

            // Skip destroyed or not created item
            FlexibleSpaceWithImageBaseFragment f =
                    (FlexibleSpaceWithImageBaseFragment) mPagerAdapter.getItemAt(i);
            if (f == null) {
                continue;
            }

            View view = f.getView();
            if (view == null) {
                continue;
            }
            f.setScrollY(scrollY, mFlexibleSpaceHeight);
            f.updateFlexibleSpace(scrollY);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mostRecentLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * This adapter provides three types of fragments as an example.
     * {@linkplain #createItem(int)} should be modified if you use this example for your app.
     */
    private static class NavigationAdapter extends CacheFragmentStatePagerAdapter {

        private static final String[] TITLES = new String[]{"第一天","第二天","第三天","第四天","第五天","第六天","第七天"};

        private int mScrollY;

        public NavigationAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setScrollY(int scrollY) {
            mScrollY = scrollY;
        }

        @Override
        protected Fragment createItem(int position) {
            FlexibleSpaceWithImageBaseFragment f = new FlexibleSpaceWithImageRecyclerViewFragment();
            f.setArguments(mScrollY);
            return f;
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }
    }

    private Location mostRecentLocation;
    private void getLocation() {
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(criteria, true);
        //In order to make sure the device is getting the location, request updates.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 1, 0, this);
        mostRecentLocation = locationManager.getLastKnownLocation(provider);
    }

    private String centerURL = "";
    /** Sets up the WebView object and loads the URL of the page **/
    private void setupWebView(){
        if(mostRecentLocation != null){
            centerURL = "javascript:centerAt(" +
                    mostRecentLocation.getLatitude() + "," +
                    mostRecentLocation.getLongitude()+ ")";
        }
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        //Wait for the page to load then send the location information

        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url){
                if(!centerURL.equals("")) {
                    mWebView.loadUrl(centerURL);
                }
            }
        });
        mWebView.loadUrl(MAP_URL);
        /** Allows JavaScript calls to access application resources **/
        mWebView.addJavascriptInterface(new JavaScriptInterface(), "android");
    }
    /** Sets up the interface for getting access to Latitude and Longitude data from device
     **/
    private class JavaScriptInterface {
        public double getLatitude(){
            return mostRecentLocation.getLatitude();
        }
        public double getLongitude(){
            return mostRecentLocation.getLongitude();
        }
    }
}
