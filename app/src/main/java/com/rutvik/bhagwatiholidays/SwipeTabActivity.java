package com.rutvik.bhagwatiholidays;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.*;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;


import java.util.ArrayList;
import java.util.List;

import bhfragment.FragmentAirTickets;
import bhfragment.FragmentDrawer;
import bhfragment.FragmentHolidays;
import bhfragment.FragmentHotelPackages;
import bhfragment.FragmentHotels;
import bhfragment.FragmentVisa;

public class SwipeTabActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {

    private Toolbar mToolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ProgressDialog mProgressDialog;
    private FragmentDrawer drawerFragment;

    private App app;

    public com.nostra13.universalimageloader.core.ImageLoader imageLoader;
    public DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_tab);

        app = (App) getApplication();

        app.trackScreenView("Swipe Tab Activity");

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setLogo(R.drawable.bh_icon);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        //tabLayout.setBackgroundColor(Color.parseColor("#2973bd"));
        //tabLayout.setTabTextColors(Color.parseColor("#a9c7e5"), Color.BLACK);
        tabLayout.setupWithViewPager(viewPager);

        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);


        imageLoader = ImageLoader.getInstance();

        options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.loading_image)
                .cacheInMemory()
                .cacheOnDisc()
                .displayer(new RoundedBitmapDisplayer(5))
                .build();

        //imageLoader.displayImage (app.getUser().getProfilePic(), (android.widget.ImageView) drawerFragment.getView().findViewById(R.id.iv_userImage), options);


        //((TextView) drawerFragment.getView().findViewById(R.id.tv_userName)).setText(app.getUser().getName());


    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentAirTickets(), "AIR TICKETS");
        adapter.addFragment(new FragmentHotels(), "HOTELS");
        adapter.addFragment(new FragmentHotelPackages(), "HOLIDAYS");
        adapter.addFragment(new FragmentVisa(), "VISA");
        viewPager.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bh_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

       /* switch (item.getItemId()) {
            case R.id.menu_offers:
                startActivity(new Intent(SwipeTabActivity.this, OffersActivity.class));
                break;

            case R.id.menu_bus:
                startActivity(new Intent(SwipeTabActivity.this, BusActivity.class));
                break;

            case R.id.menu_settings:
                startActivity(new Intent(SwipeTabActivity.this, SettingsActivity.class));
                break;

            default:
                return super.onOptionsItemSelected(item);
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {

        if (position == 1) {
            startActivity(new Intent(this, OffersActivity.class));
        } else if (position == 5) {
            Intent supportIntent = new Intent(Intent.ACTION_DIAL);
            supportIntent.setData(Uri.parse("tel:07940223333"));
            startActivity(supportIntent);
        } else if (position == 6) {
            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"info@bhagwatiholidays.com"});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback");
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        }


    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
