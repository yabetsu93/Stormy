package com.jabespauya.stormy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by jabespauya on 8/13/2017 AD.
 */

public class AlertDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.no_connection)
                .setMessage(R.string.error_message)
                .setPositiveButton(R.string.ok_button,null);

        AlertDialog dialog = builder.create();
        return dialog;
    }
}
