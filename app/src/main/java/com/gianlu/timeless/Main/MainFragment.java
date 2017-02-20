package com.gianlu.timeless.Main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.Objects.Duration;
import com.gianlu.timeless.Objects.Summary;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

public class MainFragment extends Fragment implements CardsAdapter.ISaveChart {
    private static final int REQUEST_CODE = 1;
    private CardsAdapter.IPermissionRequest handler;

    public static MainFragment getInstance(Context context, WakaTime.Range range) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString("title", range.getFormal(context));
        args.putSerializable("range", range);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (handler != null && requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK)
                handler.onGranted();
            else
                CommonUtils.UIToast(getActivity(), Utils.ToastMessages.WRITE_DENIED);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final SwipeRefreshLayout layout = (SwipeRefreshLayout) inflater.inflate(R.layout.main_fragment, container, false);
        layout.setColorSchemeResources(Utils.getColors());
        final ProgressBar loading = (ProgressBar) layout.findViewById(R.id.stats_loading);
        final RecyclerView list = (RecyclerView) layout.findViewById(R.id.stats_list);
        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        final TextView error = (TextView) layout.findViewById(R.id.stats_error);
        final WakaTime.Range range = (WakaTime.Range) getArguments().getSerializable("range");

        if (range == null) {
            loading.setEnabled(false);
            error.setVisibility(View.VISIBLE);
            return layout;
        }

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WakaTime.getInstance().getRangeSummary(range.getStartAndEnd(), new WakaTime.ISummary() {
                    @Override
                    public void onSummary(final List<Summary> summaries, final Summary summary) {
                        if (range == WakaTime.Range.TODAY) {
                            WakaTime.getInstance().getDurations(getContext(), new Date(), new WakaTime.IDurations() {
                                @Override
                                public void onDurations(final List<Duration> durations) {
                                    Activity activity = getActivity();
                                    if (activity != null) {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                layout.setRefreshing(false);
                                                error.setVisibility(View.GONE);

                                                list.setAdapter(new CardsAdapter(getContext(), new CardsAdapter.CardsList()
                                                        .addSummary(summary)
                                                        .addDurations(getString(R.string.durationsSummary), durations)
                                                        .addPieChart(getString(R.string.projectsSummary), summary.projects)
                                                        .addPieChart(getString(R.string.languagesSummary), summary.languages)
                                                        .addPieChart(getString(R.string.editorsSummary), summary.editors)
                                                        .addPieChart(getString(R.string.operatingSystemsSummary), summary.operating_systems), MainFragment.this));
                                            }
                                        });
                                    }
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
                                                    error.setText(ex.getMessage());
                                                    error.setVisibility(View.VISIBLE);
                                                }
                                            });
                                        }
                                    } else {
                                        CommonUtils.UIToast(getActivity(), Utils.ToastMessages.FAILED_REFRESHING, ex, new Runnable() {
                                            @Override
                                            public void run() {
                                                layout.setRefreshing(false);
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        layout.setRefreshing(false);
                                        error.setVisibility(View.GONE);

                                        list.setAdapter(new CardsAdapter(getContext(), new CardsAdapter.CardsList()
                                                .addSummary(summary)
                                                .addProjectsBarChart(getString(R.string.periodActivity), summaries)
                                                .addPieChart(getString(R.string.projectsSummary), summary.projects)
                                                .addPieChart(getString(R.string.languagesSummary), summary.languages)
                                                .addPieChart(getString(R.string.editorsSummary), summary.editors)
                                                .addPieChart(getString(R.string.operatingSystemsSummary), summary.operating_systems), MainFragment.this));
                                    }
                                });
                            }
                        }
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
                                        error.setText(ex.getMessage());
                                        error.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        } else {
                            CommonUtils.UIToast(getActivity(), Utils.ToastMessages.FAILED_REFRESHING, ex, new Runnable() {
                                @Override
                                public void run() {
                                    layout.setRefreshing(false);
                                }
                            });
                        }
                    }
                });
            }
        });

        WakaTime.getInstance().getRangeSummary(range.getStartAndEnd(), new WakaTime.ISummary() {
            @Override
            public void onSummary(final List<Summary> summaries, final Summary summary) {
                if (range == WakaTime.Range.TODAY) {
                    WakaTime.getInstance().getDurations(getContext(), new Date(), new WakaTime.IDurations() {
                        @Override
                        public void onDurations(final List<Duration> durations) {
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loading.setVisibility(View.GONE);
                                        list.setVisibility(View.VISIBLE);
                                        error.setVisibility(View.GONE);

                                        list.setAdapter(new CardsAdapter(getContext(), new CardsAdapter.CardsList()
                                                .addSummary(summary)
                                                .addDurations(getString(R.string.durationsSummary), durations)
                                                .addPieChart(getString(R.string.projectsSummary), summary.projects)
                                                .addPieChart(getString(R.string.languagesSummary), summary.languages)
                                                .addPieChart(getString(R.string.editorsSummary), summary.editors)
                                                .addPieChart(getString(R.string.operatingSystemsSummary), summary.operating_systems), MainFragment.this));
                                    }
                                });
                            }
                        }

                        @Override
                        public void onException(final Exception ex) {
                            if (ex instanceof WakaTimeException) {
                                Activity activity = getActivity();
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            loading.setVisibility(View.GONE);
                                            error.setText(ex.getMessage());
                                            error.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            } else {
                                CommonUtils.UIToast(getActivity(), Utils.ToastMessages.FAILED_LOADING, ex, new Runnable() {
                                    @Override
                                    public void run() {
                                        loading.setVisibility(View.GONE);
                                        error.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        }
                    });
                } else {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loading.setVisibility(View.GONE);
                                list.setVisibility(View.VISIBLE);
                                error.setVisibility(View.GONE);

                                list.setAdapter(new CardsAdapter(getContext(), new CardsAdapter.CardsList()
                                        .addSummary(summary)
                                        .addProjectsBarChart(getString(R.string.periodActivity), summaries)
                                        .addPieChart(getString(R.string.projectsSummary), summary.projects)
                                        .addPieChart(getString(R.string.languagesSummary), summary.languages)
                                        .addPieChart(getString(R.string.editorsSummary), summary.editors)
                                        .addPieChart(getString(R.string.operatingSystemsSummary), summary.operating_systems), MainFragment.this));
                            }
                        });
                    }
                }
            }

            @Override
            public void onException(final Exception ex) {
                if (ex instanceof WakaTimeException) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loading.setVisibility(View.GONE);
                                error.setText(ex.getMessage());
                                error.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } else {
                    CommonUtils.UIToast(getActivity(), Utils.ToastMessages.FAILED_LOADING, ex, new Runnable() {
                        @Override
                        public void run() {
                            loading.setVisibility(View.GONE);
                            error.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

        return layout;
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
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                        }
                    }));
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public void onSaveRequested(View chart, String name) {
        File dest = new File(Utils.getImageDirectory(null), name + ".png");
        try (OutputStream out = new FileOutputStream(dest)) {
            Bitmap bitmap = Utils.createBitmap(chart);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();

            CommonUtils.UIToast(getActivity(), "Image has been saved as " + dest.getPath() + "!", Toast.LENGTH_LONG);
        } catch (IOException ex) {
            CommonUtils.UIToast(getActivity(), Utils.ToastMessages.FAILED_SAVING_CHART, ex);
        }
    }
}
