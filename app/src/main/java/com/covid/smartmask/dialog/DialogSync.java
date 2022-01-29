package com.covid.smartmask.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.covid.smartmask.R;

import java.util.Objects;

public class DialogSync extends AppCompatDialogFragment {
    private EditText editUrl;
    private Switch switchSync;
    private DialogSyncListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (DialogSyncListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_sync, null);
        builder.setView(view)
                .setTitle("Sincronicación")
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String url = editUrl.getText().toString();
                        Boolean sync = switchSync.isChecked();
                        if (editUrl.getText().toString().isEmpty()) {
                            Toast.makeText(getContext(), "URL no puede estar vacía, cancelando sincronizacion", Toast.LENGTH_LONG).show();
                            listener.setSyncValues("", false);
                        } else {
                            if (Patterns.WEB_URL.matcher(url).matches()) {
                                listener.setSyncValues(url.trim(), sync);
                            } else {
                                Toast.makeText(getContext(), "URL no válida, cancelando sincronizacion", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });

        editUrl = view.findViewById(R.id.editUrl);
        switchSync = view.findViewById(R.id.switchSync);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean sync = settings.getBoolean("syncData", false);
        String url = settings.getString("syncURL", "");

        if (!url.isEmpty()) {
            editUrl.setText(url);
        }
        switchSync.setChecked(sync);

        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    return i == KeyEvent.KEYCODE_BACK;
                }
                return false;
            }
        });

        return builder.create();
    }

    public interface DialogSyncListener {
        void setSyncValues(String url, Boolean sync);
    }
}
