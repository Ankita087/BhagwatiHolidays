package com.rutvik.bhagwatiholidays;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

import adapter.ViewPagerAdapter;
import bhfragment.FlightResultFragment;
import extras.Log;
import liveapimodels.ApiConstants;
import liveapimodels.flightsearchresult.FlightSearchResult;
import liveapimodels.flightsearchresult.Results;
import liveapimodels.flightsearchresult.Segments;
import model.MultiFlightResult;
import model.SingleFlightResult;
import model.SingleMultiFlightResult;

public class ActivityFlightSearchResult extends AppCompatActivity
{

    private static final String TAG = App.APP_TAG + ActivityFlightSearchResult.class.getSimpleName();

    private TabLayout tabLayout;
    private ViewPager viewPager;

    final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

    FlightResultFragment oneWayFlight, returnFlight;

    private HashMap<String, String> postParams;

    private TextView tvOrigin, tvDestination;

    private ImageView ivOneWay, ivReturn;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_search_result);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        ((TextView) findViewById(R.id.tv_toolbarTitle)).setText("Available Flights");

        ivOneWay = (ImageView) findViewById(R.id.iv_oneWay);
        ivReturn = (ImageView) findViewById(R.id.iv_return);

        tvDestination = (TextView) findViewById(R.id.tv_destination);
        tvOrigin = (TextView) findViewById(R.id.tv_origin);

        if (getIntent().getSerializableExtra("post_param") != null)
        {
            postParams = (HashMap<String, String>) getIntent().getSerializableExtra("post_param");
            tvOrigin.setText(postParams.get("origin").toString());
            tvDestination.setText(postParams.get("destination").toString());
        }

        tabLayout = (TabLayout) findViewById(R.id.tabs);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(2);
        setupViewPager(viewPager);


        tabLayout.setupWithViewPager(viewPager);


    }

    private void setupTabIcons()
    {

        tabLayout.getTabAt(0).setIcon(R.drawable.logo);

    }

    private void setupViewPager(ViewPager viewPager)
    {

        oneWayFlight = FlightResultFragment.getInstance(this);
        returnFlight = FlightResultFragment.getInstance(this);

        if (postParams.get("journey_type").equals(ApiConstants.JourneyType.ONE_WAY))
        {
            ivOneWay.setVisibility(View.VISIBLE);
            viewPagerAdapter.addFragment(oneWayFlight, "ONE-WAY");
            tabLayout.setVisibility(View.GONE);
        }
        if (postParams.get("journey_type").equals(ApiConstants.JourneyType.RETURN))
        {
            ivReturn.setVisibility(View.VISIBLE);
            viewPagerAdapter.addFragment(oneWayFlight, "ONE-WAY");
            viewPagerAdapter.addFragment(returnFlight, "RETURN");
        }

        viewPager.setAdapter(viewPagerAdapter);


    }

    @Override
    protected void onStart()
    {
        super.onStart();


        if (postParams != null)
        {
            if (((App) getApplication()).getApiAuthentication() == null)
            {
                return;
            }
            final String authToken = ((App) getApplication()).getApiAuthentication().getTokenId();
            if (!authToken.isEmpty())
            {
                try
                {
                    searchFlightsAsync(authToken, postParams.get("journey_type"));
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    private void searchFlightsAsync(final String authToken, final String journeyType)
    {

        new LiveAPI.SearchFlights(authToken, postParams)
        {
            @Override
            protected void onPostExecute(FlightSearchResult flightSearchResult)
            {
                if (flightSearchResult != null)
                {
                    Results[][] r = flightSearchResult.getResponse().getResults();
                    android.util.Log.i(TAG, "RESULT[][] length: " + r[0].length);
                    for (int i = 0; i < r[0].length; i++)
                    {
                        final Results result = r[0][i];
                        final Segments[][] s = r[0][i].getSegments();

                        //will be true for INTERNATIONAL FLIGHTS as they have multi stops
                        if (s[0].length > 1)
                        {
                            final MultiFlightResult multiFlightResult= new MultiFlightResult();
                            for (int j = 0; j < s[0].length; j++)
                            {
                                final Segments segment = s[0][j];
                                Log.i(TAG, result.toString());

                                SingleMultiFlightResult singleFlightResult = new SingleMultiFlightResult();

                                singleFlightResult.setOriginDestination(segment.getOrigin().getAirport().getCityCode()
                                        + " - "
                                        + segment.getDestination().getAirport().getCityCode());

                                singleFlightResult.setAirCode(segment.getAirline().getAirlineCode());
                                singleFlightResult.setFlightNumber(segment.getAirline().getFlightNumber());
                                singleFlightResult.setFlightName(segment.getAirline().getAirlineCode());
                                singleFlightResult.setStartTime(segment.getOrigin().getDepTime());
                                singleFlightResult.setEndTime(segment.getDestination().getArrTime());

                                if (segment.getStopPoint().isEmpty())
                                {
                                    singleFlightResult.setIsNonStop("Non Stop");
                                } else
                                {
                                    singleFlightResult.setIsNonStop(segment.getStopPoint());
                                }

                                multiFlightResult.addSingleMultiFlightResult(singleFlightResult);

                            }

                            multiFlightResult.setPublishedFair(result.getFare().getPublishedFare());
                            oneWayFlight.addMultiFlightSearchResult(multiFlightResult);

                        } else
                        {
                            //flights having no multi stops
                            final Segments segment = s[0][0];
                            Log.i(TAG, result.toString());
                            SingleFlightResult singleFlightResult = new SingleFlightResult();
                            singleFlightResult.setAirPrice(result.getFare().getPublishedFare());
                            singleFlightResult.setAirCode(segment.getAirline().getAirlineCode());
                            singleFlightResult.setFlightNumber(segment.getAirline().getFlightNumber());
                            singleFlightResult.setFlightName(segment.getAirline().getAirlineCode());
                            singleFlightResult.setStartTime(segment.getOrigin().getDepTime());
                            singleFlightResult.setEndTime(segment.getDestination().getArrTime());
                            if (segment.getStopPoint().isEmpty())
                            {
                                singleFlightResult.setIsNonStop("Non Stop");
                            } else
                            {
                                singleFlightResult.setIsNonStop(segment.getStopPoint());
                            }

                            oneWayFlight.addFlightSearchResult(singleFlightResult);
                        }
                    }

                    oneWayFlight.hideProgressBar();

                    //will be true for journey type RETURN
                    if (journeyType.equals(ApiConstants.JourneyType.RETURN))
                    {
                        if (returnFlight != null)
                        {
                            for (int i = 0; i < r[1].length; i++)
                            {
                                final Results result = r[1][i];
                                final Segments segment = r[1][i].getSegments()[0][0];
                                Log.i(TAG, result.toString());
                                SingleFlightResult singleFlightResult = new SingleFlightResult();
                                singleFlightResult.setAirPrice(result.getFare().getPublishedFare());
                                singleFlightResult.setAirCode(segment.getAirline().getAirlineCode());
                                singleFlightResult.setFlightNumber(segment.getAirline().getFlightNumber());
                                singleFlightResult.setFlightName(segment.getAirline().getAirlineCode());
                                singleFlightResult.setStartTime(segment.getOrigin().getDepTime());
                                singleFlightResult.setEndTime(segment.getDestination().getArrTime());
                                if (segment.getStopPoint().isEmpty())
                                {
                                    singleFlightResult.setIsNonStop("Non Stop");
                                } else
                                {
                                    singleFlightResult.setIsNonStop(segment.getStopPoint());
                                }

                                returnFlight.addFlightSearchResult(singleFlightResult);
                            }

                            returnFlight.hideProgressBar();
                        }
                    }
                }
            }
        }.execute();

    }

}
