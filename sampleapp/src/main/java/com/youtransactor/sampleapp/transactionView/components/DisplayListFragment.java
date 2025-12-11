package com.youtransactor.sampleapp.transactionView.components;

import static com.youTransactor.uCube.rpc.Constants.EVT_APP_SELECT_LANG;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.youTransactor.uCube.payment.PaymentUtils;
import com.youtransactor.sampleapp.R;

import java.io.Serializable;
import java.util.ArrayList;

public class DisplayListFragment extends Fragment {
    private static final String TAG = DisplayListFragment.class.getSimpleName();

    private static final String PARAMS_KEY = "params";
    public static final int DISPLAY_LIST_LANGUAGE = 0;
    public static final int DISPLAY_LIST_AID = 1;

    private CloseFragmentListener closeFragmentListener;

    public record DisplayListParams(ArrayList<String> itemList,
                                    int listType) implements Serializable {
    }

    public static DisplayListFragment newInstance(DisplayListParams params) {
        Bundle args = new Bundle();
        args.putSerializable(PARAMS_KEY, params);
        DisplayListFragment fragment = new DisplayListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CloseFragmentListener) {
            closeFragmentListener = (CloseFragmentListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display_list, container, false);
        ListView listView = view.findViewById(R.id.list_view);

        Bundle args = getArguments();
        DisplayListParams params = null;
        if (args != null) {
            params = (DisplayListParams) args.getSerializable(PARAMS_KEY);
        }

        if (params == null) {
            return view;
        }

        ArrayList<String> itemList = params.itemList;
        int list_type = params.listType;

        if (itemList != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.list_item, R.id.text_item, itemList);
            Log.e(TAG, String.format("Item: %d", itemList.size()));
            for (String item : itemList) {
                Log.e(TAG, "Item: " + item);
            }
            listView.setAdapter(adapter);

            listView.setOnItemClickListener((parent, view1, position, id) -> {
                String clickedItem = (String) parent.getItemAtPosition(position);
                Toast.makeText(requireContext(), "Clicked: " + clickedItem, Toast.LENGTH_SHORT).show();

                if (list_type == DISPLAY_LIST_LANGUAGE) {
                    PaymentUtils.evtSelectedItem(EVT_APP_SELECT_LANG, position, (event, params1) -> {
                        switch (event) {
                            case FAILED:
                            case SUCCESS:
                                break;
                        }
                    });
                } else {
                    PaymentUtils.send_event_filter_cless_aid(position,
                            PaymentUtils.getEmvClessDynamicParam(),
                            (event, params1) -> {
                                switch (event) {
                                    case FAILED:
                                    case SUCCESS:
                                        break;
                                }
                            });
                }

                closeFragmentListener.onCloseFragment();
            });
        }

        return view;
    }
}
