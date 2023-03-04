package com.hope_studio.base_ads.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.hope_studio.base_ads.R;
import com.wang.avi.AVLoadingIndicatorView;

public class LoadingDialog extends Dialog {

    public LoadingDialog(@NonNull Context context) {
        super(context);
    }

    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    private LoadingDialog dialog;

    public LoadingDialog showDialog(Context context, boolean cancelable) {
        dialog = new LoadingDialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.lib_loading_dialog_ads);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(cancelable);
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.2f;
        dialog.getWindow().setAttributes(lp);
        AVLoadingIndicatorView loadingBar = dialog.findViewById(R.id.loadingBar);
        loadingBar.show();
        dialog.show();
        return dialog;
    }

    public void dismissDialog() {
        if (dialog == null) return;
        dialog.dismiss();
    }

    public boolean dialogIsShowing() {
        if (dialog == null) return false;
        return dialog.isShowing();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setOnKeyListener((dialog, keyCode, event) -> keyCode == android.view.KeyEvent.KEYCODE_BACK);
    }
}
