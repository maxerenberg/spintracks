package com.example.spintracks.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class DeleteConfirmDialogFragment extends DialogFragment {
    private final String prompt;
    private final Runnable callback;

    public DeleteConfirmDialogFragment(String prompt, Runnable callback) {
        this.prompt = prompt;
        this.callback = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(prompt)
                .setNegativeButton("No", (dialog, id) -> {
                    // do nothing
                })
                .setPositiveButton("Yes", (dialog, id) -> {
                    callback.run();
                });
        return builder.create();
    }
}
