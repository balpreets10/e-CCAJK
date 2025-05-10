package com.mycca.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.mycca.R;
import com.mycca.custom.CustomDrawer.CardDrawerLayout;
import com.mycca.custom.FabRevealMenu.FabView.FABRevealMenu;
import com.mycca.custom.FancyAlertDialog.FancyAlertDialogType;
import com.mycca.custom.Progress.ProgressDialog;
import com.mycca.fragments.AboutUsFragment;
import com.mycca.fragments.AddNewsFragment;
import com.mycca.fragments.BrowserFragment;
import com.mycca.fragments.ContactUsFragment;
import com.mycca.fragments.FeedbackFragment;
import com.mycca.fragments.HomeFragment;
import com.mycca.fragments.InspectionFragment;
import com.mycca.fragments.KYPFragment;
import com.mycca.fragments.LatestNewsFragment;
import com.mycca.fragments.LocatorFragment;
import com.mycca.fragments.LoginFragment;
import com.mycca.fragments.PanAdhaarUploadSubmittableFragment;
import com.mycca.fragments.SavedModelsListFragment;
import com.mycca.fragments.SettingsFragment;
import com.mycca.fragments.SubmitGrievanceFragment;
import com.mycca.fragments.TrackGrievanceFragment;
import com.mycca.fragments.UpdateGrievanceFragment;
import com.mycca.models.StaffModel;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Helper;
import com.mycca.tools.IOHelper;
import com.mycca.tools.LocaleHelper;
import com.mycca.tools.Preferences;

import java.util.ArrayList;
import java.util.List;

import shortbread.Shortcut;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    final int TYPE_ADMIN = 1;
    final int TYPE_STAFF = 2;
    private static final int RC_SIGN_IN = 420;
    boolean first;
    String title;

    FrameLayout frameLayout;
    NavigationView navigationView;
    CardDrawerLayout drawerLayout;
    Toolbar toolbar;
    Fragment fragment;
    Bundle bundle;

    //FancyShowCaseQueue mQueue;
    FABRevealMenu fabRevealMenu;
    StaffModel staffModel;
    ProgressDialog progressDialog;
    FirebaseAuth mAuth;
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void attachBaseContext(Context newBase) {
       super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbar();
        bindViews();
        init();
        if (Preferences.getInstance().getStringPref(this, Preferences.PREF_OFFICE_ADDRESS) == null ||
                Preferences.getInstance().getStringPref(this, Preferences.PREF_WEBSITE) == null ||
                Preferences.getInstance().getStringPref(this, Preferences.PREF_OFFICE_LABEL) == null) {

            CustomLogger.getInstance().logDebug("Other state Preferences null", CustomLogger.Mask.SPLASH_ACTIVITY);
            FireBaseHelper.getInstance().getOtherStateData(this, null);
        }
        showFragment(getString(R.string.home), new HomeFragment(), null);

        first = getIntent().getBooleanExtra("First", false);
        if (first)
            showAuthDialog(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                showFragment(getString(R.string.settings), new SettingsFragment(), null);
                break;
            case R.id.action_invite:
                showInviteIntent();
                break;
            case R.id.action_about_us:
                showFragment(getString(R.string.about_us), new AboutUsFragment(), null);
                break;
            case R.id.action_feedback:
                showFragment(getString(R.string.feedback), new FeedbackFragment(), null);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.navmenu_home:
                showFragment(getString(R.string.home), new HomeFragment(), null);
                break;
            case R.id.navmenu_visit_cca_website:
                fragment = new BrowserFragment();
                bundle = new Bundle();
                String url = Preferences.getInstance().getStringPref(this, Preferences.PREF_WEBSITE);
                if (url == null)
                    Helper.getInstance().showMessage(this, getString(R.string.website_n_a),
                            getString(R.string.app_name), FancyAlertDialogType.WARNING);
                else {
                    bundle.putString("url", url);
                    showFragment(getString(R.string.app_name), fragment, bundle);
                }
                break;
            case R.id.navmenu_pension:
                fragment = new SubmitGrievanceFragment();
                bundle = new Bundle();
                bundle.putInt("Type", R.string.pension);
                title = getString(R.string.pension_grievance);
                if (checkUserAuthenticated()) {
                    showFragment(title, fragment, bundle);
                }
                break;
            case R.id.navmenu_gpf:
                fragment = new SubmitGrievanceFragment();
                bundle = new Bundle();
                bundle.putInt("Type", R.string.gpf);
                title = getString(R.string.gpf_grievance);
                if (checkUserAuthenticated()) {
                    showFragment(title, fragment, bundle);
                }
                break;
            case R.id.navmenu_saved_grievances:
                fragment = new SavedModelsListFragment<>();
                bundle = new Bundle();
                bundle.putString("FileName", IOHelper.GRIEVANCES);
                title = getString(R.string.view_saved_grievance);
                if (checkUserAuthenticated()) {
                    showFragment(title, fragment, bundle);
                }
                break;
            case R.id.navmenu_aadhaar:
                fragment = new PanAdhaarUploadSubmittableFragment();
                bundle = new Bundle();
                bundle.putString("Root", FireBaseHelper.ROOT_ADHAAR);
                title = getString(R.string.upload_aadhaar);
                if (checkUserAuthenticated()) {
                    showFragment(title, fragment, bundle);
                }
                break;
            case R.id.navmenu_pan:
                fragment = new PanAdhaarUploadSubmittableFragment();
                bundle = new Bundle();
                bundle.putString("Root", FireBaseHelper.ROOT_PAN);
                title = getString(R.string.upload_pan);
                if (checkUserAuthenticated()) {
                    showFragment(title, fragment, bundle);
                }
                break;
            case R.id.navmenu_life_certificate:
                fragment = new PanAdhaarUploadSubmittableFragment();
                bundle = new Bundle();
                bundle.putString("Root", FireBaseHelper.ROOT_LIFE);
                title = getString(R.string.upload_life_certificate);
                if (checkUserAuthenticated()) {
                    showFragment(title, fragment, bundle);
                }
                break;
            case R.id.navmenu_remarriage_certificate:
                fragment = new PanAdhaarUploadSubmittableFragment();
                bundle = new Bundle();
                bundle.putString("Root", FireBaseHelper.ROOT_RE_MARRIAGE);
                title = getString(R.string.upload_re_marriage_certificate);
                if (checkUserAuthenticated()) {
                    showFragment(title, fragment, bundle);
                }
                break;
            case R.id.navmenu_reemployment:
                fragment = new PanAdhaarUploadSubmittableFragment();
                bundle = new Bundle();
                bundle.putString("Root", FireBaseHelper.ROOT_RE_EMPLOYMENT);
                title = getString(R.string.upload_re_employment_certificate);
                if (checkUserAuthenticated()) {
                    showFragment(title, fragment, bundle);
                }
                break;
            case R.id.navmenu_kyp:
                fragment = new KYPFragment();
                title = getString(R.string.kyp);
                if (checkUserAuthenticated()) {
                    showFragment(title, fragment, null);
                }
                break;
            case R.id.navmenu_tracking:
                showFragment(getString(R.string.track_grievances), new TrackGrievanceFragment(), null);
                break;
            case R.id.navmenu_contact_us:
                showFragment(getString(R.string.contact_us), new ContactUsFragment(), null);
                break;
            case R.id.navmenu_latest_news:
                fragment = new LatestNewsFragment();
                showFragment(getString(R.string.latest_news), fragment, null);
                break;
            case R.id.navmenu_hotspot_locator:
                bundle = new Bundle();
                bundle.putString("Locator", FireBaseHelper.ROOT_WIFI);
                showFragment(getString(R.string.wifi), new LocatorFragment(), bundle);
                break;
            case R.id.navmenu_gp_locator:
                bundle = new Bundle();
                bundle.putString("Locator", FireBaseHelper.ROOT_GP);
                showFragment(getString(R.string.gp), new LocatorFragment(), bundle);
                break;
            case R.id.navmenu_login:
                fragment = new LoginFragment();
                title = getString(R.string.app_name);
                if (checkUserAuthenticated())
                    showFragment(title, fragment, null);
                break;
            case R.id.navmenu_update_grievances:
                fragment = new UpdateGrievanceFragment();
                title = getString(R.string.update_grievances);
                showFragment(title, fragment, null);
                break;
            case R.id.navmenu_inspection:
                fragment = new InspectionFragment();
                title = getString(R.string.inspection);
                showFragment(title, fragment, null);
                break;
            case R.id.navmenu_saved_inspections:
                fragment = new SavedModelsListFragment<>();
                bundle = new Bundle();
                bundle.putString("FileName", IOHelper.INSPECTIONS);
                title = getString(R.string.saved_inspections);
                showFragment(title, fragment, bundle);
                break;
            case R.id.navmenu_add_news:
                fragment = new AddNewsFragment();
                title = getString(R.string.add_news);
                showFragment(title, fragment, null);
                break;
            case R.id.navmenu_logout:
                logout();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Shortcut(id = "hotspotNearby", icon = R.drawable.ic_wifi_black_24dp, shortLabel = "HotSpot Locations")
    public void ShowHotSpotLocations() {
        CustomLogger.getInstance().logDebug("ShowHotSpotLocations", CustomLogger.Mask.MAIN_ACTIVITY);
        Bundle bundle = new Bundle();
        bundle.putString("Locator", FireBaseHelper.ROOT_WIFI);
        showFragment(getString(R.string.wifi), new LocatorFragment(), bundle);
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void bindViews() {
        frameLayout = findViewById(R.id.fragmentPlaceholder);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        progressDialog = new ProgressDialog(this);
    }

    private void init() {

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Helper.getInstance().hideKeyboardFrom(MainActivity.this);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        drawerLayout.setViewScale(Gravity.START, 0.85f);
        drawerLayout.setRadius(Gravity.START, 35);
        drawerLayout.setViewElevation(Gravity.START, 30);
        actionBarDrawerToggle.syncState();

        staffModel = Preferences.getInstance().getStaffPref(this);
        if (staffModel != null) {
            if (staffModel.getType() == TYPE_ADMIN)
                ManageNavigationView(true, true);
            else
                ManageNavigationView(true, false);
        } else {
            ManageNavigationView(false, false);
        }
        navigationView.setNavigationItemSelectedListener(this);

        mAuth = FireBaseHelper.getInstance().getAuth();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    public void showFragment(String title, Fragment fragment, Bundle bundle) {
        this.fragment = null;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setSubtitle("");
        }
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentPlaceholder, fragment).commit();
        navigationView.setCheckedItem(R.id.navmenu_home);
    }

    private boolean checkUserAuthenticated() {
        if (mAuth.getCurrentUser() == null) {
            showAuthDialog(true);
            return false;
        } else
            return true;
    }

    public void showAuthDialog(boolean skipped) {
        if (skipped) {
            Helper.getInstance().showFancyAlertDialog(this,
                    getString(R.string.auth_message1),
                    getString(R.string.sign_in_with_google),
                    getString(R.string.sign_in),
                    this::signInWithGoogle,
                    getString(R.string.cancel),
                    () -> {

                    },
                    FancyAlertDialogType.WARNING);
        } else Helper.getInstance().showFancyAlertDialog(this,
                getString(R.string.auth_message2),
                getString(R.string.sign_in_with_google),
                getString(R.string.sign_in),
                this::signInWithGoogle,
                getString(R.string.skip),
                () -> {
                },
                FancyAlertDialogType.WARNING
        );
    }

    public void signInWithGoogle() {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        CustomLogger.getInstance().logVerbose("Logging with google", CustomLogger.Mask.MAIN_ACTIVITY);
    }

    public void signOutFromGoogle() {
        mAuth.signOut();
        mGoogleSignInClient.signOut();

        if (staffModel != null) {
            Preferences.getInstance().clearPrefs(MainActivity.this, Preferences.PREF_STAFF_DATA);
            ManageNavigationView(false, false);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        CustomLogger.getInstance().logDebug("firebaseAuthWithGoogle: " + credential.getSignInMethod(), CustomLogger.Mask.MAIN_ACTIVITY);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {

                        CustomLogger.getInstance().logDebug("signInWithCredential:success", CustomLogger.Mask.MAIN_ACTIVITY);
                        FireBaseHelper.getInstance().addTokenOnFireBase();
                        Helper.getInstance().showFancyAlertDialog(MainActivity.this, "",
                                getString(R.string.sign_in_success),
                                getString(R.string.ok),
                                () -> {
                                    if (fragment != null) {
                                        showFragment(title, fragment, bundle);
                                    }
                                },
                                null, null, FancyAlertDialogType.SUCCESS);

                        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragmentPlaceholder);
                        if (f instanceof SettingsFragment)
                            ((SettingsFragment) f).manageSignOut();
                        else if (f instanceof HomeFragment)
                            ((HomeFragment) f).setupWelcomeBar();

                    } else {
                        CustomLogger.getInstance().logWarn("signInWithCredential:failure", task.getException(), CustomLogger.Mask.MAIN_ACTIVITY);
                        if (task.getException().getClass() == FirebaseAuthInvalidUserException.class) {
                            Helper.getInstance().showFancyAlertDialog(MainActivity.this, getString(R.string.try_again_different_account), getString(R.string.sign_in_fail), getString(R.string.ok), null, null, null, FancyAlertDialogType.ERROR);
                        } else {
                            Helper.getInstance().showFancyAlertDialog(MainActivity.this, getString(R.string.try_again), getString(R.string.sign_in_fail), getString(R.string.ok), null, null, null, FancyAlertDialogType.ERROR);
                        }
                    }

                });
    }

    private void logout() {
        Helper.getInstance().showFancyAlertDialog(this,
                "",
                getString(R.string.logout),
                getString(R.string.ok),
                () -> {
                    showFragment(getString(R.string.home), new HomeFragment(), null);
                    Preferences.getInstance().clearPrefs(MainActivity.this, Preferences.PREF_STAFF_DATA);
                    ManageNavigationView(false, false);
                    Helper.getInstance().showFancyAlertDialog(MainActivity.this, "", getString(R.string.logged_out), getString(R.string.ok), () -> {
                    }, null, null, FancyAlertDialogType.SUCCESS);

                },
                getString(R.string.cancel),
                () -> {

                },
                FancyAlertDialogType.WARNING);


    }

    private void doExit() {

        Helper.getInstance().showFancyAlertDialog(this,
                "",
                getString(R.string.exit),
                getString(R.string.ok),
                this::finish,
                getString(R.string.cancel),
                () -> {
                },
                FancyAlertDialogType.WARNING);
    }

    public void OnLoginFailure(String message) {
        Helper.getInstance().showFancyAlertDialog(this,
                message, getString(R.string.login), getString(R.string.ok), null, null, null, FancyAlertDialogType.ERROR);
    }

    public void OnLoginSuccessful(StaffModel staffModel) {
        this.staffModel = staffModel;
        Toast.makeText(this, getString(R.string.staff_login_success), Toast.LENGTH_SHORT).show();
        Preferences.getInstance().setModelPref(this, Preferences.PREF_STAFF_DATA, staffModel);
        if (staffModel.getType() == TYPE_ADMIN) {
            ManageNavigationView(true, true);
        } else {
            ManageNavigationView(true, false);
        }

        showFragment(getString(R.string.home), new HomeFragment(), null);
    }

    public void ManageNavigationView(boolean signedIn, boolean admin) {
        MenuItem menuItem = navigationView.getMenu().findItem(R.id.staff_panel);
        if (signedIn) {
            navigationView.getMenu().findItem(R.id.staff_login).setVisible(false);
            menuItem.setVisible(true);
            if (admin) {
                menuItem.getSubMenu().findItem(R.id.navmenu_inspection).setVisible(true);
                menuItem.getSubMenu().findItem(R.id.navmenu_saved_inspections).setVisible(true);
            } else {
                menuItem.getSubMenu().findItem(R.id.navmenu_inspection).setVisible(false);
                menuItem.getSubMenu().findItem(R.id.navmenu_saved_inspections).setVisible(false);
            }
        } else {
            navigationView.getMenu().findItem(R.id.staff_login).setVisible(true);
            menuItem.setVisible(false);
        }
    }

    private void showInviteIntent() {

        Resources resources = getResources();
        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SEND);
        // Native email client doesn't currently support HTML, but it doesn't hurt to try in case they fix it
        //emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(resources.getString(R.string.share_email_native)));
        //emailIntent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.share_email_subject));
        emailIntent.setType("message/rfc822");

        PackageManager pm = getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");

        Intent openInChooser = Intent.createChooser(emailIntent, resources.getString(R.string.app_name));

        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<>();
        for (int i = 0; i < resInfo.size(); i++) {
            // Extract the label, append it, and repackage it in a LabeledIntent
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;
            if (packageName.contains("android.email")) {
                emailIntent.setPackage(packageName);
            } else if (packageName.contains("com.twitter.android") || packageName.contains("com.facebook") || packageName.contains("mms") || packageName.contains("messaging") || packageName.contains("com.whatsapp") || packageName.contains("com.google.android.gm")) {

                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");

                if (packageName.contains("twitter")) {
                    intent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.invitation_deep_link));
                } else if (packageName.contains("facebook")) {
                    intent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.invitation_message_heading) + "\n\n" + resources.getString(R.string.invitation_deep_link));
                } else if (packageName.contains("mms") || packageName.contains("messaging") || packageName.contains("whatsapp")) {
                    intent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.invitation_message_heading) + "\n\n" + resources.getString(R.string.invitation_deep_link));
                } else if (packageName.contains("android.gm")) { // If Gmail shows up twice, try removing this else-if clause and the reference to "android.gm" above
                    intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.invitation_message));
                    intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(resources.getString(R.string.invitation_message_heading) + "<br><br>" + resources.getString(R.string.invitation_deep_link)));
                    intent.setType("message/rfc822");
                } else {
                    intent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.invitation_message_heading) + "\n\n" + resources.getString(R.string.invitation_deep_link));
                }
                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }
        // convert intentList to array
        LabeledIntent[] extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);

        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        startActivity(openInChooser);
    }

    public FABRevealMenu getFabRevealMenu() {
        return fabRevealMenu;
    }

    public void setFabRevealMenu(FABRevealMenu fabRevealMenu) {
        this.fabRevealMenu = fabRevealMenu;
    }

    //    public FancyShowCaseQueue getmQueue() {
//        return mQueue;
//    }
//
//    public void setmQueue(FancyShowCaseQueue mQueue) {
//        this.mQueue = mQueue;
//    }

    @Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragmentPlaceholder);
        if (fabRevealMenu != null && fabRevealMenu.isShowing()) {
            fabRevealMenu.closeMenu();
        } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);

        } else if (f instanceof HomeFragment) {
            doExit();
        } else if (f instanceof BrowserFragment) {
            if (((BrowserFragment) f).canGoBack()) {
                ((BrowserFragment) f).goBack();
            } else {
                ((BrowserFragment) f).stopLoading();
                showFragment(getString(R.string.home), new HomeFragment(), null);
            }
        } else {
            showFragment(getString(R.string.home), new HomeFragment(), null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CustomLogger.getInstance().logDebug(Integer.toString(resultCode), CustomLogger.Mask.MAIN_ACTIVITY);
        List<Fragment> allFragments = getSupportFragmentManager().getFragments();

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                progressDialog.setMessage(getString(R.string.signing_in));
                progressDialog.show();
                GoogleSignInAccount account = task.getResult(ApiException.class);
                CustomLogger.getInstance().logDebug("signed in: " + account.getEmail(), CustomLogger.Mask.MAIN_ACTIVITY);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                CustomLogger.getInstance().logWarn("Google sign in failed", e, CustomLogger.Mask.MAIN_ACTIVITY);
                progressDialog.dismiss();
                Helper.getInstance().showFancyAlertDialog(MainActivity.this, getString(R.string.try_again), getString(R.string.sign_in_fail), getString(R.string.ok), null, null, null, FancyAlertDialogType.ERROR);
            }
        } else {
            for (Fragment frag : allFragments) {
                frag.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> allFragments = getSupportFragmentManager().getFragments();

        for (Fragment frag : allFragments) {
            CustomLogger.getInstance().logDebug("onRequestPermissionsResult: " + frag.toString(), CustomLogger.Mask.MAIN_ACTIVITY);
            frag.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
