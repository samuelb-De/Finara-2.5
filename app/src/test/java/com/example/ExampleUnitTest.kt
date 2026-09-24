package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testColombianBanksList() {
    val banks = com.example.ui.components.ColombianBanks.list
    assertTrue(banks.isNotEmpty())
    assertTrue(banks.contains("Bancolombia"))
    assertTrue(banks.contains("Banco de Bogotá"))
    assertTrue(banks.contains("Davivienda"))
    assertTrue(banks.contains("Nu Colombia (Nubank)"))
    assertTrue(banks.contains("Lulo Bank"))
    assertTrue(banks.contains("BBVA Colombia"))
    assertTrue(banks.all { it.isNotBlank() })
  }
}
