package com.erdees.foodcostcalc.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.erdees.foodcostcalc.data.dish.DishDao
import com.erdees.foodcostcalc.getOrAwaitValue
import com.erdees.foodcostcalc.model.Dish
import com.erdees.foodcostcalc.model.Product
import com.erdees.foodcostcalc.model.ProductIncluded
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class DishDaoTest {



}