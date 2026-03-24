package com.meowreader.client.data.network

import com.google.gson.annotations.SerializedName

data class IndexResponse(
    val id: String,
    val date: String,
    val strategy: String,
    val path: String
)

data class PaperGroupResponse(
    @SerializedName("group_update_date") val groupUpdateDate: String,
    val papers: List<PaperResponse>
)

data class PaperResponse(
    val title: String,
    @SerializedName("article_update_date") val articleUpdateDate: String,
    @SerializedName("source_journal") val sourceJournal: String,
    @SerializedName("body_text") val bodyText: String,
    @SerializedName("difficulty_constant") val difficultyConstant: Double,
    @SerializedName("question_type") val questionType: String,
    val questions: List<QuestionResponse>
)

data class QuestionResponse(
    @SerializedName("q_number") val qNumber: Int,
    val stem: String,
    val options: OptionsResponse,
    val answer: String,
    val explanation: String
)

data class OptionsResponse(
    val A: String,
    val B: String,
    val C: String,
    val D: String
)
