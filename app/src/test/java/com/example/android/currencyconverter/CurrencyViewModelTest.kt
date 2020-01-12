package com.example.android.currencyconverter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.currencyconverter.model.Currencies
import com.example.android.currencyconverter.model.Currency
import com.example.android.currencyconverter.repository.CurrencyConverterRepository
import com.example.android.currencyconverter.viewmodel.CurrencyViewModel
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.math.BigDecimal


@RunWith(JUnit4::class)
class CurrencyViewModelTest {

    @Rule @JvmField val instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var mCurrencyViewModel: CurrencyViewModel
    var mRepository: CurrencyConverterRepository = mock()

    @Before
    fun setUp() {
        this.mCurrencyViewModel = CurrencyViewModel(mock())
    }

    @Test
    fun testConversionFail() {
        mCurrencyViewModel.convert(BigDecimal(100))
        Assert.assertTrue("TRUE", mCurrencyViewModel.mErrorMessage.value == "Select a currency please.")
    }

    @Test
    fun testConversionFromUSDSucceed() {
        mCurrencyViewModel.mCurrencyList.value = Currencies(mutableListOf(
            Currency("USD", "US Dollar", BigDecimal(1), null),
            Currency("JPY", "Japanese yen", BigDecimal(109), null)
        ))
        mCurrencyViewModel.mSelectedCurrency = Currency("USD", "US Dollar", BigDecimal(1), null)

        mCurrencyViewModel.convert(BigDecimal(100))

        Assert.assertTrue("TRUE", mCurrencyViewModel.mCurrencyList.value!!
            .currencies[0]?.latestCalculation == BigDecimal(100))
        Assert.assertTrue("TRUE", mCurrencyViewModel.mCurrencyList.value!!
            .currencies[1]?.latestCalculation == BigDecimal(109*100))
    }

    // TODO Other tests

}