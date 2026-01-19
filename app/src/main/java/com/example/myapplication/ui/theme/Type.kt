package com.example.myapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
 import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

// Set of Material typography styles to start with
val Typography = Typography(
  titleLarge = TextStyle(
    fontFamily = FontFamily(Font(R.font.google_sans)),
    fontWeight = FontWeight.Medium,
    fontSize = 24.sp,
    lineHeight = 32.sp,
  ),
  headlineSmall = TextStyle(
    fontFamily = FontFamily(Font(R.font.google_sans)),
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
  ),
  /* Other default text styles to override

  labelSmall = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Medium,
      fontSize = 11.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.5.sp
  )
  */
)