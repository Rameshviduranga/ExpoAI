package com.example.expoai

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- DATA MODELS ---
data class AITool(val name: String, val rating: String, val category: String, val url: String)

val allAITools = listOf(
    AITool("ChatGPT", "4.9", "Text", "https://chat.openai.com"),
    AITool("Claude", "4.8", "Text", "https://claude.ai"),
    AITool("Gemini", "4.7", "Text", "https://gemini.google.com"),
    AITool("Midjourney", "4.9", "Image", "https://www.midjourney.com"),
    AITool("DALL-E", "4.6", "Image", "https://openai.com/dall-e-3"),
    AITool("Sora", "4.8", "Video", "https://openai.com/sora"),
    AITool("Jasper", "4.5", "Text", "https://www.jasper.ai"),
    AITool("Leonardo", "4.7", "Image", "https://leonardo.ai"),
    AITool("Perplexity", "4.8", "Search", "https://www.perplexity.ai"),
    AITool("Llama 3", "4.6", "Text", "https://llama.meta.com"),
    AITool("Stable Diffusion", "4.7", "Image", "https://stability.ai"),
    AITool("Runway", "4.5", "Video", "https://runwayml.com"),
    AITool("ElevenLabs", "4.9", "Audio", "https://elevenlabs.io"),
    AITool("GitHub Copilot", "4.8", "Code", "https://github.com/features/copilot"),
    AITool("Notion AI", "4.6", "Productivity", "https://www.notion.so/product/ai"),
    AITool("Adobe Firefly", "4.7", "Image", "https://www.adobe.com/sensei/generative-ai/firefly.html"),
    AITool("Canva AI", "4.5", "Design", "https://www.canva.com/ai-powered-design-tools/"),
    AITool("Grammarly", "4.8", "Text", "https://www.grammarly.com"),
    AITool("Otter.ai", "4.4", "Audio", "https://otter.ai"),
    AITool("Synthesia", "4.6", "Video", "https://www.synthesia.io"),
    AITool("HeyGen", "4.7", "Video", "https://www.heygen.com"),
    AITool("Descript", "4.5", "Audio", "https://www.descript.com"),
    AITool("Grok", "4.3", "Text", "https://x.ai"),
    AITool("Pika", "4.4", "Video", "https://pika.art")
)

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            ExpoAITheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            DrawerContent(navController, { scope.launch { drawerState.close() } }, onThemeToggle = { isDarkMode = !isDarkMode }, isDarkMode = isDarkMode)
                        }
                    }
                ) {
                    Scaffold(
                        topBar = {
                            if (currentRoute !in listOf("splash", "welcome", "login", "signup", "privacy", "help", "feedback", "about")) {
                                CenterAlignedTopAppBar(
                                    title = { Text("ExpoAI", fontWeight = FontWeight.Bold) },
                                    navigationIcon = {
                                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                            Icon(Icons.Default.Menu, "Menu")
                                        }
                                    },
                                    actions = {
                                        IconButton(onClick = { /* Handle notifications */ }) {
                                            Icon(Icons.Default.Notifications, "Notifications")
                                        }
                                        IconButton(onClick = {
                                            auth.signOut()
                                            navController.navigate("login") {
                                                popUpTo("home") { inclusive = true }
                                            }
                                        }) {
                                            Icon(Icons.AutoMirrored.Filled.Logout, "Logout")
                                        }
                                    },
                                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        },
                        bottomBar = {
                            if (currentRoute?.startsWith("home") == true || currentRoute == "quiz" || currentRoute == "menu" || currentRoute?.startsWith("details/") == true) {
                                FixedBottomNav(navController)
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            AppNavigation(navController, { isDarkMode = !isDarkMode }, isDarkMode, auth)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerContent(navController: NavHostController, onClose: () -> Unit, onThemeToggle: () -> Unit, isDarkMode: Boolean) {
    Column(modifier = Modifier.fillMaxHeight().padding(16.dp)) {
        Text("ExpoAI Menu", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))
        DrawerItem(Icons.Default.Star, "Top Rated") { navController.navigate("home"); onClose() }
        DrawerItem(Icons.Default.NewReleases, "Newest") { navController.navigate("home"); onClose() }
        DrawerItem(Icons.Default.Quiz, "Quizzes") { navController.navigate("quiz"); onClose() }
        DrawerItem(if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode, "Theme") { onThemeToggle(); onClose() }
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        DrawerItem(Icons.Default.PrivacyTip, "Privacy Policy") { navController.navigate("privacy"); onClose() }
        DrawerItem(Icons.AutoMirrored.Filled.Help, "Help Center") { navController.navigate("help"); onClose() }
        DrawerItem(Icons.Default.Feedback, "Feedback") { navController.navigate("feedback"); onClose() }
        DrawerItem(Icons.Default.Info, "About") { navController.navigate("about"); onClose() }
    }
}

@Composable
fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

// --- 1. THEME ---
@Composable
fun ExpoAITheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) darkColorScheme(primary = Color(0xFFBB86FC), background = Color(0xFF121212), surface = Color(0xFF1E1E1E))
    else lightColorScheme(primary = Color(0xFF6200EE), background = Color(0xFFF5F5F5), surface = Color.White)
    MaterialTheme(colorScheme = colorScheme, content = content)
}

// --- 2. NAVIGATION GRAPH ---
@Composable
fun AppNavigation(navController: NavHostController, onThemeToggle: () -> Unit, isDarkMode: Boolean, auth: FirebaseAuth) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("welcome") { WelcomeScreen(navController) }
        composable("home") { DashboardScreen(navController) }
        composable("quiz") { QuizScreen() }
        composable("menu") { MenuPage(navController, onThemeToggle, isDarkMode) }
        composable("login") { LoginScreen(navController, auth) }
        composable("signup") { SignUpScreen(navController, auth) }
        composable("privacy") { PrivacyPolicyScreen(navController) }
        composable("help") { HelpCenterScreen(navController) }
        composable("feedback") { FeedbackScreen(navController) }
        composable("about") { AboutScreen(navController) }
        composable("details/{toolName}") { backStackEntry ->
            val toolName = backStackEntry.arguments?.getString("toolName") ?: "AI Tool"
            ToolDetailsPage(toolName)
        }
    }
}

// --- 3. SPLASH SCREEN ---
@Composable
fun SplashScreen(navController: NavHostController) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500), label = ""
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500)
        navController.navigate("welcome") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp).scale(alphaAnim),
                tint = Color(0xFFBB86FC)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = "ExpoAI",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.scale(alphaAnim)
            )
        }
    }
}

// --- 4. WELCOME SCREEN ---
@Composable
fun WelcomeScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(24.dp)) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ExpoAI", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Discover Your Next AI Tool & Guide", color = Color.LightGray, fontSize = 18.sp, textAlign = TextAlign.Center)
        }
        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 20.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
        ) { Text("Get Started", color = Color.White) }
    }
}

// --- LOGIN SCREEN ---
@Composable
fun LoginScreen(navController: NavHostController, auth: FirebaseAuth) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Welcome Back", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Login to your account", color = Color.Gray)
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), leadingIcon = { Icon(Icons.Default.Email, null) } )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), leadingIcon = { Icon(Icons.Default.Lock, null) } )
            Spacer(Modifier.height(32.dp))
            Button(onClick = { 
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate("signup") }) {
                Text("Don't have an account? Sign Up")
            }
        }
        IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.padding(16.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
    }
}

// --- SIGN UP SCREEN ---
@Composable
fun SignUpScreen(navController: NavHostController, auth: FirebaseAuth) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Sign up to explore ExpoAI", color = Color.Gray)
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), leadingIcon = { Icon(Icons.Default.Email, null) } )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), leadingIcon = { Icon(Icons.Default.Lock, null) } )
            Spacer(Modifier.height(32.dp))
            Button(onClick = { 
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Sign up successful! Please log in.", Toast.LENGTH_LONG).show()
                                auth.signOut() // Auto logout after signup
                                navController.navigate("login") {
                                    popUpTo("signup") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("Sign Up", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate("login") }) {
                Text("Already have an account? Login")
            }
        }
        IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.padding(16.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
    }
}

// --- PRIVACY POLICY SCREEN ---
@Composable
fun PrivacyPolicyScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            item {
                Text("Last updated: May 2024", color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))
                Text("At ExpoAI, we take your privacy seriously. This Privacy Policy explains how we collect, use, and protect your information.", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(24.dp))
                
                PrivacySection("1. Information Collection", "We collect minimal information required to provide our services, such as your email address if you choose to create an account.")
                PrivacySection("2. How We Use Data", "Your data is used to personalize your experience, track your quiz progress, and improve our AI tool directory.")
                PrivacySection("3. Third-Party Services", "ExpoAI provides information about third-party AI tools. Please note that these tools have their own privacy policies.")
                PrivacySection("4. Data Security", "We implement standard security measures to protect your data from unauthorized access or disclosure.")
                PrivacySection("5. Contact Us", "If you have any questions about this policy, please contact us at support@expoai.com.")
                
                Spacer(Modifier.height(32.dp))
                Text("© 2024 ExpoAI Team. All rights reserved.", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun PrivacySection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text(content, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
    }
}

// --- HELP CENTER SCREEN ---
@Composable
fun HelpCenterScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Help Center", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            item {
                Text("How can we help you?", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))
                HelpItem("How to use AI tools?", "Browse the directory on the home screen, select a tool to see details, and click the link to visit its official website.")
                HelpItem("How does the quiz work?", "Select a difficulty level (Beginner, Intermediate, Expert) and answer 10 questions. You get 10 points for each correct answer.")
                HelpItem("Can I use this offline?", "Most features require an active internet connection to fetch the latest AI tool data and links.")
                HelpItem("Is my data safe?", "Yes, we only store minimal data required for your experience. Check our Privacy Policy for more details.")
            }
        }
    }
}

@Composable
fun HelpItem(question: String, answer: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(question, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(answer, fontSize = 14.sp)
        }
    }
}

// --- FEEDBACK SCREEN ---
@Composable
fun FeedbackScreen(navController: NavHostController) {
    var feedbackText by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Feedback", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("We'd love to hear from you!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Text("Rate your experience:", color = Color.Gray)
            Row(Modifier.padding(vertical = 16.dp)) {
                repeat(5) { index ->
                    IconButton(onClick = { rating = index + 1 }) {
                        Icon(
                            imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (index < rating) Color(0xFFFFD700) else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                label = { Text("Tell us what you think...") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { /* Submit logic */ navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Submit Feedback", fontWeight = FontWeight.Bold) }
        }
    }
}

// --- ABOUT SCREEN ---
@Composable
fun AboutScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("About ExpoAI", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("ExpoAI", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text("Version 2.0.1", color = Color.Gray)
            Spacer(Modifier.height(32.dp))
            Text(
                "ExpoAI is your ultimate companion for exploring the rapidly evolving world of Artificial Intelligence. Our mission is to simplify AI discovery and guide users toward the best tools for their needs.",
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(24.dp))
            Text("Made with ❤️ for AI Enthusiasts", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.weight(1f))
            Text("© 2024 ExpoAI Team", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

// --- 5. UPDATED QUIZ SCREEN ---
enum class QuizLevel { BEGINNER, INTERMEDIATE, EXPERT }
data class Question(val text: String, val options: List<String>, val correctIndex: Int, val level: QuizLevel)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuizScreen() {
    val allQuestions = remember { listOf(
        // BEGINNER
        Question("What does AI stand for?", listOf("Artful Intelligence", "Artificial Intelligence", "Automated Interface", "Active Info"), 1, QuizLevel.BEGINNER),
        Question("Which AI is by OpenAI?", listOf("Claude", "Gemini", "ChatGPT", "Llama"), 2, QuizLevel.BEGINNER),
        Question("What is Sora used for?", listOf("Text to Video", "Text to Audio", "Coding", "Mathematics"), 0, QuizLevel.BEGINNER),
        Question("Company that created Gemini?", listOf("Microsoft", "Google", "Meta", "Amazon"), 1, QuizLevel.BEGINNER),
        Question("Midjourney creates what?", listOf("Writing code", "Images", "Chess moves", "Stocks"), 1, QuizLevel.BEGINNER),
        Question("Is ChatGPT a Robot?", listOf("Yes", "No, it's software", "It's a person", "Maybe"), 1, QuizLevel.BEGINNER),
        Question("Can AI think like humans?", listOf("Exactly the same", "No, it uses math", "Yes, it has a brain", "Only on Tuesdays"), 1, QuizLevel.BEGINNER),
        Question("Which is an AI assistant?", listOf("Siri", "Hammer", "Bicycle", "Toaster"), 0, QuizLevel.BEGINNER),
        Question("AI 'training' uses what?", listOf("Gyms", "Data", "Magic", "Exercise"), 1, QuizLevel.BEGINNER),
        Question("AI 'Chat' means what?", listOf("Running", "Talking/Texting", "Cooking", "Driving"), 1, QuizLevel.BEGINNER),
        // INTERMEDIATE
        Question("What is a 'Prompt'?", listOf("A virus", "Instruction to AI", "A car", "A cable"), 1, QuizLevel.INTERMEDIATE),
        Question("What is 'LLM'?", listOf("Large Language Model", "Long Logic Mode", "Light Level Map", "Low Memory"), 0, QuizLevel.INTERMEDIATE),
        Question("Which AI is 'Multimodal'?", listOf("Handles text, image, audio", "Only text", "Handles text, image, audio", "Only math"), 1, QuizLevel.INTERMEDIATE),
        Question("What is 'Hallucination'?", listOf("AI tired", "AI making up facts", "AI turning off", "AI getting faster"), 1, QuizLevel.INTERMEDIATE),
        Question("Who founded OpenAI?", listOf("Jeff Bezos", "Sam Altman", "Mark Zuckerberg", "Tim Cook"), 1, QuizLevel.INTERMEDIATE),
        Question("What is 'Deep Learning'?", listOf("Neural networks", "Ocean study", "Memory loss", "Fast typing"), 0, QuizLevel.INTERMEDIATE),
        Question("What is a 'GPU' used for?", listOf("Gaming only", "AI Processing", "Printing", "Sound"), 1, QuizLevel.INTERMEDIATE),
        Question("What is 'NLP'?", listOf("Natural Language Processing", "New Life Plan", "Next Level Power", "No Logic"), 0, QuizLevel.INTERMEDIATE),
        Question("Claude AI is by whom?", listOf("Anthropic", "Google", "Apple", "Netflix"), 0, QuizLevel.INTERMEDIATE),
        Question("What is 'Inference'?", listOf("Running the model", "Deleting the model", "Drawing", "Sleeping"), 0, QuizLevel.INTERMEDIATE),
        // EXPERT
        Question("What is a 'Transformer'?", listOf("The T in GPT", "A toy", "A battery", "A screen"), 0, QuizLevel.EXPERT),
        Question("What is 'Zero-shot'?", listOf("Task without examples", "No coffee", "A camera feature", "Deleting data"), 0, QuizLevel.EXPERT),
        Question("What is 'RLHF'?", listOf("Reinforcement Learning", "Radio Low Frequency", "Really High Fast", "Robot Life"), 0, QuizLevel.EXPERT),
        Question("What is 'Tokenization'?", listOf("Splitting text for AI", "Buying coins", "Security key", "Video editing"), 1, QuizLevel.EXPERT),
        Question("What is 'Attention Mechanism'?", listOf("Weighting input parts", "AI being polite", "User focus", "Alarm system"), 1, QuizLevel.EXPERT),
        Question("What is 'Overfitting'?", listOf("Model knows data too well", "Tight clothes", "Data is too small", "Processor heat"), 0, QuizLevel.EXPERT),
        Question("What is 'Backpropagation'?", listOf("Neural net training math", "Moving backward", "A spine issue", "Copying files"), 0, QuizLevel.EXPERT),
        Question("What is 'Stochastic'?", listOf("Predictable", "Randomness/Probability", "Static", "A metal type"), 0, QuizLevel.EXPERT),
        Question("What is 'Weights' in AI?", listOf("Learned parameters", "Hardware mass", "Heavy data", "User importance"), 0, QuizLevel.EXPERT),
        Question("What is 'AGI'?", listOf("Artificial General Intelligence", "A Great Idea", "All Google Info", "Active Gaming"), 0, QuizLevel.EXPERT)
    )}

    var selectedLevel by remember { mutableStateOf<QuizLevel?>(null) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var pointsAdded by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableIntStateOf(-1) }
    val scope = rememberCoroutineScope()
    val filtered = allQuestions.filter { it.level == selectedLevel }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(id = R.drawable.quiz_bg), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))

        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            val currentLevel = selectedLevel
            if (currentLevel == null) {
                Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                    Text("CHOOSE YOUR LEVEL", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Spacer(Modifier.height(40.dp))
                    QuizLevelBtn("BEGINNER", MaterialTheme.colorScheme.primary) { selectedLevel = QuizLevel.BEGINNER }
                    QuizLevelBtn("INTERMEDIATE", MaterialTheme.colorScheme.secondary) { selectedLevel = QuizLevel.INTERMEDIATE }
                    QuizLevelBtn("EXPERT", MaterialTheme.colorScheme.tertiary) { selectedLevel = QuizLevel.EXPERT }
                }
            } else if (!showResult) {
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("LEVEL: ${currentLevel.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                            Text("SCORE: $score", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            AnimatedVisibility(visible = pointsAdded, enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically()) {
                                Text("+10", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { (currentIndex + 1).toFloat() / 10 }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = MaterialTheme.colorScheme.primary, trackColor = Color.White.copy(alpha = 0.2f))
                    Spacer(Modifier.height(40.dp))
                    AnimatedContent(targetState = currentIndex, label = "", transitionSpec = { (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut()) }) { idx ->
                        val q = filtered[idx]
                        Column {
                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))) {
                                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Question ${idx + 1}/10", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(12.dp))
                                    Text(q.text, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Color.Black)
                                }
                            }
                            Spacer(Modifier.height(32.dp))
                            q.options.forEachIndexed { i, opt ->
                                val color = when {
                                    selectedOption == -1 -> Color.White.copy(alpha = 0.15f)
                                    i == q.correctIndex -> Color(0xFF4CAF50)
                                    i == selectedOption -> Color(0xFFF44336)
                                    else -> Color.White.copy(alpha = 0.05f)
                                }
                                Button(
                                    onClick = { if (selectedOption == -1) { 
                                        selectedOption = i
                                        if (i == q.correctIndex) { score += 10; pointsAdded = true }
                                        scope.launch { delay(1200); pointsAdded = false; if(currentIndex < 9) { currentIndex++; selectedOption = -1 } else showResult = true }
                                    }},
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(60.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
                                    shape = RoundedCornerShape(16.dp)
                                ) { 
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("${'A' + i}. ", fontWeight = FontWeight.Bold)
                                        Text(opt, fontWeight = FontWeight.Medium)
                                        Spacer(Modifier.weight(1f))
                                        if (selectedOption != -1 && i == q.correctIndex) Icon(Icons.Default.CheckCircle, null)
                                        if (selectedOption == i && i != q.correctIndex) Icon(Icons.Default.Cancel, null)
                                    }
                                }
                            }

                        }
                    }
                }

            } else {
                Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFD700), modifier = Modifier.size(100.dp))
                    Text("CHALLENGE COMPLETED!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Final Score: $score", fontSize = 48.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                    Button(onClick = { selectedLevel = null; currentIndex = 0; score = 0; showResult = false; selectedOption = -1 }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 24.dp)) { Text("PLAY AGAIN", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun QuizLevelBtn(text: String, color: Color, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onClick() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.8f)), elevation = CardDefaults.cardElevation(4.dp)) {
        Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) { Text(text, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White) }
    }
}

// --- 6. DASHBOARD ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val filteredTools = allAITools.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val categories = listOf("Recent Used", "Most Viewed", "Top Rated", "Newest")

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectedCategory != null) {
                    IconButton(onClick = { selectedCategory = null }){ Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
                OutlinedTextField(
                    value = searchQuery, onValueChange = { searchQuery = it; if (it.isNotEmpty()) selectedCategory = null }, 
                    modifier = Modifier.fillMaxWidth(), 
                    placeholder = { Text("Search 24+ AI tools...") }, 
                    leadingIcon = { Icon(Icons.Default.Search, null) }, 
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
        if (selectedCategory != null) {
            item { Text(selectedCategory!!, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
            items(allAITools.shuffled().chunked(3)) { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { tool -> AICard(tool, Modifier.weight(1f)) { navController.navigate("details/${tool.name}") } }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        } else if (searchQuery.isNotEmpty()) {
            items(filteredTools.chunked(3)) { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { tool -> AICard(tool, Modifier.weight(1f)) { navController.navigate("details/${tool.name}") } }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        } else {
            items(categories) { cat -> CategoryRowSection(cat, navController) { selectedCategory = cat } }
        }
    }
}

@Composable
fun CategoryRowSection(title: String, navController: NavHostController, onSeeMore: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 16.dp)) {
            val tools = allAITools.shuffled().take(8)
            items(tools) { tool -> AICard(tool, Modifier.width(140.dp)) { navController.navigate("details/${tool.name}") } }
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onSeeMore, modifier = Modifier.align(Alignment.CenterEnd)) { Text("See more.", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
    }
}

@Composable
fun AICard(tool: AITool, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.aspectRatio(0.85f).clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(4.dp)) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Row(modifier = Modifier.align(Alignment.TopEnd), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp)); Text(tool.rating, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) { Text(tool.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center); Text(tool.category, fontSize = 11.sp, color = Color.Gray) }
        }
    }
}

// --- 7. DETAILS ---
@Composable
fun ToolDetailsPage(toolName: String) {
    val uriHandler = LocalUriHandler.current
    val tool = allAITools.find { it.name == toolName }
    val features = listOf("Advanced reasoning", "Neural network processing", "Fast response time", "High accuracy", "Safe & Ethical AI")
    var userRating by remember { mutableIntStateOf(0) }

    LazyColumn(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer))))
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(modifier = Modifier.size(100.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                        Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.padding(20.dp).fillMaxSize(), tint = Color.White)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(toolName, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.2f)) {
                        Text(tool?.category ?: "AI Tool", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
        
        item {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Overview", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    // Star Rating UI
                    Row {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (index < userRating) Color(0xFFFFD700) else Color.Gray,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { userRating = index + 1 }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Discover the power of $toolName. This advanced AI solution leverages cutting-edge technology to provide comprehensive assistance and creative results for professionals and enthusiasts alike.",
                    fontSize = 16.sp, color = Color.Gray, lineHeight = 24.sp
                )
                
                Spacer(Modifier.height(32.dp))
                Text("Key Features", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
            }
        }
        
        items(features) { f ->
            Card(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(f, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
        
        item {
            Button(
                onClick = { tool?.let { uriHandler.openUri(it.url) } ?: uriHandler.openUri("https://google.com") },
                modifier = Modifier.fillMaxWidth().padding(24.dp).height(64.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Launch, null)
                Spacer(Modifier.width(8.dp))
                Text("Visit Official Website", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        item { Spacer(Modifier.height(40.dp)) }
    }
}

// --- 8. SETTINGS PAGE ---
@Composable
fun MenuPage(navController: NavHostController, onThemeToggle: () -> Unit, isDarkMode: Boolean) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Settings & Support", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp)) }
        item { SettingItem(Icons.Default.Brightness6, "Theme", if (isDarkMode) "Dark Mode" else "Light Mode") { onThemeToggle() } }
        item { SettingItem(Icons.Default.Notifications, "Notifications", "Alerts and Sounds") {} }
        item { SettingItem(Icons.Default.Language, "Language", "English (US)") {} }
        item { SettingItem(Icons.Default.PrivacyTip, "Privacy & Policy", "Data and Terms") { navController.navigate("privacy") } }
        item { SettingItem(Icons.AutoMirrored.Filled.Help, "Help Center", "FAQs and Guides") { navController.navigate("help") } }
        item { SettingItem(Icons.Default.Feedback, "Feedback", "Rate our App") { navController.navigate("feedback") } }
        item { SettingItem(Icons.Default.Info, "About ExpoAI", "Version 2.0.1") { navController.navigate("about") } }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { Text("© 2024 ExpoAI Team. All rights reserved.", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray) }
    }
}

@Composable
fun SettingItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) }; Spacer(Modifier.width(16.dp)); Column { Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text(subtitle, fontSize = 12.sp, color = Color.Gray) }; Spacer(Modifier.weight(1f)); Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray) } }
}

// --- 9. BOTTOM NAV ---
@Composable
fun FixedBottomNav(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    Surface(modifier = Modifier.fillMaxWidth().height(80.dp), shadowElevation = 15.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) { Row(Modifier.fillMaxSize(), Arrangement.SpaceAround, Alignment.CenterVertically) { listOf(Triple("home", "Home", Icons.Default.Home), Triple("quiz", "Quiz", Icons.AutoMirrored.Filled.Assignment), Triple("menu", "Settings", Icons.Default.Settings)).forEach { (route, label, icon) -> val isSelected = currentRoute == route; val scale by animateFloatAsState(if (isSelected) 1.2f else 1f, label = ""); Column(modifier = Modifier.clip(CircleShape).background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent).clickable { if (!isSelected) navController.navigate(route) }.padding(horizontal = 20.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, label, modifier = Modifier.scale(scale), tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray); Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray) } } } }
}
