package com.example.tvshow.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.tvshow.R;
import com.example.tvshow.adapters.WatchlistAdapter;
import com.example.tvshow.databinding.ActivityWatchlistBinding;
import com.example.tvshow.listeners.WatchlistListener;
import com.example.tvshow.models.TVShow;
import com.example.tvshow.viewmodels.WatchlistViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class WatchlistActivity extends AppCompatActivity implements WatchlistListener {

    private ActivityWatchlistBinding activityWatchlistBinding;
    private WatchlistViewModel viewModel;
    private WatchlistAdapter watchlistAdapter;
    private List<TVShow> watchlist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityWatchlistBinding= DataBindingUtil.setContentView(this,R.layout.activity_watchlist);
        doInitialization();
    }

    private void doInitialization(){
        viewModel=new ViewModelProvider(this).get(WatchlistViewModel.class);
        activityWatchlistBinding.imageBack.setOnClickListener(view -> onBackPressed());
        watchlist=new ArrayList<>();
    }

    private void loadWatchlist(){
        activityWatchlistBinding.setIsLoading(true);
        CompositeDisposable compositeDisposable=new CompositeDisposable();
        compositeDisposable.add(viewModel.loadWatchlist().subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tvShows -> {
                    activityWatchlistBinding.setIsLoading(false);
                    if (watchlist.size()>0){
                        watchlist.clear();
                    }
                    watchlist.addAll(tvShows);
                    watchlistAdapter=new WatchlistAdapter(watchlist,this);
                    activityWatchlistBinding.watchlistRecyclerView.setAdapter(watchlistAdapter);
                    activityWatchlistBinding.watchlistRecyclerView.setVisibility(View.VISIBLE);
                    compositeDisposable.dispose();
                }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWatchlist();
    }

    @Override
    public void onTVShowClicked(TVShow tvShow) {
        Intent intent =new Intent(getApplicationContext(),TVShowDetailsActivity.class);
        intent.putExtra("tvShow",tvShow);
        startActivity(intent);
    }


    @Override
    public void removeTVShowFromWatchList(TVShow tvShow, int position) {

        CompositeDisposable compositeDisposableForDelete=new CompositeDisposable();
        compositeDisposableForDelete.add(viewModel.removeTVShowFromWatchlist(tvShow)
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(()->{
            watchlist.remove(position);
            watchlistAdapter.notifyItemRemoved(position);
            watchlistAdapter.notifyItemRangeChanged(position,watchlistAdapter.getItemCount());
            compositeDisposableForDelete.dispose();
                }));

    }

}
