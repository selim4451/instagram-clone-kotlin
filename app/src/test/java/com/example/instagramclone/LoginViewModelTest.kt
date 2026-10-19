package com.example.instagramclone

import com.example.instagramclone.data.repository.AuthRepository
import com.example.instagramclone.util.Resource
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class LoginViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val api = FakeAuthApi()
    private val tokenStorage = FakeTokenStorage()
    private val viewModel = LoginViewModel(AuthRepository(api, tokenStorage))

    @Test
    fun `başlangıçta hiçbir durum yoktur`() {
        assertNull(viewModel.state.value)
    }

    @Test
    fun `başarılı girişte durum Success olur ve token telefona kaydedilir`() {
        viewModel.login("selim@example.com", "gizli123")

        val state = viewModel.state.value
        assertTrue(state is Resource.Success)
        assertEquals("selim", (state as Resource.Success).data.user.username)
        assertEquals("token-123", tokenStorage.token)
    }

    @Test
    fun `istek sürerken durum Loading olur ve ikinci istek gönderilmez`() {
        val response = CompletableDeferred<Unit>()
        var calls = 0
        api.onLogin = { calls++; response.await(); error("not reached") }

        viewModel.login("selim@example.com", "gizli123")
        viewModel.login("selim@example.com", "gizli123")

        assertEquals(Resource.Loading, viewModel.state.value)
        assertEquals(1, calls)
    }

    @Test
    fun `yanlış şifrede sunucunun mesajı gösterilir ve token kaydedilmez`() {
        api.onLogin = { throw httpError(401, "E-posta veya şifre hatalı") }

        viewModel.login("selim@example.com", "yanlis")

        val state = viewModel.state.value as Resource.Error
        assertEquals("E-posta veya şifre hatalı", state.message)
        assertEquals(401, state.httpCode)
        assertNull(tokenStorage.token)
    }

    @Test
    fun `sunucuya ulaşılamazsa ağ hatası döner, uygulama çökmez`() {
        api.onLogin = { throw IOException("connection refused") }

        viewModel.login("selim@example.com", "gizli123")

        val state = viewModel.state.value as Resource.Error
        assertTrue(state.isNetworkError)
        assertNull(state.message)
    }
}
