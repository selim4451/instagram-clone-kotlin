package com.example.instagramclone

import com.example.instagramclone.data.remote.dto.RegisterRequest
import com.example.instagramclone.data.remote.dto.UserResponse
import com.example.instagramclone.data.repository.AuthRepository
import com.example.instagramclone.util.Resource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RegisterViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val api = FakeAuthApi()
    private val tokenStorage = FakeTokenStorage()
    private val viewModel = RegisterViewModel(AuthRepository(api, tokenStorage))

    @Test
    fun `kayıt formundaki bilgiler sunucuya aynen gönderilir`() {
        var sent: RegisterRequest? = null
        api.onRegister = { request ->
            sent = request
            UserResponse(1, request.username, request.email)
        }

        viewModel.register("selim", "selim@example.com", "gizli123")

        assertEquals(RegisterRequest("selim", "selim@example.com", "gizli123"), sent)
    }

    @Test
    fun `başarılı kayıtta durum Success olur ama otomatik giriş yapılmaz`() {
        viewModel.register("selim", "selim@example.com", "gizli123")

        assertTrue(viewModel.state.value is Resource.Success)
        assertNull(tokenStorage.token)
    }

    @Test
    fun `kullanıcı adı alınmışsa sunucunun mesajı gösterilir`() {
        api.onRegister = { throw httpError(409, "Bu kullanıcı adı zaten alınmış") }

        viewModel.register("selim", "selim@example.com", "gizli123")

        assertEquals("Bu kullanıcı adı zaten alınmış", (viewModel.state.value as Resource.Error).message)
    }
}
