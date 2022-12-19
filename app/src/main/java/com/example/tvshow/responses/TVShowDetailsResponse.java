package com.example.tvshow.responses;

import com.example.tvshow.models.TVShowDetails;
import com.google.gson.annotations.SerializedName;

public class TVShowDetailsResponse {

    @SerializedName("tvShow")
    private TVShowDetails tvShowDetails;

    public TVShowDetails getTvShowDetails(){

        return tvShowDetails;
    }

}
