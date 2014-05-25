package com.wizard.TestLAB;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Koen on 30-Apr-14.
 */
public class OnPointDialogFragment extends DialogFragment
{
    private static final String DEBUGTAG = "OpenFileDialogFragment";
    private Activity act;
    private IonDialogDoneListener onDialogDoneListener;
    private TextView textViewInfo;
    private Button buttonOpen;
    private Button buttonCancel;



    public static OnPointDialogFragment newInstance()
    {
        OnPointDialogFragment dlg = new OnPointDialogFragment();
        return dlg;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        act = activity;
        try
        {
            onDialogDoneListener = (IonDialogDoneListener) activity;
        }
        catch(ClassCastException cce)
        {
            Log.d(DEBUGTAG, "Activity is not listening");
            Log.d(DEBUGTAG, cce.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().setTitle("Title");
        View v = inflater.inflate(R.layout.dialog_fragment_on_point, container);

        textViewInfo = (TextView) v.findViewById(R.id.textView_title);


        buttonOpen = (Button) v.findViewById(R.id.button_dlgfrag_onpoint_ok);
        buttonCancel = (Button) v.findViewById(R.id.button_dlgfrag_onpoint_cancel);

        buttonOpen.setOnClickListener(onButtonClickListener);
        buttonCancel.setOnClickListener(onButtonClickListener);
        return v;
    }

    private View.OnClickListener onButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if(R.id.button_dlgfrag_onpoint_ok == v.getId())
            {
                onDialogDoneListener.onDialogDone(getTag(), false, "OK");
                OnPointDialogFragment.this.dismiss();
            }
            else
            {
                onDialogDoneListener.onDialogDone(getTag(), true, "Cancel");
                OnPointDialogFragment.this.dismiss();
            }
        }
    };

    public void setTitle(String title)
    {
        getDialog().setTitle(title);
    }

    public void setContent(String content)
    {
        textViewInfo.setText(content);
    }

}

