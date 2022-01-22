package com.covid.smartmask.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.covid.smartmask.R;

public class DialogTimer extends AppCompatDialogFragment {
    private NumberPicker number_picker_min;
    private NumberPicker number_picker_seg;
    private DialogTimerListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (DialogTimerListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_timer, null);
        builder.setView(view)
                .setTitle("Cambiar frecuencia de mensajes")
                .setCancelable(false)
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Cambiar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int minutes = number_picker_min.getValue();
                        int seconds =  number_picker_seg.getValue();
                        listener.updateTempTimer(((minutes * 60)+ seconds) * 1000);
                    }
                });

        number_picker_min = view.findViewById(R.id.number_picker_min);
        number_picker_seg = view.findViewById(R.id.number_picker_seg);
        number_picker_min.setMinValue(0);
        number_picker_seg.setMinValue(1);
        number_picker_min.setMaxValue(59);
        number_picker_seg.setMaxValue(59);

        number_picker_min.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d",i);
            }
        });
        number_picker_seg.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d",i);
            }
        });

        return builder.create();
    }

    public interface DialogTimerListener{
        void updateTempTimer(int newMillisTime);
    }
}
