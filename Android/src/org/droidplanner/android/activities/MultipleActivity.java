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
        setContentView(R.layout.activity_multiple);



        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        MultipleFragment fragment = new MultipleFragment();
        fragmentTransaction.add(R.id.multi_layout1, fragment, "1");


        MultipleFragment fragment2 = new MultipleFragment();
        fragmentTransaction.add(R.id.multi_layout2, fragment2, "2");

        MultipleFragment fragment3 = new MultipleFragment();
        fragmentTransaction.add(R.id.multi_layout3, fragment3, "3");

        fragmentTransaction.commit();
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


}
