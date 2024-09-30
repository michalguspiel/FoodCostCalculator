package com.erdees.foodcostcalc

import com.erdees.foodcostcalc.domain.model.Ad
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.utils.ads.ListAdsInjectorManager
import io.kotest.matchers.shouldBe
import org.junit.Test

fun testProductDomain(id: Long = 0) = ProductDomain(
    id = id,
    name = "Test Product",
    pricePerUnit = 10.0,
    waste = 10.0,
    tax = 10.0,
    unit = "kg"
)

class ListAdsInjectorManagerTests {

    @Test
    fun `GIVEN there is 11 items in the list, WHEN AdFrequency is 7, THEN there's one ad`() {
        val list = List(11, init = { testProductDomain(it.toLong()) })
        val listInjector = ListAdsInjectorManager(list, 7)
        listInjector.listInjectedWithAds.size shouldBe 12
    }

    @Test
    fun `GIVEN there is 20 items in the list, WHEN AdFrequency is 5, THEN there are 4 ads`() {
        val list = List(20, init = { testProductDomain(it.toLong()) })
        val listInjector = ListAdsInjectorManager(list, 5)
        listInjector.listInjectedWithAds.size shouldBe 24
    }

    @Test
    fun `GIVEN there is 15 items in the list, WHEN AdFrequency is 3, THEN there are 6 ads`() {
        val list = List(15, init = { testProductDomain(it.toLong()) })
        val listInjector = ListAdsInjectorManager(list, 3)
        listInjector.listInjectedWithAds.size shouldBe 21
    }

    @Test
    fun `GIVEN there is 0 items in the list, WHEN AdFrequency is 5, THEN there are 0 ads`() {
        val list = List(0, init = { testProductDomain(it.toLong()) })
        val listInjector = ListAdsInjectorManager(list, 5)
        listInjector.listInjectedWithAds.size shouldBe 0
    }

    @Test
    fun `GIVEN there is 5 items in the list, WHEN AdFrequency is 10, THEN there are 0 ads`() {
        val list = List(5, init = { testProductDomain(it.toLong()) })
        val listInjector = ListAdsInjectorManager(list, 10)
        listInjector.listInjectedWithAds.size shouldBe 5
    }

    @Test
    fun `GIVEN 7 items in the list, AdFrequency 3, THEN listInjectedWithAds has ads on index 3 and 6`() {
        val list = List(7, init = { testProductDomain(it.toLong()) })
        val listInjector = ListAdsInjectorManager(list, 3)
        listInjector.listInjectedWithAds.size shouldBe 9
        listInjector.listInjectedWithAds[3] shouldBe Ad
        listInjector.listInjectedWithAds[6] shouldBe Ad
    }


    @Test
    fun `GIVEN 15 items in the list, AdFrequency 3, THEN listInjectedWithAds has all products on correct indexes`() {
        val list = List(15, init = { testProductDomain(it.toLong()) })
        val listInjector = ListAdsInjectorManager(list, 3)
        listInjector.listInjectedWithAds.size shouldBe 21
        listInjector.listInjectedWithAds[0] shouldBe testProductDomain(0)
        listInjector.listInjectedWithAds[1] shouldBe testProductDomain(1)
        listInjector.listInjectedWithAds[2] shouldBe testProductDomain(2)
        listInjector.listInjectedWithAds[3] shouldBe Ad
        listInjector.listInjectedWithAds[4] shouldBe testProductDomain(3)
        listInjector.listInjectedWithAds[5] shouldBe testProductDomain(4)
        listInjector.listInjectedWithAds[6] shouldBe Ad
        listInjector.listInjectedWithAds[7] shouldBe testProductDomain(5)
        listInjector.listInjectedWithAds[8] shouldBe testProductDomain(6)
        listInjector.listInjectedWithAds[9] shouldBe Ad
        listInjector.listInjectedWithAds[10] shouldBe testProductDomain(7)
        listInjector.listInjectedWithAds[11] shouldBe testProductDomain(8)
        listInjector.listInjectedWithAds[12] shouldBe Ad
        listInjector.listInjectedWithAds[13] shouldBe testProductDomain(9)
        listInjector.listInjectedWithAds[14] shouldBe testProductDomain(10)
        listInjector.listInjectedWithAds[15] shouldBe Ad
        listInjector.listInjectedWithAds[16] shouldBe testProductDomain(11)
        listInjector.listInjectedWithAds[17] shouldBe testProductDomain(12)
        listInjector.listInjectedWithAds[18] shouldBe Ad
        listInjector.listInjectedWithAds[19] shouldBe testProductDomain(13)
        listInjector.listInjectedWithAds[20] shouldBe testProductDomain(14)
    }
}