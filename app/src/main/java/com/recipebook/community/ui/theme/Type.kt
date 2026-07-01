package com.recipebook.community.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.recipebook.community.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

val NunitoFamily = FontFamily(
    Font(GoogleFont("Nunito"), provider, FontWeight.Normal),
    Font(GoogleFont("Nunito"), provider, FontWeight.SemiBold),
    Font(GoogleFont("Nunito"), provider, FontWeight.Bold),
    Font(GoogleFont("Nunito"), provider, FontWeight.ExtraBold)
)

val AppTypography = Typography(
    displayLarge  = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp),
    headlineLarge = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.Bold,      fontSize = 24.sp),
    headlineMedium= TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.Bold,      fontSize = 20.sp),
    titleLarge    = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.SemiBold,  fontSize = 18.sp),
    bodyLarge     = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.Normal,    fontSize = 16.sp),
    bodyMedium    = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.Normal,    fontSize = 14.sp),
    labelLarge    = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.Bold,      fontSize = 14.sp),
    labelMedium   = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.SemiBold,  fontSize = 12.sp),
)
