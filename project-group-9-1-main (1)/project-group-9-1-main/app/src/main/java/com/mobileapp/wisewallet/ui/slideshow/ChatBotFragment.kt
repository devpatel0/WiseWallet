/*
    This file displays the chatbot and all of its UI components
 */
package com.mobileapp.wisewallet.ui.slideshow

import androidx.fragment.app.Fragment
import androidx.compose.ui.platform.ComposeView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.fragment.app.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.mobileapp.wisewallet.R


class ChatBotFragment : Fragment() {

    private val viewModel: ChatBotViewModel by viewModels()
    


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ChatBotUI(viewModel)
            }
        }
    }

    @Composable
    fun ChatBotUI(viewModel: ChatBotViewModel) {
        val (text, setText) = remember { mutableStateOf("") }
        val response by viewModel.response.collectAsState()
        val messages by viewModel.messages.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()


        Column(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(messages) { message ->
                    if (message.sender == Sender.USER) {
                        // User message bubble
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {

                            Text(
                                text = message.content,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(Color(0xFF81D4FA), RoundedCornerShape(8.dp)) // display message
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )

                            //Spacer(modifier = Modifier.weight(1f))

                            Image(
                                painter = painterResource(id = R.drawable.ic_chat_bot), // chatbot profile pic
                                contentDescription = "User profile picture",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape) // Makes the image circular
                                    .padding(8.dp)
                            )
                        }
                    } else {
                        // Bot message bubble
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_chat_wallet), // user profile pic
                                contentDescription = "bot profile picture",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape) // Makes the image circular
                                    .padding(8.dp)
                            )
                            Text(
                                text = message.content,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                textAlign = TextAlign.Start,
                                color = Color.Black
                            )
                        }
                    }
                }
                if (isLoading) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Spacer(modifier = Modifier.width(16.dp))
                            TypingIndicator(modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
            TextField(
                value = text,
                onValueChange = setText,
                placeholder = { Text("Type your message here") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                if (text.isNotBlank()) {
                    viewModel.sendMessage(text.trim())
                    // Update sendMessage to include 'from'
                    setText("")
                }

            },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF81D4FA), // Light blue color
                    contentColor = Color.White // White text color
                )
                ) {
                Text("Send")
            }
            if (response.startsWith("Error:")) {
                Text(response, color = Color.Red, modifier = Modifier.padding(8.dp))
            }
        }
    }
}
@Composable
fun TypingIndicator(modifier: Modifier = Modifier) { // displays floating dots while the chatbot thinks and types
    val transition = rememberInfiniteTransition()
    val color = MaterialTheme.colors.primary

    val dot1Alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                1f at 300 with LinearOutSlowInEasing
                0.3f at 600
            }
        )
    )

    val dot2Alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                1f at 450 with LinearOutSlowInEasing
                0.3f at 750
            },
            initialStartOffset = StartOffset(150)
        )
    )

    val dot3Alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                1f at 600 with LinearOutSlowInEasing
                0.3f at 900
            },
            initialStartOffset = StartOffset(300)
        )
    )

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Dot(alpha = dot1Alpha, color = color)
        Spacer(modifier = Modifier.width(4.dp))
        Dot(alpha = dot2Alpha, color = color)
        Spacer(modifier = Modifier.width(4.dp))
        Dot(alpha = dot3Alpha, color = color)
    }
}

@Composable
fun Dot(alpha: Float, color: Color, size: Dp = 8.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .alpha(alpha)
            .background(color, CircleShape)
    )
}


