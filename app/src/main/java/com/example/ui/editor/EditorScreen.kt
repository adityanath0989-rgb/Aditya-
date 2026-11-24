package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.data.DocumentItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    documentId: String?,
    documents: List<DocumentItem>,
    onSaveDocument: (DocumentItem) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenAiAssistant: (String) -> Unit
) {
    val existingDoc = documents.find { it.id == documentId }

    var title by remember { mutableStateOf(existingDoc?.title ?: "") }
    var content by remember { mutableStateOf(existingDoc?.content ?: "") }
    var language by remember { mutableStateOf(existingDoc?.language ?: "English") }
    var category by remember { mutableStateOf(existingDoc?.category ?: "Creative Literature") }
    var genre by remember { mutableStateOf(existingDoc?.genre ?: "Short Story") }
    var isDistractionFree by remember { mutableStateOf(false) }

    val wordCount = remember(content) { content.split("\\s+".toRegex()).filter { it.isNotBlank() }.size }
    val charCount = content.length
    val readingTime = maxOf(1, wordCount / 200)

    val languages = listOf("English", "Hindi", "Bengali", "French", "Arabic", "Spanish", "Japanese", "German")
    val genres = listOf("Short Story", "Novel", "Poetry", "Essay", "Memoir", "Research Article", "Play")

    var showLanguageMenu by remember { mutableStateOf(false) }
    var showGenreMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (!isDistractionFree) {
                TopAppBar(
                    title = {
                        TextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("Untitled Manuscript...") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            singleLine = true
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isDistractionFree = !isDistractionFree }) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Distraction Free")
                        }
                        IconButton(onClick = {
                            val doc = DocumentItem(
                                id = existingDoc?.id ?: "",
                                title = title,
                                content = content,
                                language = language,
                                category = category,
                                genre = genre,
                                wordCount = wordCount,
                                characterCount = charCount,
                                readingTimeMins = readingTime
                            )
                            onSaveDocument(doc)
                        }) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                        IconButton(onClick = { onOpenAiAssistant(content) }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!isDistractionFree) {
                Surface(
                    tonalElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Language Selector dropdown button
                        Box {
                            OutlinedButton(onClick = { showLanguageMenu = true }, shape = RoundedCornerShape(8.dp)) {
                                Text(language)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = showLanguageMenu, onDismissRequest = { showLanguageMenu = false }) {
                                languages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang) },
                                        onClick = {
                                            language = lang
                                            showLanguageMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Genre Selector dropdown button
                        Box {
                            OutlinedButton(onClick = { showGenreMenu = true }, shape = RoundedCornerShape(8.dp)) {
                                Text(genre)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = showGenreMenu, onDismissRequest = { showGenreMenu = false }) {
                                genres.forEach { g ->
                                    DropdownMenuItem(
                                        text = { Text(g) },
                                        onClick = {
                                            genre = g
                                            showGenreMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Text(
                            text = "$wordCount words • $readingTime min read",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
        ) {
            // Formatting Toolbar Simulation
            if (!isDistractionFree) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { content += "**" }) { Icon(Icons.Default.FormatBold, contentDescription = "Bold", modifier = Modifier.size(20.dp)) }
                        IconButton(onClick = { content += "*" }) { Icon(Icons.Default.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(20.dp)) }
                        IconButton(onClick = { content += "\n# " }) { Icon(Icons.Default.Title, contentDescription = "Heading", modifier = Modifier.size(20.dp)) }
                        IconButton(onClick = { content += "\n- " }) { Icon(Icons.Default.FormatListBulleted, contentDescription = "List", modifier = Modifier.size(20.dp)) }
                        IconButton(onClick = { content += "\n> " }) { Icon(Icons.Default.FormatQuote, contentDescription = "Quote", modifier = Modifier.size(20.dp)) }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Main Writing Canvas
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                placeholder = { Text("Begin writing your literary work in any language...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    lineHeight = 26.sp
                )
            )
        }
    }
}
