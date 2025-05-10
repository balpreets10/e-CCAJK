package com.mycca.fragments;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.mycca.R;
import com.mycca.activity.MainActivity;
import com.mycca.adapter.RecyclerViewAdapterNews;
import com.mycca.custom.CustomImageSlider.SliderLayout;
import com.mycca.custom.CustomImageSlider.SliderTypes.BaseSliderView;
import com.mycca.custom.CustomImageSlider.SliderTypes.TextSliderView;
import com.mycca.custom.CustomImageSlider.Tricks.ViewPagerEx;
import com.mycca.custom.FancyAlertDialog.FancyAlertDialogType;
import com.mycca.models.NewsModel;
import com.mycca.models.SliderImageModel;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.Helper;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Preferences;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    SliderLayout sliderLayout;
    RecyclerView recyclerView;
    TextView tvLatestNews, tvUserName, tvVisit;
    ImageButton moveRight, moveLeft;
    LinearLayoutManager linearLayoutManager;
    View view;
    final String TAG = "HomeFragment";
    RecyclerViewAdapterNews adapterNews;
    ArrayList<NewsModel> newsModelArrayList;
    MainActivity activity;

    public HomeFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);
        bindViews(view);
        init();
        //        if (Preferences.getInstance().getBooleanPref(getContext(), Preferences.PREF_HELP_HOME)) {
//            showTutorial();
//            Preferences.getInstance().setBooleanPref(getContext(), Preferences.PREF_HELP_HOME, false);
//        }
        return view;
    }

    private void bindViews(View view) {
        sliderLayout = view.findViewById(R.id.slider_home);
        tvLatestNews = view.findViewById(R.id.tv_home_latest_news);
        recyclerView = view.findViewById(R.id.recycler_view_home_latest_news);
        moveRight = view.findViewById(R.id.img_btn_move_right);
        moveLeft = view.findViewById(R.id.img_btn_move_left);
        tvUserName = view.findViewById(R.id.tv_home_username);
        tvVisit = view.findViewById(R.id.tv_home_visit);
    }

    private void init() {

        activity = (MainActivity) getActivity();
        tvVisit.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_drawable_location, 0, R.drawable.ic_keyboard_arrow_right_black_24dp, 0);
        tvLatestNews.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_news_icon, 0, R.drawable.ic_keyboard_arrow_right_black_24dp, 0);

        tvVisit.setOnClickListener(v -> {
            String location = Preferences.getInstance().getStringPref(activity, Preferences.PREF_OFFICE_COORDINATES);
            String label = Preferences.getInstance().getStringPref(activity, Preferences.PREF_OFFICE_LABEL);
            if (label != null) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + location + label);

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                if (mapIntent.resolveActivity(activity.getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(getContext(), getString(R.string.no_map_app), Toast.LENGTH_SHORT).show();
                }
            } else {
                Helper.getInstance().showMessage(activity, getString(R.string.office_location_n_a),
                        getString(R.string.app_name), FancyAlertDialogType.WARNING);
            }
        });

        tvLatestNews.setOnClickListener(v -> activity.showFragment(getString(R.string.latest_news), new LatestNewsFragment(), null));

        newsModelArrayList = new ArrayList<>();
        adapterNews = new RecyclerViewAdapterNews(newsModelArrayList, activity, true);

        recyclerView.setAdapter(adapterNews);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        //        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true);
//        linearLayoutManager.setStackFromEnd(true);
//        recyclerView.setLayoutManager(linearLayoutManager);
//        SnapHelper snapHelperStart = new GravitySnapHelper(Gravity.START);
//        snapHelperStart.attachToRecyclerView(recyclerView);
//
//        moveRight.setOnClickListener(v -> {
//            if (linearLayoutManager.findLastVisibleItemPosition() - 1 >= 0)
//                recyclerView.smoothScrollToPosition(linearLayoutManager.findLastVisibleItemPosition() - 1);
//        });
//
//        moveLeft.setOnClickListener(v -> recyclerView.smoothScrollToPosition(linearLayoutManager.findFirstVisibleItemPosition() + 1));

        getNews();
        setupWelcomeBar();
        getSliderData();

    }

    private void getNews() {

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    NewsModel newsModel = dataSnapshot.getValue(NewsModel.class);
                    newsModelArrayList.add(0, newsModel);
                    adapterNews.notifyItemInserted(0);
                    CustomLogger.getInstance().logDebug("onChildAdded: " + newsModel.getHeadline(), CustomLogger.Mask.HOME_FRAGMENT);
                    recyclerView.smoothScrollToPosition(newsModelArrayList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    NewsModel newsModel = dataSnapshot.getValue(NewsModel.class);
                    for (int i = 0; i < newsModelArrayList.size(); i++) {
                        if (newsModel != null && newsModelArrayList.get(i).getKey().equals(newsModel.getKey())) {
                            newsModelArrayList.remove(i);
                            adapterNews.notifyItemRemoved(i);
                            newsModelArrayList.add(i, newsModel);
                            adapterNews.notifyItemInserted(i);
                            CustomLogger.getInstance().logDebug("onChildChanged: " + newsModel.getHeadline() + " pos - " + i, CustomLogger.Mask.HOME_FRAGMENT);
                            break;
                        }
                    }

                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    NewsModel newsModel = dataSnapshot.getValue(NewsModel.class);
                    for (NewsModel nm : newsModelArrayList) {
                        if (newsModel != null && nm.getKey().equals(newsModel.getKey())) {
                            newsModelArrayList.remove(nm);
                            break;
                        }
                    }
                    adapterNews.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        FireBaseHelper.getInstance().getDataFromFireBase(null, childEventListener, FireBaseHelper.ROOT_NEWS);
    }

    public void setupWelcomeBar() {
        FirebaseUser user = FireBaseHelper.getInstance().getAuth().getCurrentUser();
        if (user != null) {
            tvUserName.setVisibility(View.VISIBLE);
            String username = getString(R.string.hello) + " " + user.getDisplayName();
            tvUserName.setText(username);
        } else
            tvUserName.setVisibility(View.GONE);
    }

    private void getSliderData() {
        CustomLogger.getInstance().logDebug("getting SliderData: ", CustomLogger.Mask.HOME_FRAGMENT);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                CustomLogger.getInstance().logDebug("ChildAdded: ", CustomLogger.Mask.HOME_FRAGMENT);
                SliderImageModel sliderImageModel = dataSnapshot.getValue(SliderImageModel.class);
                if (sliderImageModel != null) {
                    CustomLogger.getInstance().logDebug("Image: " + sliderImageModel.getImageName(), CustomLogger.Mask.HOME_FRAGMENT);
                    CustomLogger.getInstance().logDebug("url: " + sliderImageModel.getImageUrl(), CustomLogger.Mask.HOME_FRAGMENT);
                    addImageToSlider(sliderImageModel);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        FireBaseHelper.getInstance().getDataFromFireBase(null, childEventListener, FireBaseHelper.ROOT_SLIDER);
    }

    private void addImageToSlider(SliderImageModel sliderImageModel) {

        TextSliderView textSliderView = new TextSliderView(getContext());
        // initialize a SliderLayout
        textSliderView
                .description(sliderImageModel.getTitle())
                .image(sliderImageModel.getImageUrl())
                .setProgressBarVisible(true)
                .setBackgroundColor(Color.WHITE)
                .setOnSliderClickListener(this);

        //add your extra information
        textSliderView.bundle(new Bundle());
        textSliderView.getBundle().putString("extraTitle", sliderImageModel.getTitle());
        textSliderView.getBundle().putString("extraLink", sliderImageModel.getLinkUrl());
        sliderLayout.addSlider(textSliderView);
    }

    private void loadWebSite(String title, String url) {

        if (url != null) {
            BrowserFragment browserFragment = new BrowserFragment();
            Bundle bundle = new Bundle();
            bundle.putString("url", url);
            activity.showFragment(title, browserFragment, bundle);
        }
    }

    @Override
    public void onStop() {
        sliderLayout.stopAutoCycle();
        super.onStop();
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

        String name = (String) slider.getBundle().get("extraTitle");
        String link = (String) slider.getBundle().get("extraLink");
        loadWebSite(name, link);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        CustomLogger.getInstance().logDebug("Slider Page Changed: " + position, CustomLogger.Mask.HOME_FRAGMENT);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

}

//    private void showTutorial() {
//
//        final FancyShowCaseView fancyShowCaseView1 = new FancyShowCaseView.Builder(activity)
//                .title("Tap on images to open respective websites. Tap anywhere to continue")
//                .focusOn(sliderLayout)
//                .focusShape(FocusShape.ROUNDED_RECTANGLE)
//                .fitSystemWindows(true)
//                .build();
//
//        final FancyShowCaseView fancyShowCaseView2 = new FancyShowCaseView.Builder(activity)
//                .title("Tap on news to view in detail")
//                .focusOn(recyclerView)
//                .focusShape(FocusShape.ROUNDED_RECTANGLE)
//                .titleStyle(R.style.FancyShowCaseDefaultTitleStyle, Gravity.BOTTOM | Gravity.CENTER)
//                .fitSystemWindows(true)
//                .build();
//
//        final FancyShowCaseView fancyShowCaseView3 = new FancyShowCaseView.Builder(activity)
//                .title("Open Main Menu from here")
//                .focusCircleAtPosition(0, 0, 200)
//                .build();
//
//        final FancyShowCaseView fancyShowCaseView4 = new FancyShowCaseView.Builder(activity)
//                .title("Touch here to open Secondary Menu")
//                .focusCircleAtPosition(Resources.getSystem().getDisplayMetrics().widthPixels, 0, 200)
//                .build();
//
//        activity.setmQueue(new FancyShowCaseQueue()
//                .add(fancyShowCaseView1)
//                .add(fancyShowCaseView2)
//                .add(fancyShowCaseView3)
//                .add(fancyShowCaseView4));
//        activity.getmQueue().setCompleteListener(() -> {
//            activity.setmQueue(null);
//            if (FireBaseHelper.getInstance(getContext()).mAuth.getCurrentUser() == null)
//                activity.showAuthDialog(false);
//        });
//
//        activity.getmQueue().show();
//    }