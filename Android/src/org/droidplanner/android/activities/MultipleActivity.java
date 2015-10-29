package org.droidplanner.android.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.droidplanner.R;
import org.droidplanner.android.fragments.MultipleFragment;
import org.droidplanner.android.fragments.MultipleFragment2;

import android.net.Uri;


public class MultipleActivity extends FragmentActivity implements MultipleFragment.OnFragmentInteractionListener,
        MultipleFragment2.OnFragmentInteractionListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        multipleMapView(4);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_multiple, menu);
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

    public void onFragmentInteraction(Uri uri)
    {

    }

    public void onFragmentInteraction2(Uri uri)
    {

    }

    public void multipleMapView(int num_maps)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MultipleFragment fragment1 = new MultipleFragment();
        MultipleFragment fragment2 = new MultipleFragment();
        MultipleFragment fragment3 = new MultipleFragment();
        MultipleFragment fragment4 = new MultipleFragment();
        switch(num_maps)
        {
            case 1:
                setContentView(R.layout.activity_multiple1);
                fragmentTransaction.add(R.id.multi_layout1, fragment1, "1");
                break;
            case 2:
                setContentView(R.layout.activity_multiple2);
                fragmentTransaction.add(R.id.multi_layout1, fragment1, "1");
                fragmentTransaction.add(R.id.multi_layout2, fragment2, "2");
                break;
            case 3:
                setContentView(R.layout.activity_multiple3);
                fragmentTransaction.add(R.id.multi_layout1, fragment1, "1");
                fragmentTransaction.add(R.id.multi_layout2, fragment2, "2");
                fragmentTransaction.add(R.id.multi_layout3, fragment3, "3");

                break;
            case 4:
                setContentView(R.layout.activity_multiple4);
                fragmentTransaction.add(R.id.multi_layout1, fragment1, "1");
                fragmentTransaction.add(R.id.multi_layout2, fragment2, "2");
                fragmentTransaction.add(R.id.multi_layout3, fragment3, "3");
                fragmentTransaction.add(R.id.multi_layout4, fragment4, "4");
                break;
        }

        fragmentTransaction.commit();
    }


}
