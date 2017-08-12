package com.gianlu.timeless.Main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.GrantActivity;
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.Models.Duration;
import com.gianlu.timeless.Models.Summary;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.Utils;
import com.google.android.gms.analytics.HitBuilders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

public class MainFragment extends Fragment implements CardsAdapter.ISaveChart, WakaTime.ISummary {
    private static final int REQUEST_CODE = 1;
    private CardsAdapter.IPermissionRequest handler;
    private WakaTime.Range range;
    private SwipeRefreshLayout layout;
    private ProgressBar loading;
    private RecyclerView list;
    private TextView error;

    public static MainFragment getInstance(Context context, WakaTime.Range range) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString("title", range.getFormal(context));
        args.putSerializable("range", range);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (handler != null && requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) handler.onGranted();
            else Toaster.show(getActivity(), Utils.ToastMessages.WRITE_DENIED);
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = (SwipeRefreshLayout) inflater.inflate(R.layout.main_fragment, container, false);
        layout.setColorSchemeResources(Utils.getColors());
        loading = layout.findViewById(R.id.stats_loading);
        list = layout.findViewById(R.id.stats_list);
        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        error = layout.findViewById(R.id.stats_error);

        range = (WakaTime.Range) getArguments().getSerializable("range");
        if (range == null) {
            loading.setEnabled(false);
            error.setVisibility(View.VISIBLE);
            return layout;
        }

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WakaTime.getInstance().getRangeSummary(range.getStartAndEnd(), MainFragment.this);
            }
        });

        WakaTime.getInstance().getRangeSummary(range.getStartAndEnd(), this);

        return layout;
    }

    @Override
    public void onSummary(final List<Summary> summaries, final Summary summary) {
        if (isDetached() || getContext() == null) return;

        final CardsAdapter.CardsList cards = new CardsAdapter.CardsList()
                .addSummary(summary)
                .addPieChart(getString(R.string.projectsSummary), summary.projects)
                .addPieChart(getString(R.string.languagesSummary), summary.languages)
                .addPieChart(getString(R.string.editorsSummary), summary.editors)
                .addPieChart(getString(R.string.operatingSystemsSummary), summary.operating_systems);

        if (range == WakaTime.Range.TODAY) {
            WakaTime.getInstance().getRangeSummary(range.getWeekBefore(), new WakaTime.ISummary() {
                @Override
                public void onSummary(@Nullable final List<Summary> beforeSummaries, @Nullable final Summary beforeSummary) {
                    WakaTime.getInstance().getDurations(getContext(), new Date(), new WakaTime.IDurations() {
                        @Override
                        public void onDurations(final List<Duration> durations) {
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        layout.setRefreshing(false);
                                        loading.setVisibility(View.GONE);
                                        list.setVisibility(View.VISIBLE);
                                        error.setVisibility(View.GONE);

                                        cards.addDurations(1, getString(R.string.durationsSummary), durations);
                                        if (beforeSummary != null)
                                            cards.addPercentage(1, getString(R.string.averageImprovement), summary.total_seconds, Summary.doTotalSecondsAverage(beforeSummaries));

                                        list.setAdapter(new CardsAdapter(getContext(), cards, MainFragment.this));
                                    }
                                });
                            }
                        }

                        @Override
                        public void onException(final Exception ex) {
                            MainFragment.this.onException(ex);
                        }

                        @Override
                        public void onWakaTimeException(WakaTimeException ex) {
                            MainFragment.this.onWakaTimeException(ex);
                        }
                    });
                }

                @Override
                public void onWakaTimeException(WakaTimeException ex) {
                    Toaster.show(getActivity(), Utils.ToastMessages.INVALID_TOKEN, ex);
                    startActivity(new Intent(getContext(), GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }

                @Override
                public void onException(Exception ex) {
                    onSummary(null, null);
                }
            });
        } else {
            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        layout.setRefreshing(false);
                        loading.setVisibility(View.GONE);
                        list.setVisibility(View.VISIBLE);
                        error.setVisibility(View.GONE);

                        list.setAdapter(new CardsAdapter(getContext(), cards
                                .addProjectsBarChart(1, getString(R.string.periodActivity), summaries), MainFragment.this));
                    }
                });
            }
        }
    }

    @Override
    public void onWakaTimeException(WakaTimeException ex) {
        Toaster.show(getActivity(), Utils.ToastMessages.INVALID_TOKEN, ex);
        startActivity(new Intent(getContext(), GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    public void onException(final Exception ex) {
        if (ex instanceof WakaTimeException) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        layout.setRefreshing(false);
                        loading.setVisibility(View.GONE);
                        error.setText(ex.getMessage());
                        error.setVisibility(View.VISIBLE);
                    }
                });
            }
        } else {
            if (layout.isRefreshing()) {
                Toaster.show(getActivity(), Utils.ToastMessages.FAILED_REFRESHING, ex, new Runnable() {
                    @Override
                    public void run() {
                        layout.setRefreshing(false);
                    }
                });
            } else {
                Toaster.show(getActivity(), Utils.ToastMessages.FAILED_LOADING, ex, new Runnable() {
                    @Override
                    public void run() {
                        loading.setVisibility(View.GONE);
                        error.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    @Override
    public void onWritePermissionRequested(CardsAdapter.IPermissionRequest handler) {
        this.handler = handler;
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            CommonUtils.showDialog(getActivity(), new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.writeExternalStorageRequest_title)
                    .setMessage(R.string.writeExternalStorageRequest_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                        }
                    }));
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public void onSaveRequested(View chart, String name) {
        File dest = new File(Utils.getImageDirectory(null), name + ".png");
        try (OutputStream out = new FileOutputStream(dest)) {
            Bitmap bitmap = Utils.createBitmap(chart);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();

            Toaster.show(getActivity(), getString(R.string.savedIn, dest.getPath()), Toast.LENGTH_LONG, null, null, null);
        } catch (IOException ex) {
            Toaster.show(getActivity(), Utils.ToastMessages.FAILED_SAVING_CHART, ex);
        }

        ThisApplication.sendAnalytics(getContext(), new HitBuilders.EventBuilder()
                .setCategory(ThisApplication.CATEGORY_USER_INPUT)
                .setAction(ThisApplication.ACTION_SAVED_CHART)
                .build());
    }
}
