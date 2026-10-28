package com.example.instagramclone

import androidx.lifecycle.SavedStateHandle
import com.example.instagramclone.data.repository.PostRepository
import com.example.instagramclone.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CommentsViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val api = FakePostApi()

    private fun createViewModel(postId: Long = 7) =
        CommentsViewModel(SavedStateHandle(mapOf(CommentsActivity.EXTRA_POST_ID to postId)), PostRepository(api))

    private fun CommentsViewModel.texts() = (comments.value as Resource.Success).data.map { it.text }

    @Test
    fun `ekran açılınca doğru gönderinin yorumları yüklenir`() {
        var requestedPostId: Long? = null
        api.onComments = { postId ->
            requestedPostId = postId
            listOf(comment(1, "ilk"), comment(2, "ikinci"))
        }

        val viewModel = createViewModel(postId = 7)

        assertEquals(7L, requestedPostId)
        assertEquals(listOf("ilk", "ikinci"), viewModel.texts())
    }

    @Test
    fun `gönderilen yorum boşlukları kırpılıp listenin sonuna eklenir`() = runTest(UnconfinedTestDispatcher()) {
        api.onComments = { listOf(comment(1, "ilk")) }
        var sentText: String? = null
        api.onAddComment = { _, text ->
            sentText = text
            comment(2, text)
        }
        val viewModel = createViewModel()

        viewModel.send("  yeni yorum  ")

        assertEquals("yeni yorum", sentText)
        assertEquals(listOf("ilk", "yeni yorum"), viewModel.texts())
        assertEquals(CommentsEvent.Sent, viewModel.events.first())
        assertTrue(viewModel.changed)
    }

    @Test
    fun `boş yorum sunucuya gönderilmez`() {
        var calls = 0
        api.onAddComment = { _, text -> calls++; comment(2, text) }
        val viewModel = createViewModel()

        viewModel.send("   ")

        assertEquals(0, calls)
        assertFalse(viewModel.changed)
    }

    @Test
    fun `yorum gönderilemezse liste değişmez ve hata bildirilir`() = runTest(UnconfinedTestDispatcher()) {
        api.onComments = { listOf(comment(1, "ilk")) }
        api.onAddComment = { _, _ -> throw httpError(400, "Yorum boş olamaz") }
        val viewModel = createViewModel()

        viewModel.send("bir şey")

        assertEquals(listOf("ilk"), viewModel.texts())
        val event = viewModel.events.first() as CommentsEvent.Failed
        assertEquals("Yorum boş olamaz", event.error.message)
    }

    @Test
    fun `silinen yorum listeden kalkar`() {
        api.onComments = { listOf(comment(1, "kalacak"), comment(2, "silinecek")) }
        val viewModel = createViewModel()

        viewModel.delete(2)

        assertEquals(listOf("kalacak"), viewModel.texts())
        assertTrue(viewModel.changed)
    }
}
