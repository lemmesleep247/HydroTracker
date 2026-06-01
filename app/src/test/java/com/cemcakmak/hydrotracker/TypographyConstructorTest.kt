package com.cemcakmak.hydrotracker

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import org.junit.Test

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class TypographyConstructorTest {
    @Test
    fun testDefaultEmphasizedWeights() {
        val t = Typography()
        println("displayLarge weight: ${t.displayLarge.fontWeight}")
        println("displayLargeEmphasized weight: ${t.displayLargeEmphasized.fontWeight}")
        println("labelLarge weight: ${t.labelLarge.fontWeight}")
        println("labelLargeEmphasized weight: ${t.labelLargeEmphasized.fontWeight}")
        println("titleMedium weight: ${t.titleMedium.fontWeight}")
        println("titleMediumEmphasized weight: ${t.titleMediumEmphasized.fontWeight}")
        println("Are displayLarge and displayLargeEmphasized equal? ${t.displayLarge == t.displayLargeEmphasized}")
        println("Are labelLarge and labelLargeEmphasized equal? ${t.labelLarge == t.labelLargeEmphasized}")
        
        // Assert they are different
        assert(t.displayLarge != t.displayLargeEmphasized) { "displayLarge should differ from displayLargeEmphasized" }
        assert(t.labelLarge != t.labelLargeEmphasized) { "labelLarge should differ from labelLargeEmphasized" }
    }
}
