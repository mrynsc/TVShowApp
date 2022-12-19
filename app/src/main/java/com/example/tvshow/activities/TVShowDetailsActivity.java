package com.example.tvshow.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;


import android.content.Intent;
import android.content.res.Resources;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tvshow.R;
import com.example.tvshow.adapters.EpisodesAdapter;
import com.example.tvshow.adapters.ImageSliderAdapter;
import com.example.tvshow.adapters.PostClass;
import com.example.tvshow.databinding.ActivityTvshowDetailsBinding;
import com.example.tvshow.databinding.LayoutEpisodesBottomSheetBinding;
import com.example.tvshow.models.TVShow;
import com.example.tvshow.viewmodels.TVShowDetailsViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class TVShowDetailsActivity extends AppCompatActivity {

    private ActivityTvshowDetailsBinding activityTvshowDetailsBinding;
    private TVShowDetailsViewModel tvShowDetailsViewModel;
    private BottomSheetDialog episodesBottomSheetDialog;
    private LayoutEpisodesBottomSheetBinding layoutEpisodesBottomSheetBinding;
    private TVShow tvShow;
    private Boolean isTVShowAvailableInWatchlist = false;
    private ArrayList<String> usernamesFromParse;
    private ArrayList<String> userCommentsFromParse;
    private ArrayList<Bitmap> userImageFromParse;
    private PostClass postClass;
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        download();
        listView = findViewById(R.id.commentsList);

        usernamesFromParse = new ArrayList<>();
        userCommentsFromParse = new ArrayList<>();
        userImageFromParse = new ArrayList<>();

        postClass = new PostClass(usernamesFromParse,userCommentsFromParse,userImageFromParse,this);

        listView.setAdapter(postClass);




        activityTvshowDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_tvshow_details);
        doInitialization();
    }

    private void doInitialization() {

        tvShowDetailsViewModel = new ViewModelProvider(this).get(TVShowDetailsViewModel.class);
        activityTvshowDetailsBinding.imageBack.setOnClickListener(view -> onBackPressed());
        tvShow = (TVShow) getIntent().getSerializableExtra("tvShow");
        checkTVShowInWatchlist();
        getTVShowDetails();

    }

    private void checkTVShowInWatchlist() {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(tvShowDetailsViewModel.getTVShowFromWatchlist(String.valueOf(tvShow.getId()))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tvShow -> {
                    isTVShowAvailableInWatchlist = true;
                    activityTvshowDetailsBinding.imageWatchList.setImageResource(R.drawable.ic_add);
                    compositeDisposable.dispose();
                }));
    }

    public void download() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if (e != null) {
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                } else {

                    if (objects.size() > 0) {

                        for (final ParseObject object : objects) {

                            ParseFile parseFile = (ParseFile) object.get("image");

                            parseFile.getDataInBackground(new GetDataCallback() {
                                @Override
                                public void done(byte[] data, ParseException e) {

                                    if (e == null && data != null) {

                                        Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);

                                        userImageFromParse.add(bitmap);
                                        usernamesFromParse.add(object.getString("username"));
                                        userCommentsFromParse.add(object.getString("comment"));

                                        postClass.notifyDataSetChanged();


                                    }

                                }
                            });

                        }

                    }

                }

            }
        });


    }


    private void getTVShowDetails() {

        activityTvshowDetailsBinding.setIsLoading(true);
        String tvShowId = String.valueOf(tvShow.getId());
        tvShowDetailsViewModel.getTVShowDetails(tvShowId).observe(this, tvShowDetailsResponse -> {
            activityTvshowDetailsBinding.setIsLoading(false);
            if (tvShowDetailsResponse.getTvShowDetails() != null) {
                if (tvShowDetailsResponse.getTvShowDetails().getPictures() != null) {
                    loadImageSlider(tvShowDetailsResponse.getTvShowDetails().getPictures());
                }
                activityTvshowDetailsBinding.setTvShowImageURL(tvShowDetailsResponse.getTvShowDetails().getImagePath());
                activityTvshowDetailsBinding.imageTvShow.setVisibility(View.VISIBLE);
                activityTvshowDetailsBinding.setDescription(String.valueOf(HtmlCompat.fromHtml(tvShowDetailsResponse
                        .getTvShowDetails().getDescription(), HtmlCompat.FROM_HTML_MODE_LEGACY)));


                activityTvshowDetailsBinding.textDescription.setVisibility(View.VISIBLE);
                activityTvshowDetailsBinding.textReadMore.setVisibility(View.VISIBLE);
                activityTvshowDetailsBinding.textReadMore.setOnClickListener(view -> {
                    if (activityTvshowDetailsBinding.textReadMore.getText().toString().equals("Read More")) {
                        activityTvshowDetailsBinding.textDescription.setMaxLines(Integer.MAX_VALUE);
                        activityTvshowDetailsBinding.textDescription.setEllipsize(null);
                        activityTvshowDetailsBinding.textReadMore.setText(R.string.read_less);
                    } else {
                        activityTvshowDetailsBinding.textDescription.setMaxLines(4);
                        activityTvshowDetailsBinding.textDescription.setEllipsize(TextUtils.TruncateAt.END);
                        activityTvshowDetailsBinding.textReadMore.setText(R.string.read_more);

                    }
                });
                activityTvshowDetailsBinding.setRating(
                        String.format(
                                Locale.getDefault(),
                                "%.2f",
                                Double.parseDouble(tvShowDetailsResponse.getTvShowDetails().getRating())
                        )
                );
                if (tvShowDetailsResponse.getTvShowDetails().getGenres() != null) {
                    activityTvshowDetailsBinding.setGenre(tvShowDetailsResponse.getTvShowDetails().getGenres()[0]);
                } else {
                    activityTvshowDetailsBinding.setGenre("N/A");
                }
                activityTvshowDetailsBinding.setRuntime(tvShowDetailsResponse.getTvShowDetails().getRuntime() + "Min");
                activityTvshowDetailsBinding.viewDivider1.setVisibility(View.VISIBLE);
                activityTvshowDetailsBinding.layoutMisc.setVisibility(View.VISIBLE);
                activityTvshowDetailsBinding.viewDivider2.setVisibility(View.VISIBLE);

                activityTvshowDetailsBinding.uploadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(getApplicationContext(),CommentActivity.class);
                        startActivity(intent);
                    }
                });

                activityTvshowDetailsBinding.buttonEpisodes.setVisibility(View.VISIBLE);
                activityTvshowDetailsBinding.buttonEpisodes.setOnClickListener(view -> {
                    if (episodesBottomSheetDialog == null) {
                        episodesBottomSheetDialog = new BottomSheetDialog(TVShowDetailsActivity.this);
                        layoutEpisodesBottomSheetBinding = DataBindingUtil.inflate(LayoutInflater.from(TVShowDetailsActivity.this),
                                R.layout.layout_episodes_bottom_sheet, findViewById(R.id.episodesContainer), false);
                        episodesBottomSheetDialog.setContentView(layoutEpisodesBottomSheetBinding.getRoot());
                        layoutEpisodesBottomSheetBinding.episodesRecyclerView.setAdapter(
                                new EpisodesAdapter(tvShowDetailsResponse.getTvShowDetails().getEpisodes())
                        );
                        layoutEpisodesBottomSheetBinding.textTitle.setText(
                                String.format("Episodes | %s", tvShow.getName())
                        );
                        layoutEpisodesBottomSheetBinding.imageClose.setOnClickListener(view1 -> episodesBottomSheetDialog.dismiss());
                    }
                    FrameLayout frameLayout = episodesBottomSheetDialog.findViewById(
                            com.google.android.material.R.id.design_bottom_sheet
                    );
                    if (frameLayout != null) {
                        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(frameLayout);
                        bottomSheetBehavior.setPeekHeight(Resources.getSystem().getDisplayMetrics().heightPixels);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                    episodesBottomSheetDialog.show();

                });
                activityTvshowDetailsBinding.imageWatchList.setOnClickListener(view -> {
                    CompositeDisposable compositeDisposable = new CompositeDisposable();
                    if (isTVShowAvailableInWatchlist) {
                        compositeDisposable.add(tvShowDetailsViewModel.removeTVShowFromWatchlist(tvShow)
                                .subscribeOn(Schedulers.computation())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                    isTVShowAvailableInWatchlist = false;
                                    activityTvshowDetailsBinding.imageWatchList.setImageResource(R.drawable.ic_eye);
                                    Toast.makeText(getApplicationContext(), "Kaldırıldı", Toast.LENGTH_LONG).show();
                                    compositeDisposable.dispose();
                                }));


                    } else {
                        compositeDisposable.add(tvShowDetailsViewModel.addToWatchList(tvShow)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                    activityTvshowDetailsBinding.imageWatchList.setImageResource(R.drawable.ic_add);
                                    Toast.makeText(getApplicationContext(), "Eklendi", Toast.LENGTH_LONG).show();
                                    compositeDisposable.dispose();
                                })

                        );
                    }
                });
                activityTvshowDetailsBinding.imageWatchList.setVisibility(View.VISIBLE);
                loadBasicTVShowDetails();

            }
        });

    }



    private void loadImageSlider(String[] sliderImages) {

        activityTvshowDetailsBinding.sliderViewPager.setOffscreenPageLimit(1);
        activityTvshowDetailsBinding.sliderViewPager.setAdapter(new ImageSliderAdapter(sliderImages));
        activityTvshowDetailsBinding.sliderViewPager.setVisibility(View.VISIBLE);
        activityTvshowDetailsBinding.viewFadingEdge.setVisibility(View.VISIBLE);
        setupSliderIndicators(sliderImages.length);
        activityTvshowDetailsBinding.sliderViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentSliderIndicator(position);
            }
        });

    }

    private void setupSliderIndicators(int count) {

        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8, 0, 8, 0);
        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.background_slider_indicator_inactive));

            indicators[i].setLayoutParams(layoutParams);
            activityTvshowDetailsBinding.layoutSliderIndicators.addView(indicators[i]);
        }
        activityTvshowDetailsBinding.layoutSliderIndicators.setVisibility(View.VISIBLE);
        setCurrentSliderIndicator(0);

    }

    private void setCurrentSliderIndicator(int position) {
        int childCount = activityTvshowDetailsBinding.layoutSliderIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) activityTvshowDetailsBinding.layoutSliderIndicators.getChildAt(i);
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.background_slider_indicator_active));

            } else {

                imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.background_slider_indicator_inactive));
            }

        }
    }

    private void loadBasicTVShowDetails() {
        activityTvshowDetailsBinding.setTvShowName(tvShow.getName());
        activityTvshowDetailsBinding.setNetworkCountry(tvShow.getNetwork() + " (" +
                tvShow.getCountry() + ")"
        );

        activityTvshowDetailsBinding.setStatus(tvShow.getStatus());
        activityTvshowDetailsBinding.setStartedDate(tvShow.getStartDate());
        activityTvshowDetailsBinding.textName.setVisibility(View.VISIBLE);
        activityTvshowDetailsBinding.textNetworkCountry.setVisibility(View.VISIBLE);
        activityTvshowDetailsBinding.textStatus.setVisibility(View.VISIBLE);
        activityTvshowDetailsBinding.textStarted.setVisibility(View.VISIBLE);
    }

}