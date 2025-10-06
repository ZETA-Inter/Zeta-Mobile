package com.example.feature_fornecedor.ranking;

import com.google.gson.annotations.SerializedName;

public class RankingEntry {
    @SerializedName(value = "id",        alternate = {"workerId","codigo","idWorker"})
    public Integer id;

    @SerializedName(value = "name",      alternate = {"workerName","nome","worker","fullName"})
    public String name;

    @SerializedName(value = "position",  alternate = {"rankPosition","posicao","rank","rankingPosition","ordem","order","place"})
    public Integer position;

    @SerializedName(value = "points",    alternate = {"totalPoints","pontuacao","score","pts"})
    public Integer points;

    @SerializedName(value = "photo",     alternate = {"avatar","foto","imageUrl","photoUrl","urlFoto","image"})
    public String photo;
}
