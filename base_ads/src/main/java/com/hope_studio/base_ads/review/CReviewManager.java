package com.hope_studio.base_ads.review;


import static com.hope_studio.base_ads.review.CReviewPreferenceHelper.getInstallDate;
import static com.hope_studio.base_ads.review.CReviewPreferenceHelper.getLaunchTimes;
import static com.hope_studio.base_ads.review.CReviewPreferenceHelper.getRemindInterval;
import static com.hope_studio.base_ads.review.CReviewPreferenceHelper.isFirstLaunch;
import static com.hope_studio.base_ads.review.CReviewPreferenceHelper.setInstallDate;
import static com.hope_studio.base_ads.review.CReviewPreferenceHelper.setRemindIntervalDate;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import java.util.Date;


public class CReviewManager {

    private static CReviewManager singleton;

    private final Context context;

    private int installDate = 10;

    private int launchTimes = 10;

    private int remindInterval = 1;

    private boolean isDebug = false;

    private CReviewManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static CReviewManager with(Context context) {
        if (singleton == null) {
            synchronized (CReviewManager.class) {
                if (singleton == null) {
                    singleton = new CReviewManager(context);
                }
            }
        }
        return singleton;
    }

    public static boolean showRateDialogIfMeetsConditions(Activity activity) {
        boolean isMeetsConditions = singleton.isDebug || singleton.shouldShowRateDialog();
        if (isMeetsConditions) {
            singleton.showRateDialog(activity);
        }
        return isMeetsConditions;
    }

    private static boolean isOverDate(long targetDate, int threshold) {
        return new Date().getTime() - targetDate >= threshold * 24 * 60 * 60 * 1000;
    }

    public CReviewManager setLaunchTimes(int launchTimes) {
        this.launchTimes = launchTimes;
        return this;
    }

    public CReviewManager setInstallDays(int installDate) {
        this.installDate = installDate;
        return this;
    }

    public CReviewManager setRemindInterval(int remindInterval) {
        this.remindInterval = remindInterval;
        return this;
    }

    public void monitor() {
        if (isFirstLaunch(context)) {
            setInstallDate(context);
        }
        CReviewPreferenceHelper.setLaunchTimes(context, getLaunchTimes(context) + 1);
    }

    public void showRateDialog(Activity activity) {
        if (!activity.isFinishing()) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showDialog(activity);
                }
            }, 5000);
        }
    }

    public boolean shouldShowRateDialog() {
        return isOverLaunchTimes() &&
                isOverInstallDate() &&
                isOverRemindDate();
    }

    private boolean isOverLaunchTimes() {
        return getLaunchTimes(context) >= launchTimes;
    }

    private boolean isOverInstallDate() {
        return isOverDate(getInstallDate(context), installDate);
    }

    private boolean isOverRemindDate() {
        return isOverDate(getRemindInterval(context), remindInterval);
    }

    public boolean isDebug() {
        return isDebug;
    }

    public CReviewManager setDebug(boolean isDebug) {
        this.isDebug = isDebug;
        return this;
    }

    public void showDialog(Activity activity) {
        com.google.android.play.core.review.ReviewManager manager = ReviewManagerFactory.create(context);
        Task<ReviewInfo> request = manager.requestReviewFlow();

        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();
                startReview(activity, manager, reviewInfo);
            } else {
                // There was some problem, continue regardless of the result.
                setRemindIntervalDate(context);
            }
        });
    }

    public void startReview(Activity activity, ReviewManager manager, ReviewInfo reviewInfo) {
        Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
        flow.addOnCompleteListener(task -> {
            // The flow has finished. The API does not indicate whether the user
            // reviewed or not, or even whether the review dialog was shown. Thus, no
            // matter the result, we continue our app flow.
            setRemindIntervalDate(context);
        });
    }

}

