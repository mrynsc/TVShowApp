package com.example.tvshow.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.tvshow.database.TVShowsDatabase;
import com.example.tvshow.models.TVShow;
import com.example.tvshow.repositories.TVShowDetailsRepository;
import com.example.tvshow.responses.TVShowDetailsResponse;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class TVShowDetailsViewModel extends AndroidViewModel {

    private TVShowDetailsRepository tvShowDetailsRepository;
    private TVShowsDatabase tvShowsDatabase;

    public TVShowDetailsViewModel(@NonNull Application application) {
        super(application);
        tvShowDetailsRepository=new TVShowDetailsRepository();
        tvShowsDatabase=TVShowsDatabase.getTvShowsDatabase(application);
    }
    public LiveData<TVShowDetailsResponse>getTVShowDetails(String tvShowId){

        return tvShowDetailsRepository.getTVShowDetails(tvShowId);
    }

    public Completable addToWatchList(TVShow tvShow){

        return tvShowsDatabase.tvShowDao().addToWatchlist(tvShow);
    }

    public Flowable<TVShow> getTVShowFromWatchlist(String tvShowId){

        return tvShowsDatabase.tvShowDao().getTVShowFromWatchList(tvShowId);
    }

    public Completable removeTVShowFromWatchlist(TVShow tvShow){
        return tvShowsDatabase.tvShowDao().removeFromWatchlist(tvShow);
    }



}
