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

public class AddCategoryFragment extends DialogFragment {

    private IOnAddCategoryDialogListener _callback;

    public interface IOnAddCategoryDialogListener {
        void OnAddCategoryDialogListener(String newText, String oldText);
    }

    Context _context = null;

    public AddCategoryFragment() {
        // Required empty public constructor
    }

    public static AddCategoryFragment newInstance(Bundle bundle) {
        AddCategoryFragment fragment = new AddCategoryFragment();
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

        TextView title = (TextView)view.findViewById(R.id.textTitle);
        title.setText("Add Category");

        TextView descr = (TextView)view.findViewById(R.id.textInstructions);
        descr.setText("Enter the name of a new category");

        final EditText editText = (EditText)view.findViewById(R.id.editText);
        editText.setText(oldName, TextView.BufferType.EDITABLE);

        final TextView quantityLabel = (TextView)view.findViewById(R.id.labelQuantity);
        quantityLabel.setVisibility(View.GONE);

        final EditText editQuantity = (EditText)view.findViewById(R.id.editQuantity);
        editQuantity.setVisibility(View.GONE);

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
                    if (_callback != null) {
                        _callback.OnAddCategoryDialogListener(text, oldName);
                        HideKeyboard(editText);
                        dismiss();
                    }
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

    //private IOnAddCategoryDialogListener _callback;

    @Override
    public void onAttach(Activity activity) {
        // API less than 23
        _context = activity;
        super.onAttach(activity);
        try
        {
            _callback = (IOnAddCategoryDialogListener)activity;
        }
        catch(ClassCastException e)
        {
            Toast.makeText(activity, "IOnAddCategoryDialogListener not implemented!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAttach(Context context)
    {
        // API 23 and greater
        _context = context;
        super.onAttach(context);
        try
        {
            _callback = (IOnAddCategoryDialogListener)context;
        }
        catch(ClassCastException e)
        {
            Toast.makeText(context, "IOnAddCategoryDialogListener not implemented!", Toast.LENGTH_SHORT).show();
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
