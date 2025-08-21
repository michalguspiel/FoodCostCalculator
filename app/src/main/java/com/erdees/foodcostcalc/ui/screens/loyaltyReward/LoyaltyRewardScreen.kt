package com.erdees.foodcostcalc.ui.screens.loyaltyReward

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.navigation.Screen
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import kotlinx.coroutines.launch

@Composable
@Screen
fun LoyaltyRewardScreen(
    navController: NavController,
    viewModel: LoyaltyRewardViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()

    LoyaltyRewardContent(
        onContinue = {
            scope.launch {
                viewModel.markLoyaltyScreenSeen()
                navController.navigate(FCCScreen.Products) {
                    popUpTo(FCCScreen.LoyaltyReward) { inclusive = true }
                }
            }
        }
    )
}

@Composable
private fun LoyaltyRewardContent(
    onContinue: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Hero Visual - Trophy Icon in a golden circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD700).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Sharp.Star,
                    contentDescription = "Star",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFFFD700)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Headline
            Text(
                text = "A Gift For Your Loyalty",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "As one of our earliest supporters, we want to say thank you. We've automatically upgraded you to our new Premium plan—with all of its powerful new features—for free, forever. You will continue to pay your original, lower price. It's our gift to you for believing in this app from the start.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Feature Highlights
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "You now have access to:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                FeatureItem("✓ Unlimited Dishes & Half-Products")
                FeatureItem("✓ Cloud Backup & Sync (Coming Soon!)")
                FeatureItem("✓ And much more!")
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Call to Action Button
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Awesome, Thanks!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FeatureItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Preview(showBackground = true, name = "Loyalty Reward Screen")
@Composable
private fun LoyaltyRewardScreenPreview() {
    FCCTheme {
        LoyaltyRewardContent(
            onContinue = { }
        )
    }
}