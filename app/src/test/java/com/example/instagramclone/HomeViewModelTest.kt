package com.example.instagramclone

import com.example.instagramclone.data.repository.PostRepository
import com.example.instagramclone.util.Resource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class HomeViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val api = FakePostApi()

    private fun createViewModel() = HomeViewModel(PostRepository(api))

    @Test
    fun `ekran açılınca akış kendiliğinden yüklenir`() {
        api.onFeed = { listOf(post(2, "ikinci"), post(1, "birinci")) }

        val viewModel = createViewModel()

        val feed = viewModel.feed.value as Resource.Success
        assertEquals(listOf("ikinci", "birinci"), feed.data.map { it.caption })
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun `yenileme yeni gönderileri getirir`() {
        api.onFeed = { listOf(post(1, "birinci")) }
        val viewModel = createViewModel()

        api.onFeed = { listOf(post(2, "yeni"), post(1, "birinci")) }
        viewModel.refresh()

        assertEquals(2, (viewModel.feed.value as Resource.Success).data.size)
    }

    @Test
    fun `yenileme başarısız olursa eski liste silinmez, sadece hata gösterilir`() {
        api.onFeed = { listOf(post(1, "birinci")) }
        val viewModel = createViewModel()

        api.onFeed = { throw IOException("offline") }
        viewModel.refresh()

        assertEquals(listOf("birinci"), (viewModel.feed.value as Resource.Success).data.map { it.caption })
        assertTrue(viewModel.error.value!!.isNetworkError)
    }

    @Test
    fun `gösterilen hata temizlenince tekrar gösterilmez`() {
        api.onFeed = { throw httpError(401, "Oturum geçersiz ya da süresi dolmuş") }
        val viewModel = createViewModel()
        assertEquals("Oturum geçersiz ya da süresi dolmuş", viewModel.error.value!!.message)

        viewModel.errorShown()

        assertNull(viewModel.error.value)
    }

    @Test
    fun `gönderi fotoğraf adresi sunucu adresiyle birleştirilir`() {
        assertEquals(BuildConfig.BASE_URL.trimEnd('/') + "/uploads/7.jpg", post(7, "").fullImageUrl)
    }
}
