package com.mobileapp.wisewallet.ui.slideshow

import  androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// Adjust the OpenAIRequest to include the messages and model, as per the API documentation
data class OpenAIRequest(val messages: List<Message>, val model: String)
data class ChatMessage(val content: String, val sender: Sender)
enum class Sender { USER, BOT }
data class Message(val role: String, val content: String)

// The response structure should match the OpenAI's chat completion response
data class OpenAIResponse(val choices: List<Choice>) {
    data class Choice(val message: Message)
}

// Define the Retrofit interface for the OpenAI API
interface OpenAIService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun createCompletion(@Body body: OpenAIRequest, @retrofit2.http.Header("Authorization") authHeader: String): OpenAIResponse
}



class ChatBotViewModel : ViewModel() {

    private val _response = MutableStateFlow("")
    val response: StateFlow<String> = _response
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val openAIService: OpenAIService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIService::class.java)
    }

    fun sendMessage(userInput: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.emit(true)
                delay(1000) // You might already have an API call here

                val initialMessage = Message(role = "system", content = "You are a financial advisor. Please only respond to finance-related questions.")

                val updatedMessages = _messages.value + ChatMessage(content = userInput, sender = Sender.USER)
                _messages.emit(updatedMessages)

                val requestMessages = mutableListOf(initialMessage) // Start with the system message
                requestMessages.addAll(updatedMessages.map {
                    Message(role = if (it.sender == Sender.USER) "user" else "assistant", content = it.content)
                })
                val openAIRequest = OpenAIRequest(messages = requestMessages, model = "gpt-3.5-turbo")
                val authHeader = "Bearer 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"
                val openAIResponse = openAIService.createCompletion(openAIRequest, authHeader)
                val botMessage = openAIResponse.choices.first().message.content
                _response.emit(botMessage)
                _messages.emit(updatedMessages + ChatMessage(content = botMessage, sender = Sender.BOT))
                _isLoading.emit(false) // Indicate loading end

            } catch (e: Exception) {
                _isLoading.emit(false) // Indicate loading end if there is an error
                _response.emit("Error: ${e.localizedMessage}")
            }
        }
    }

}

