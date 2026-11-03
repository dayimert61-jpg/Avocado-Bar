package com.example.avocadobar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

// --- Theme Colors ---
val AvocadoGreen = Color(0xFF567D46)
val BrightLime = Color(0xFF829B35)
val CreamBackground = Color(0xFFF4E8C1)
val WarmBrown = Color(0xFF4A2E10)
val SoftRed = Color(0xFFD9534F)

// --- Game Models ---
data class Recipe(
    val name: String,
    val emoji: String,
    val ingredients: List<String>,
    val rewardPoints: Int
)

val availableRecipes = listOf(
    Recipe("Avocado Toast", "🍞🥑🍳", listOf("Sourdough", "Mashed Avocado", "Poached Egg"), 50),
    Recipe("Guacamole Bowl", "🥑🍋🌽", listOf("Avocado", "Lime Juice", "Tortilla Chips"), 40),
    Recipe("Green Smoothie", "🥤🥑🍌", listOf("Avocado", "Banana", "Almond Milk"), 35),
    Recipe("Avocado Salad", "🥗🥑🍅", listOf("Avocado", "Cherry Tomatoes", "Olive Oil"), 45)
)

val allIngredients = listOf(
    "Sourdough", "Mashed Avocado", "Poached Egg",
    "Lime Juice", "Tortilla Chips", "Banana",
    "Almond Milk", "Cherry Tomatoes", "Olive Oil", "Cheese"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AvocadoBarGameApp()
            }
        }
    }
}

@Composable
fun AvocadoBarGameApp() {
    var score by remember { mutableStateOf(0) }
    var currentRecipe by remember { mutableStateOf(availableRecipes.random()) }
    var selectedIngredients by remember { mutableStateOf(setOf<String>()) }
    var feedbackMessage by remember { mutableStateOf("") }
    var streakCount by remember { mutableStateOf(0) }

    // Logic to verify order
    fun serveOrder() {
        val isCorrect = currentRecipe.ingredients.all { selectedIngredients.contains(it) } &&
                selectedIngredients.size == currentRecipe.ingredients.size

        if (isCorrect) {
            streakCount++
            val bonus = if (streakCount > 2) 20 else 0
            val totalGained = currentRecipe.rewardPoints + bonus
            score += totalGained
            feedbackMessage = "GREAT JOB! +$totalGained Pts ${if (bonus > 0) "(Combo Bonus!)" else ""}"
        } else {
            streakCount = 0
            score = (score - 15).coerceAtLeast(0)
            feedbackMessage = "WRONG RECIPE! -15 Pts"
        }

        // Reset for next order
        selectedIngredients = emptySet()
        currentRecipe = availableRecipes.random()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Header & Scoreboard
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AvocadoGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AVOCADO BAR 🥑",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Fresh & Healthy Kitchen",
                        fontSize = 12.sp,
                        color = CreamBackground
                    )
                }
                Surface(
                    color = CreamBackground,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Score: $score",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmBrown,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 2. Customer Order Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BrightLime, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CUSTOMER ORDER",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrightLime
                )
                Text(
                    text = "${currentRecipe.emoji} ${currentRecipe.name}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmBrown,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "Required: ${currentRecipe.ingredients.joinToString(", ")}",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 3. Feedback Banner
        if (feedbackMessage.isNotEmpty()) {
            Text(
                text = feedbackMessage,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (feedbackMessage.contains("GREAT")) AvocadoGreen else SoftRed,
                textAlign = TextAlign.Center
            )
        }

        // 4. Kitchen Prep Station (Ingredient Selector)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Select Ingredients:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WarmBrown,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Grid of Ingredients
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                allIngredients.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            val isSelected = selectedIngredients.contains(item)
                            Button(
                                onClick = {
                                    selectedIngredients = if (isSelected) {
                                        selectedIngredients - item
                                    } else {
                                        selectedIngredients + item
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) BrightLime else Color.White,
                                    contentColor = if (isSelected) Color.White else WarmBrown
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text(text = item, fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // 5. Serve Button Action
        Button(
            onClick = { serveOrder() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AvocadoGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "SERVE DISH 🥑",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
