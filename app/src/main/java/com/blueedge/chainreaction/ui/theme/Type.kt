package com.blueedge.chainreaction.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.blueedge.chainreaction.R
import com.blueedge.chainreaction.data.AppFont

val DynaPuffFamily = FontFamily(
    Font(R.font.dynapuff, FontWeight.Normal),
    Font(R.font.dynapuff, FontWeight.Medium),
    Font(R.font.dynapuff, FontWeight.SemiBold),
    Font(R.font.dynapuff, FontWeight.Bold),
    Font(R.font.dynapuff, FontWeight.Black),
)

val SourGummyFamily = FontFamily(
    Font(R.font.sourgummy, FontWeight.Normal),
    Font(R.font.sourgummy, FontWeight.Medium),
    Font(R.font.sourgummy, FontWeight.SemiBold),
    Font(R.font.sourgummy, FontWeight.Bold),
    Font(R.font.sourgummy, FontWeight.Black),
)

val ComicReliefFamily = FontFamily(
    Font(R.font.comicrelief, FontWeight.Normal),
    Font(R.font.comicrelief, FontWeight.Medium),
    Font(R.font.comicrelief_bold, FontWeight.SemiBold),
    Font(R.font.comicrelief_bold, FontWeight.Bold),
    Font(R.font.comicrelief_bold, FontWeight.Black),
)

fun fontFamilyForAppFont(appFont: AppFont): FontFamily = when (appFont) {
    AppFont.DEFAULT -> FontFamily.Default
    AppFont.DYNAPUFF -> DynaPuffFamily
    AppFont.SOUR_GUMMY -> SourGummyFamily
    AppFont.COMIC_RELIEF -> ComicReliefFamily
}

fun createTypography(family: FontFamily) = Typography(
    displayLarge = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Black,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Black,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Default typography
val Typography = createTypography(FontFamily.Default)