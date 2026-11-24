package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BookItem
import com.example.data.DocumentItem
import com.example.data.LiteratureRepository
import com.example.ui.ai.AiAssistantScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.bookstudio.BookStudioScreen
import com.example.ui.editor.EditorScreen
import com.example.ui.export.ExportScreen
import com.example.ui.home.HomeScreen
import com.example.ui.library.LibraryScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.stats.StatisticsScreen
import com.example.ui.theme.WorldLiteratureTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        setContent {
            WorldLiteratureTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContent()
                }
            }
        }
    }
}

sealed interface Screen {
    data object Home : Screen
    data object Library : Screen
    data class Editor(val documentId: String?) : Screen
    data object BookStudio : Screen
    data object AiAssistant : Screen
    data object Statistics : Screen
    data object Settings : Screen
    data class Export(val bookId: String) : Screen
}

@Composable
fun MainAppContent() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { LiteratureRepository(context) }
    val scope = rememberCoroutineScope()

    var currentUserUid by remember { mutableStateOf(Firebase.auth.currentUser?.uid) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var aiInitialPrompt by remember { mutableStateOf("") }

    val documents by repository.observeDocuments(currentUserUid ?: "").collectAsStateWithLifecycle(initialValue = emptyList())
    val books by repository.observeBooks(currentUserUid ?: "").collectAsStateWithLifecycle(initialValue = emptyList())

    // Seed sample data if logged in
    LaunchedEffect(currentUserUid) {
        val uid = currentUserUid
        if (!uid.isNullOrBlank()) {
            repository.seedSampleDataIfNeeded(uid)
        }
    }

    if (currentUserUid.isNullOrBlank()) {
        AuthScreen(onSignedIn = { uid ->
            currentUserUid = uid
        })
    } else {
        Scaffold(
            bottomBar = {
                if (currentScreen !is Screen.Editor && currentScreen !is Screen.Export) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        NavigationBarItem(
                            selected = currentScreen is Screen.Home,
                            onClick = { currentScreen = Screen.Home },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.Library,
                            onClick = { currentScreen = Screen.Library },
                            icon = { Icon(Icons.Default.MenuBook, contentDescription = "Library") },
                            label = { Text("Library") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.BookStudio,
                            onClick = { currentScreen = Screen.BookStudio },
                            icon = { Icon(Icons.Default.AutoStories, contentDescription = "Books") },
                            label = { Text("Studio") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.AiAssistant,
                            onClick = { currentScreen = Screen.AiAssistant },
                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI") },
                            label = { Text("AI Assistant") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.Statistics,
                            onClick = { currentScreen = Screen.Statistics },
                            icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
                            label = { Text("Stats") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.Settings,
                            onClick = { currentScreen = Screen.Settings },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") }
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (val screen = currentScreen) {
                    is Screen.Home -> HomeScreen(
                        documents = documents,
                        books = books,
                        onNavigateToEditor = { docId -> currentScreen = Screen.Editor(docId) },
                        onNavigateToLibrary = { currentScreen = Screen.Library },
                        onNavigateToBookStudio = { currentScreen = Screen.BookStudio },
                        onNavigateToAi = { currentScreen = Screen.AiAssistant },
                        onNavigateToStats = { currentScreen = Screen.Statistics }
                    )
                    is Screen.Library -> LibraryScreen(
                        documents = documents,
                        onNavigateToEditor = { docId -> currentScreen = Screen.Editor(docId) },
                        onDeleteDocument = { docId ->
                            scope.launch { repository.deleteDocument(docId) }
                        }
                    )
                    is Screen.Editor -> EditorScreen(
                        documentId = screen.documentId,
                        documents = documents,
                        onSaveDocument = { doc ->
                            scope.launch {
                                currentUserUid?.let { repository.saveDocument(it, doc) }
                                currentScreen = Screen.Library
                            }
                        },
                        onNavigateBack = { currentScreen = Screen.Library },
                        onOpenAiAssistant = { contentSnippet ->
                            aiInitialPrompt = contentSnippet
                            currentScreen = Screen.AiAssistant
                        }
                    )
                    is Screen.BookStudio -> BookStudioScreen(
                        books = books,
                        onSaveBook = { book ->
                            scope.launch {
                                currentUserUid?.let { repository.saveBook(it, book) }
                            }
                        },
                        onNavigateToExport = { bookId -> currentScreen = Screen.Export(bookId) }
                    )
                    is Screen.AiAssistant -> AiAssistantScreen(initialPrompt = aiInitialPrompt)
                    is Screen.Statistics -> StatisticsScreen(documents = documents, books = books)
                    is Screen.Settings -> SettingsScreen(onSignedOut = { currentUserUid = null })
                    is Screen.Export -> ExportScreen(
                        bookId = screen.bookId,
                        books = books,
                        onNavigateBack = { currentScreen = Screen.BookStudio }
                    )
                }
            }
        }
    }
}
