package com.smoftware.mygrocerylist;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class AddListFragment extends DialogFragment {
    private IOnAddListDialogListener _callback;

    public interface IOnAddListDialogListener {
        void OnAddListDialogListener(String newText, String oldText);
    }

    Context _context = null;

    public AddListFragment() {
        // Required empty public constructor
    }

    public static AddListFragment newInstance(Bundle bundle) {
        AddListFragment fragment = new AddListFragment();
        fragment.setArguments(bundle);
        fragment.setStyle(STYLE_NO_TITLE, 0);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final String oldName = getArguments().getString("name");

        // Use this to return your custom view for this Fragment
        View view = inflater.inflate(R.layout.input_dialog, container, false);
        Button buttonCancel = (Button)view.findViewById(R.id.cancelButton);
        Button buttonSave = (Button)view.findViewById(R.id.saveButton);

        TextView title = (TextView)view.findViewById(R.id.textView1);
        TextView descr = (TextView)view.findViewById(R.id.textView2);

        if (oldName.equals(""))
        {
            title.setText("Add List");
            descr.setText("Enter the name of a new list");
        }
        else
        {
            title.setText("Edit Name");
            descr.setText("Enter a new name for the list");
        }

        final EditText editText = (EditText)view.findViewById(R.id.editText);
        editText.setText(oldName, TextView.BufferType.EDITABLE);

        ShowKeyboard(editText);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                HideKeyboard(editText);
                dismiss();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String text = editText.getText().toString().trim();

                if (text.equals(""))
                {
                    Toast.makeText(_context, "Name cannot be blank", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    _callback.OnAddListDialogListener(text, oldName);
                    HideKeyboard(editText);
                    dismiss();
                }
            }
        });

        return view;
    }

    @Override
    public void onStart()
    {
        // Auto size the dialog to fit screen
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        }

        super.onStart();
    }

    @Override
    public void onResume()
    {
        // Auto size the dialog to fit screen
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        }

        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        // API 23 and greater
        _context = context;
        super.onAttach(context);
        try
        {
            _callback = (AddListFragment.IOnAddListDialogListener)context;
        }
        catch(ClassCastException e)
        {
            Toast.makeText(context, "IOnAddListDialogListener not implemented!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        // API less than 23
        _context = activity;
        super.onAttach(activity);
        try
        {
            _callback = (AddListFragment.IOnAddListDialogListener)activity;
        }
        catch(ClassCastException e)
        {
            Toast.makeText(activity, "IOnAddListDialogListener not implemented!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _callback = null;
    }

    public void ShowKeyboard(View view)
    {
        if (_context != null)
        {
            view.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager)_context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    public void HideKeyboard(View view)
    {
        if (_context != null)
        {
            InputMethodManager inputMethodManager = (InputMethodManager)_context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
