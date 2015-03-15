package sumeetkumar.in.wearsense.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import sumeetkumar.in.wearsense.R;
import sumeetkumar.in.wearsense.services.AlarmManager;
import sumeetkumar.in.wearsense.services.StartSensingBroadcastReceiver;
import sumeetkumar.in.wearsense.utils.BLEScanner;
import sumeetkumar.in.wearsense.utils.BLESignalScanner;
import sumeetkumar.in.wearsense.utils.Constants;
import sumeetkumar.in.wearsense.utils.Logger;
import sumeetkumar.in.wearsense.utils.SoundPlayer;


public class MainActivity extends ActionBarActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    private NewDataReceivedBroadcastReceiver dataReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        Logger.log("scheduling alarm");
        AlarmManager.setupRepeatingAlarmToWakeUpApplication(
                this.getApplicationContext(),
                Constants.TIME_RANGE_TO_SHOW_ALERT_IN_MINUTES * 60 * 1000);

        if (dataReceiver == null) {
            dataReceiver = new NewDataReceivedBroadcastReceiver(new Handler());

            Log.d("WEA", "New configuration receiver created in main activity");
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.NEW_DATA_INTENT_FILTER);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            getApplicationContext().registerReceiver(dataReceiver, filter);
        }

    }

    @Override
    protected void onDestroy() {

        if (dataReceiver != null) {
            getApplication().unregisterReceiver(dataReceiver);
            dataReceiver = null;
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void readSensors() {
        Intent intent = new Intent(this, StartSensingBroadcastReceiver.class);
        sendBroadcast(intent);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onViewCreated(View view,
                                  Bundle savedInstanceState) {
            final TextView txtStatus = (TextView) getActivity().findViewById(R.id.txtFragment);

            Button btnGetData = (Button) getActivity().findViewById(R.id.btnGetData);
            btnGetData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) getActivity()).readSensors();
                    txtStatus.setText("Asking wear for new data");
                }
            });

            TextView txtData = (TextView) getActivity().findViewById(R.id.txtWearData);
            txtData.setMovementMethod(new ScrollingMovementMethod());

            Button btnPlaySound = (Button) getActivity().findViewById(R.id.btnPlaySound);
            btnPlaySound.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SoundPlayer player = new SoundPlayer();
                    player.playSound();
                }
            });

            Button btnScan = (Button) getActivity().findViewById(R.id.btnScan);
            btnScan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            BLESignalScanner.getSignalStrength("NA", getActivity().getApplicationContext());
                            BLEScanner bleScanner = new BLEScanner(getActivity());
                        }
                    });
                }
            });
        }
    }

    public class NewDataReceivedBroadcastReceiver extends BroadcastReceiver {
        private final Handler handler;

        public NewDataReceivedBroadcastReceiver(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            try {
                final String message = intent.getStringExtra(Constants.NEW_DATA);
                final TextView txtData = (TextView) findViewById(R.id.txtWearData);
                final TextView txtStatus = (TextView) findViewById(R.id.txtFragment);

                // Post the UI updating code to our Handler
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                            String currentDateandTime = sdf.format(new Date());

                            txtStatus.setText("Updated at: " + currentDateandTime);
                            txtData.setText(message);
                        }
                    });
                }
            } catch (Exception ex) {
                Logger.log(ex.getMessage());
            }
        }
    }
}
