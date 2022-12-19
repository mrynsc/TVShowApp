package com.example.tvshow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.tvshow.repositories.MostPopularTVShowsRepository;
import com.example.tvshow.responses.TVShowsResponse;

public class MostPopularTVShowsViewModel extends ViewModel {

    private MostPopularTVShowsRepository mostPopularTVShowsRepository;

    public MostPopularTVShowsViewModel(){

        mostPopularTVShowsRepository=new MostPopularTVShowsRepository();

    }

    public LiveData<TVShowsResponse>getMostPopularTVShows(int page){

        return mostPopularTVShowsRepository.getMostPopularTVShows(page);
    }

}
