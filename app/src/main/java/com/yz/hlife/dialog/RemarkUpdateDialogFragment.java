package com.yz.hlife.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.DialogFragment;

import com.yz.hlife.R;

import org.jetbrains.annotations.NotNull;

public class RemarkUpdateDialogFragment extends DialogFragment {
    private static String lastUserId = null;

    public interface RemarkDialogListener {
        public void onUpdateRemark(String userId, String remark);
    }

    private RemarkDialogListener listener;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (RemarkDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_update_remark, null);
        AppCompatEditText userIdView = view.findViewById(R.id.user_id);
        if (!TextUtils.isEmpty(lastUserId)) {
            userIdView.setText(lastUserId);
        }
        AppCompatEditText remarkView = view.findViewById(R.id.user_remark);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String userId = userIdView.getText().toString();
                        String remark = remarkView.getText().toString();
                        if (!TextUtils.isEmpty(userId)) {
                            lastUserId = userId;
                        }
                        if (listener != null) {
                            listener.onUpdateRemark(userId, remark);
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }
}
