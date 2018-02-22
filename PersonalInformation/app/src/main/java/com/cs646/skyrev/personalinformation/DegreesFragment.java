package com.cs646.skyrev.personalinformation;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class DegreesFragment extends Fragment {

    ListView listView;

    String[] majorArray;
    String[] advancedDegrees;

    public DegreesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        advancedDegrees = getActivity().getResources().getStringArray(R.array.advanced_degrees);
        majorArray = getActivity().getResources()
                .getStringArray(getArguments().getInt("majorListId"));

        // Set action bar title
        getActivity().setTitle(advancedDegrees[getArguments().getInt("advancedDegreesIndex")]);

        // When there are no items to be displayed in the selected Advanced Degree category,
        // return the category instead
        if(majorArray.length == 0) {
            Intent intent = new Intent();
            intent.putExtra("selectedMajor",
                    advancedDegrees[getArguments().getInt("advancedDegreesIndex")]);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_degrees, container, false);

        // Attach array adapter and item click listener to the list view
        listView = view.findViewById(R.id.degrees_fragment);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.list_item_style,
                majorArray
        );
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((adapterView, view1, position, id) -> {
                    String majorPrefix = getArguments().getString("majorPrefix");
                    String selectedMajor;
                    if(majorPrefix == null || majorPrefix.isEmpty() ){
                        selectedMajor = majorArray[position];
                    } else {
                        selectedMajor = majorPrefix + " " + majorArray[position];
                    }

                    // Send selected major to personal information activity
                    Intent intent = new Intent();
                    intent.putExtra("selectedMajor", selectedMajor);
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                }
        );

        return view;
    }
}
