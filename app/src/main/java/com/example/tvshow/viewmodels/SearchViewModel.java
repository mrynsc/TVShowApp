package com.example.tvshow.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.tvshow.repositories.SearchTVShowRepository;
import com.example.tvshow.responses.TVShowsResponse;

public class SearchViewModel extends ViewModel {

    private SearchTVShowRepository searchTVShowRepository;

    public SearchViewModel(){

        searchTVShowRepository=new SearchTVShowRepository();
    }

    public LiveData<TVShowsResponse> searchTVShow(String query,int page){
        return searchTVShowRepository.searchTVShow(query,page);
    }

}
