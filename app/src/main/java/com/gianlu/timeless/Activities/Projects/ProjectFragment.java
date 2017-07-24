package com.gianlu.timeless.Activities.Projects;

import android.Manifest;
import android.app.Activity;
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
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Activities.CommitsActivity;
import com.gianlu.timeless.GrantActivity;
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.Models.Duration;
import com.gianlu.timeless.Models.Project;
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

public class ProjectFragment extends Fragment implements CardsAdapter.ISaveChart, WakaTime.ISummary {
    private static final int REQUEST_CODE = 1;
    private CardsAdapter.IPermissionRequest handler;
    private Date start;
    private Date end;
    private Project project;
    private SwipeRefreshLayout layout;
    private ProgressBar loading;
    private TextView error;
    private RecyclerView list;

    public static ProjectFragment getInstance(Project project, Pair<Date, Date> range) {
        ProjectFragment fragment = new ProjectFragment();
        fragment.setHasOptionsMenu(true);
        Bundle args = new Bundle();
        args.putSerializable("project", project);
        args.putSerializable("start", range.first);
        args.putSerializable("end", range.second);
        args.putString("title", project.name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.project, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.projectFragment_commits).setVisible(project != null && project.hasRepository);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.projectFragment_commits:
                if (project == null) break;
                startActivity(new Intent(getContext(), CommitsActivity.class).putExtra("project_id", project.id));
                break;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (handler != null && requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) handler.onGranted();
            else Toaster.show(getActivity(), Utils.ToastMessages.WRITE_DENIED);
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
                            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                        }
                    }));
        } else {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public void onSaveRequested(View chart, String name) {
        Project project = (Project) getArguments().getSerializable("project");
        if (project != null) {
            File dest = new File(Utils.getImageDirectory(project.name), name + ".png");
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
        } else {
            Toaster.show(getActivity(), Utils.ToastMessages.FAILED_SAVING_CHART, new NullPointerException("Project is null"));
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = (SwipeRefreshLayout) inflater.inflate(R.layout.project_fragment, container, false);
        layout.setColorSchemeResources(Utils.getColors());
        loading = (ProgressBar) layout.findViewById(R.id.projectFragment_loading);
        error = (TextView) layout.findViewById(R.id.projectFragment_error);
        list = (RecyclerView) layout.findViewById(R.id.projectFragment_list);
        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        project = (Project) getArguments().getSerializable("project");
        start = (Date) getArguments().getSerializable("start");
        end = (Date) getArguments().getSerializable("end");

        if (project == null || start == null || end == null) {
            loading.setEnabled(false);
            error.setVisibility(View.VISIBLE);
            return layout;
        }

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WakaTime.getInstance().getRangeSummary(start, end, project, ProjectFragment.this);
            }
        });

        WakaTime.getInstance().getRangeSummary(start, end, project, this);

        return layout;
    }

    @Override
    public void onSummary(final List<Summary> summaries, final Summary summary) {
        if (start.getTime() == end.getTime()) {
            WakaTime.getInstance().getDurations(getContext(), start, project, new WakaTime.IDurations() {
                @Override
                public void onDurations(final List<Duration> durations) {
                    final Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                layout.setRefreshing(false);
                                error.setVisibility(View.GONE);
                                loading.setVisibility(View.GONE);
                                list.setVisibility(View.VISIBLE);

                                list.setAdapter(new CardsAdapter(getContext(), new CardsAdapter.CardsList()
                                        .addSummary(summary)
                                        .addDurations(activity.getString(R.string.durationsSummary), durations)
                                        .addPieChart(activity.getString(R.string.languagesSummary), summary.languages)
                                        .addFileList(activity.getString(R.string.filesSummary), summary.entities), ProjectFragment.this));
                            }
                        });
                    }
                }

                @Override
                public void onException(final Exception ex) {
                    ProjectFragment.this.onException(ex);
                }

                @Override
                public void onWakaTimeException(WakaTimeException ex) {
                    ProjectFragment.this.onWakaTimeException(ex);
                }
            });
        } else {
            final Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        error.setVisibility(View.GONE);
                        loading.setVisibility(View.GONE);
                        list.setVisibility(View.VISIBLE);

                        list.setAdapter(new CardsAdapter(getContext(), new CardsAdapter.CardsList()
                                .addSummary(summary)
                                .addLineChart(activity.getString(R.string.periodActivity), summaries)
                                .addPieChart(activity.getString(R.string.languagesSummary), summary.languages)
                                .addFileList(activity.getString(R.string.filesSummary), summary.entities), ProjectFragment.this));
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
}
