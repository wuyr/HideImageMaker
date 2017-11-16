package com.wuyr.hideimagemaker.utils;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.wuyr.hideimagemaker.R;

/**
 * Created by wuyr on 4/5/16 10:55 PM.
 */
public class MySnackBar {

    public static Snackbar show(@NonNull View view, @NonNull CharSequence text, int duration) {
        return show(view, text, duration, null, null);
    }

    public static Snackbar show(@NonNull View view, @NonNull CharSequence text, int duration,
                            @Nullable String actionText, @Nullable View.OnClickListener listener) {
        Snackbar snackbar = Snackbar.make(view, text, duration);
        Snackbar.SnackbarLayout root = (Snackbar.SnackbarLayout) snackbar.getView();
        root.setBackgroundColor(view.getContext().getResources().getColor(R.color.colorPrimaryDark));
        ((TextView) root.findViewById(R.id.snackbar_text)).setTextColor(Color.WHITE);
        if (actionText != null && listener != null)
            snackbar.setAction(actionText, listener).setActionTextColor(view.getResources().getColor(R.color.md_red_500));
        snackbar.show();
        return snackbar;
    }
}
