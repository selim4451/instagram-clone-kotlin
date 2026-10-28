package com.example.instagramclone

import com.example.instagramclone.data.remote.dto.LikeResponse
import com.example.instagramclone.data.repository.PostRepository
import com.example.instagramclone.util.Resource
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class HomeViewModelLikeTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val api = FakePostApi().apply { onFeed = { listOf(post(1, "fotoğraf", likeCount = 5)) } }

    private fun createViewModel() = HomeViewModel(PostRepository(api))

    private fun HomeViewModel.firstPost() = (feed.value as Resource.Success).data.first()

    @Test
    fun `kalbe basınca sunucu cevabını beklemeden kalp dolar ve sayı artar`() {
        val response = CompletableDeferred<LikeResponse>()
        api.onLike = { _, _ -> response.await() }
        val viewModel = createViewModel()

        viewModel.toggleLike(1)

        assertTrue(viewModel.firstPost().likedByMe)
        assertEquals(6, viewModel.firstPost().likeCount)
    }

    @Test
    fun `sunucu cevap verince sayı sunucudaki değerle güncellenir`() {
        api.onLike = { _, _ -> LikeResponse(likeCount = 9, likedByMe = true) }
        val viewModel = createViewModel()

        viewModel.toggleLike(1)

        assertEquals(9, viewModel.firstPost().likeCount)
    }

    @Test
    fun `beğeni başarısız olursa kalp ve sayı eski haline döner, hata gösterilir`() {
        api.onLike = { _, _ -> throw IOException("offline") }
        val viewModel = createViewModel()

        viewModel.toggleLike(1)

        assertFalse(viewModel.firstPost().likedByMe)
        assertEquals(5, viewModel.firstPost().likeCount)
        assertTrue(viewModel.error.value!!.isNetworkError)
    }

    @Test
    fun `beğenilmiş gönderide kalbe basınca beğeni geri alınır`() {
        api.onFeed = { listOf(post(1, "fotoğraf", likeCount = 5, likedByMe = true)) }
        api.onLike = { _, liked -> LikeResponse(likeCount = 4, likedByMe = liked) }
        val viewModel = createViewModel()

        viewModel.toggleLike(1)

        assertEquals(listOf(1L to false), api.likeCalls)
        assertFalse(viewModel.firstPost().likedByMe)
        assertEquals(4, viewModel.firstPost().likeCount)
    }

    @Test
    fun `istek sürerken kalbe tekrar basmak ikinci istek göndermez`() {
        val response = CompletableDeferred<LikeResponse>()
        api.onLike = { _, _ -> response.await() }
        val viewModel = createViewModel()

        viewModel.toggleLike(1)
        viewModel.toggleLike(1)

        assertEquals(1, api.likeCalls.size)
    }

    @Test
    fun `çift dokunma beğenir ama zaten beğenilmişse beğeniyi geri almaz`() {
        val viewModel = createViewModel()

        viewModel.likeFromDoubleTap(1)
        viewModel.likeFromDoubleTap(1)

        assertEquals(listOf(1L to true), api.likeCalls)
        assertTrue(viewModel.firstPost().likedByMe)
    }
}
