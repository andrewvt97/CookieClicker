package com.example.cookieclicker

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.cookieclicker.data.Datasource
import com.example.cookieclicker.model.Cookie
import com.example.cookieclicker.ui.theme.CookieClickerTheme


private const val TAG = "Main Activity"
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "OnCreate Called")
        enableEdgeToEdge()
        setContent {
            CookieClickerTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                ) {
                    CookieClickerApp(cookies = Datasource().cookieList)
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "OnStart Called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume Called")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart Called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause Called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop Called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy Called")
    }
}

fun determineDessertToShow(
    cookies: List<Cookie>,
    cookiesSold: Int
): Cookie {
    var cookieToShow = cookies.first()
    for (cookie in cookies) {
        if (cookiesSold >= cookie.stock) {
            cookieToShow = cookie
        } else {
            break
        }
    }

    return cookieToShow
}

@Composable
private fun CookieClickerApp(
    cookies: List<Cookie>
) {

    var revenue by rememberSaveable { mutableStateOf(0) }
    var cookiesSold by rememberSaveable { mutableStateOf(0) }
    val currentCookieIndex by rememberSaveable { mutableStateOf(0) }
    var currentCookiePrice by rememberSaveable { mutableStateOf(cookies[currentCookieIndex].price) }
    var currentCookieImageId by rememberSaveable { mutableStateOf(cookies[currentCookieIndex].imageId) }


    Scaffold(topBar = {
        val intentContext = LocalContext.current
        val layoutDirection = LocalLayoutDirection.current
        CookieClickerAppBar(
            onShareButtonClicked = {
                shareSoldCookiesInformation(
                    intentContext = intentContext,
                    cookiesSold = cookiesSold,
                    revenue = revenue
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = WindowInsets.safeDrawing.asPaddingValues()
                        .calculateStartPadding(layoutDirection),
                    end = WindowInsets.safeDrawing.asPaddingValues()
                        .calculateEndPadding(layoutDirection)
                )
                .background(MaterialTheme.colorScheme.primary)
        )
    }) { contentPadding ->
        DessertClickerScreen(
            revenue = revenue,
            cookiesClicked = cookiesSold,
            cookieImageId = currentCookieImageId,
            onCookieClicked = {

                // Update the revenue
                revenue += currentCookiePrice
                cookiesSold++

                // Show the next dessert
                val cookieToShow = determineDessertToShow(cookies, cookiesSold)
                currentCookieImageId = cookieToShow.imageId
                currentCookiePrice = cookieToShow.price
            },
            modifier = Modifier.padding(contentPadding)
        )
    }
}

@Composable
private fun CookieClickerAppBar(
    onShareButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.padding(start = 16.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleLarge,
        )

        IconButton(
            onClick = onShareButtonClicked,
            modifier = Modifier.padding(end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = stringResource(R.string.share),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

    }
}


fun shareSoldCookiesInformation(intentContext: Context, cookiesSold: Int, revenue: Int) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            intentContext.getString(R.string.share_text, cookiesSold, revenue)
        )
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)

    try {
        ContextCompat.startActivity(intentContext, shareIntent, null)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            intentContext,
            intentContext.getString(R.string.sharing_not_available),
            Toast.LENGTH_LONG
        ).show()
    }
}

@Composable
fun DessertClickerScreen(
    revenue: Int,
    cookiesClicked: Int,
    @DrawableRes cookieImageId: Int,
    onCookieClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.bakery_back),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Image(
                    painter = painterResource(cookieImageId),
                    contentDescription = null,
                    modifier = Modifier
                        .width(150.dp)
                        .height(150.dp)
                        .align(Alignment.Center)
                        .clickable { onCookieClicked() },
                    contentScale = ContentScale.Crop,
                )
            }
            TransactionInfo(
                revenue = revenue,
                cookiesClicked = cookiesClicked,
                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
            )
        }
    }
}

@Composable
fun TransactionInfo(
    revenue: Int,
    cookiesClicked: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        CookiesClickedInfo(
            cookiesClicked = cookiesClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        RevenueInfo(
            revenue = revenue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
fun CookiesClickedInfo(cookiesClicked: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.cookies_clicked),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = cookiesClicked.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun RevenueInfo(revenue: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.total_revenue),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = "$${revenue}",
            textAlign = TextAlign.Right,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Preview
@Composable
fun MyDessertClickerAppPreview() {
    CookieClickerTheme {
        CookieClickerApp(listOf(Cookie(R.drawable.chocolatechip, 5, 0)))
    }
}