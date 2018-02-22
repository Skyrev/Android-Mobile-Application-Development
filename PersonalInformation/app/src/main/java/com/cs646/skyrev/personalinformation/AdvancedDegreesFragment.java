package com.cs646.skyrev.personalinformation;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;


public class AdvancedDegreesFragment extends ListFragment {

    ArrayAdapter arrayAdapter;

    public AdvancedDegreesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_advanced_degrees, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set action bar title
        getActivity().setTitle(getString(R.string.sdsu_advanced_degrees));

        // Attach array adapter and item click listener to the list view
        arrayAdapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.advanced_degrees,
                R.layout.list_item_style
        );
        setListAdapter(arrayAdapter);
        getListView().setOnItemClickListener((adapterView, view, position, id) -> {

                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    String[] majorPrefixes = getActivity().getResources()
                                            .getStringArray(R.array.major_prefixes);

                    // bundle holds the arguments to be passed
                    // to the next fragment (DegreesFragment)
                    Bundle bundle = new Bundle();

                    switch (position) {
                        case 0:
                            bundle.putInt("majorListId", R.array.doctor_of_philosophy);
                            break;
                        case 1:
                            bundle.putInt("majorListId", R.array.doctor_of_education);
                            break;
                        case 2:
                            bundle.putInt("majorListId", R.array.master_of_arts);
                            break;
                        case 3:
                            bundle.putInt("majorListId", R.array.master_of_science);
                            break;
                        case 4:
                            bundle.putInt("majorListId", R.array.master_of_fine_arts);
                            break;
                        case 5:
                            bundle.putInt("majorListId", R.array.professional_masters_degrees);
                            break;
                    }
                    bundle.putInt("advancedDegreesIndex", position);
                    bundle.putString("majorPrefix", majorPrefixes[position]);

                    // Replace current fragment with DegreesFragment
                    // and pass bundle as its arguments
                    Fragment degreesFragment = new DegreesFragment();
                    degreesFragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.major_selection, degreesFragment)
                            .addToBackStack(null).commit();
                }
        );
    }
}
