package com.gianlu.timeless;


import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.android.vending.billing.IInAppBillingService;
import com.gianlu.commonutils.Billing.Billing;
import com.gianlu.commonutils.Billing.Product;
import com.gianlu.commonutils.Billing.ProductAdapter;
import com.gianlu.commonutils.Billing.PurchasedProduct;
import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.LogsActivity;
import com.google.android.gms.analytics.HitBuilders;

import org.json.JSONException;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class PreferencesActivity extends AppCompatPreferenceActivity {
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.preferences);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item))
                NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        if (header.iconRes == R.drawable.ic_announcement_black_24dp) {
            startActivity(new Intent(this, LogsActivity.class));
            return;
        }

        super.onHeaderClick(header, position);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || ThirdPartFragment.class.getName().equals(fragmentName)
                || AboutFragment.class.getName().equals(fragmentName);
    }

    public static class ThirdPartFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.thrid_part_pref);
            getActivity().setTitle(R.string.third_part);
            setHasOptionsMenu(true);

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton(android.R.string.ok, null);

            findPreference("mpAndroidChart").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    CommonUtils.showDialog(getActivity(), builder
                            .setTitle("MPAndroidChart")
                            .setMessage(R.string.mpAndroidChart_details));
                    return true;
                }
            });

            findPreference("materialDateRangePicker").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    CommonUtils.showDialog(getActivity(), builder
                            .setTitle("MaterialDateRangePicker")
                            .setMessage(R.string.materialDateRangePicker_details));
                    return true;
                }
            });

            findPreference("scribejava").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    CommonUtils.showDialog(getActivity(), builder
                            .setTitle("ScribeJava")
                            .setMessage(R.string.scribejava_details));
                    return true;
                }
            });

            findPreference("apacheLicense").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.apache.org/licenses/LICENSE-2.0")));
                    return true;
                }
            });

            findPreference("mitLicense").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://opensource.org/licenses/MIT")));
                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                startActivity(new Intent(getActivity(), PreferencesActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class AboutFragment extends PreferenceFragment {
        private int requestCode;
        private String devString;
        private ProgressDialog pd;
        private IInAppBillingService billingService;
        private ServiceConnection serviceConnection;

        @Override
        public void onStart() {
            super.onStart();

            if (billingService == null) {
                serviceConnection = new ServiceConnection() {
                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        billingService = null;
                    }

                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        billingService = IInAppBillingService.Stub.asInterface(service);
                        if (pd != null && pd.isShowing())
                            donate();
                    }
                };

                getActivity().bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND").setPackage("com.android.vending"),
                        serviceConnection, Context.BIND_AUTO_CREATE);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == this.requestCode) {
                if (data.getIntExtra("RESPONSE_CODE", RESULT_CANCELED) == RESULT_OK) {
                    try {
                        PurchasedProduct purchasedProduct = new PurchasedProduct(data.getStringExtra("INAPP_PURCHASE_DATA"));
                        if (Objects.equals(purchasedProduct.developerPayload, devString)) {
                            if (purchasedProduct.purchaseState == PurchasedProduct.PURCHASED) {
                                CommonUtils.UIToast(getActivity(), CommonUtils.ToastMessage.THANK_YOU, "Purchased " + purchasedProduct.productId + " with order ID " + purchasedProduct.orderId);
                            } else if (purchasedProduct.purchaseState == PurchasedProduct.CANCELED) {
                                CommonUtils.UIToast(getActivity(), CommonUtils.ToastMessage.PURCHASING_CANCELED);
                            }
                        } else {
                            CommonUtils.UIToast(getActivity(), CommonUtils.ToastMessage.FAILED_BUYING_ITEM, new Exception("Payloads mismatch!"));
                        }
                    } catch (JSONException ex) {
                        CommonUtils.UIToast(getActivity(), CommonUtils.ToastMessage.FAILED_BUYING_ITEM, ex);
                    }
                } else {
                    CommonUtils.UIToast(getActivity(), CommonUtils.ToastMessage.PURCHASING_CANCELED);
                }
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.about_pref);
            getActivity().setTitle(R.string.about_app);
            setHasOptionsMenu(true);

            pd = CommonUtils.fastIndeterminateProgressDialog(getActivity(), R.string.connectingBillingService);
            findPreference("donate").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    donate();
                    return true;
                }
            });

            findPreference("email").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    CommonUtils.sendEmail(getActivity(), getString(R.string.app_name));
                    return true;
                }
            });

            try {
                findPreference("app_version").setSummary(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
            } catch (PackageManager.NameNotFoundException ex) {
                findPreference("app_version").setSummary(R.string.unknown);
            }
        }

        private void donate() {
            CommonUtils.showDialog(getActivity(), pd);
            if (billingService == null)
                return;

            Billing.requestProductsDetails(getActivity(), billingService, new Billing.IRequestProductDetails() {
                @Override
                public void onReceivedDetails(final Billing.IRequestProductDetails handler, final List<Product> products) {
                    final Billing.IBuyProduct buyHandler = new Billing.IBuyProduct() {
                        @Override
                        public void onGotIntent(PendingIntent intent, String developerString) {
                            devString = developerString;
                            requestCode = new Random().nextInt();

                            try {
                                getActivity().startIntentSenderForResult(intent.getIntentSender(), requestCode, new Intent(), 0, 0, 0);
                            } catch (IntentSender.SendIntentException ex) {
                                CommonUtils.UIToast(getActivity(), CommonUtils.ToastMessage.FAILED_CONNECTION_BILLING_SERVICE, ex);
                            }
                        }

                        @Override
                        public void onAPIException(int code) {
                            handler.onAPIException(code);
                        }

                        @Override
                        public void onUserCancelled() {
                            handler.onUserCancelled();
                        }

                        @Override
                        public void onFailed(Exception ex) {
                            CommonUtils.UIToast(getActivity(), CommonUtils.ToastMessage.FAILED_CONNECTION_BILLING_SERVICE, ex);
                        }
                    };
                    pd.dismiss();

                    RecyclerView list = new RecyclerView(getActivity());
                    list.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                    list.setAdapter(new ProductAdapter(getActivity(), products, new ProductAdapter.IAdapter() {
                        @Override
                        public void onItemSelected(Product product) {
                            Billing.buyProduct(getActivity(), billingService, product, buyHandler);
                        }
                    }));

                    CommonUtils.showDialog(getActivity(), new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.donate))
                            .setNegativeButton(android.R.string.cancel, null)
                            .setView(list));

                    ThisApplication.sendAnalytics(getActivity(), new HitBuilders.EventBuilder()
                            .setCategory(ThisApplication.CATEGORY_USER_INPUT)
                            .setAction(ThisApplication.ACTION_DONATE_OPEN)
                            .build());
                }

                @Override
                public void onAPIException(int code) {
                    if (code == Billing.RESULT_BILLING_UNAVAILABLE)
                        CommonUtils.UIToast(getActivity(), CommonUtils.ToastMessage.FAILED_CONNECTION_BILLING_SERVICE, "Code: " + code);
                    else
                        CommonUtils.UIToast(getActivity(), CommonUtils.ToastMessage.FAILED_BUYING_ITEM, "Code: " + code);
                }

                @Override
                public void onUserCancelled() {
                    CommonUtils.UIToast(getActivity(), CommonUtils.ToastMessage.BILLING_USER_CANCELLED);
                }

                @Override
                public void onFailed(Exception ex) {
                    CommonUtils.UIToast(getActivity(), CommonUtils.ToastMessage.FAILED_CONNECTION_BILLING_SERVICE, ex);
                }
            });
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            if (billingService != null)
                getActivity().unbindService(serviceConnection);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                startActivity(new Intent(getActivity(), PreferencesActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
