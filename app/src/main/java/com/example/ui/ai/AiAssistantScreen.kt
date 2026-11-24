package com.example.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AiAssistantRepository
import kotlinx.coroutines.launch

@Composable
fun AiAssistantScreen(initialPrompt: String = "") {
    val repository = remember { AiAssistantRepository() }
    val scope = rememberCoroutineScope()

    var promptInput by remember { mutableStateOf(initialPrompt) }
    var selectedTask by remember { mutableStateOf("plot") }
    var aiResponse by remember { mutableStateOf("Ask Gemini AI assistant to brainstorm plot twists, improve prose, translate across languages, or develop characters.") }
    var isLoading by remember { mutableStateOf(false) }

    val tasks = listOf(
        "plot" to "Plot Ideas",
        "grammar" to "Improve Prose",
        "translate" to "Translate",
        "characters" to "Character Profile",
        "summary" to "Summarize"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "AI Literary Assistant",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Powered by Google Gemini via Firebase AI Logic",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Task selector chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tasks) { (key, label) ->
                FilterChip(
                    selected = selectedTask == key,
                    onClick = { selectedTask = key },
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = promptInput,
            onValueChange = { promptInput = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = { Text("Enter prompt, paste excerpt, or ask for literary consultation in any language...") },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (promptInput.isNotBlank()) {
                    isLoading = true
                    aiResponse = "Consulting Gemini AI literary model..."
                    scope.launch {
                        val result = repository.getLiteraryAssistance(promptInput, selectedTask)
                        isLoading = false
                        aiResponse = result.getOrElse { "AI request failed: ${it.localizedMessage}" }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyzing...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate AI Response")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Response box
        Text(
            text = "AI Output",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                item {
                    Text(
                        text = aiResponse,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Serif,
                            lineHeight = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
