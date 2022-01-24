package com.covid.smartmask.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.covid.smartmask.R;

public class DialogOximetro extends AppCompatDialogFragment {
    private EditText edit_oxigen;
    private EditText edit_heart;
    private DialogOximetroListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (DialogOximetroListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_oximeter, null);
        builder.setView(view)
                .setTitle("Oximetro")
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String oxigen = edit_oxigen.getText().toString();
                        String heart = edit_heart.getText().toString();
                        listener.saveValues(oxigen, heart);
                    }
                });

        edit_oxigen = view.findViewById(R.id.edit_oxigen);
        edit_heart = view.findViewById(R.id.edit_heart);
        return builder.create();
    }

    public interface DialogOximetroListener {
        void saveValues(String oxigen, String heart);
    }
}
